package org.robolectric.shadows;

import android.net.http.AndroidHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Strings;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class AndroidHttpClientTest {

  @Test
  public void testNewInstance() throws Exception {
    AndroidHttpClient client = AndroidHttpClient.newInstance("foo");
    assertThat(client).isNotNull();
  }

  @Test
  public void testNewInstanceWithContext() throws Exception {
    AndroidHttpClient client = AndroidHttpClient.newInstance("foo", Robolectric.application);
    assertThat(client).isNotNull();
  }

  @Test
  public void testExecute() throws IOException {
    AndroidHttpClient client = AndroidHttpClient.newInstance("foo");
    Robolectric.addPendingHttpResponse(200, "foo");
    HttpResponse resp = client.execute(new HttpGet("/foo"));
    assertThat(resp.getStatusLine().getStatusCode()).isEqualTo(200);
    assertThat(Strings.fromStream(resp.getEntity().getContent())).isEqualTo("foo");
  }
}
