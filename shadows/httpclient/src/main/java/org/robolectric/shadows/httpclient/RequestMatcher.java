package org.robolectric.shadows.httpclient;

import org.apache.http.HttpRequest;

/**
 * @deprecated Apache HTTP client is deprecated in Android. Please migrate to an other solution
 */
@Deprecated
public interface RequestMatcher {
  boolean matches(HttpRequest request);
}
