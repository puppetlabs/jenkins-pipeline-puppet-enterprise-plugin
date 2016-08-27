package org.jenkinsci.plugins.workflow;

class Puppet implements Serializable {

  private org.jenkinsci.plugins.workflow.cps.CpsScript script

  def credentialsId

  public Puppet(org.jenkinsci.plugins.workflow.cps.CpsScript script) {
    this.script = script
  }

  public <V> V credentials(String creds) {
    credentialsId = creds
  }

  public <V> V codeDeploy(Map parameters = [:], String env) {
    String credentials

    if (parameters.credentials) {
      credentials = parameters.credentials
    } else {
      credentials = credentialsId
    }

    if(credentials == null) {
      System.out "No Credentials Provided for puppet.codeDeploy call"
    }

    node {
      script.puppetCode(environment: env, credentialsId: credentialsId)
    }
  }

  public <V> V job(Map parameters = [:], String env) {
    String credentials
    String target = null
    Boolean noop = false
    Integer concurrency = null

    if (parameters.credentials) {
      credentials = parameters.credentials
    } else {
      credentials = credentialsId
    }

    if (parameters.target) {
      assert parameters.target instanceof String
      target = parameters.target
    }

    if (parameters.noop) {
      assert parameters.noop instanceof Boolean
      noop = parameters.noop
    }

    if (parameters.concurrency) {
      assert parameters.concurrency instanceof Integer
      concurrency = parameters.concurrency
    }

    if(credentials == null) {
      System.out "No Credentials Provided for puppet.run call"
    }

    node {
      script.puppetJob(environment: env, target: target, concurrency: concurrency, credentialsId: credentials)
    }
  }

  private <V> V node(Closure<V> body) {
    if (script.env.NODE_NAME != null) {
        // Already inside a node block.
        body()
    } else {
        script.node {
            body()
        }
    }
  }
}
