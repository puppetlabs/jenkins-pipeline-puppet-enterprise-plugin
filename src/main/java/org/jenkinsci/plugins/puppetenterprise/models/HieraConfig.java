package org.jenkinsci.plugins.puppetenterprise.models;

import hudson.security.ACL;
import hudson.XmlFile;
import hudson.model.Saveable;
import jenkins.model.Jenkins;
import org.json.*;
import java.util.*;
import java.util.HashMap;
import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class HieraConfig implements Serializable, Saveable {
  private static HashMap hierarchy = new HashMap();

  public HieraConfig() {
    loadGlobalConfig();
  }

  public Object getKeyValue(String environment, String key) {
    HashMap envHierarchy = (HashMap) HieraConfig.hierarchy.get(environment);

    if (envHierarchy == null) {
      return null;
    }

    return envHierarchy.get(key);
  }

  public void setKeyValue(String environment, String key, Object value) {
    if (HieraConfig.hierarchy.get(environment) == null) {
      HieraConfig.hierarchy.put(environment, new HashMap());
    }

    HashMap envHierarchy = (HashMap) HieraConfig.hierarchy.get(environment);

    envHierarchy.put(key, value);
    HieraConfig.hierarchy.put(environment, envHierarchy);

    try {
      save();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  public void loadGlobalConfig() {
    try {
      XmlFile xml = getConfigFile();
      if (xml.exists()) {
        HieraConfig.hierarchy = (HashMap) xml.read();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void save() throws IOException {
    getConfigFile().write(HieraConfig.hierarchy);
  }

  public static XmlFile getConfigFile() {
    return new XmlFile(new File(Jenkins.getInstance().getRootDir(), "puppet_enterprise_hiera_store.xml"));
  }
}
