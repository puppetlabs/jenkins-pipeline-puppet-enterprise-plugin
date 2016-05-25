package org.jenkinsci.plugins.puppetenterprise.models;

import java.io.*;
import java.util.*;

public class PEResponse {
  private Integer code = null;
  private Object body = null;

  public PEResponse(Object body, Integer code) {
    this.body = body;
    this.code = code;
  }

  public Integer getResponseCode() {
    return this.code;
  }

  public Object getResponseBody() {
    return this.body;
  }
}
