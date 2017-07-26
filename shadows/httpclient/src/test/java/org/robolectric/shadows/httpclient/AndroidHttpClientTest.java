package org.robolectric.shadows.httpclient;

import static org.assertj.core.api.Assertions.assertThat;

import android.net.http.AndroidHttpClient;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.Strings;
import org.robolectric.util.TestRunnerWithManifest;

@RunWith(TestRunnerWithManifest.class)
public class AndroidHttpClientTest {

  @Test
  public void testNewInstance() throws Exception {
    AndroidHttpClient client = AndroidHttpClient.newInstance("foo");
    assertThat(client).isNotNull();
  }

  @Test
  public void testNewInstanceWithContext() throws Exception {
    AndroidHttpClient client = AndroidHttpClient.newInstance("foo", RuntimeEnvironment.application);
    assertThat(client).isNotNull();
  }

  @Test
  public void testExecute() throws IOException {
    AndroidHttpClient client = AndroidHttpClient.newInstance("foo");
    FakeHttp.addPendingHttpResponse(200, "foo");
    HttpResponse resp = client.execute(new HttpGet("/foo"));
    assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(Strings.fromStream(resp.getEntity().getContent())).isEqualTo("foo");
  }
}