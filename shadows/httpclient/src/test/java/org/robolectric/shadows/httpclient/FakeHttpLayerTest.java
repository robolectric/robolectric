package org.robolectric.shadows.httpclient;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link FakeHttpLayer} */
@RunWith(AndroidJUnit4.class)
public class FakeHttpLayerTest {
  private FakeHttpLayer.RequestMatcherBuilder requestMatcherBuilder;

  @Before
  public void setUp() {
    requestMatcherBuilder = new FakeHttpLayer.RequestMatcherBuilder();
  }

  @Test
  public void requestMatcherBuilder_shouldAddHost() {
    requestMatcherBuilder.host("example.com");
    assertThat(requestMatcherBuilder.getHostname()).isEqualTo("example.com");
  }

  @Test
  public void requestMatcherBuilder_shouldAddMethod() {
    requestMatcherBuilder.method("POST");
    assertThat(requestMatcherBuilder.getMethod()).isEqualTo("POST");
  }

  @Test
  public void requestMatcherBuilder_shouldAddPath() {
    requestMatcherBuilder.path("foo/bar");
    assertThat(requestMatcherBuilder.getPath()).isEqualTo("/foo/bar");
  }

  @Test
  public void requestMatcherBuilder_shouldAddParams() {
    requestMatcherBuilder.param("param1", "param one");
    assertThat(requestMatcherBuilder.getParam("param1")).isEqualTo("param one");
  }

  @Test
  public void requestMatcherBuilder_shouldAddHeaders() {
    requestMatcherBuilder.header("header1", "header one");
    assertThat(requestMatcherBuilder.getHeader("header1")).isEqualTo("header one");
  }

  @Test
  public void matches_shouldMatchHeaders() {
    requestMatcherBuilder.header("header1", "header one");
    HttpGet match = new HttpGet("example.com");
    HttpGet noMatch = new HttpGet("example.com");
    match.setHeader(new BasicHeader("header1", "header one"));
    noMatch.setHeader(new BasicHeader("header1", "header not a match"));

    assertThat(requestMatcherBuilder.matches(new HttpGet("example.com"))).isFalse();
    assertThat(requestMatcherBuilder.matches(noMatch)).isFalse();
    assertThat(requestMatcherBuilder.matches(match)).isTrue();
  }

  @Test
  public void matches_shouldMatchPostBody() throws Exception {
    final String expectedText = "some post body text";

    requestMatcherBuilder.postBody(
        actualPostBody -> EntityUtils.toString(actualPostBody).equals(expectedText));

    HttpPut match = new HttpPut("example.com");
    match.setEntity(new StringEntity(expectedText));

    HttpPost noMatch = new HttpPost("example.com");
    noMatch.setEntity(new StringEntity("some text that does not match"));

    assertThat(requestMatcherBuilder.matches(new HttpGet("example.com"))).isFalse();
    assertThat(requestMatcherBuilder.matches(noMatch)).isFalse();
    assertThat(requestMatcherBuilder.matches(match)).isTrue();
  }
}
