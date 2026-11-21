package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;

import android.content.Context;
import android.net.http.BidirectionalStream;
import android.net.http.ConnectionMigrationOptions;
import android.net.http.DnsOptions;
import android.net.http.HttpEngine;
import android.net.http.Proxy;
import android.net.http.ProxyOptions;
import android.net.http.QuicOptions;
import android.net.http.UrlRequest;
import android.net.http.UrlResponseInfo;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.chromium.net.CronetEngine;
import org.chromium.net.impl.JavaCronetEngineBuilderImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = UPSIDE_DOWN_CAKE)
public class ShadowHttpEngineTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private Context context;
  private MockWebServer server;
  private HttpEngine engine;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
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
  public void constructor_nullContextThrows() {
    assertThrows(NullPointerException.class, () -> new HttpEngine.Builder((Context) null));
  }

  @Test
  public void openConnection_fetchesResponseBody() throws Exception {
    server.enqueue(new MockResponse.Builder().code(200).body("hello world").build());

    engine = new HttpEngine.Builder(context).build();

    URL url = server.url("/hello").url();
    HttpURLConnection connection = (HttpURLConnection) engine.openConnection(url);
    try {
      assertThat(connection.getURL()).isEqualTo(url);
      assertThat(connection.getResponseCode()).isEqualTo(200);
      try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), UTF_8)) {
        assertThat(CharStreams.toString(reader)).isEqualTo("hello world");
      }
    } finally {
      connection.disconnect();
    }

    RecordedRequest recordedRequest = server.takeRequest();
    assertThat(recordedRequest.getTarget()).isEqualTo("/hello");
  }

  @Test
  public void builderOptions_delegatesToCronetAndBuilds() throws Exception {
    HttpEngine.Builder builder = new HttpEngine.Builder(context);
    assertThat(builder.getDefaultUserAgent()).isNotEmpty();

    engine =
        builder
            .setUserAgent("RobolectricAgent/1.0")
            .setStoragePath(temporaryFolder.newFolder().getAbsolutePath())
            .setEnableQuic(true)
            .setEnableHttp2(true)
            .setEnableBrotli(true)
            .setEnableHttpCache(HttpEngine.Builder.HTTP_CACHE_DISK, 1024 * 1024)
            .addQuicHint("example.com", 443, 443)
            .addPublicKeyPins(
                "example.com",
                Collections.singleton(new byte[32]),
                true,
                Instant.now().plusSeconds(3600))
            .setEnablePublicKeyPinningBypassForLocalTrustAnchors(true)
            .setQuicOptions(new QuicOptions.Builder().build())
            .setDnsOptions(new DnsOptions.Builder().build())
            .setConnectionMigrationOptions(new ConnectionMigrationOptions.Builder().build())
            .build();

    assertThat(engine).isNotNull();
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE, maxSdk = UPSIDE_DOWN_CAKE)
  public void builderOptions_sdk34Specific() {
    HttpEngine.Builder builder = new HttpEngine.Builder(context);
    ReflectionHelpers.callInstanceMethod(
        builder, "setThreadPriority", ClassParameter.from(int.class, 0));
    ReflectionHelpers.callInstanceMethod(
        builder, "enableNetworkQualityEstimator", ClassParameter.from(boolean.class, true));
    engine = builder.build();

    assertThat(engine).isNotNull();
  }

  @Test
  @Config(minSdk = CINNAMON_BUN)
  public void setProxyOptions_validatesNonNullAndBuilds() {
    HttpEngine.Builder builder = new HttpEngine.Builder(context);
    engine = ProxyOptionsHelper.verifySetProxyOptionsAndBuild(builder);
    assertThat(engine).isNotNull();
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void preload_firstCallSucceeds_secondThrows() {
    HttpEngine.preload();
    assertThrows(IllegalStateException.class, HttpEngine::preload);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void preload_resetsBetweenTests() {
    HttpEngine.preload();
  }

  @Test
  public void setCronetEngineBuilder_usesCustomBuilder() {
    AtomicReference<String> capturedUserAgent = new AtomicReference<>();
    AtomicBoolean builtCalled = new AtomicBoolean();
    CronetEngine.Builder customBuilder =
        new CronetEngine.Builder(new JavaCronetEngineBuilderImpl(context)) {
          @Override
          public String getDefaultUserAgent() {
            return "CustomUserAgent/1.0";
          }

          @Override
          public CronetEngine.Builder setUserAgent(String userAgent) {
            capturedUserAgent.set(userAgent);
            return super.setUserAgent(userAgent);
          }

          @Override
          public CronetEngine build() {
            builtCalled.set(true);
            return super.build();
          }
        };

    ShadowHttpEngine.setCronetEngineBuilder(customBuilder);

    HttpEngine.Builder builder = new HttpEngine.Builder(context);
    assertThat(builder.getDefaultUserAgent()).isEqualTo("CustomUserAgent/1.0");
    engine = builder.setUserAgent("OverrideAgent/2.0").build();
    assertThat(engine).isNotNull();
    assertThat(capturedUserAgent.get()).isEqualTo("OverrideAgent/2.0");
    assertThat(builtCalled.get()).isTrue();
  }

  @Test
  public void setCronetEngineBuilder_resetsBetweenTests() {
    HttpEngine.Builder builder = new HttpEngine.Builder(context);
    assertThat(builder.getDefaultUserAgent()).isNotEqualTo("CustomUserAgent/1.0");
  }

  @Test
  public void setCronetEngineBuilder_rejectsInvalidType() {
    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () -> ShadowHttpEngine.setCronetEngineBuilder("not a CronetEngine.Builder"));
    assertThat(error).hasMessageThat().contains("CronetEngine$Builder");
  }

  @Test
  public void createUrlStreamHandlerFactory_bindToNetwork_andStreamBuilders() {
    engine = new HttpEngine.Builder(context).build();

    URLStreamHandlerFactory factory = engine.createUrlStreamHandlerFactory();
    assertThat(factory).isNotNull();
    assertThat(factory.createURLStreamHandler("http")).isNull();

    engine.bindToNetwork(null);

    Executor directExecutor = Runnable::run;
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            engine.newUrlRequestBuilder(
                "https://example.com",
                directExecutor,
                new UrlRequest.Callback() {
                  @Override
                  public void onRedirectReceived(
                      UrlRequest request, UrlResponseInfo info, String newLocationUrl) {}

                  @Override
                  public void onResponseStarted(UrlRequest request, UrlResponseInfo info) {}

                  @Override
                  public void onReadCompleted(
                      UrlRequest request, UrlResponseInfo info, ByteBuffer byteBuffer) {}

                  @Override
                  public void onSucceeded(UrlRequest request, UrlResponseInfo info) {}

                  @Override
                  public void onFailed(
                      UrlRequest request, UrlResponseInfo info, android.net.http.HttpException e) {}

                  @Override
                  public void onCanceled(
                      @NonNull UrlRequest request, @NonNull UrlResponseInfo info) {}
                }));

    assertThrows(
        UnsupportedOperationException.class,
        () ->
            engine.newBidirectionalStreamBuilder(
                "https://example.com",
                directExecutor,
                new BidirectionalStream.Callback() {
                  @Override
                  public void onStreamReady(BidirectionalStream stream) {}

                  @Override
                  public void onResponseHeadersReceived(
                      BidirectionalStream stream, UrlResponseInfo info) {}

                  @Override
                  public void onReadCompleted(
                      BidirectionalStream stream,
                      UrlResponseInfo info,
                      ByteBuffer buffer,
                      boolean endOfStream) {}

                  @Override
                  public void onWriteCompleted(
                      BidirectionalStream stream,
                      UrlResponseInfo info,
                      ByteBuffer buffer,
                      boolean endOfStream) {}

                  @Override
                  public void onResponseTrailersReceived(
                      BidirectionalStream stream,
                      UrlResponseInfo info,
                      android.net.http.HeaderBlock trailers) {}

                  @Override
                  public void onSucceeded(BidirectionalStream stream, UrlResponseInfo info) {}

                  @Override
                  public void onFailed(
                      BidirectionalStream stream,
                      UrlResponseInfo info,
                      android.net.http.HttpException error) {}

                  @Override
                  public void onCanceled(BidirectionalStream stream, UrlResponseInfo info) {}
                }));
  }
}

class ProxyOptionsHelper {
  static HttpEngine verifySetProxyOptionsAndBuild(HttpEngine.Builder builder) {
    assertThrows(NullPointerException.class, () -> builder.setProxyOptions(null));

    Proxy proxy =
        Proxy.createHttpProxy(
            Proxy.SCHEME_HTTP,
            "localhost",
            8080,
            Runnable::run,
            new Proxy.HttpConnectCallback() {
              @Override
              public void onBeforeRequest(Request request) {}

              @Override
              public int onResponseReceived(
                  List<android.util.Pair<String, String>> responseHeaders, int statusCode) {
                return RESPONSE_ACTION_PROCEED;
              }
            });
    ProxyOptions proxyOptions =
        ProxyOptions.fromProxyList(
            Collections.singletonList(proxy),
            ProxyOptions.ALL_PROXIES_FAILED_BEHAVIOR_ALLOW_DIRECT);

    return builder.setProxyOptions(proxyOptions).build();
  }
}
