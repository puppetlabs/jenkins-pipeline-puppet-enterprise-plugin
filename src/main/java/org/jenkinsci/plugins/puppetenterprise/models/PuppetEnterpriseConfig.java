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
  private String puppetMasterUrl = "";
  private String puppetMasterCACertificate = "";

  public PuppetEnterpriseConfig() {
    loadGlobalConfig();
  }

  public void setPuppetMasterUrl(String url) throws IOException, java.net.UnknownHostException {
    this.puppetMasterUrl = url;
    this.puppetMasterCACertificate = retrievePuppetMasterCACertificate();
  }

  public void setPuppetMasterCACertificate(String cert) {
    this.puppetMasterCACertificate = cert;
  }

  public String getPuppetMasterCACertificate() {
    return this.puppetMasterCACertificate;
  }

  private String retrievePuppetMasterCACertificate() throws java.net.UnknownHostException, IOException {
    String returnString = "";

    SSLContextBuilder builder = new SSLContextBuilder();

    try {
      builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());

      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
      CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

      HttpGet httpGet = new HttpGet("https://" + puppetMasterUrl + ":8140/puppet-ca/v1/certificate/ca");
      returnString = IOUtils.toString(httpclient.execute(httpGet).getEntity().getContent());
    } catch(java.security.NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch(java.security.KeyStoreException e) {
      e.printStackTrace();
    } catch(java.security.KeyManagementException e) {
      e.printStackTrace();
    }

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
      if (puppetFileHandler.exists()) {
        String cmd = "/opt/puppetlabs/bin/puppet config print server --config /etc/puppetlabs/puppet/puppet.conf";

        try {
          Process p = Runtime.getRuntime().exec(cmd);
          p.waitFor();
          InputStreamReader is = new InputStreamReader(p.getInputStream());
          BufferedReader br = new BufferedReader(is);

          String lines = "";
          String line = "";

          while ((line = br.readLine()) != null) { lines = lines + line; }

          puppetMasterUrl = lines;
          this.puppetMasterCACertificate = retrievePuppetMasterCACertificate();
        } catch(IOException e) {
          e.printStackTrace();
        } catch(InterruptedException e) {
          e.printStackTrace();
        }
      } else {
        puppetMasterUrl = "https://puppet";
      }

      try {
        save();
      } catch(IOException e) {
        e.printStackTrace();
      }
    }

    return puppetMasterUrl;
  }

  public void save() throws IOException {
    getConfigFile().write(this);
  }

  public static XmlFile getConfigFile() {
    return new XmlFile(new File(Jenkins.getInstance().getRootDir(), "puppet_enterprise.xml"));
  }
}
