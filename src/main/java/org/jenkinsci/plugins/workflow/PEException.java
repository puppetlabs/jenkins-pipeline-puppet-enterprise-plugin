package org.jenkinsci.plugins.workflow;

import java.io.*;

public class PEException extends Exception {
  public PEException() { super(); }
  public PEException(String message) { super(message); }
  public PEException(Integer code) { super("API call to PE resulted in status code (" + code + ")"); }
  public PEException(String message, Integer code) { super("PE API call resulted in code (" + code + ") and message \"" + message + "\""); }
}
