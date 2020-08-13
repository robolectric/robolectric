package org.robolectric.integrationtests.securityproviders;

import java.net.URL;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Integration tests for {@link java.security.Provider} related features. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.ALL_SDKS)
public class SecurityProvidersTest {

  @Test
  public void conscryptProvider_isFunctioning() throws Exception {
    URL url = new URL("https://www.google.com");
    url.openConnection().getInputStream();

    OkHttpClient client = new OkHttpClient.Builder().build();
    Request request = new Request.Builder().url(url).build();
    client.newCall(request).execute();
  }
}
