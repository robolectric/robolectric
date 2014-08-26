package org.robolectric.shadows;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public interface HttpResponseGenerator {
  public HttpResponse getResponse(HttpRequest request);
}
