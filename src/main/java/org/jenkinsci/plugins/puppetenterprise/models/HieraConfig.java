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
import java.util.logging.Level;
import java.util.logging.Logger;

public class HieraConfig implements Serializable, Saveable {
  private static HashMap hierarchy = new HashMap();

  private static final Logger logger = Logger.getLogger(HieraConfig.class.getName());

  public HieraConfig() {
    loadGlobalConfig();
  }

  public Object getKeyValue(String path, String key) {
    HashMap pathHierarchy = (HashMap) HieraConfig.hierarchy.get(path);

    if (pathHierarchy == null) {
      return null;
    }

    return pathHierarchy.get(key);
  }

  public Set<String> getPaths() {
    return hierarchy.keySet();
  }

  public Set<String> getKeys(String path) {
    HashMap pathHierarchy = (HashMap) hierarchy.get(path);
    return pathHierarchy.keySet();
  }

  public void setKeyValue(String path, String key, Object value) {
    if (HieraConfig.hierarchy.get(path) == null) {
      HieraConfig.hierarchy.put(path, new HashMap());
    }

    HashMap pathHierarchy = (HashMap) HieraConfig.hierarchy.get(path);

    pathHierarchy.put(key, value);
    HieraConfig.hierarchy.put(path, pathHierarchy);

    try {
      save();
    } catch(IOException e) {
      logger.log(Level.SEVERE, "Error saving Hiera configuration: " + e.getMessage());
    }
  }

  public void loadGlobalConfig() {
    try {
      XmlFile xml = getConfigFile();
      if (xml.exists()) {
        HieraConfig.hierarchy = (HashMap) xml.read();
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error loading Hiera configuration: " + e.getMessage());
    }
  }

  public void save() throws IOException {
    getConfigFile().write(HieraConfig.hierarchy);
  }

  public static XmlFile getConfigFile() {
    return new XmlFile(new File(Jenkins.getInstance().getRootDir(), "puppet_enterprise_hiera_store.xml"));
  }
}
