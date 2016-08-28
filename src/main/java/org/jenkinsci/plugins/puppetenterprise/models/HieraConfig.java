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

    HashMap keyData = (HashMap) pathHierarchy.get(key);

    return keyData.get("value");
  }

  public String getKeySource(String path, String key) {
    HashMap pathHierarchy = (HashMap) HieraConfig.hierarchy.get(path);

    if (pathHierarchy == null) {
      return null;
    }

    HashMap keyData = (HashMap) pathHierarchy.get(key);

    return (String) keyData.get("source");
  }

  public Set<String> getPaths() {
    return hierarchy.keySet();
  }

  public Set<String> getKeys(String path) {
    HashMap pathHierarchy = (HashMap) hierarchy.get(path);
    return pathHierarchy.keySet();
  }

  public void deletePath(String path) {
    if (hierarchy.get(path) == null) {
      logger.log(Level.WARNING, "Attempted to delete non-existent hiera Scope " + path);
    } else {
      hierarchy.remove(path);

      try {
        save();
      } catch(IOException e) {
        logger.log(Level.SEVERE, "Error saving Hiera configuration: " + e.getMessage());
      }
    }
  }

  public void deleteKey(String key, String path) {
    if (hierarchy.get(path) == null) {
      logger.log(Level.WARNING, "Attempted to delete key '" + key + " from non-existent hiera Scope " + path);
    } else {
      HashMap pathHierarchy = (HashMap) hierarchy.get(path);

      if (pathHierarchy.get(key) == null) {
        logger.log(Level.WARNING, "Attempted to delete non-existent key '" + key + " from hiera Scope " + path);
      } else {
        pathHierarchy.remove(key);

        try {
          save();
        } catch(IOException e) {
          logger.log(Level.SEVERE, "Error saving Hiera configuration: " + e.getMessage());
        }
      }
    }
  }

  public void setKeyValue(String path, String key, String source, Object value) {
    if (HieraConfig.hierarchy.get(path) == null) {
      HieraConfig.hierarchy.put(path, new HashMap());
    }

    HashMap pathHierarchy = (HashMap) HieraConfig.hierarchy.get(path);

    HashMap keyData = new HashMap();
    keyData.put("source", source);
    keyData.put("value", value);

    pathHierarchy.put(key, keyData);
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
