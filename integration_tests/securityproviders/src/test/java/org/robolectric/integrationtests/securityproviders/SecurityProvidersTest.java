package org.robolectric.integrationtests.securityproviders;

import static com.google.common.truth.Truth.assertThat;

import java.security.Provider;
import java.security.Security;
import javax.net.ssl.HttpsURLConnection;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.tls.HandshakeCertificates;
import okhttp3.tls.HeldCertificate;
import org.conscrypt.OpenSSLProvider;
import org.junit.After;
import org.junit.Before;
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

  private final HeldCertificate certificate =
      new HeldCertificate.Builder().addSubjectAlternativeName("localhost").build();
  private final MockWebServer server = new MockWebServer();

  @Before
  public void setUp() throws Exception {
    server.useHttps(
        new HandshakeCertificates.Builder()
            .heldCertificate(certificate)
            .build()
            .sslSocketFactory());
    server.enqueue(new MockResponse.Builder().build());
    server.start();
  }

  @After
  public void tearDown() throws Exception {
    server.close();
  }

  @Test
  public void jsseProvider_isFunctioning() throws Exception {
    HttpsURLConnection connection = (HttpsURLConnection) server.url("/").url().openConnection();
    connection.setSSLSocketFactory(clientCertificates().sslSocketFactory());

    assertThat(connection.getResponseCode()).isEqualTo(200);
    connection.disconnect();
  }

  @Test
  public void conscryptProvider_isSupported() throws Exception {
    if (!"Conscrypt".equals(Security.getProviders()[0].getName())) {
      Security.insertProviderAt(CONSCRYPT_PROVIDER, 1);
    }
    HandshakeCertificates clientCertificates = clientCertificates();
    assertThat(clientCertificates.sslContext().getProvider().getName()).isEqualTo("Conscrypt");
    OkHttpClient client =
        new OkHttpClient.Builder()
            .sslSocketFactory(
                clientCertificates.sslSocketFactory(), clientCertificates.trustManager())
            .build();
    Request request = new Request.Builder().url(server.url("/")).build();

    try (Response response = client.newCall(request).execute()) {
      assertThat(response.code()).isEqualTo(200);
    }
  }

  /** Trusts only the certificate of the local server. */
  private HandshakeCertificates clientCertificates() {
    return new HandshakeCertificates.Builder()
        .addTrustedCertificate(certificate.certificate())
        .build();
  }
}
