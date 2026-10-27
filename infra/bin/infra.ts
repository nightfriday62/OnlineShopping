#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib/core';
import {EcrStack} from '../lib/ecr-stack';
import {NetworkStack} from '../lib/network-stack';
import {DataStack} from '../lib/data-stack';
import {EcsStack} from '../lib/ecs-stack';
import {IamStack} from "../lib/iam-stack";
import {CICDPipeline} from "../lib/cicd-pipeline";

const app = new cdk.App();

const ecrStack = new EcrStack(app, 'EcrStack');
const networkStack = new NetworkStack(app, 'NetworkStack');
const iamStack = new IamStack(app, 'IamStack');
const dataStack = new DataStack(app, 'DataStack', {
    vpc: networkStack.vpc,
    networkStack: networkStack,
    iamStack: iamStack,
});

const ecsStack = new EcsStack(app, 'EcsStack', {
    ecrRepo: ecrStack.ecrRepo,
    dataStack: dataStack,
    networkStack: networkStack,
    iamStack: iamStack,
});
new CICDPipeline(app, 'CICDPipeline', {
    ecrRepo: ecrStack.ecrRepo,
    fargateService: ecsStack.fargateService,
})