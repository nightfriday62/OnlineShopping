import * as cdk from 'aws-cdk-lib';
import {Construct} from 'constructs';
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as rds from "aws-cdk-lib/aws-rds";

import * as ecs_patterns from "aws-cdk-lib/aws-ecs-patterns";
import * as tasks from 'aws-cdk-lib/aws-stepfunctions-tasks';
import * as stepfunctions from 'aws-cdk-lib/aws-stepfunctions';
import * as custom from 'aws-cdk-lib/custom-resources';
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as ecr from "aws-cdk-lib/aws-ecr-assets";
import * as path from 'path';
import {IRepository} from 'aws-cdk-lib/aws-ecr';

interface MyEcsConstructStackProps extends cdk.StackProps {
    ecrRepo: IRepository;
    vpc: ec2.IVpc;
    db: rds.DatabaseInstance;
}

export class EcsStack extends cdk.Stack {
    public readonly fargateService: ecs_patterns.ApplicationLoadBalancedFargateService;

    constructor(scope: Construct, id: string, props: MyEcsConstructStackProps) {
        super(scope, id, props);
        const vpc = props.vpc;
        const db = props.db;
        const cluster = new ecs.Cluster(this, "MyCluster", {
            vpc: vpc
        });

        const initTaskSG = new ec2.SecurityGroup(this, 'InitTaskSG', {
            vpc: vpc,
            description: 'Security group for init ECS task',
            allowAllOutbound: true
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
                DB_USER: 'rootroot',
                DB_PASSWORD: 'rootroot',
            }
        })

        const runTask = new tasks.EcsRunTask(this, "Run Init SQL Task", {
            integrationPattern: stepfunctions.IntegrationPattern.RUN_JOB,
            cluster: cluster,
            taskDefinition: initTaskDef,
            launchTarget: new tasks.EcsFargateLaunchTarget(),
            securityGroups: [initTaskSG],
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

        const usePipelineImage = this.node.tryGetContext("usePipelineImage") === "true";

        const image = usePipelineImage
            ? ecs.ContainerImage.fromEcrRepository(props.ecrRepo, "latest")
            : ecs.ContainerImage.fromRegistry("amazon/amazon-ecs-sample");

        const containerPort = usePipelineImage? 8080:80;

        this.fargateService = new ecs_patterns.ApplicationLoadBalancedFargateService(this, "MyFargateService", {
            cluster: cluster,
            desiredCount: 1,
            taskImageOptions: {
                containerName: "onlineshopping",
                image: image,
                containerPort: containerPort,
                environment: {
                    DB_HOST: db.dbInstanceEndpointAddress,
                    DB_USER: 'rootroot',
                    DB_PASSWORD: 'rootroot',
                }
            },
            publicLoadBalancer: true,
            memoryLimitMiB: 512,
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

        startExecution.node.addDependency(db);
        this.fargateService.node.addDependency(startExecution);

        new cdk.CfnOutput(this, "RDSEndpoint", {
            value: db.dbInstanceEndpointAddress
        });

        new cdk.CfnOutput(this,"LoadBalancerURL", {
            value: this.fargateService.loadBalancer.loadBalancerDnsName
        })


    }

}
