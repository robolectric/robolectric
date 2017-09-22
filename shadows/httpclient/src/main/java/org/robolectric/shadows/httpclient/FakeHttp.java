package org.robolectric.shadows.httpclient;

import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

/**
 * Collection of static methods used interact with HTTP requests / responses.
 */
public class FakeHttp {
  private static FakeHttpLayer instance = new FakeHttpLayer();

  /**
   * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
   *
   * @param statusCode   the status code of the response
   * @param responseBody the body of the response
   * @param headers      optional headers for the request
   */
  public static void addPendingHttpResponse(int statusCode, String responseBody, Header... headers) {
    getFakeHttpLayer().addPendingHttpResponse(statusCode, responseBody, headers);
  }

  /**
   * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
   *
   * @param statusCode   the status code of the response
   * @param responseBody the body of the response
   * @param contentType  the contentType of the response
   * @deprecated use {@link #addPendingHttpResponse(int, String, org.apache.http.Header...)} instead
   */
  @Deprecated
  public static void addPendingHttpResponseWithContentType(int statusCode, String responseBody, Header contentType) {
    getFakeHttpLayer().addPendingHttpResponse(statusCode, responseBody, contentType);
  }

  /**
   * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
   *
   * @param httpResponse the response
   */
  public static void addPendingHttpResponse(HttpResponse httpResponse) {
    getFakeHttpLayer().addPendingHttpResponse(httpResponse);
  }

  /**
   * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
   *
   * @param httpResponseGenerator an HttpResponseGenerator that will provide responses
   */
  public static void addPendingHttpResponse(HttpResponseGenerator httpResponseGenerator) {
    getFakeHttpLayer().addPendingHttpResponse(httpResponseGenerator);
  }

  /**
   * Accessor to obtain HTTP requests made during the current test in the order in which they were made.
   *
   * @param index index of the request to retrieve.
   * @return the requested request.
   */
  public static HttpRequest getSentHttpRequest(int index) {
    return getFakeHttpLayer().getSentHttpRequestInfo(index).getHttpRequest();
  }

  public static HttpRequest getLatestSentHttpRequest() {
    return ShadowDefaultRequestDirector.getLatestSentHttpRequest();
  }

  /**
   * Accessor to find out if HTTP requests were made during the current test.
   *
   * @return whether a request was made.
   */
  public static boolean httpRequestWasMade() {
    return getFakeHttpLayer().hasRequestInfos();
  }

  public static boolean httpRequestWasMade(String uri) {
    return getFakeHttpLayer().hasRequestMatchingRule(
        new FakeHttpLayer.UriRequestMatcher(uri));
  }

  /**
   * Accessor to obtain metadata for an HTTP request made during the current test in the order in which they were made.
   *
   * @param index index of the request to retrieve.
   * @return the requested request metadata.
   */
  public static HttpRequestInfo getSentHttpRequestInfo(int index) {
    return getFakeHttpLayer().getSentHttpRequestInfo(index);
  }

  /**
   * Accessor to obtain HTTP requests made during the current test in the order in which they were made.
   *
   * @return the requested request or null if there are none.
   */
  public static HttpRequest getNextSentHttpRequest() {
    HttpRequestInfo httpRequestInfo = getFakeHttpLayer().getNextSentHttpRequestInfo();
    return httpRequestInfo == null ? null : httpRequestInfo.getHttpRequest();
  }

  /**
   * Accessor to obtain metadata for an HTTP request made during the current test in the order in which they were made.
   *
   * @return the requested request metadata or null if there are none.
   */
  public static HttpRequestInfo getNextSentHttpRequestInfo() {
    return getFakeHttpLayer().getNextSentHttpRequestInfo();
  }

  /**
   * Adds an HTTP response rule. The response will be returned when the rule is matched.
   *
   * @param method   method to match.
   * @param uri      uri to match.
   * @param response response to return when a match is found.
   */
  public static void addHttpResponseRule(String method, String uri, HttpResponse response) {
    getFakeHttpLayer().addHttpResponseRule(method, uri, response);
  }

  /**
   * Adds an HTTP response rule with a default method of GET. The response will be returned when the rule is matched.
   *
   * @param uri      uri to match.
   * @param response response to return when a match is found.
   */
  public static void addHttpResponseRule(String uri, HttpResponse response) {
    getFakeHttpLayer().addHttpResponseRule(uri, response);
  }

  /**
   * Adds an HTTP response rule. The response will be returned when the rule is matched.
   *
   * @param uri      uri to match.
   * @param response response to return when a match is found.
   */
  public static void addHttpResponseRule(String uri, String response) {
    getFakeHttpLayer().addHttpResponseRule(uri, response);
  }

  /**
   * Adds an HTTP response rule. The response will be returned when the rule is matched.
   *
   * @param requestMatcher custom {@code RequestMatcher}.
   * @param response       response to return when a match is found.
   */
  public static void addHttpResponseRule(RequestMatcher requestMatcher, HttpResponse response) {
    getFakeHttpLayer().addHttpResponseRule(requestMatcher, response);
  }

  /**
   * Adds an HTTP response rule. For each time the rule is matched, responses will be shifted
   * off the list and returned. When all responses have been given and the rule is matched again,
   * an exception will be thrown.
   *
   * @param requestMatcher custom {@code RequestMatcher}.
   * @param responses      responses to return in order when a match is found.
   */
  public static void addHttpResponseRule(RequestMatcher requestMatcher, List<? extends HttpResponse> responses) {
    getFakeHttpLayer().addHttpResponseRule(requestMatcher, responses);
  }

  public static FakeHttpLayer getFakeHttpLayer() {
    return instance;
  }

  public static void setDefaultHttpResponse(int statusCode, String responseBody) {
    getFakeHttpLayer().setDefaultHttpResponse(statusCode, responseBody);
  }

  public static void setDefaultHttpResponse(HttpResponse defaultHttpResponse) {
    getFakeHttpLayer().setDefaultHttpResponse(defaultHttpResponse);
  }

  public static void clearHttpResponseRules() {
    getFakeHttpLayer().clearHttpResponseRules();
  }

  public static void clearPendingHttpResponses() {
    getFakeHttpLayer().clearPendingHttpResponses();
  }

  public static void reset() {
    instance = new FakeHttpLayer();
  }
}
