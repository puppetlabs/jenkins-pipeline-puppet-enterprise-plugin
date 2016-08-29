package org.jenkinsci.plugins.workflow.steps;

import java.util.*;
import com.google.inject.Inject;
import hudson.Extension;
import hudson.Util;
import hudson.util.ListBoxModel;
import hudson.security.ACL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.apache.commons.lang.StringUtils;
import hudson.model.Run;
import hudson.model.Item;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import javax.annotation.Nonnull;

import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.plugins.plaincredentials.*;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.AncestorInPath;
import java.io.Serializable;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.jenkinsci.plugins.puppetenterprise.PuppetEnterpriseManagement;
import org.jenkinsci.plugins.puppetenterprise.models.PEResponse;
import org.jenkinsci.plugins.workflow.PEException;

public final class CodeDeployStep extends PuppetEnterpriseStep implements Serializable {

  private static final Logger logger = Logger.getLogger(CodeDeployStep.class.getName());

  private String environment = "";

  @DataBoundSetter private void setEnvironment(String environment) {
    this.environment = Util.fixEmpty(environment);
  }

  public String getEnvironment() {
    return this.environment;
  }

  @DataBoundConstructor public CodeDeployStep() {
    super.loadConfig();
  }

  public static class CodeDeployStepExecution extends AbstractSynchronousStepExecution<Void> {

    @Inject private transient CodeDeployStep step;
    @StepContextParameter private transient Run<?, ?> run;
    @StepContextParameter private transient TaskListener listener;

    @Override protected Void run() throws Exception {
      HashMap body = new HashMap();
      ArrayList environments = new ArrayList();
      environments.add(step.getEnvironment());

      body.put("wait", true);
      body.put("environments", environments);

      PEResponse result = step.request("/code-manager/v1/deploys", 8170, "POST", body);

      if (!step.isSuccessful(result)) {
        ArrayList envResults = (ArrayList) result.getResponseBody();
        HashMap firstHash = (HashMap) envResults.get(0);
        HashMap error = (HashMap) firstHash.get("error");

        logger.log(Level.SEVERE, error.toString());
        throw new PEException(error.toString(), result.getResponseCode(), listener);
      } else {
        listener.getLogger().println("Successfully deployed " + environments + " Puppet environment code.");
        logger.log(Level.INFO, "Successfully deployed " + environments + " Puppet environment code.");
      }

      return null;
    }

    private static final long serialVersionUID = 1L;
  }

  public Boolean isSuccessful(PEResponse response) {
    Integer responseCode = response.getResponseCode();
    Object responseBody = response.getResponseBody();

    if (responseCode < 200 || responseCode >= 300) {
      return false;
    }

    if (responseBody instanceof ArrayList) {
      ArrayList responseList = (ArrayList) responseBody;

      Iterator itr = responseList.iterator();
      while(itr.hasNext()) {
        HashMap envResponse = (HashMap) itr.next();

        if (envResponse.get("status").equals("failed")) {
          return false;
        }
      }
    }

    if (responseBody instanceof HashMap) {
      HashMap responseHash = (HashMap) responseBody;

      if (responseHash.get("error") != null) {
        return false;
      }
    }

    return true;
  }

  @Extension public static final class DescriptorImpl extends AbstractStepDescriptorImpl {
    public DescriptorImpl() {
      super(CodeDeployStepExecution.class);
    }

    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item context, @QueryParameter String source) {
      if (context == null || !context.hasPermission(Item.CONFIGURE)) {
        return new ListBoxModel();
      }
      return new StandardListBoxModel().withEmptySelection().withAll(
      CredentialsProvider.lookupCredentials(StringCredentials.class, context, ACL.SYSTEM, URIRequirementBuilder.fromUri(source).build()));
    }

    @Override public String getFunctionName() {
      return "puppetCode";
    }

    @Override public String getDisplayName() {
      return "Deploy Puppet Environment Code";
    }
  }
}
