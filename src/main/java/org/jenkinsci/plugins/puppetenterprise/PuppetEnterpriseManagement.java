package org.jenkinsci.plugins.puppetenterprise;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.io.FileInputStream;

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
import net.sf.json.JSONObject;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
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

  public String getPuppetMasterUrl() {
    return config.getPuppetMasterUrl();
  }

  public HttpResponse doSaveConfig(StaplerRequest req) {
    try {
      JSONObject json = req.getSubmittedForm().getJSONObject("config");
      config.setPuppetMasterUrl(json.getString("puppetMasterUrl"));

      try {
        config.save();
      } catch(IOException e) {
        e.printStackTrace();
      }
    } catch (ServletException e) {
      e.printStackTrace();
    }
    return new HttpRedirect("index");
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
