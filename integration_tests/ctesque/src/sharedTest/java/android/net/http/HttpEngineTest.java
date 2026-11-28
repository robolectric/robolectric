package android.net.http;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

@DoNotInstrument
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class HttpEngineTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private MockWebServer server;
  private HttpEngine engine;

  @Before
  public void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
  }

  @After
  public void tearDown() throws Exception {
    if (engine != null) {
      engine.shutdown();
    }
    server.close();
  }

  @Test
  public void openConnection_fetchesResponseBody() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    server.enqueue(new MockResponse.Builder().code(200).body("User-agent: *\nDisallow: /").build());

    engine = new HttpEngine.Builder(context).build();

    URL url = server.url("/robots.txt").url();
    HttpURLConnection con = (HttpURLConnection) engine.openConnection(url);

    try {
      assertThat(con.getURL()).isEqualTo(url);
      assertThat(con.getResponseCode()).isEqualTo(200);
      try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), UTF_8)) {
        assertThat(CharStreams.toString(reader)).contains("Disallow");
      }
    } finally {
      con.disconnect();
    }

    RecordedRequest recordedRequest = server.takeRequest();
    assertThat(recordedRequest.getTarget()).isEqualTo("/robots.txt");
  }

  @Test
  public void createUrlStreamHandlerFactory_returnsNonNull() {
    Context context = ApplicationProvider.getApplicationContext();
    engine = new HttpEngine.Builder(context).build();

    URLStreamHandlerFactory factory = engine.createUrlStreamHandlerFactory();
    assertThat(factory).isNotNull();
  }

  @Test
  public void getVersionString_isNonEmpty() {
    assertThat(HttpEngine.getVersionString()).isNotEmpty();
  }

  @Test
  public void builderOptions_buildEngine() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    HttpEngine.Builder builder = new HttpEngine.Builder(context);
    assertThat(builder.getDefaultUserAgent()).isNotEmpty();

    engine =
        builder
            .setUserAgent("RobolectricTest/1.0")
            .setStoragePath(temporaryFolder.newFolder().getAbsolutePath())
            .setEnableHttpCache(HttpEngine.Builder.HTTP_CACHE_DISK, 1024 * 1024)
            .setEnableQuic(true)
            .setEnableHttp2(true)
            .setEnableBrotli(true)
            .addQuicHint("example.com", 443, 443)
            .setEnablePublicKeyPinningBypassForLocalTrustAnchors(true)
            .setQuicOptions(new QuicOptions.Builder().build())
            .setDnsOptions(new DnsOptions.Builder().build())
            .setConnectionMigrationOptions(new ConnectionMigrationOptions.Builder().build())
            .build();

    assertThat(engine).isNotNull();
  }
}
