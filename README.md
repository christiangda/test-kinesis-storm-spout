# Test AWS Kinesis Spout with Apache Storm 1.0.2
Until [AWS kinesis-storm-spout](https://github.com/awslabs/kinesis-storm-spout)
maintaners don't have created a new release with support for [Apache Storm 1.0.2](http://storm.apache.org/), you will need merge a [kinesis-storm-spout github project --> PR #37](https://github.com/awslabs/kinesis-storm-spout/pull/37)
to master branch to create your own artifact.

The goal of this project is show you how to use [AWS kinesis-storm-spout](https://github.com/awslabs/kinesis-storm-spout) with [Apache Storm 1.0.2](http://storm.apache.org/) before we don't have a official release!

## How to create a new artifact

I'm asumming that you have installed
* [Maven >= 3.3.9](https://maven.apache.org/)
* [IntelliJ >= 2016.x](https://www.jetbrains.com/idea/)
* [AWS Command Line Interface](http://docs.aws.amazon.com/cli/latest/userguide/installing.html)

and also configured your AWS Cli like this
```
cat ~/.aws/credentials
[test-kinesis]
aws_access_key_id = <your access key here>
aws_secret_access_key = <your secret key here>

```

### Get the Pull Request #37
```
cd /tmp
git clone https://github.com/awslabs/kinesis-storm-spout.git
cd kinesis-storm-spout/
git fetch origin pull/37/head:upgrade-storm
git checkout upgrade-storm
```

### Override project version with your own version
```
cd /tmp/kinesis-storm-spout/
mvn versions:set -DnewVersion=1.2.0-custom-1
```

### Compile and Package
```
cd /tmp/kinesis-storm-spout/
mvn clean package
```

### Override project DeplymentRepository to deploy local
I'm asumming that you have installed Maven, and remember to chage
`<put your username here!>`
```
cd /tmp/kinesis-storm-spout/

mvn -Dgpg.skip -DaltDeploymentRepository=internal.repo::default::file:///home/<put your username here!>/.m2 deploy
```

### Using IntelliJ to run Apache Storm Topoly in local mode
![Intellij Runnig Cof](running-conf.png)

![Intellij Running Topology in local mode](local-mode.png)
