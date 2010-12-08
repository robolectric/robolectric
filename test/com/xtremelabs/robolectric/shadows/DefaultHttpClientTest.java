package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.Strings;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class DefaultHttpClientTest {
    private DefaultHttpClient client;

    @Before
    public void setUp_EnsureStaticStateIsReset() {
        assertTrue(ShadowAbstractHttpClient.httpResponses.isEmpty());
        assertTrue(ShadowAbstractHttpClient.httpRequests.isEmpty());
        client = new DefaultHttpClient();
    }

    @After
    public void tearDown_EnsureStaticStateIsReset() throws Exception {
        ShadowAbstractHttpClient.addPendingResponse(200, "a happy response body");
    }

    @Test
    public void shouldGetHttpResponseFromExecute() throws Exception {
        ShadowAbstractHttpClient.addPendingResponse(new TestHttpResponse(200, "a happy response body"));
        HttpResponse response = client.execute(new HttpGet("http://example.com"));

        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("a happy response body"));
    }

    @Test
    public void shouldGetHttpResponseFromExecuteSimpleApi() throws Exception {
        ShadowAbstractHttpClient.addPendingResponse(200, "a happy response body");
        HttpResponse response = client.execute(new HttpGet("http://example.com"));

        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("a happy response body"));
    }
    
    @Test
    public void shouldHandleMultipleInvocations() throws Exception {

        ShadowAbstractHttpClient.addPendingResponse(200, "a happy response body");
        ShadowAbstractHttpClient.addPendingResponse(201, "another happy response body");
        
        HttpResponse response1 = client.execute(new HttpGet("http://example.com"));
        HttpResponse response2 = client.execute(new HttpGet("www.example.com"));
        
        assertThat(response1.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response1.getEntity().getContent()), equalTo("a happy response body"));

        assertThat(response2.getStatusLine().getStatusCode(), equalTo(201));
        assertThat(Strings.fromStream(response2.getEntity().getContent()), equalTo("another happy response body"));
    }

    @Test
    public void shouldHandleMultipleInvocationsOfExecute() throws Exception {
        ShadowAbstractHttpClient.addPendingResponse(200, "a happy response body");
        ShadowAbstractHttpClient.addPendingResponse(201, "another happy response body");

        client.execute(new HttpGet("http://example.com"));
        client.execute(new HttpGet("www.example.com"));

        HttpUriRequest request1 = (HttpUriRequest) ShadowAbstractHttpClient.getRequest(0);
        assertThat(request1.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request1.getURI(), equalTo(URI.create("http://example.com")));

        HttpUriRequest request2 = (HttpUriRequest) ShadowAbstractHttpClient.getRequest(1);
        assertThat(request2.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request2.getURI(), equalTo(URI.create("www.example.com")));
    }

    @Test
    public void shouldRejectUnexpectedCallsToExecute() throws Exception {
        try {
            client.execute(new HttpGet("http://example.com"));
            fail();
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), equalTo("Unexpected call to execute, no pending responses are available."));
        }
    }
}
