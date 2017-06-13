package org.robolectric.shadows.httpclient;

import org.apache.http.HttpRequest;

public interface RequestMatcher {
  public boolean matches(HttpRequest request);
}
