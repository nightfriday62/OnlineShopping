import * as cdk from 'aws-cdk-lib';
import {Construct} from 'constructs';
import * as ec2 from "aws-cdk-lib/aws-ec2";

export class NetworkStack extends cdk.Stack {
    public readonly vpc: ec2.Vpc;
    public readonly appSecurityGroup: ec2.SecurityGroup;
    public readonly dbSecurityGroup: ec2.SecurityGroup;
    public readonly cacheSecurityGroup: ec2.SecurityGroup;
    public readonly elasticSearchSecurityGroup: ec2.SecurityGroup;



    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        this.vpc = new ec2.Vpc(this, "MyVpc", {
            maxAzs: 3
        })
    }
}
