import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import { Repository } from 'aws-cdk-lib/aws-ecr';

export class EcrStack extends cdk.Stack {
    public readonly ecrRepo: Repository;

    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        this.ecrRepo = new Repository(this, 'OnlineShoppingRepo', {
            repositoryName: 'onlineshopping',
            removalPolicy: cdk.RemovalPolicy.DESTROY
        });
    }
}