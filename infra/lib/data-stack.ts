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
            engine: rds.DatabaseInstanceEngine.mysql({version: rds.MysqlEngineVersion.VER_8_4_10}),
            instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.MICRO),
            vpc: props.vpc,
            credentials: rds.Credentials.fromPassword('root', cdk.SecretValue.unsafePlainText('rootroot')),
            allocatedStorage: 20,
            databaseName: 'online_shopping',
            vpcSubnets: {subnetType: ec2.SubnetType.PRIVATE_ISOLATED},
            securityGroups: [props.networkStack.dbSecurityGroup],
            removalPolicy: cdk.RemovalPolicy.DESTROY,
            deleteAutomatedBackups: true,
            allowMajorVersionUpgrade: true
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


        const ES_VERSION = "9.5.4";
        const IK_PLUGIN_URL = `https://get.infini.cloud/elasticsearch/analysis-ik/${ES_VERSION}`;

        this.esInstance = new ec2.Instance(this, "ElasticsearchInstance", {
            vpc: props.vpc,
            // Needs internet access to pull the Docker image and the IK plugin (the VPC has no NAT gateway).
            // The security group still only admits the app on 9200.
            vpcSubnets: {subnetType: ec2.SubnetType.PUBLIC},
            associatePublicIpAddress: true,
            instanceType: ec2.InstanceType.of(ec2.InstanceClass.T3, ec2.InstanceSize.SMALL),
            machineImage: ec2.MachineImage.latestAmazonLinux2023(),
            blockDevices: [{deviceName: "/dev/xvda", volume: ec2.BlockDeviceVolume.ebs(8)}],
            securityGroup: props.networkStack.elasticSearchSecurityGroup,
            role: props.iamStack.ESRole,
            requireImdsv2: true,
            userDataCausesReplacement: true,
            resourceSignalTimeout: cdk.Duration.minutes(20),
        });

        // The logical ID carries a user-data hash (userDataCausesReplacement), so it can't be embedded
        // in the user data; the instance reads it from the tag CloudFormation puts on it.
        const signal = (code: string) =>
            `/opt/aws/bin/cfn-signal -e ${code} --stack ${this.stackName} --resource $LOGICAL_ID --region ${this.region}`;

        this.esInstance.userData.addCommands(
            "set -euxo pipefail",
            "TOKEN=$(curl -s -X PUT http://169.254.169.254/latest/api/token -H 'X-aws-ec2-metadata-token-ttl-seconds: 600')",
            "INSTANCE_ID=$(curl -s -H \"X-aws-ec2-metadata-token: $TOKEN\" http://169.254.169.254/latest/meta-data/instance-id)",
            `LOGICAL_ID=$(for i in {1..30}; do V=$(aws ec2 describe-tags --region ${this.region} --filters Name=resource-id,Values=$INSTANCE_ID Name=key,Values=aws:cloudformation:logical-id --query 'Tags[0].Value' --output text 2>/dev/null) && [ -n "$V" ] && [ "$V" != None ] && echo $V && break; sleep 5; done)`,
            `trap '${signal("1")}' ERR`,
            "dnf install -y docker unzip aws-cfn-bootstrap",
            "systemctl enable --now docker",
            "sysctl -w vm.max_map_count=262144",
            "echo vm.max_map_count=262144 > /etc/sysctl.d/99-elasticsearch.conf",

            // Elasticsearch image with the IK analyzer plugin (version must match the server exactly)
            "mkdir -p /opt/es/build /opt/es/certs",
            "chmod 777 /opt/es/certs",
            `printf 'FROM docker.elastic.co/elasticsearch/elasticsearch:${ES_VERSION}\\nRUN bin/elasticsearch-plugin install --batch ${IK_PLUGIN_URL}\\n' > /opt/es/build/Dockerfile`,
            `docker build -t es-ik:${ES_VERSION} /opt/es/build`,

            // CA and node certificate whose SAN is this instance's private IP
            "IP=$(curl -s -H \"X-aws-ec2-metadata-token: $TOKEN\" http://169.254.169.254/latest/meta-data/local-ipv4)",
            `docker run --rm -v /opt/es/certs:/certs --entrypoint bin/elasticsearch-certutil es-ik:${ES_VERSION} ca --silent --pem --out /certs/ca.zip`,
            "unzip -o /opt/es/certs/ca.zip -d /opt/es/certs",
            `docker run --rm -v /opt/es/certs:/certs --entrypoint bin/elasticsearch-certutil es-ik:${ES_VERSION} cert --silent --pem --name node --ca-cert /certs/ca/ca.crt --ca-key /certs/ca/ca.key --dns localhost --ip $IP --ip 127.0.0.1 --out /certs/node.zip`,
            "unzip -o /opt/es/certs/node.zip -d /opt/es/certs",
            "chown -R 1000:0 /opt/es/certs",
            "chmod -R u=rwX,g=rX,o= /opt/es/certs",

            "ELASTIC_PASSWORD=$(openssl rand -hex 16)",
            `docker run -d --name es01 --restart unless-stopped -p 9200:9200 -m 1400m ` +
            "-e discovery.type=single-node -e ES_JAVA_OPTS='-Xms512m -Xmx512m' " +
            "-e ELASTIC_PASSWORD=$ELASTIC_PASSWORD " +
            "-e xpack.security.enabled=true -e xpack.security.http.ssl.enabled=true " +
            "-e xpack.security.http.ssl.certificate=certs/node/node.crt " +
            "-e xpack.security.http.ssl.key=certs/node/node.key " +
            "-v /opt/es/certs:/usr/share/elasticsearch/config/certs:ro -v esdata:/usr/share/elasticsearch/data " +
            `es-ik:${ES_VERSION}`,
            "for i in {1..60}; do curl -s --cacert /opt/es/certs/ca/ca.crt -u elastic:$ELASTIC_PASSWORD https://$IP:9200 >/dev/null 2>&1 && break; sleep 10; done",
            "curl -sf --cacert /opt/es/certs/ca/ca.crt -u elastic:$ELASTIC_PASSWORD https://$IP:9200 >/dev/null",

            "API_KEY_RESPONSE=$(curl -sSf --cacert /opt/es/certs/ca/ca.crt -u elastic:$ELASTIC_PASSWORD -X POST https://$IP:9200/_security/api_key -H 'Content-Type: application/json' -d '{\"name\":\"online-shopping-app-key\"}')",
            "aws ssm put-parameter --name /online-shopping/es/api-key --type SecureString --value \"$API_KEY_RESPONSE\" --overwrite --region " + this.region,
            "aws ssm put-parameter --name /online-shopping/es/ca-cert --type String --value \"$(cat /opt/es/certs/ca/ca.crt)\" --overwrite --region " + this.region,
            signal("0"),
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
        this.mqInstance.userData.addCommands(
            "IP=$(curl -s -H \"X-aws-ec2-metadata-token: $(curl -s -X PUT http://169.254.169.254/latest/api/token -H 'X-aws-ec2-metadata-token-ttl-seconds: 60')\" http://169.254.169.254/latest/meta-data/local-ipv4)",
            "sed -i \"s/^brokerIP1=.*/brokerIP1=$IP/\" /home/ec2-user/broker.conf",
            "sudo systemctl restart docker",
        );
        new cdk.CfnOutput(this, "RocketMQPrivateIp", {
            value: this.mqInstance.instancePrivateIp,
        });

    }
}
