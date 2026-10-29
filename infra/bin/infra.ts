#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib/core';
import {EcrStack} from '../lib/ecr-stack';
import {NetworkStack} from '../lib/network-stack';
import {DataStack} from '../lib/data-stack';
import {EcsStack} from '../lib/ecs-stack';
import {IamStack} from "../lib/iam-stack";
import {CICDPipeline} from "../lib/cicd-pipeline";

const app = new cdk.App();
const env = {account: process.env.CDK_DEFAULT_ACCOUNT, region: 'ca-central-1'};

const ecrStack = new EcrStack(app, 'EcrStack', {env});
const networkStack = new NetworkStack(app, 'NetworkStack', {env});
const iamStack = new IamStack(app, 'IamStack', {env});
const dataStack = new DataStack(app, 'DataStack', {
    env,
    vpc: networkStack.vpc,
    networkStack: networkStack,
    iamStack: iamStack,
});

const ecsStack = new EcsStack(app, 'EcsStack', {
    env,
    ecrRepo: ecrStack.ecrRepo,
    dataStack: dataStack,
    networkStack: networkStack,
    iamStack: iamStack,
});
new CICDPipeline(app, 'CICDPipeline', {
    env,
    ecrRepo: ecrStack.ecrRepo,
    fargateService: ecsStack.fargateService,
})