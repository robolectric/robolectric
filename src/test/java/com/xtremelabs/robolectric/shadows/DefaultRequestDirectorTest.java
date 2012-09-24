package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.org.apache.http.FakeHttpLayer;
import com.xtremelabs.robolectric.tester.org.apache.http.RequestMatcher;
import com.xtremelabs.robolectric.tester.org.apache.http.TestHttpResponse;
import com.xtremelabs.robolectric.util.Strings;
import junit.framework.Assert;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class DefaultRequestDirectorTest {
    private DefaultRequestDirector requestDirector;
    private ConnectionKeepAliveStrategy connectionKeepAliveStrategy;

    @Before
    public void setUp_EnsureStaticStateIsReset() {
        FakeHttpLayer fakeHttpLayer = Robolectric.getFakeHttpLayer();
        assertFalse(fakeHttpLayer.hasPendingResponses());
        assertFalse(fakeHttpLayer.hasRequestInfos());
        assertFalse(fakeHttpLayer.hasResponseRules());
        assertNull(fakeHttpLayer.getDefaultResponse());

        connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy() {
            @Override public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
                return 0;
            }
        };
        requestDirector = new DefaultRequestDirector(null, null, null, connectionKeepAliveStrategy, null, null, null, null, null, null, null, null);
    }

    @After
    public void tearDown_EnsureStaticStateIsReset() throws Exception {
        Robolectric.addPendingHttpResponse(200, "a happy response body");
    }

    @Test
    public void shouldGetHttpResponseFromExecute() throws Exception {
        Robolectric.addPendingHttpResponse(new TestHttpResponse(200, "a happy response body"));
        HttpResponse response = requestDirector.execute(null, new HttpGet("http://example.com"), null);

        assertNotNull(response);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("a happy response body"));
    }

    @Test
    public void shouldPreferPendingResponses() throws Exception {
        Robolectric.addPendingHttpResponse(new TestHttpResponse(200, "a happy response body"));

        Robolectric.addHttpResponseRule(HttpGet.METHOD_NAME, "http://some.uri",
                new TestHttpResponse(200, "a cheery response body"));

        HttpResponse response = requestDirector.execute(null, new HttpGet("http://some.uri"), null);

        assertNotNull(response);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("a happy response body"));
    }

    @Test
    public void shouldReturnRequestsByRule() throws Exception {
        Robolectric.addHttpResponseRule(HttpGet.METHOD_NAME, "http://some.uri",
                new TestHttpResponse(200, "a cheery response body"));

        HttpResponse response = requestDirector.execute(null, new HttpGet("http://some.uri"), null);

        assertNotNull(response);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("a cheery response body"));
    }

    @Test
    public void shouldReturnRequestsByRule_MatchingMethod() throws Exception {
        Robolectric.setDefaultHttpResponse(404, "no such page");
        Robolectric.addHttpResponseRule(HttpPost.METHOD_NAME, "http://some.uri",
                new TestHttpResponse(200, "a cheery response body"));

        HttpResponse response = requestDirector.execute(null, new HttpGet("http://some.uri"), null);

        assertNotNull(response);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(404));
    }

    @Test
    public void shouldReturnRequestsByRule_AnyMethod() throws Exception {
        Robolectric.addHttpResponseRule("http://some.uri", new TestHttpResponse(200, "a cheery response body"));

        HttpResponse getResponse = requestDirector.execute(null, new HttpGet("http://some.uri"), null);
        assertNotNull(getResponse);
        assertThat(getResponse.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(getResponse.getEntity().getContent()), equalTo("a cheery response body"));

        HttpResponse postResponse = requestDirector.execute(null, new HttpPost("http://some.uri"), null);
        assertNotNull(postResponse);
        assertThat(postResponse.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(postResponse.getEntity().getContent()), equalTo("a cheery response body"));
    }

    @Test
    public void shouldReturnRequestsByRule_KeepsTrackOfOpenContentStreams() throws Exception {
        TestHttpResponse testHttpResponse = new TestHttpResponse(200, "a cheery response body");
        Robolectric.addHttpResponseRule("http://some.uri", testHttpResponse);

        assertThat(testHttpResponse.entityContentStreamsHaveBeenClosed(), equalTo(true));

        HttpResponse getResponse = requestDirector.execute(null, new HttpGet("http://some.uri"), null);
        InputStream getResponseStream = getResponse.getEntity().getContent();
        assertThat(Strings.fromStream(getResponseStream), equalTo("a cheery response body"));
        assertThat(testHttpResponse.entityContentStreamsHaveBeenClosed(), equalTo(false));

        HttpResponse postResponse = requestDirector.execute(null, new HttpPost("http://some.uri"), null);
        InputStream postResponseStream = postResponse.getEntity().getContent();
        assertThat(Strings.fromStream(postResponseStream), equalTo("a cheery response body"));
        assertThat(testHttpResponse.entityContentStreamsHaveBeenClosed(), equalTo(false));

        getResponseStream.close();
        assertThat(testHttpResponse.entityContentStreamsHaveBeenClosed(), equalTo(false));

        postResponseStream.close();
        assertThat(testHttpResponse.entityContentStreamsHaveBeenClosed(), equalTo(true));
    }

    @Test
    public void shouldReturnRequestsByRule_WithTextResponse() throws Exception {
        Robolectric.addHttpResponseRule("http://some.uri", "a cheery response body");

        HttpResponse response = requestDirector.execute(null, new HttpGet("http://some.uri"), null);

        assertNotNull(response);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("a cheery response body"));
    }

    @Test
    public void clearHttpResponseRules_shouldRemoveAllRules() throws Exception {
        Robolectric.addHttpResponseRule("http://some.uri", "a cheery response body");
        Robolectric.clearHttpResponseRules();
        Robolectric.addHttpResponseRule("http://some.uri", "a gloomy response body");

        HttpResponse response = requestDirector.execute(null, new HttpGet("http://some.uri"), null);

        assertNotNull(response);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("a gloomy response body"));
    }

    @Test
    public void clearPendingHttpResponses() throws Exception {
        Robolectric.addPendingHttpResponse(200, "earlier");
        Robolectric.clearPendingHttpResponses();
        Robolectric.addPendingHttpResponse(500, "later");

        HttpResponse response = requestDirector.execute(null, new HttpGet("http://some.uri"), null);

        assertNotNull(response);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(500));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("later"));
    }

    @Test
    public void shouldReturnRequestsByRule_WithCustomRequestMatcher() throws Exception {
        Robolectric.setDefaultHttpResponse(404, "no such page");

        Robolectric.addHttpResponseRule(new RequestMatcher() {
            @Override public boolean matches(HttpRequest request) {
                return request.getRequestLine().getUri().equals("http://matching.uri");
            }
        }, new TestHttpResponse(200, "a cheery response body"));

        HttpResponse response = requestDirector.execute(null, new HttpGet("http://matching.uri"), null);
        assertNotNull(response);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("a cheery response body"));

        response = requestDirector.execute(null, new HttpGet("http://non-matching.uri"), null);
        assertNotNull(response);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(404));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("no such page"));
    }

    @Test
    public void shouldGetHttpResponseFromExecuteSimpleApi() throws Exception {
        Robolectric.addPendingHttpResponse(200, "a happy response body");
        HttpResponse response = requestDirector.execute(null, new HttpGet("http://example.com"), null);

        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("a happy response body"));
    }

    @Test
    public void shouldHandleMultipleInvocations() throws Exception {
        Robolectric.addPendingHttpResponse(200, "a happy response body");
        Robolectric.addPendingHttpResponse(201, "another happy response body");

        HttpResponse response1 = requestDirector.execute(null, new HttpGet("http://example.com"), null);
        HttpResponse response2 = requestDirector.execute(null, new HttpGet("www.example.com"), null);

        assertThat(response1.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response1.getEntity().getContent()), equalTo("a happy response body"));

        assertThat(response2.getStatusLine().getStatusCode(), equalTo(201));
        assertThat(Strings.fromStream(response2.getEntity().getContent()), equalTo("another happy response body"));
    }

    @Test
    public void shouldHandleMultipleInvocationsOfExecute() throws Exception {
        Robolectric.addPendingHttpResponse(200, "a happy response body");
        Robolectric.addPendingHttpResponse(201, "another happy response body");

        requestDirector.execute(null, new HttpGet("http://example.com"), null);
        requestDirector.execute(null, new HttpGet("www.example.com"), null);

        HttpUriRequest request1 = (HttpUriRequest) Robolectric.getSentHttpRequest(0);
        assertThat(request1.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request1.getURI(), equalTo(URI.create("http://example.com")));

        HttpUriRequest request2 = (HttpUriRequest) Robolectric.getSentHttpRequest(1);
        assertThat(request2.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request2.getURI(), equalTo(URI.create("www.example.com")));
    }

    @Test
    public void shouldRejectUnexpectedCallsToExecute() throws Exception {
        try {
            requestDirector.execute(null, new HttpGet("http://example.com"), null);
            fail();
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), equalTo("Unexpected call to execute, no pending responses are available. See Robolectric.addPendingResponse(). Request was: GET http://example.com"));
        }
    }

    @Test
    public void shouldRecordExtendedRequestData() throws Exception {
        Robolectric.addPendingHttpResponse(200, "a happy response body");
        HttpGet httpGet = new HttpGet("http://example.com");
        requestDirector.execute(null, httpGet, null);

        assertSame(Robolectric.getSentHttpRequestInfo(0).getHttpRequest(), httpGet);
        ConnectionKeepAliveStrategy strategy = shadowOf((DefaultRequestDirector) Robolectric.getSentHttpRequestInfo(0).getRequestDirector()).getConnectionKeepAliveStrategy();
        assertSame(strategy, connectionKeepAliveStrategy);
    }

    @Test
    public void shouldSupportBasicResponseHandlerHandleResponse() throws Exception {
        Robolectric.addPendingHttpResponse(200, "OK", new BasicHeader("Content-Type", "text/plain"));

        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(new HttpGet("http://www.nowhere.org"));

        assertThat(((HttpUriRequest) Robolectric.getSentHttpRequest(0)).getURI(),
                equalTo(URI.create("http://www.nowhere.org")));

        Assert.assertNotNull(response);
        String responseStr = new BasicResponseHandler().handleResponse(response);
        Assert.assertEquals("OK", responseStr);
    }

    @Test
    public void shouldFindLastRequestMade() throws Exception {
        Robolectric.addPendingHttpResponse(200, "a happy response body");
        Robolectric.addPendingHttpResponse(200, "a happy response body");
        Robolectric.addPendingHttpResponse(200, "a happy response body");

        DefaultHttpClient client = new DefaultHttpClient();
        client.execute(new HttpGet("http://www.first.org"));
        client.execute(new HttpGet("http://www.second.org"));
        client.execute(new HttpGet("http://www.third.org"));

        assertThat(((HttpUriRequest) Robolectric.getLatestSentHttpRequest()).getURI(),
                equalTo(URI.create("http://www.third.org")));
    }


    @Test
    public void shouldSupportConnectionTimeoutWithExceptions() throws Exception {
        Robolectric.setDefaultHttpResponse(new TestHttpResponse() {
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
        Robolectric.setDefaultHttpResponse(new TestHttpResponse() {
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
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        DefaultHttpClient client = new DefaultHttpClient();
        client.execute(new HttpGet("http://www.this-host-should-not-exist-123456790.org:999"));
    }

    @Test
    public void shouldSupportRealHttpRequestsAddingRequestInfo() throws Exception {
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        DefaultHttpClient client = new DefaultHttpClient();

        // it's really bad to depend on an external server in order to get a test pass,
        // but this test is about making sure that we can intercept calls to external servers
        // so, I think that in this specific case, it's appropriate...
        client.execute(new HttpGet("http://google.com"));

        assertNotNull(Robolectric.getFakeHttpLayer().getLastSentHttpRequestInfo());
        assertNotNull(Robolectric.getFakeHttpLayer().getLastHttpResponse());
    }

    @Test
    public void shouldReturnResponseFromHttpResponseGenerator() throws Exception {
        Robolectric.addPendingHttpResponse(new HttpResponseGenerator() {
            @Override
            public HttpResponse getResponse(HttpRequest request) {
                return new TestHttpResponse(200, "a happy response body");
            }
        });
        HttpResponse response = requestDirector.execute(null, new HttpGet("http://example.com"), null);

        assertNotNull(response);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("a happy response body"));
    }

}
