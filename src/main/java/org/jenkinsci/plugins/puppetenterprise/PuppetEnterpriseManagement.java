package org.jenkinsci.plugins.puppetenterprise;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.io.FileInputStream;

import com.google.inject.Inject;

import javax.servlet.ServletException;
import org.apache.commons.io.IOUtils;

import jenkins.model.Jenkins;
import hudson.XmlFile;
import hudson.Extension;
import hudson.Util;
import hudson.model.Hudson;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import hudson.util.FormValidation;
import hudson.util.FormApply;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import org.jenkinsci.plugins.puppetenterprise.models.PuppetEnterpriseConfig;

@Extension
public class PuppetEnterpriseManagement extends ManagementLink {
  private static final String ICON_PATH = "/plugin/workflow-puppet-enterprise/images/cfg_logo.png";

  private PuppetEnterpriseConfig config;

  public PuppetEnterpriseManagement() {
    this.config = new PuppetEnterpriseConfig();
  }

  public String getFullURL(){
    return Stapler.getCurrentRequest().getOriginalRequestURI().substring(1);
  }

  @Override
  public String getDisplayName() {
    return "Puppet Enterprise";
  }

  @Override
  public String getDescription() {
    return "The Puppet Master location and communication configurations";
  }

  @Override
  public String getIconFileName() {
    return ICON_PATH;
  }

  public String getMaster() {
    return config.getPuppetMasterUrl();
  }

  public FormValidation doCheckMaster(@QueryParameter String master) throws IOException, ServletException {
    try {
      config.validatePuppetMasterUrl(master);
      return FormValidation.ok("Succesfully Communicated with Puppet master.");
    } catch(java.net.UnknownHostException e) {
      return FormValidation.error("Unknown host");
    } catch(java.security.NoSuchAlgorithmException e) {
      return FormValidation.error("Unable to negotiate SSL connection with host. " + e.getMessage());
    } catch(java.security.KeyStoreException e) {
      return FormValidation.error("Unable to negotiate SSL connection with host. " + e.getMessage());
    } catch(java.security.KeyManagementException e) {
      return FormValidation.error("Unable to negotiate SSL connection with host. " + e.getMessage());
    } catch(org.apache.http.conn.HttpHostConnectException e) {
      return FormValidation.error("Unable to reach host. Check pe-puppetserver process is running and no firewalls are blocking port 8140.");
    }
  }

  public HttpResponse doSaveConfig(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    try {
      JSONObject json = req.getSubmittedForm().getJSONObject("config");

      config.setPuppetMasterUrl(json.getString("puppetMasterUrl"));
    } catch(Exception e) {
      throw new ServletException(e);
    }

    return FormApply.success(".");
  }

  public String getIconUrl(String rootUrl) {
    if (rootUrl.endsWith("/")) {
      return rootUrl + ICON_PATH.substring(1);
    }
    return rootUrl + ICON_PATH;
  }

  @Override
  public String getUrlName() {
    return "puppetenterprise";
  }
}
