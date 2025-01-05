package org.robolectric.shadows.httpclient;

import org.apache.http.HttpRequest;

public interface RequestMatcher {
  boolean matches(HttpRequest request);
}
