package org.jenkinsci.plugins.puppetenterprise.models;

import hudson.security.ACL;
import hudson.XmlFile;
import hudson.model.Saveable;
import jenkins.model.Jenkins;
import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;

import org.apache.http.*;
import org.apache.http.util.ExceptionUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.conn.ssl.*;
import org.apache.commons.io.IOUtils;

public class PuppetEnterpriseConfig implements Serializable, Saveable {
  private String puppetMasterUrl = null;
  private String puppetMasterCACertificate = "";

  public PuppetEnterpriseConfig() {
    loadGlobalConfig();
  }

  public void validatePuppetMasterUrl(String url) throws IOException, java.net.UnknownHostException,
    java.security.NoSuchAlgorithmException, java.security.KeyStoreException, java.security.KeyManagementException, org.apache.http.conn.HttpHostConnectException  {
    //this makes a connection to the master, so if the connection fails, the given address is invalid
    retrievePuppetMasterCACertificate(url);
  }

  public void setPuppetMasterUrl(String url) throws IOException, java.net.UnknownHostException,
    java.security.NoSuchAlgorithmException, java.security.KeyStoreException, java.security.KeyManagementException, org.apache.http.conn.HttpHostConnectException  {
    this.puppetMasterUrl = url;
    this.puppetMasterCACertificate = retrievePuppetMasterCACertificate();
    save();
  }

  public void setPuppetMasterCACertificate(String cert) {
    this.puppetMasterCACertificate = cert;
  }

  public String getPuppetMasterCACertificate() {
    return this.puppetMasterCACertificate;
  }

  private String retrievePuppetMasterCACertificate() throws java.net.UnknownHostException, IOException,
    java.security.NoSuchAlgorithmException, java.security.KeyStoreException, java.security.KeyManagementException, org.apache.http.conn.HttpHostConnectException {

    return retrievePuppetMasterCACertificate(this.puppetMasterUrl);
  }

  private String retrievePuppetMasterCACertificate(String address) throws java.net.UnknownHostException, IOException,
    java.security.NoSuchAlgorithmException, java.security.KeyStoreException, java.security.KeyManagementException, org.apache.http.conn.HttpHostConnectException {
    String returnString = "";

    SSLContextBuilder builder = new SSLContextBuilder();

    builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());

    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
    CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

    HttpGet httpGet = new HttpGet("https://" + address + ":8140/puppet-ca/v1/certificate/ca");
    returnString = IOUtils.toString(httpclient.execute(httpGet).getEntity().getContent());

    return returnString;
  }

  public void loadGlobalConfig() {
    try {
      XmlFile xml = getConfigFile();
      if (xml.exists()) {
        xml.unmarshal(this);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getPuppetMasterUrl() {
    if (!this.puppetMasterUrl.equals("")) {
      return this.puppetMasterUrl;
    } else {
      String puppetConfigPath = "/etc/puppetlabs/puppet/puppet.conf";
      File puppetFileHandler = new File(puppetConfigPath);
      try {
        if (puppetFileHandler.exists()) {
          String cmd = "/opt/puppetlabs/bin/puppet config print server --config /etc/puppetlabs/puppet/puppet.conf";

          Process p = Runtime.getRuntime().exec(cmd);
          p.waitFor();
          InputStreamReader is = new InputStreamReader(p.getInputStream());
          BufferedReader br = new BufferedReader(is);

          String lines = "";
          String line = "";

          while ((line = br.readLine()) != null) { lines = lines + line; }

          this.puppetMasterUrl = lines;
        } else {
          this.puppetMasterUrl = "https://puppet";
        }

        setPuppetMasterUrl(puppetMasterUrl);

      //If we fail here, it's the same as not having a config
      } catch(java.io.IOException e) {
      } catch(java.security.NoSuchAlgorithmException e) {
      } catch(java.security.KeyStoreException e) {
      } catch(java.security.KeyManagementException e) {
      } catch(InterruptedException e) {}
    }

    return this.puppetMasterUrl;
  }

  public void save() throws IOException {
    getConfigFile().write(this);
  }

  public static XmlFile getConfigFile() {
    return new XmlFile(new File(Jenkins.getInstance().getRootDir(), "puppet_enterprise.xml"));
  }
}
