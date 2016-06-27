package org.jenkinsci.plugins.workflow.steps;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.lang.InterruptedException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.*;
import javax.net.ssl.*;
import java.net.*;
import com.google.inject.Inject;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.conn.ssl.*;
import org.apache.commons.io.IOUtils;

import hudson.XmlFile;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import hudson.Util;
import hudson.model.Item;
import hudson.AbortException;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.security.ACL;
import java.io.Serializable;
import org.json.*;
import com.json.parsers.*;
import com.json.exceptions.JSONParsingException;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.plugins.plaincredentials.*;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.*;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.commons.io.IOUtils;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.annotation.Nonnull;

import org.jenkinsci.plugins.puppetenterprise.models.PuppetEnterpriseConfig;
import org.jenkinsci.plugins.puppetenterprise.models.PEResponse;
import org.jenkinsci.plugins.workflow.PEException;

public abstract class PuppetEnterpriseStep extends AbstractStepImpl implements Serializable {

  private static final Logger logger = Logger.getLogger(PuppetEnterpriseStep.class.getName());

  private String credentialsId;
  private PuppetEnterpriseConfig config;

  @DataBoundSetter public void setCredentialsId(String credentialsId) {
    this.credentialsId = Util.fixEmpty(credentialsId);
  }

  // @DataBoundSetter public void setPuppetMasterUrl(String url) {
  //   this.config.setPuppetMasterUrl(url);
  // }

  public void loadConfig() {
    this.config = new PuppetEnterpriseConfig();
  }

  private static StringCredentials lookupCredentials(@Nonnull String credentialId) {
      return CredentialsMatchers.firstOrNull(
               CredentialsProvider.lookupCredentials(StringCredentials.class, Jenkins.getInstance(), ACL.SYSTEM, null),
               CredentialsMatchers.withId(credentialId)
             );
  }

  private String getToken() {
    return lookupCredentials(credentialsId).getSecret().toString();
  }

  private KeyStore getTrustStore(final InputStream pathToPemFile) throws IOException, KeyStoreException,
  NoSuchAlgorithmException, CertificateException {
    final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(null);

    // load all certs
    for (java.security.cert.Certificate cert : CertificateFactory.getInstance("X509")
    .generateCertificates(pathToPemFile)) {
      final X509Certificate crt = (X509Certificate) cert;

      try {
        final String alias = crt.getSubjectX500Principal().getName();
        ks.setCertificateEntry(alias, crt);
      } catch (KeyStoreException exp) {
        System.out.println(exp.getMessage());
      }
    }

    return ks;
  }

  private CloseableHttpClient createHttpClient() {
    java.security.cert.Certificate ca;
    SSLConnectionSocketFactory sslsf = null;

    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");

      String caString = config.getPuppetMasterCACertificate();
      InputStream caStream = new ByteArrayInputStream(caString.getBytes(StandardCharsets.UTF_8));
      ca = cf.generateCertificate(caStream);

      // Create a KeyStore containing our trusted CAs
      String keyStoreType = KeyStore.getDefaultType();
      KeyStore keyStore = KeyStore.getInstance(keyStoreType);
      keyStore.load(null, null);
      keyStore.setCertificateEntry("ca", ca);

      SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(keyStore).build();
      sslsf = new SSLConnectionSocketFactory(sslcontext,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

    } catch(CertificateException e) { logger.log(Level.SEVERE, e.getMessage()); }
      catch(KeyStoreException e) { logger.log(Level.SEVERE, e.getMessage()); }
      catch(IOException e) { logger.log(Level.SEVERE, e.getMessage()); }
      catch(NoSuchAlgorithmException e) { logger.log(Level.SEVERE, e.getMessage()); }
      catch(KeyManagementException e) { logger.log(Level.SEVERE, e.getMessage()); }

    if (sslsf == null) { System.out.println("sslsf is null"); }

    return HttpClients.custom().setSSLSocketFactory(sslsf).build();
  }

  public final PEResponse request(String url, String method, Map body) throws Exception {
    URI uri = new URI (url);
    return this.request(uri.getPath(), uri.getPort(), method, body);
  }

  public final PEResponse request(String endpoint, Integer port, String method, Map body) throws Exception {
    JSONObject jsonBody = new JSONObject(body);
    String responseString = "";
    Object responseBody = new Object();
    String accessToken = getToken();
    PEResponse peResponse = null;

    HttpClient httpClient = createHttpClient();

    try {
      HttpResponse response = null;

      if (method == "POST") {
        HttpPost request = new HttpPost("https://" + config.getPuppetMasterUrl() + ":" + port + endpoint);

        if (body != null) {
          request.addHeader("content-type", "application/json");
          request.addHeader("X-Authentication", accessToken);
          StringEntity requestJson = new StringEntity(jsonBody.toString());
          request.setEntity(requestJson);
        }
        response = httpClient.execute(request);
      }

      if (method == "GET") {
        HttpGet request = new HttpGet("https://" + config.getPuppetMasterUrl() + ":" + port + endpoint);
        request.addHeader("X-Authentication", accessToken);
        response = httpClient.execute(request);
      }

      String json = IOUtils.toString(response.getEntity().getContent());
      JsonParserFactory factory = JsonParserFactory.getInstance();
      JSONParser parser = factory.newJsonParser();
      Integer responseCode = response.getStatusLine().getStatusCode();

      try {
        responseBody = parser.parseJson(json);

        //Sometimes there's a root key in the has and I don't know why.
        // It might only happen when the JSON is an array
        if (responseBody instanceof HashMap) {
          HashMap responseBodyHash = (HashMap) responseBody;
          if (responseBodyHash.get("root") != null) {
            responseBody = responseBodyHash.get("root");
          }
        }

      } catch(JSONParsingException e) {
        logger.log(Level.SEVERE, e.getMessage());

        HashMap errorContent = new HashMap();
        errorContent.put("error", json);
        return new PEResponse(errorContent, responseCode);
      }

      peResponse = new PEResponse(responseBody, responseCode);

    } catch(IOException e) {
      logger.log(Level.SEVERE, e.getMessage());
    }

    return peResponse;
  }

  public String getCredentialsId() { return credentialsId; }

  public String getPuppetMasterUrl() { return config.getPuppetMasterUrl(); }
}
