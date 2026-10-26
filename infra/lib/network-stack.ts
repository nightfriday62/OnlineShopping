import * as cdk from 'aws-cdk-lib';
import {Construct} from 'constructs';
import * as ec2 from "aws-cdk-lib/aws-ec2";

export class NetworkStack extends cdk.Stack {
    public readonly vpc: ec2.Vpc;
    public readonly appSecurityGroup: ec2.SecurityGroup;
    public readonly dbSecurityGroup: ec2.SecurityGroup;
    public readonly cacheSecurityGroup: ec2.SecurityGroup;
    public readonly elasticSearchSecurityGroup: ec2.SecurityGroup;
    public readonly mqSecurityGroup: ec2.SecurityGroup;


    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        this.vpc = new ec2.Vpc(this, "MyVpc", {
            maxAzs: 3
        })
        this.appSecurityGroup = new ec2.SecurityGroup(this, "AppSecurityGroup", {
            vpc: this.vpc,
            allowAllOutbound: true,
            description: "Security group for application servers"
        });

        this.dbSecurityGroup = new ec2.SecurityGroup(this, "DbSecurityGroup", {
            vpc: this.vpc,
            allowAllOutbound: true,
            description: "Security group for database servers"
        });
        this.dbSecurityGroup.addIngressRule(this.appSecurityGroup, ec2.Port.tcp(3306), "Allow MySQL access from app servers");

        this.cacheSecurityGroup = new ec2.SecurityGroup(this, "CacheSecurityGroup", {
            vpc: this.vpc,
            allowAllOutbound: true,
            description: "Security group for cache servers"
        });
        this.cacheSecurityGroup.addIngressRule(this.appSecurityGroup, ec2.Port.tcp(6379), "Allow Redis access from app servers");

        this.elasticSearchSecurityGroup = new ec2.SecurityGroup(this, "ElasticSearchSecurityGroup", {
            vpc: this.vpc,
            allowAllOutbound: true,
            description: "Security group for elasticsearch servers"
        });
        this.elasticSearchSecurityGroup.addIngressRule(this.appSecurityGroup, ec2.Port.tcp(9200), "Allow Elasticsearch access from app servers");

        this.mqSecurityGroup = new ec2.SecurityGroup(this, "MqSecurityGroup", {
            vpc: this.vpc,
            allowAllOutbound: true,
            description: "Security group for MQ servers"
        });
        this.mqSecurityGroup.addIngressRule(this.appSecurityGroup, ec2.Port.tcp(9876), "Allow MQ name server access from app servers");
        this.mqSecurityGroup.addIngressRule(this.appSecurityGroup, ec2.Port.tcp(10911), "Allow MQ broker access from app servers");
    }
}
