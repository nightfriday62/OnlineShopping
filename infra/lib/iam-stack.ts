import * as iam from "aws-cdk-lib/aws-iam";
import * as cdk from "aws-cdk-lib";
import {Construct} from "constructs";

export class IamStack extends cdk.Stack {
    public readonly serverRole: iam.Role;
    public readonly ESRole: iam.Role;

    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        this.serverRole = new iam.Role(this, "ServerRole", {
            roleName: "serverRole",
            assumedBy: new iam.ServicePrincipal("ec2.amazonaws.com")});
        this.serverRole.addToPolicy(new iam.PolicyStatement({
            actions:["ssm:GetParameter"],
            resources: [`arn:aws:ssm:${cdk.Aws.REGION}:${cdk.Aws.ACCOUNT_ID}:parameter/online-shopping/server/*`]
        }));

        this.ESRole = new iam.Role(this, "ESRole", {
            roleName: "ESRole",
            assumedBy: new iam.ServicePrincipal("ec2.amazonaws.com")});
        this.ESRole.addToPolicy(new iam.PolicyStatement({
            actions:["ssm:GetParameter", "ssm:PutParameter"],
            resources: [`arn:aws:ssm:${cdk.Aws.REGION}:${cdk.Aws.ACCOUNT_ID}:parameter/online-shopping/es/*`]
        }))
    }}