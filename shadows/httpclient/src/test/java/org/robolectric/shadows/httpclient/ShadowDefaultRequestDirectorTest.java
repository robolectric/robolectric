package org.robolectric.shadows.httpclient;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.robolectric.shadows.httpclient.Shadows.shadowOf;

import com.google.common.io.CharStreams;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.util.TestRunnerWithManifest;

@RunWith(TestRunnerWithManifest.class)
public class ShadowDefaultRequestDirectorTest {

  private DefaultRequestDirector requestDirector;
  private ConnectionKeepAliveStrategy connectionKeepAliveStrategy;

  @Before
  public void setUp_EnsureStaticStateIsReset() {
    FakeHttpLayer fakeHttpLayer = FakeHttp.getFakeHttpLayer();
    assertFalse(fakeHttpLayer.hasPendingResponses());
    assertFalse(fakeHttpLayer.hasRequestInfos());
    assertFalse(fakeHttpLayer.hasResponseRules());

    connectionKeepAliveStrategy =
        new ConnectionKeepAliveStrategy() {
          @Override
          public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
            return 0;
          }
        };
    requestDirector =
        new DefaultRequestDirector(
            null,
            null,
            null,
            connectionKeepAliveStrategy,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
  }

  @After
  public void tearDown_EnsureStaticStateIsReset() throws Exception {
    FakeHttp.addPendingHttpResponse(200, "a happy response body");
  }

  @Test
  public void shouldGetHttpResponseFromExecute() throws Exception {
    FakeHttp.addPendingHttpResponse(new TestHttpResponse(200, "a happy response body"));
    HttpResponse response = requestDirector.execute(null, new HttpGet("http://example.com"), null);

    assertNotNull(response);
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(getStringContent(response)).isEqualTo("a happy response body");
  }

  @Test
  public void shouldPreferPendingResponses() throws Exception {
    FakeHttp.addPendingHttpResponse(new TestHttpResponse(200, "a happy response body"));

    FakeHttp.addHttpResponseRule(HttpGet.METHOD_NAME, "http://some.uri",
        new TestHttpResponse(200, "a cheery response body"));

    HttpResponse response = requestDirector.execute(null, new HttpGet("http://some.uri"), null);

    assertNotNull(response);
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(getStringContent(response)).isEqualTo("a happy response body");
  }

  @Test
  public void shouldReturnRequestsByRule() throws Exception {
    FakeHttp.addHttpResponseRule(HttpGet.METHOD_NAME, "http://some.uri",
        new TestHttpResponse(200, "a cheery response body"));

    HttpResponse response = requestDirector.execute(null, new HttpGet("http://some.uri"), null);

    assertNotNull(response);
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(getStringContent(response)).isEqualTo("a cheery response body");
  }

  @Test
  public void shouldReturnRequestsByRule_MatchingMethod() throws Exception {
    FakeHttp.setDefaultHttpResponse(404, "no such page");
    FakeHttp.addHttpResponseRule(HttpPost.METHOD_NAME, "http://some.uri",
        new TestHttpResponse(200, "a cheery response body"));

    HttpResponse response = requestDirector.execute(null, new HttpGet("http://some.uri"), null);

    assertNotNull(response);
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(404);
  }

