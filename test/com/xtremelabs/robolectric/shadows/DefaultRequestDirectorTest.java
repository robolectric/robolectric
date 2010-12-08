package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.Strings;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class DefaultRequestDirectorTest {
    private DefaultRequestDirector requestDirector;

    @Before
    public void setUp_EnsureStaticStateIsReset() {
        assertTrue(ShadowDefaultRequestDirector.httpResponses.isEmpty());
        assertTrue(ShadowDefaultRequestDirector.httpRequests.isEmpty());
        requestDirector = new DefaultRequestDirector(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @After
    public void tearDown_EnsureStaticStateIsReset() throws Exception {
        Robolectric.addPendingResponse(200, "a happy response body");
    }

    @Test
    public void shouldGetHttpResponseFromExecute() throws Exception {
        Robolectric.addPendingResponse(new TestHttpResponse(200, "a happy response body"));
        HttpResponse response = requestDirector.execute(null, new HttpGet("http://example.com"), null);

        assertNotNull(response);
        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("a happy response body"));
    }

    @Test
    public void shouldGetHttpResponseFromExecuteSimpleApi() throws Exception {
        Robolectric.addPendingResponse(200, "a happy response body");
        HttpResponse response = requestDirector.execute(null, new HttpGet("http://example.com"), null);

        assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response.getEntity().getContent()), equalTo("a happy response body"));
    }

    @Test
    public void shouldHandleMultipleInvocations() throws Exception {

        Robolectric.addPendingResponse(200, "a happy response body");
        Robolectric.addPendingResponse(201, "another happy response body");

        HttpResponse response1 = requestDirector.execute(null, new HttpGet("http://example.com"), null);
        HttpResponse response2 = requestDirector.execute(null, new HttpGet("www.example.com"), null);

        assertThat(response1.getStatusLine().getStatusCode(), equalTo(200));
        assertThat(Strings.fromStream(response1.getEntity().getContent()), equalTo("a happy response body"));

        assertThat(response2.getStatusLine().getStatusCode(), equalTo(201));
        assertThat(Strings.fromStream(response2.getEntity().getContent()), equalTo("another happy response body"));
    }

    @Test
    public void shouldHandleMultipleInvocationsOfExecute() throws Exception {
        Robolectric.addPendingResponse(200, "a happy response body");
        Robolectric.addPendingResponse(201, "another happy response body");

        requestDirector.execute(null, new HttpGet("http://example.com"), null);
        requestDirector.execute(null, new HttpGet("www.example.com"), null);

        HttpUriRequest request1 = (HttpUriRequest) Robolectric.getRequest(0);
        assertThat(request1.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request1.getURI(), equalTo(URI.create("http://example.com")));

        HttpUriRequest request2 = (HttpUriRequest) Robolectric.getRequest(1);
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
}
