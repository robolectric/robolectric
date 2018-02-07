package org.robolectric.shadows.httpclient;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.util.TestRunnerWithManifest;

@RunWith(TestRunnerWithManifest.class)
public class FakeHttpTest {

  @Test
  public void httpRequestWasSent_ReturnsTrueIfRequestWasSent() throws IOException, HttpException {
    makeRequest("http://example.com");

    assertTrue(FakeHttp.httpRequestWasMade());
  }

  @Test
  public void httpRequestWasMade_ReturnsFalseIfNoRequestWasMade() {
    assertFalse(FakeHttp.httpRequestWasMade());
  }

  @Test
  public void httpRequestWasMade_returnsTrueIfRequestMatchingGivenRuleWasMade() throws IOException, HttpException {
    makeRequest("http://example.com");
    assertTrue(FakeHttp.httpRequestWasMade("http://example.com"));
  }

  @Test
  public void httpRequestWasMade_returnsFalseIfNoRequestMatchingGivenRuleWasMAde() throws IOException, HttpException {
    makeRequest("http://example.com");
    assertFalse(FakeHttp.httpRequestWasMade("http://example.org"));
  }

  private void makeRequest(String uri) throws HttpException, IOException {
    FakeHttp.addPendingHttpResponse(200, "a happy response body");

    ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy() {
      @Override
      public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
        return 0;
      }

    };
    DefaultRequestDirector requestDirector = new DefaultRequestDirector(null, null, null, connectionKeepAliveStrategy, null, null, null, null, null, null, null, null);

    requestDirector.execute(null, new HttpGet(uri), null);
  }
}