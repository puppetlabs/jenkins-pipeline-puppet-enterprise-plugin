package org.jenkinsci.plugins.puppetenterprise.api;

import java.io.*;
import java.util.*;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import hudson.model.UnprotectedRootAction;
import hudson.Extension;
import org.json.*;
import javax.servlet.ServletException;

import org.jenkinsci.plugins.puppetenterprise.models.HieraConfig;

@Extension
public class HieraDataStore implements UnprotectedRootAction {
  private static final String ICON_PATH = "/plugin/workflow-puppet-enterprise/images/cfg_logo.png";

  private HieraConfig hiera = null;

  public HieraDataStore() {
    hiera = new HieraConfig();
  }

  @Override
  public String getUrlName() {
    return "hiera";
  }

  @Override
  public String getDisplayName() {
    return "Hiera Data Lookup";
  }

  @Override
  public String getIconFileName() {
    return ICON_PATH;
  }

  public void doLookup(StaplerRequest req, StaplerResponse rsp) throws IOException {
    net.sf.json.JSONObject form = null;

    try {
      form = req.getSubmittedForm();
    } catch(ServletException e) {
      e.printStackTrace();
    }

    String returnValue = "";
    String environment = form.getString("environment");
    String key = form.getString("key");

    Object value = hiera.getKeyValue(environment, key);

    if (value == null) {
      returnValue = "";
    }

    if (value instanceof String) {
      returnValue = (String) value;
    } else if (value instanceof ArrayList) {
      ArrayList valueArray = (ArrayList) value;
      returnValue = new JSONObject(valueArray).toString();
    } else if (value instanceof HashMap) {
      HashMap valueHash = (HashMap) value;
      returnValue = new JSONObject(valueHash).toString();
    }

    rsp.setContentType("application/json;charset=UTF-8");
    rsp.getOutputStream().print(returnValue);
  }
}
