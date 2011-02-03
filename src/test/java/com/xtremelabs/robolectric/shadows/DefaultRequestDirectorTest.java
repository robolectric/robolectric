package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.org.apache.http.FakeHttpLayer;
import com.xtremelabs.robolectric.tester.org.apache.http.RequestMatcher;
import com.xtremelabs.robolectric.tester.org.apache.http.TestHttpResponse;
import com.xtremelabs.robolectric.util.Strings;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.protocol.HttpContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    public void shouldReturnRequestsByRule_WithTextResponse() throws Exception {
        Robolectric.addHttpResponseRule("http://some.uri", "a cheery response body");

        HttpResponse response = requestDirector.execute(null, new HttpGet("http://some.uri"), null);

        assertNotNull(response);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("a cheery response body"));
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
            assertThat(expected.getMessage(), equalTo("Unexpected call to execute, no pending responses are available. See Robolectric.addPendingResponse()."));
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
}
