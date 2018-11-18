package org.robolectric.shadows.httpclient;

import static com.google.common.truth.Truth.assertThat;

import java.util.Map;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

public class ParamsParserTest {
  @Test
  public void parseParams_shouldParsePostEntitiesIntoParams() throws Exception {
    HttpPost post = new HttpPost("example.com");
    StringEntity entity = new StringEntity("param1=foobar");
    entity.setContentType("application/x-www-form-urlencoded");
    post.setEntity(entity);
    Map<String,String> params = ParamsParser.parseParams(post);
    assertThat(params.get("param1")).isEqualTo("foobar");
  }

  @Test
  public void parseParams_shouldParsePutEntitiesIntoParams() throws Exception {
    HttpPut put = new HttpPut("example.com");
    StringEntity entity = new StringEntity("param1=foobar");
    entity.setContentType("application/x-www-form-urlencoded");
    put.setEntity(entity);
    Map<String,String> params = ParamsParser.parseParams(put);
    assertThat(params.get("param1")).isEqualTo("foobar");
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
