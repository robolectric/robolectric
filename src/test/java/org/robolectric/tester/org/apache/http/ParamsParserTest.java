package org.robolectric.tester.org.apache.http;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
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
    StringEntity entity = new StringEntity("param1=foobar");
    entity.setContentType("application/x-www-form-urlencoded");
    post.setEntity(entity);
    Map<String,String> params = ParamsParser.parseParams(post);
    assertThat("foobar").isEqualTo(params.get("param1"));
  }

  @Test
  public void parseParams_shouldParsePutEntitiesIntoParams() throws Exception {
    HttpPut put = new HttpPut("example.com");
    StringEntity entity = new StringEntity("param1=foobar");
    entity.setContentType("application/x-www-form-urlencoded");
    put.setEntity(entity);
    Map<String,String> params = ParamsParser.parseParams(put);
    assertThat("foobar").isEqualTo(params.get("param1"));
  }

  @Test
  public void parseParams_shouldDoNothingForEmptyEntity() throws Exception {
    HttpPut put = new HttpPut("example.com");
    Map<String,String> params = ParamsParser.parseParams(put);
    assertThat(params).isEmpty();
  }

  @Test
  public void parseParams_shouldParseParamsFromGetRequests() throws Exception {
    HttpGet httpGet = new HttpGet("http://example.com/path?foo=bar");
    Map<String, String> parsed = ParamsParser.parseParams(httpGet);
    assertThat(parsed.size()).isEqualTo(1);
    assertThat(parsed.get("foo")).isEqualTo("bar");
  }

  @Test
  public void parseParams_returnsNullForUnsupportedOperations() throws Exception {
    HttpDelete httpDelete = new HttpDelete("http://example.com/deleteme");
    assertThat(ParamsParser.parseParams(httpDelete)).isEmpty();
  }
}
