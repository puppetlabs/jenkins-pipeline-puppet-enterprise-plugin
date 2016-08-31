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

  public Object getKeyValue(String scope, String key) {
    HashMap scopeHierarchy = (HashMap) HieraConfig.hierarchy.get(scope);

    if (scopeHierarchy == null) {
      return null;
    }

    HashMap keyData = (HashMap) scopeHierarchy.get(key);

    return keyData.get("value");
  }

  public String getKeySource(String scope, String key) {
    HashMap scopeHierarchy = (HashMap) HieraConfig.hierarchy.get(scope);

    if (scopeHierarchy == null) {
      return null;
    }

    HashMap keyData = (HashMap) scopeHierarchy.get(key);

    return (String) keyData.get("source");
  }

  public Set<String> getScopes() {
    return hierarchy.keySet();
  }

  public Set<String> getKeys(String scope) {
    HashMap scopeHierarchy = (HashMap) hierarchy.get(scope);
    return scopeHierarchy.keySet();
  }

  public void deleteScope(String scope) {
    if (hierarchy.get(scope) == null) {
      logger.log(Level.WARNING, "Attempted to delete non-existent hiera Scope " + scope);
    } else {
      hierarchy.remove(scope);

      try {
        save();
      } catch(IOException e) {
        logger.log(Level.SEVERE, "Error saving Hiera configuration: " + e.getMessage());
      }
    }
  }

  public void deleteKey(String key, String scope) {
    if (hierarchy.get(scope) == null) {
      logger.log(Level.WARNING, "Attempted to delete key '" + key + " from non-existent hiera Scope " + scope);
    } else {
      HashMap scopeHierarchy = (HashMap) hierarchy.get(scope);

      if (scopeHierarchy.get(key) == null) {
        logger.log(Level.WARNING, "Attempted to delete non-existent key '" + key + " from hiera Scope " + scope);
      } else {
        scopeHierarchy.remove(key);

        try {
          save();
        } catch(IOException e) {
          logger.log(Level.SEVERE, "Error saving Hiera configuration: " + e.getMessage());
        }
      }
    }
  }

  public void setKeyValue(String scope, String key, String source, Object value) {
    if (HieraConfig.hierarchy.get(scope) == null) {
      HieraConfig.hierarchy.put(scope, new HashMap());
    }

    HashMap scopeHierarchy = (HashMap) HieraConfig.hierarchy.get(scope);

    HashMap keyData = new HashMap();
    keyData.put("source", source);
    keyData.put("value", value);

    scopeHierarchy.put(key, keyData);
    HieraConfig.hierarchy.put(scope, scopeHierarchy);

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
