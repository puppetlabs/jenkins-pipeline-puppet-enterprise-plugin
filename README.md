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
    puppet.credentials 'pe-access-token'
    puppet.codeDeploy 'production'
    puppet.job 'production', target: 'App[instance'], noop: true, concurrency: 10
}
```

### Experimental Hiera Feature

This plugin also provides an experimental feature that provides a Hiera
key/value store for Hiera. Key/value pairs are set using the provided
`puppet.hiera` method.  Pairs are assigned to specific Puppet environments.  The
[hiera-http](https://github.com/crayfishx/hiera-http)  backend performs a key lookup for the requesting node's
Puppet environment. An example configuration:

```
:backends:
  - http

:http:
  :host: jenkins.example.com
  :port: 8080
  :output: json
  :cache_timeout: 10
  :failure: graceful
  :paths:
    - /hiera/lookup?path=%{clientcert}&key=%{key}
    - /hiera/lookup?path=%{environment}&key=%{key}
```


To set values from the Jenkins Pipeline script:

```
node {
  puppet.hiera path: 'host.example.com', key: 'keyname', value: 'keyvalue'
  puppet.hiera path: 'dc-location', key: 'keyname', value: 'keyvalue'
  puppet.hiera path: 'production', key: 'keyname', value: 'keyvalue'
}
```

This is experimental because the values are stored in an XML file on the
Jenkins server.  There is no audit history of the data and therefor no way to
replicate past values. Also, if the file is lost due to, for example, disk
failure, the current values are lost.  So only use this if you trust your
Jenkins server backups and don't care about audit history.

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

### puppet.codeDeploy

The `puppet.codeDeploy` method tells Puppet Enterprise to deploy new Puppet code,
Hiera data, and modules to a specified Puppet environment. To lean more about
code management in Puppet Enterprise, go here: [https://docs.puppet.com/pe/latest/code_mgr.html]

**groovy script invocation**: puppet.codeDeploy()

**Parameters**

* environment - The environment to deploy. String. Required
* credentials - The Jenkins credentials storing the PE RBAC token. String. Required if puppet.credentials not used.

**Example**

```
  puppet.codeDeploy 'production', credentials: 'pe-access-token'
```

### puppet.job

**groovy script invocation**: puppet.job()

**Parameters**

* environment - The environment to run the job in. String
* target - Target in environment to deploy to. Can be app, app instance, or app component. Defaults to entire environment. String
* credentialsId - ID of the Jenkins Secret text credentials. String. Required if puppet.credentials not used
* concurrency - Level of maximum concurrency when issuing Puppet runs. Defaults to unlimited. Integer.
* noop - Whether to run Puppet in noop mode. Defaults to false. Boolean

**Example**

```
  puppet.job 'staging'
  puppet.job 'production', concurrency: 10, noop: true
  puppet.job 'production', concurrency: 10, noop: true, credentialsId: 'pe-access-token'
```

### puppet.hiera

**groovy script invocation**: puppet.hiera()

**Parameters**

* path - The path (scope) of the data lookup from Hiera. Usually this will be an environment name. Required. String
* key - The name of the key that Hiera will lookup. Required. String
* value - The value of the key to be returned to Hiera's lookup call. Required. Can be string, array, or hash

**Example**

```
  puppet.hiera path: 'staging', key: 'app-build-version', value: 'master'
  puppet.hiera path: 'production', key: 'app-build-version', value: '8f3ea2'
  puppet.hiera path: 'dc1-us-example', key: 'list-example', value: ['a,'b','c']
  puppet.hiera path: 'host.example.com', key: 'hash-example', value: ['a':1, 'bool':false, 'c': 'string']
```
