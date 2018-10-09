package org.robolectric.shadows;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(AndroidHttpClient.class)
public class ShadowAndroidHttpClient {
  @RealObject private AndroidHttpClient client;

  private HttpClient httpClient = new DefaultHttpClient();

  @Implementation
  protected static AndroidHttpClient newInstance(String userAgent) {
    return ReflectionHelpers.callConstructor(AndroidHttpClient.class);
  }

  @Implementation
  protected static AndroidHttpClient newInstance(String userAgent, Context context) {
    return ReflectionHelpers.callConstructor(AndroidHttpClient.class);
  }

  @Implementation
  protected HttpParams getParams() {
    return httpClient.getParams();
  }

  @Implementation
  protected ClientConnectionManager getConnectionManager() {
    return httpClient.getConnectionManager();
  }

  @Implementation
  protected HttpResponse execute(HttpUriRequest httpUriRequest)
      throws IOException, ClientProtocolException {
    return httpClient.execute(httpUriRequest);
  }

  @Implementation
  protected HttpResponse execute(HttpUriRequest httpUriRequest, HttpContext httpContext)
      throws IOException, ClientProtocolException {
    return httpClient.execute(httpUriRequest, httpContext);
  }

  @Implementation
  protected HttpResponse execute(HttpHost httpHost, HttpRequest httpRequest)
      throws IOException, ClientProtocolException {
    return httpClient.execute(httpHost, httpRequest);
  }

  @Implementation
  protected HttpResponse execute(
      HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext)
      throws IOException, ClientProtocolException {
    return httpClient.execute(httpHost, httpRequest, httpContext);
  }

  @Implementation
  protected <T> T execute(
      HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler)
      throws IOException, ClientProtocolException {
    return httpClient.execute(httpUriRequest, responseHandler);
  }

  @Implementation
  protected <T> T execute(
      HttpUriRequest httpUriRequest,
      ResponseHandler<? extends T> responseHandler,
      HttpContext httpContext)
      throws IOException, ClientProtocolException {
    return httpClient.execute(httpUriRequest, responseHandler, httpContext);
  }

  @Implementation
  protected <T> T execute(
      HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<? extends T> responseHandler)
      throws IOException, ClientProtocolException {
    return httpClient.execute(httpHost, httpRequest, responseHandler);
  }

  @Implementation
  protected <T> T execute(
      HttpHost httpHost,
      HttpRequest httpRequest,
      ResponseHandler<? extends T> responseHandler,
      HttpContext httpContext)
      throws IOException, ClientProtocolException {
    return httpClient.execute(httpHost, httpRequest, responseHandler, httpContext);
  }
}