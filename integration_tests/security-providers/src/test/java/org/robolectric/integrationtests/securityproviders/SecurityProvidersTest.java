package org.robolectric.integrationtests.securityproviders;

import java.net.URL;
import java.security.Provider;
import java.security.Security;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.conscrypt.OpenSSLProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Integration tests for {@link java.security.Provider} related features. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.ALL_SDKS)
public class SecurityProvidersTest {
  // Conscrypt (which calls into native libraries) can only be loaded once as per JNI spec:
  // https://docs.oracle.com/javase/1.5.0/docs/guide/jni/spec/invocation.html#library_version
  // If we try to load it into multiple class loaders, UnsatisfiedLinkErrors will be thrown.
  private static final Provider CONSCRYPT_PROVIDER = new OpenSSLProvider();

  @Test
  public void jsseProvider_isFunctioning() throws Exception {
    URL url = new URL("https://www.google.com");
    url.openConnection().getInputStream();
  }

  @Test
  public void conscryptProvider_isSupported() throws Exception {
    if (!"Conscrypt".equals(Security.getProviders()[0].getName())) {
      Security.insertProviderAt(CONSCRYPT_PROVIDER, 1);
    }
    OkHttpClient client = new OkHttpClient.Builder().build();
    Request request = new Request.Builder().url("https://www.google.com").build();
    client.newCall(request).execute();
  }
}
