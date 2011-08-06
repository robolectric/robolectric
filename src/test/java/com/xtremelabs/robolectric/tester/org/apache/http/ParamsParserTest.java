package com.xtremelabs.robolectric.tester.org.apache.http;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ParamsParserTest {
    @Test
    public void parseParams_shouldParsePostEntitiesIntoParams() throws Exception {
        HttpPost post = new HttpPost("example.com");
        TestHttpResponse response = new TestHttpResponse();
        response.setResponseBody("param1=foobar");

        post.setEntity(response.getEntity());
        Map<String,String> params = ParamsParser.parseParams(post);

        assertThat("foobar", equalTo(params.get("param1")));
    }

    @Test
    public void parseParams_shouldNotParseNonPostEntitiesIntoParams() throws Exception {
        HttpGet httpGet = new HttpGet("example.com");
        TestHttpResponse response = new TestHttpResponse();
        response.setResponseBody("param1=foobar");
        assertThat(ParamsParser.parseParams(httpGet).size(), equalTo(0));
    }
}
