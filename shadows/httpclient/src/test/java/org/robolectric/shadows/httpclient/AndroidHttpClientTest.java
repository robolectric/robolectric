package org.robolectric.shadows.httpclient;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.net.http.AndroidHttpClient;
import android.os.Build;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(maxSdk = Build.VERSION_CODES.LOLLIPOP_MR1)
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
    assertThat(CharStreams.toString(new InputStreamReader(resp.getEntity().getContent())))
        .isEqualTo("foo");
  }
}