import static groovy.grape.Grape.grab

grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.2')

import groovy.json.*
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

def pe_request( Map parameters = [:], String endpoint ) {
  def puppet_master = ''
  def token = ''

  assert parameters.port instanceof Integer

  if (parameters.method) {
    assert parameters.method instanceof String
    assert [GET,POST,DELETE,PUT].contains(parameters.method)
  } else {
    parameters.method = GET
  }

  if (parameters.body) {
    assert parameters.body instanceof Map
    parameters.body = new JsonBuilder(parameters.body).toString()
  }

  def http = new HTTPBuilder("https://${puppet_master}:${parameters.port}${endpoint}")

  http.request(parameters.method, JSON) { req ->
    body = parameters.body
    response.success = { resp, reader ->
      def slurper = new JsonSlurper()
      return slurper.parseText(string)
    }
  }
}

def deployCode(environments = 'all' ) {
  def api = '/code-manager/v1'
  def request_body = ["wait": true]

  if (environments == 'all') {
    request_body["deploy-all"] = true
  } else if (environments instanceof List || environments instanceof String) {
    request_body["environments"] = environments
  } else {
    throw new Exception("Provided environments in unkonwn format. Should be List or String")
  }

  pe_request("${api}/deploys", port: 8170, method: 'POST', body: request_body)
}
