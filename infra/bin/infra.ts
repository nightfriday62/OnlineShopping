#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib/core';
import { EcrStack } from '../lib/ecr-stack';
import { NetworkStack } from '../lib/network-stack';
import { DataStack } from '../lib/data-stack';
import { EcsStack } from '../lib/ecs-stack';

const app = new cdk.App();

const ecrStack = new EcrStack(app, 'EcrStack');
const networkStack = new NetworkStack(app, 'NetworkStack');
const dataStack = new DataStack(app, 'DataStack', {
  vpc: networkStack.vpc,
});

new EcsStack(app, 'EcsStack', {
  ecrRepo: ecrStack.ecrRepo,
  vpc: networkStack.vpc,
  db: dataStack.db,
});
