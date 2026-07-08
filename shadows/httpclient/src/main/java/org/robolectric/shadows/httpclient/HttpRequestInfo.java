package org.robolectric.shadows.httpclient;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.RequestDirector;
import org.apache.http.protocol.HttpContext;

/**
 * @deprecated Apache HTTP client is deprecated in Android. Please migrate to an other solution
 */
@Deprecated
public class HttpRequestInfo {
  HttpRequest httpRequest;
  HttpHost httpHost;
  HttpContext httpContext;
  RequestDirector requestDirector;

  public HttpRequestInfo(
      HttpRequest httpRequest,
      HttpHost httpHost,
      HttpContext httpContext,
      RequestDirector requestDirector) {
    this.httpRequest = httpRequest;
    this.httpHost = httpHost;
    this.httpContext = httpContext;
    this.requestDirector = requestDirector;
  }

  public HttpRequest getHttpRequest() {
    return httpRequest;
  }

  public HttpHost getHttpHost() {
    return httpHost;
  }

  public HttpContext getHttpContext() {
    return httpContext;
  }

  public RequestDirector getRequestDirector() {
    return requestDirector;
  }
}
