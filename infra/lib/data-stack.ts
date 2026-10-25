import * as cdk from 'aws-cdk-lib';
import {Construct} from 'constructs';
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as rds from "aws-cdk-lib/aws-rds";
import * as elasticache from "aws-cdk-lib/aws-elasticache";

interface DataStackProps extends cdk.StackProps {
    vpc: ec2.IVpc;
}

export class DataStack extends cdk.Stack {
    public readonly db: rds.DatabaseInstance;
    public readonly ec: elasticache.CfnCacheCluster;

    constructor(scope: Construct, id: string, props: DataStackProps) {
        super(scope, id, props);

        this.db = new rds.DatabaseInstance(this, "OnlineShoppingDB", {
            engine: rds.DatabaseInstanceEngine.mysql({version: rds.MysqlEngineVersion.VER_8_0_42}),
            instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.SMALL),
            vpc: props.vpc,
            credentials: rds.Credentials.fromPassword('rootroot', cdk.SecretValue.unsafePlainText('rootroot')),
            allocatedStorage: 20,
            databaseName: 'online_shopping',
            publiclyAccessible: true,
            vpcSubnets: {subnetType: ec2.SubnetType.PUBLIC}
        })
        this.db.connections.allowFromAnyIpv4(ec2.Port.tcp(3306), "Allow public MySQL access");


        // TODO: ElastiCache (Redis)
        const cacheSecurityGroup = new ec2.SecurityGroup(this, "OnlineShoppingCacheSG", {
            vpc: props.vpc,
            description: "Security group for Online Shopping Cache",
            allowAllOutbound: true
        });
        cacheSecurityGroup.addIngressRule(ec2.Peer.ipv4(props.vpc.vpcCidrBlock), ec2.Port.tcp(6379), "Allow Redis access from the same Vpc");
        const cacheSubnetGroup = new elasticache.CfnSubnetGroup(this, "OnlineShoppingCacheSubnetGroup", {
            description: "Subnet group for Online Shopping Cache",
            subnetIds: props.vpc.selectSubnets({subnetType: ec2.SubnetType.PRIVATE_ISOLATED}).subnetIds,
            cacheSubnetGroupName: "online-shopping-cache-subnet-group"
        });
        this.ec = new elasticache.CfnCacheCluster(this, "OnlineShoppingCache", {
            cacheNodeType: "cache.t4g.micro",
            engine: "redis",
            numCacheNodes: 1,
            cacheSubnetGroupName: cacheSubnetGroup.cacheSubnetGroupName
        })
        // TODO: Elasticsearch / OpenSearch
        // TODO: RocketMQ
    }
}
