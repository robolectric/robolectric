package com.xtremelabs.robolectric.tester.org.apache.http;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class FakeHttpLayerTest {

    private FakeHttpLayer.RequestMatcherBuilder requestMatcherBuilder;

    @Before
    public void setUp() throws Exception {
        requestMatcherBuilder = new FakeHttpLayer.RequestMatcherBuilder();
    }

    @Test
    public void requestMatcherBuilder_shouldAddHost() throws Exception {
        requestMatcherBuilder.host("example.com");
        assertThat("example.com", equalTo(requestMatcherBuilder.getHostname()));
    }

    @Test
    public void requestMatcherBuilder_shouldAddMethod() throws Exception {
        requestMatcherBuilder.method("POST");
        assertThat("POST", equalTo(requestMatcherBuilder.getMethod()));
    }

    @Test
    public void requestMatcherBuilder_shouldAddPath() throws Exception {
        requestMatcherBuilder.path("foo/bar");
        assertThat("/foo/bar", equalTo(requestMatcherBuilder.getPath()));
    }

    @Test
    public void requestMatcherBuilder_shouldAddParams() throws Exception {
        requestMatcherBuilder.param("param1", "param one");
        assertThat("param one", equalTo(requestMatcherBuilder.getParam("param1")));
    }

    @Test
    public void requestMatcherBuilder_shouldAddHeaders() throws Exception {
        requestMatcherBuilder.header("header1", "header one");
        assertThat("header one", equalTo(requestMatcherBuilder.getHeader("header1")));
    }

    @Test
    public void matches__shouldMatchHeaders() throws Exception {
        requestMatcherBuilder.header("header1", "header one");
        HttpGet match = new HttpGet("example.com");
        HttpGet noMatch = new HttpGet("example.com");
        match.setHeader(new BasicHeader("header1", "header one"));
        noMatch.setHeader(new BasicHeader("header1", "header not a match"));

        assertFalse(requestMatcherBuilder.matches(new HttpGet("example.com")));
        assertFalse(requestMatcherBuilder.matches(noMatch));
        assertTrue(requestMatcherBuilder.matches(match));
    }

    @Test
    public void matches__shouldMatchPostBody() throws Exception {
        final String expectedText = "some post body text";

        requestMatcherBuilder.postBody(new FakeHttpLayer.RequestMatcherBuilder.PostBodyMatcher() {
            @Override
            public boolean matches(HttpEntity actualPostBody) throws IOException {
                return EntityUtils.toString(actualPostBody).equals(expectedText);
            }
        });

        HttpPut match = new HttpPut("example.com");
        match.setEntity(new StringEntity(expectedText));

        HttpPost noMatch = new HttpPost("example.com");
        noMatch.setEntity(new StringEntity("some text that does not match"));

        assertFalse(requestMatcherBuilder.matches(new HttpGet("example.com")));
        assertFalse(requestMatcherBuilder.matches(noMatch));
        assertTrue(requestMatcherBuilder.matches(match));
    }
}
