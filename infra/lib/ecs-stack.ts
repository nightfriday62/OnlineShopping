import * as cdk from 'aws-cdk-lib';
import {Construct} from 'constructs';
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as iam from "aws-cdk-lib/aws-iam"

import * as ecs_patterns from "aws-cdk-lib/aws-ecs-patterns";
import * as tasks from 'aws-cdk-lib/aws-stepfunctions-tasks';
import * as stepfunctions from 'aws-cdk-lib/aws-stepfunctions';
import * as custom from 'aws-cdk-lib/custom-resources';
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as ecr from "aws-cdk-lib/aws-ecr-assets";
import * as path from 'path';
import {IRepository, Repository} from 'aws-cdk-lib/aws-ecr';
import {NetworkStack} from "./network-stack";
import {IamStack} from "./iam-stack";
import {DataStack} from "./data-stack";

interface MyEcsConstructStackProps extends cdk.StackProps {
    ecrRepo: IRepository;
    dataStack: DataStack;
    networkStack: NetworkStack;
    iamStack: IamStack;
}

export class EcsStack extends cdk.Stack {
    public readonly fargateService: ecs_patterns.ApplicationLoadBalancedFargateService;

    constructor(scope: Construct, id: string, props: MyEcsConstructStackProps) {
        super(scope, id, props);
        const db = props.dataStack.db;
        const networkStack = props.networkStack;
        const ec = props.dataStack.ec;
        const esInstance = props.dataStack.esInstance;
        const mqInstance = props.dataStack.mqInstance;

        const cluster = new ecs.Cluster(this, "MyCluster", {
            vpc: networkStack.vpc
        });


        const initTaskDef = new ecs.FargateTaskDefinition(this, "InitTaskDef", {
            memoryLimitMiB: 512,
            cpu: 256
        });

        initTaskDef.addContainer("InitContainer", {
            image: ecs.ContainerImage.fromAsset(path.join(__dirname, '../init_task'), {
                platform: ecr.Platform.LINUX_AMD64,
            }),
            logging: new ecs.AwsLogDriver({
                streamPrefix: 'InitTask',
            }),
            environment: {
                DB_HOST: db.dbInstanceEndpointAddress,
                DB_USER: 'root',
                DB_PASSWORD: 'rootroot',
            }
        })

        const runTask = new tasks.EcsRunTask(this, "Run Init SQL Task", {
            integrationPattern: stepfunctions.IntegrationPattern.RUN_JOB,
            cluster: cluster,
            taskDefinition: initTaskDef,
            launchTarget: new tasks.EcsFargateLaunchTarget(),
            securityGroups: [networkStack.appSecurityGroup],
            assignPublicIp: true,
            subnets: {subnetType: ec2.SubnetType.PUBLIC}
        });

        const stateMachine = new stepfunctions.StateMachine(this, "InitDbStateMachine", {
            definitionBody: stepfunctions.DefinitionBody.fromChainable(runTask),
            timeout: cdk.Duration.minutes(5)
        });

        const startExecution = new custom.AwsCustomResource(this, "StartInitTaskExecution", {
            onCreate: {
                service: 'StepFunctions',
                action: 'startExecution',
                parameters: {
                    stateMachineArn: stateMachine.stateMachineArn
                },
                physicalResourceId: custom.PhysicalResourceId.of('InitTaskExecution')
            },
            policy: custom.AwsCustomResourcePolicy.fromSdkCalls({
                resources: [stateMachine.stateMachineArn]
            })
        });


        const image = ecs.ContainerImage.fromAsset(path.join(__dirname, '../..'), {
            platform: ecr.Platform.LINUX_AMD64,
        });

        const containerPort = 8080

        this.fargateService = new ecs_patterns.ApplicationLoadBalancedFargateService(this, "MyFargateService", {
                cluster: cluster,
                desiredCount: 1,
                assignPublicIp: true,
                taskSubnets: {subnetType: ec2.SubnetType.PUBLIC},
                taskImageOptions: {
                    containerName: "onlineshopping",
                    image: image,
                    containerPort: containerPort,
                    environment: {
                        DB_HOST: db.dbInstanceEndpointAddress,
                        DB_USER: 'root',
                        DB_PASSWORD: 'rootroot',
                        ES_HOST: esInstance.instancePrivateIp,
                        ES_API_KEY_PARAMETER: '/online-shopping/es/api-key',
                        ROCKETMQ_NAME_SERVER: mqInstance.instancePrivateIp + ":9876",
                        REDIS_HOST: ec.attrPrimaryEndPointAddress,
                        REDIS_PORT: ec.attrPrimaryEndPointPort
                    }
                },
                loadBalancer: props.networkStack.alb,
                securityGroups: [props.networkStack.appSecurityGroup],
                memoryLimitMiB: 1024,
                cpu: 512,
                healthCheckGracePeriod: cdk.Duration.minutes(3),
                circuitBreaker: {rollback: true}
            }
        )

        const scalableTarget = this.fargateService.service.autoScaleTaskCount({
            minCapacity: 1,
            maxCapacity: 2
        });

        scalableTarget.scaleOnCpuUtilization("CpuScaling", {
            targetUtilizationPercent: 80,
            scaleInCooldown: cdk.Duration.seconds(60),
            scaleOutCooldown: cdk.Duration.seconds(60)
        })

        props.ecrRepo.grantPull(this.fargateService.taskDefinition.taskRole);
        this.fargateService.taskDefinition.taskRole.addToPrincipalPolicy(new iam.PolicyStatement({
            actions: ["ssm:GetParameter"],
            resources: ["*"]
        }));

        startExecution.node.addDependency(db);
        this.fargateService.node.addDependency(startExecution);
        this.fargateService.targetGroup.configureHealthCheck({path: "/hello"})


        new cdk.CfnOutput(this, "RDSEndpoint", {
            value: db.dbInstanceEndpointAddress
        });

        new cdk.CfnOutput(this, "LoadBalancerURL", {
            value: this.fargateService.loadBalancer.loadBalancerDnsName
        });


    }

}
