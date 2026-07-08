package org.robolectric.shadows.httpclient;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

/**
 * @deprecated Apache HTTP client is deprecated in Android. Please migrate to an other solution
 */
@Deprecated
public interface HttpResponseGenerator {
  HttpResponse getResponse(HttpRequest request);
}
