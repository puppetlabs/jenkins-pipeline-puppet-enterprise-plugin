# Introduction

This plugin adds Jenkins Pipeline steps for Puppet Enterprise. The provided
steps make it easy to interface with Puppet Enterprise services such as the
code management service and orchestrator service. 

# Features

A Pipeline project can use the provided groovy methods to deploy Puppet code to
Puppet Enterprise servers and create Puppet orchestrator jobs.

Puppet Enterprise RBAC access tokens are used to authenticate with the Puppet
Enterprise APIs, so Puppet itself doesn't have to be configured on the Jenkins
server.

```
node {
    puppetCode environment: 'production', credentialsId: 'pe-access-token'
    puppetJob  environment: 'production', target: 'App[instance]', credentialsId: 'pe-access-token'
}
```

## Configuration

### Puppet Master Address

Go to Jenkins > Manage Jenkins > Puppet Enterprise.  Fill out the DNS address
of the Puppet Enterprise Server.  Note, if the Puppet agent is installed on the
Jenkins server, it will be used to configure the Puppet Enterprise Server
address.

The Puppet Enterprise Server CA certificate is automatically pulled from the
Puppet Server's CA API. External CA's are not currently supported.

### Access Token Credentials

This plugin uses the [Plain Credentials plugin](https://wiki.jenkins-ci.org/display/JENKINS/Plain+Credentials+Plugin) to store access tokens.  

First, create a new RBAC access token in Puppet Enterprise. Follow the instructions for [generating a token for use by a service](https://docs.puppet.com/pe/latest/rbac_token_auth.html#generating-a-token-for-use-by-a-service).

Second, create new credentials in Jenkins. For **Kind** specify **Secret
text**.  Set the **Secret** value to the token printed out by the first step.
It is recommended to give the token an easy-to-identiy **ID** by clicking on
the **Advanced** button. That will make it easier to identify which token is
being used from the Jenkins Pipeline scripts.

## Pipeline Steps

### puppetCode

The `puppetCode` method tells Puppet Enterprise to deploy new Puppet code,
Hiera data, and modules to a specified Puppet environment. To lean more about
code management in Puppet Enterprise, go here: [https://docs.puppet.com/pe/latest/code_mgr.html]

**groovy script invocation**: puppetCode()

**Parameters**

* environment
* credentialsId

**Example**

```
  puppetCode environment: 'production', credentialsId: 'pe-access-token'
```

### puppetJob

**groovy script invocation**: puppetJob()

**Parameters**

* environment - The environment to run the job in. String
* target - Target in environment to deploy to. Can be app, app instance, or app component. Defaults to entire environment. String
* credentialsId - ID of the Jenkins Secret text credentials. String
* concurrency - Level of maximum concurrency when issuing Puppet runs. Defaults to unlimited. Integer.
* noop - Whether to run Puppet in noop mode. Defaults to false. Boolean

**Example**

```
  puppetJob environment: 'staging', credentialsId: 'pe-access-token'
  puppetJob environment: 'production', concurrency: 10, noop: true, credentialsId: 'pe-access-token'
```
