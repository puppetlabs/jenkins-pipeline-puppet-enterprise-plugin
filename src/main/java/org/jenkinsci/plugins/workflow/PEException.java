package org.jenkinsci.plugins.workflow;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import hudson.model.TaskListener;

public class PEException extends Exception {
  private static final Logger logger = Logger.getLogger(PEException.class.getName());

  public PEException() { super(); }

  public PEException(String message) {
    super(message);
    logger.log(Level.SEVERE, message);
  }

  public PEException(Integer code) {
    super("API call to PE resulted in status code (" + code + ")");
    logger.log(Level.SEVERE, "API call to PE resulted in status code (" + code + ")");
  }

  public PEException(String message, Integer code) {
    super("PE API call resulted in code (" + code + ") and message \"" + message + "\"");
    logger.log(Level.SEVERE, "PE API call resulted in code (" + code + ") and message \"" + message + "\"");
  }

  public PEException(String message, Integer code, TaskListener listener) {
    listener.getLogger().println(message);
  }

  public PEException(String message, TaskListener listener) {
    super(message);
    listener.getLogger().println(message);
  }
}
