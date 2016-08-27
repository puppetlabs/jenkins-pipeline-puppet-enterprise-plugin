package org.jenkinsci.plugins.workflow;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import hudson.model.TaskListener;

public class PEException extends Exception {
  private static final Logger logger = Logger.getLogger(PEException.class.getName());

  public PEException() { super(); }

  public PEException(String message) {
    logger.log(Level.SEVERE, message);
  }

  public PEException(Integer code) {
    logger.log(Level.SEVERE, "API call to PE resulted in status code (" + code + ")");
  }

  public PEException(String message, Integer code) {
    logger.log(Level.SEVERE, "PE API call resulted in code (" + code + ") and message \"" + message + "\"");
  }

  public PEException(String message, Integer code, TaskListener listener) {
    logger.log(Level.SEVERE, "PE API call resulted in code (" + code + ") and message \"" + message + "\"");
    listener.getLogger().println("PE API call resulted in code (" + code + ") and message \"" + message + "\"");
  }

  public PEException(String message, TaskListener listener) {
    logger.log(Level.SEVERE, message);
    listener.getLogger().println(message);
  }
}
