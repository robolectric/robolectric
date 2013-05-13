package org.robolectric.tester.org.apache.http;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class TestHttpResponseTest {

  @Test
  public void shouldSupportGetFirstHeader() throws Exception {
    HttpResponse resp =
        new TestHttpResponse(304, "REDIRECTED",
            new BasicHeader("Location", "http://bar.com"));

    assertThat(resp.getFirstHeader("None")).isNull();
    assertThat(new TestHttpResponse(200, "OK").getFirstHeader("Foo")).isNull();

    for (String l : new String[] { "location", "Location" }) {
      assertThat(resp.getFirstHeader(l).getValue()).isEqualTo("http://bar.com");
    }
  }

  @Test
  public void shouldSupportGetLastHeader() throws Exception {
    HttpResponse resp =
        new TestHttpResponse(304, "REDIRECTED",
            new BasicHeader("Location", "http://bar.com"),
            new BasicHeader("Location", "http://zombo.com"));

    assertThat(resp.getLastHeader("None")).isNull();

    for (String l : new String[] { "location", "Location" }) {
      assertThat(resp.getLastHeader(l).getValue()).isEqualTo("http://zombo.com");
    }
  }

  @Test
  public void shouldSupportContainsHeader() throws Exception {
    HttpResponse resp =
        new TestHttpResponse(304, "ZOMBO",
            new BasicHeader("X-Zombo-Com", "Welcome"));

    assertThat(resp.containsHeader("X-Zombo-Com")).isTrue();
    assertThat(resp.containsHeader("Location")).isFalse();
  }

  @Test
  public void shouldSupportHeaderIterator() throws Exception {
    HttpResponse resp =
        new TestHttpResponse(304, "REDIRECTED",
            new BasicHeader("Location", "http://bar.com"),
            new BasicHeader("Location", "http://zombo.com"));

    HeaderIterator it = resp.headerIterator();

    assertThat(it.hasNext()).isTrue();
    assertThat(it.nextHeader().getValue()).isEqualTo("http://bar.com");
    assertThat(it.nextHeader().getValue()).isEqualTo("http://zombo.com");
    assertThat(it.hasNext()).isFalse();
  }

  @Test
  public void shouldSupportHeaderIteratorWithArg() throws Exception {
    HttpResponse resp =
        new TestHttpResponse(304, "REDIRECTED",
            new BasicHeader("Location", "http://bar.com"),
            new BasicHeader("X-Zombo-Com", "http://zombo.com"),
            new BasicHeader("Location", "http://foo.com"));

    HeaderIterator it = resp.headerIterator("Location");

    assertThat(it.hasNext()).isTrue();
    assertThat(it.nextHeader().getValue()).isEqualTo("http://bar.com");
    assertThat(it.hasNext()).isTrue();
    assertThat(it.nextHeader().getValue()).isEqualTo("http://foo.com");
    assertThat(it.hasNext()).isFalse();
  }


  @Test
  public void shouldSupportGetHeadersWithArg() throws Exception {
    HttpResponse resp =
        new TestHttpResponse(304, "REDIRECTED",
            new BasicHeader("Location", "http://bar.com"),
            new BasicHeader("X-Zombo-Com", "http://zombo.com"),
            new BasicHeader("Location", "http://foo.com"));


    Header[] headers = resp.getHeaders("Location");
    assertThat(headers.length).isEqualTo(2);
    assertThat(headers[0].getValue()).isEqualTo("http://bar.com");
    assertThat(headers[1].getValue()).isEqualTo("http://foo.com");
  }

  @Test
  public void canAddNewBasicHeader() {
    TestHttpResponse response = new TestHttpResponse(200, "abc");
    assertThat(response.getAllHeaders().length).isEqualTo(0);
    response.addHeader(new BasicHeader("foo", "bar"));
    assertThat(response.getAllHeaders().length).isEqualTo(1);
    assertThat(response.getHeaders("foo")[0].getValue()).isEqualTo("bar");
  }

  @Test
  public void canOverrideExistingHeaderValue() {
    TestHttpResponse response = new TestHttpResponse(200, "abc", new BasicHeader("foo", "bar"));
    response.setHeader(new BasicHeader("foo", "bletch"));
    assertThat(response.getAllHeaders().length).isEqualTo(1);
    assertThat(response.getHeaders("foo")[0].getValue()).isEqualTo("bletch");
  }

  @Test
  public void onlyOverridesFirstHeaderValue() {
    TestHttpResponse response = new TestHttpResponse(200, "abc", new BasicHeader("foo", "bar"), new BasicHeader("foo", "baz"));
    response.setHeader(new BasicHeader("foo", "bletch"));
    assertThat(response.getAllHeaders().length).isEqualTo(2);
    assertThat(response.getHeaders("foo")[0].getValue()).isEqualTo("bletch");
    assertThat(response.getHeaders("foo")[1].getValue()).isEqualTo("baz");
  }

}
