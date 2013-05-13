package org.robolectric.tester.org.apache.http;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ParamsParserTest {
  @Test
  public void parseParams_shouldParsePostEntitiesIntoParams() throws Exception {
    HttpPost post = new HttpPost("example.com");
    TestHttpResponse response = new TestHttpResponse();
    response.setResponseBody("param1=foobar");

    post.setEntity(response.getEntity());
    Map<String,String> params = ParamsParser.parseParams(post);

    assertThat("foobar").isEqualTo(params.get("param1"));
  }

  @Test
  public void parseParams_shouldNotParseNonPostEntitiesIntoParams() throws Exception {
    HttpGet httpGet = new HttpGet("example.com");
    TestHttpResponse response = new TestHttpResponse();
    response.setResponseBody("param1=foobar");
    assertThat(ParamsParser.parseParams(httpGet).size()).isEqualTo(0);
  }
}
