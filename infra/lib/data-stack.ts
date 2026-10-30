import * as cdk from 'aws-cdk-lib';
import {Construct} from 'constructs';
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as rds from "aws-cdk-lib/aws-rds";
import * as elasticache from "aws-cdk-lib/aws-elasticache";
import {NetworkStack} from "./network-stack";
import {IamStack} from "./iam-stack";

interface DataStackProps extends cdk.StackProps {
    vpc: ec2.IVpc;
    networkStack: NetworkStack;
    iamStack: IamStack;
}

export class DataStack extends cdk.Stack {
    public readonly db: rds.DatabaseInstance;
    public readonly ec: elasticache.CfnReplicationGroup;
    public readonly esInstance: ec2.Instance;
    public readonly mqInstance: ec2.Instance;

    constructor(scope: Construct, id: string, props: DataStackProps) {
        super(scope, id, props);

        this.db = new rds.DatabaseInstance(this, "OnlineShoppingDB", {
            engine: rds.DatabaseInstanceEngine.mysql({version: rds.MysqlEngineVersion.VER_8_0_46}),
            instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.MICRO),
            vpc: props.vpc,
            credentials: rds.Credentials.fromPassword('root', cdk.SecretValue.unsafePlainText('rootroot')),
            allocatedStorage: 20,
            databaseName: 'online_shopping',
            vpcSubnets: {subnetType: ec2.SubnetType.PRIVATE_ISOLATED},
            securityGroups: [props.networkStack.dbSecurityGroup],
            removalPolicy: cdk.RemovalPolicy.DESTROY,
            deleteAutomatedBackups: true
        });


        const cacheSubnetGroup = new elasticache.CfnSubnetGroup(this, "OnlineShoppingCacheSubnetGroup", {
            description: "Subnet group for Online Shopping Cache",
            subnetIds: props.vpc.selectSubnets({subnetType: ec2.SubnetType.PRIVATE_ISOLATED}).subnetIds
        });
        this.ec = new elasticache.CfnReplicationGroup(this, "OnlineShoppingCache", {
            cacheNodeType: "cache.t4g.micro",
            engine: "valkey",
            cacheSubnetGroupName: props.vpc.selectSubnets({subnetType: ec2.SubnetType.PRIVATE_ISOLATED}).subnetIds.length > 0 ? cacheSubnetGroup.ref : undefined,
            replicationGroupDescription: "Replication group for Online Shopping Cache",
            securityGroupIds: [props.networkStack.cacheSecurityGroup.securityGroupId],
            transitEncryptionEnabled: false,
            numCacheClusters: 1,
            automaticFailoverEnabled: false,
        });


        const esMachineImage = ec2.MachineImage.genericLinux({
            "ca-central-1": "ami-05bca49ef71f27eae"
        });

        this.esInstance = new ec2.Instance(this, "ElasticsearchInstance", {
            vpc: props.vpc,
            vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_ISOLATED }, // pre-baked AMI, no internet needed
            instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.SMALL),
            machineImage: esMachineImage,
            securityGroup: props.networkStack.elasticSearchSecurityGroup,
            role: props.iamStack.ESRole
        });

        this.esInstance.userData.addCommands(
            "set -euxo pipefail",
            "systemctl enable --now docker",
            "docker start es01",
            "for i in {1..30}; do curl -k https://localhost:9200 >/dev/null 2>&1 && break; sleep 10; done",
            "ELASTIC_PASSWORD=$(docker exec es01 /usr/share/elasticsearch/bin/elasticsearch-reset-password -u elastic -b -s)",
            "API_KEY_RESPONSE=$(curl -sS -k -u elastic:${ELASTIC_PASSWORD} -X POST https://localhost:9200/_security/api_key -H 'Content-Type: application/json' -d '{\"name\":\"online-shopping-app-key\"}')",
            "install -m 600 -o root -g root /dev/null /root/es-bootstrap-secrets.txt",
            "echo \"elastic_password=${ELASTIC_PASSWORD}\" >> /root/es-bootstrap-secrets.txt",
            "echo \"api_key_response=${API_KEY_RESPONSE}\" >> /root/es-bootstrap-secrets.txt",
            "aws ssm put-parameter --name /online-shopping/es/api-key --type SecureString --value \"${API_KEY_RESPONSE}\" --overwrite --region " + this.region,
        );

        new cdk.CfnOutput(this, "ElasticsearchPrivateIp", {
            value: this.esInstance.instancePrivateIp,
        });
        const mqMachineImage = ec2.MachineImage.genericLinux({
            "ca-central-1": "ami-0dee711345ddec23d"
        });
        this.mqInstance = new ec2.Instance(this, "RocketMQInstance", {
            vpc: props.vpc,
            vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_ISOLATED },
            instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.SMALL),
            machineImage: mqMachineImage,
            securityGroup: props.networkStack.mqSecurityGroup
        });

        new cdk.CfnOutput(this, "RocketMQPrivateIp", {
            value: this.mqInstance.instancePrivateIp,
        });

    }
}
