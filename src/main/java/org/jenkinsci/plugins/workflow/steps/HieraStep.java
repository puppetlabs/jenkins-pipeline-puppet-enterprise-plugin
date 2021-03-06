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
import javax.annotation.Nonnull;

import org.jenkinsci.plugins.workflow.PEException;
import org.jenkinsci.plugins.puppetenterprise.models.HieraConfig;

public final class HieraStep extends AbstractStepImpl {

  private static final Logger logger = Logger.getLogger(HieraStep.class.getName());

  private String scope = "";
  private String key = "";
  private String source = "";
  private Object value = null;
  private HieraConfig hiera = null;

  @DataBoundSetter private void setScope(String scope) {
    this.scope = Util.fixEmpty(scope);
  }

  @DataBoundSetter private void setKey(@Nonnull String key) {
    this.key = key;
  }

  @DataBoundSetter private void setValue(@Nonnull Object value) {
    this.value = value;
  }

  @DataBoundSetter private void setSource(@Nonnull String source) {
    this.source = source;
  }

  public String getSceop() {
    return this.scope;
  }

  public String getKey() {
    return this.key;
  }

  public Object getValue() {
    return this.value;
  }

  public String getSource() {
    return this.source;
  }

  @DataBoundConstructor public HieraStep() {
    hiera = new HieraConfig();
  }

  public void save() {
    hiera.setKeyValue(this.scope, this.key, this.source, this.value);
    logger.log(Level.INFO, "Successfully saved key/value pair " + this.key + "/" + this.value + " to scope " + this.scope + " from source " + this.source + ".");
  }

  public static class HieraStepExecution extends AbstractSynchronousStepExecution<Void> {

    @Inject private transient HieraStep step;
    @StepContextParameter private transient Run<?, ?> run;
    @StepContextParameter private transient TaskListener listener;

    @Override protected Void run() throws Exception {
      step.save();
      return null;
    }

    private static final long serialVersionUID = 1L;
  }

  @Extension public static final class DescriptorImpl extends AbstractStepDescriptorImpl {
    public DescriptorImpl() {
      super(HieraStepExecution.class);
    }

    @Override public String getFunctionName() {
      return "puppetHiera";
    }

    @Override public String getDisplayName() {
      return "Set Hiera data.";
    }
  }
}
