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

    node {
      if (parameters.credentials) {
        credentials = parameters.credentials
      } else {
        credentials = credentialsId
      }

      if(credentials == null) {
        script.error(message: "No Credentials provided for puppet.codeDeploy. Specify 'credentials' parameter or use puppet.credentials()")
      }

      script.puppetCode(environment: env, credentialsId: credentialsId)
    }
  }

  public <V> V job(Map parameters = [:], String env) {
    String credentials
    String application
    String query
    String target = null
    ArrayList nodes = []
    Boolean noop = false
    Integer concurrency = null

    node {
      if (parameters.credentials) {
        credentials = parameters.credentials
      } else {
        credentials = credentialsId
      }

      if (parameters.application) {
        assert parameters.application instanceof String
        application = parameters.application
      }

      if (parameters.query) {
        assert parameters.query instanceof String
        query = parameters.query
      }

      if (parameters.nodes) {
        assert parameters.nodes instanceof java.util.ArrayList
        nodes = parameters.nodes
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

      if (credentials == null) {
        script.error(message: "No Credentials provided for puppet.run. Specify 'credentials' parameter or use puppet.credentials()")
      }

      try {
        script.puppetJob(environment: env, target: target, concurrency: concurrency, credentialsId: credentials, nodes: nodes, query: query, application: application)
      } catch(err) {
        script.error(message: err.message)
      }
    }
  }

  public <V> V hiera(Map parameters = [:]) {
    String credentials

    assert parameters.scope instanceof String
    assert parameters.key instanceof String

    node {
      def projectName = script.env.JOB_NAME

      script.puppetHiera(scope: parameters.scope, key: parameters.key, source: projectName, value: parameters.value)
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