  @Test
  public void shouldReturnRequestsByRule_AnyMethod() throws Exception {
    FakeHttp.addHttpResponseRule(
        "http://some.uri", new TestHttpResponse(200, "a cheery response body"));

    HttpResponse getResponse = requestDirector.execute(null, new HttpGet("http://some.uri"), null);
    assertNotNull(getResponse);
    assertThat(getResponse.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(getStringContent(getResponse)).isEqualTo("a cheery response body");

    HttpResponse postResponse =
        requestDirector.execute(null, new HttpPost("http://some.uri"), null);
    assertNotNull(postResponse);
    assertThat(postResponse.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(getStringContent(postResponse)).isEqualTo("a cheery response body");
  }

  @Test
  public void shouldReturnRequestsByRule_KeepsTrackOfOpenContentStreams() throws Exception {
    TestHttpResponse testHttpResponse = new TestHttpResponse(200, "a cheery response body");
    FakeHttp.addHttpResponseRule("http://some.uri", testHttpResponse);

    assertThat(testHttpResponse.entityContentStreamsHaveBeenClosed()).isTrue();

    HttpResponse getResponse = requestDirector.execute(null, new HttpGet("http://some.uri"), null);
    InputStream getResponseStream = getResponse.getEntity().getContent();
    assertThat(CharStreams.toString(new InputStreamReader(getResponseStream, UTF_8)))
        .isEqualTo("a cheery response body");
    assertThat(testHttpResponse.entityContentStreamsHaveBeenClosed()).isFalse();

    HttpResponse postResponse =
        requestDirector.execute(null, new HttpPost("http://some.uri"), null);
    InputStream postResponseStream = postResponse.getEntity().getContent();
    assertThat(CharStreams.toString(new InputStreamReader(postResponseStream, UTF_8)))
        .isEqualTo("a cheery response body");
    assertThat(testHttpResponse.entityContentStreamsHaveBeenClosed()).isFalse();

    getResponseStream.close();
    assertThat(testHttpResponse.entityContentStreamsHaveBeenClosed()).isFalse();

    postResponseStream.close();
    assertThat(testHttpResponse.entityContentStreamsHaveBeenClosed()).isTrue();
  }

  @Test
  public void shouldReturnRequestsByRule_WithTextResponse() throws Exception {
    FakeHttp.addHttpResponseRule("http://some.uri", "a cheery response body");

    HttpResponse response = requestDirector.execute(null, new HttpGet("http://some.uri"), null);

    assertNotNull(response);
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(getStringContent(response)).isEqualTo("a cheery response body");
  }

  @Test
  public void clearHttpResponseRules_shouldRemoveAllRules() throws Exception {
    FakeHttp.addHttpResponseRule("http://some.uri", "a cheery response body");
    FakeHttp.clearHttpResponseRules();
    FakeHttp.addHttpResponseRule("http://some.uri", "a gloomy response body");

    HttpResponse response = requestDirector.execute(null, new HttpGet("http://some.uri"), null);

    assertNotNull(response);
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(getStringContent(response)).isEqualTo("a gloomy response body");
  }

  @Test
  public void clearPendingHttpResponses() throws Exception {
    FakeHttp.addPendingHttpResponse(200, "earlier");
    FakeHttp.clearPendingHttpResponses();
    FakeHttp.addPendingHttpResponse(500, "later");

    HttpResponse response = requestDirector.execute(null, new HttpGet("http://some.uri"), null);

    assertNotNull(response);
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(500);
    assertThat(getStringContent(response)).isEqualTo("later");
  }

  @Test
  public void shouldReturnRequestsByRule_WithCustomRequestMatcher() throws Exception {
    FakeHttp.setDefaultHttpResponse(404, "no such page");

    FakeHttp.addHttpResponseRule(new RequestMatcher() {
      @Override
      public boolean matches(HttpRequest request) {
        return request.getRequestLine().getUri().equals("http://matching.uri");
      }
    }, new TestHttpResponse(200, "a cheery response body"));

    HttpResponse response = requestDirector.execute(null, new HttpGet("http://matching.uri"), null);
    assertNotNull(response);
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(getStringContent(response)).isEqualTo("a cheery response body");

    response = requestDirector.execute(null, new HttpGet("http://non-matching.uri"), null);
    assertNotNull(response);
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(404);
    assertThat(getStringContent(response)).isEqualTo("no such page");
  }

  @Test
  public void shouldGetHttpResponseFromExecuteSimpleApi() throws Exception {
    FakeHttp.addPendingHttpResponse(200, "a happy response body");
    HttpResponse response = requestDirector.execute(null, new HttpGet("http://example.com"), null);

    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(getStringContent(response)).isEqualTo("a happy response body");
  }

  @Test
  public void shouldHandleMultipleInvocations() throws Exception {
    FakeHttp.addPendingHttpResponse(200, "a happy response body");
    FakeHttp.addPendingHttpResponse(201, "another happy response body");

    HttpResponse response1 = requestDirector.execute(null, new HttpGet("http://example.com"), null);
    HttpResponse response2 = requestDirector.execute(null, new HttpGet("www.example.com"), null);

    assertThat(response1.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(getStringContent(response1)).isEqualTo("a happy response body");

    assertThat(response2.getStatusLine().getStatusCode()).isEqualTo(201);
    assertThat(getStringContent(response2)).isEqualTo("another happy response body");
  }

  @Test
  public void shouldHandleMultipleInvocationsOfExecute() throws Exception {
    FakeHttp.addPendingHttpResponse(200, "a happy response body");
    FakeHttp.addPendingHttpResponse(201, "another happy response body");

    requestDirector.execute(null, new HttpGet("http://example.com"), null);
    requestDirector.execute(null, new HttpGet("www.example.com"), null);

    HttpUriRequest request1 = (HttpUriRequest) FakeHttp.getSentHttpRequest(0);
    assertThat(request1.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
    assertThat(request1.getURI()).isEqualTo(URI.create("http://example.com"));

    HttpUriRequest request2 = (HttpUriRequest) FakeHttp.getSentHttpRequest(1);
    assertThat(request2.getMethod()).isEqualTo(HttpGet.METHOD_NAME);
    assertThat(request2.getURI()).isEqualTo(URI.create("www.example.com"));
  }

  @Test
  public void shouldRejectUnexpectedCallsToExecute() throws Exception {
    try {
      requestDirector.execute(null, new HttpGet("http://example.com"), null);
      fail();
    } catch (RuntimeException expected) {
      assertThat(expected.getMessage())
          .isEqualTo(
              "Unexpected call to execute, no pending responses are available. "
                  + "See Robolectric.addPendingResponse(). Request was: GET http://example.com");
    }
  }

  @Test
  public void shouldRecordExtendedRequestData() throws Exception {
    FakeHttp.addPendingHttpResponse(200, "a happy response body");
    HttpGet httpGet = new HttpGet("http://example.com");
    requestDirector.execute(null, httpGet, null);

    assertSame(FakeHttp.getSentHttpRequestInfo(0).getHttpRequest(), httpGet);
    ConnectionKeepAliveStrategy strategy =
        shadowOf((DefaultRequestDirector) FakeHttp.getSentHttpRequestInfo(0).getRequestDirector())
            .getConnectionKeepAliveStrategy();
    assertSame(strategy, connectionKeepAliveStrategy);
  }

  @Test
  public void getNextSentHttpRequestInfo_shouldRemoveHttpRequestInfos() throws Exception {
    FakeHttp.addPendingHttpResponse(200, "a happy response body");
    HttpGet httpGet = new HttpGet("http://example.com");
    requestDirector.execute(null, httpGet, null);

    assertSame(FakeHttp.getNextSentHttpRequestInfo().getHttpRequest(), httpGet);
    assertNull(FakeHttp.getNextSentHttpRequestInfo());
  }

  @Test
  public void getNextSentHttpRequest_shouldRemoveHttpRequests() throws Exception {
    FakeHttp.addPendingHttpResponse(200, "a happy response body");
    HttpGet httpGet = new HttpGet("http://example.com");
    requestDirector.execute(null, httpGet, null);

    assertSame(FakeHttp.getNextSentHttpRequest(), httpGet);
    assertNull(FakeHttp.getNextSentHttpRequest());
  }

  @Test
  public void shouldSupportBasicResponseHandlerHandleResponse() throws Exception {
    FakeHttp.addPendingHttpResponse(200, "OK", new BasicHeader("Content-Type", "text/plain"));

    DefaultHttpClient client = new DefaultHttpClient();
    HttpResponse response = client.execute(new HttpGet("http://www.nowhere.org"));

    assertThat(((HttpUriRequest) FakeHttp.getSentHttpRequest(0)).getURI())
        .isEqualTo(URI.create("http://www.nowhere.org"));

    Assert.assertNotNull(response);
    String responseStr = new BasicResponseHandler().handleResponse(response);
    Assert.assertEquals("OK", responseStr);
  }

  @Test
  public void shouldFindLastRequestMade() throws Exception {
    FakeHttp.addPendingHttpResponse(200, "a happy response body");
    FakeHttp.addPendingHttpResponse(200, "a happy response body");
    FakeHttp.addPendingHttpResponse(200, "a happy response body");

    DefaultHttpClient client = new DefaultHttpClient();
    client.execute(new HttpGet("http://www.first.org"));
    client.execute(new HttpGet("http://www.second.org"));
    client.execute(new HttpGet("http://www.third.org"));

    assertThat(((HttpUriRequest) FakeHttp.getLatestSentHttpRequest()).getURI())
        .isEqualTo(URI.create("http://www.third.org"));
  }


  @Test
  public void shouldSupportConnectionTimeoutWithExceptions() throws Exception {
    FakeHttp.setDefaultHttpResponse(new TestHttpResponse() {
      @Override
      public HttpParams getParams() {
        HttpParams httpParams = super.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, -1);
        return httpParams;
      }
    });

    DefaultHttpClient client = new DefaultHttpClient();
    try {
      client.execute(new HttpGet("http://www.nowhere.org"));
    } catch (ConnectTimeoutException x) {
      return;
    }

    fail("Exception should have been thrown");
  }

  @Test
  public void shouldSupportSocketTimeoutWithExceptions() throws Exception {
    FakeHttp.setDefaultHttpResponse(new TestHttpResponse() {
      @Override
      public HttpParams getParams() {
        HttpParams httpParams = super.getParams();
        HttpConnectionParams.setSoTimeout(httpParams, -1);
        return httpParams;
      }
    });

    DefaultHttpClient client = new DefaultHttpClient();
    try {
      client.execute(new HttpGet("http://www.nowhere.org"));
    } catch (ConnectTimeoutException x) {
      return;
    }

    fail("Exception should have been thrown");
  }

  @Test(expected = IOException.class)
  public void shouldSupportRealHttpRequests() throws Exception {
    FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);
    DefaultHttpClient client = new DefaultHttpClient();
    client.execute(new HttpGet("http://www.this-host-should-not-exist-123456790.org:999"));
  }

  @Test
  public void shouldSupportRealHttpRequestsAddingRequestInfo() throws Exception {
    FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);
    DefaultHttpClient client = new DefaultHttpClient();

    // it's really bad to depend on an external server in order to get a test pass,
    // but this test is about making sure that we can intercept calls to external servers
    // so, I think that in this specific case, it's appropriate...
    client.execute(new HttpGet("http://google.com"));

    assertNotNull(FakeHttp.getFakeHttpLayer().getLastSentHttpRequestInfo());
    assertNotNull(FakeHttp.getFakeHttpLayer().getLastHttpResponse());
  }

  @Test
  public void realHttpRequestsShouldMakeContentDataAvailable() throws Exception {
    FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);
    FakeHttp.getFakeHttpLayer().interceptResponseContent(true);
    DefaultHttpClient client = new DefaultHttpClient();

    client.execute(new HttpGet("http://google.com"));

    byte[] cachedContent = FakeHttp.getFakeHttpLayer().getHttpResposeContentList().get(0);
    assertThat(cachedContent.length).isNotEqualTo(0);

    InputStream content =
        FakeHttp.getFakeHttpLayer().getLastHttpResponse().getEntity().getContent();
    BufferedReader contentReader = new BufferedReader(new InputStreamReader(content, UTF_8));
    String firstLineOfContent = contentReader.readLine();
    assertThat(firstLineOfContent).contains("Google");

    BufferedReader cacheReader =
        new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedContent), UTF_8));
    String firstLineOfCachedContent = cacheReader.readLine();
    assertThat(firstLineOfCachedContent).isEqualTo(firstLineOfContent);
  }

  @Test
  public void shouldReturnResponseFromHttpResponseGenerator() throws Exception {
    FakeHttp.addPendingHttpResponse(new HttpResponseGenerator() {
      @Override
      public HttpResponse getResponse(HttpRequest request) {
        return new TestHttpResponse(200, "a happy response body");
      }
    });
    HttpResponse response = requestDirector.execute(null, new HttpGet("http://example.com"), null);

    assertNotNull(response);
    assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(getStringContent(response)).isEqualTo("a happy response body");
  }

  private static String getStringContent(HttpResponse response) throws IOException {
    return CharStreams.toString(new InputStreamReader(response.getEntity().getContent(), UTF_8));
  }

}