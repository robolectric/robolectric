package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.net.Network;
import android.net.http.BidirectionalStream;
import android.net.http.ConnectionMigrationOptions;
import android.net.http.DnsOptions;
import android.net.http.HttpEngine;
import android.net.http.QuicOptions;
import android.net.http.UrlRequest;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandlerFactory;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import org.chromium.net.CronetEngine;
import org.chromium.net.impl.JavaCronetProvider;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * Provides a pure-Java {@link HttpEngine} implementation in Robolectric using Cronet's fallback
 * provider ({@code org.chromium.net:cronet-fallback}).
 *
 * <p>{@code org.chromium.net:cronet-fallback} is an optional runtime dependency and is not bundled
 * transitively by Robolectric. To use {@link HttpEngine} in Robolectric tests on Android 14+ (API
 * 34+), add Google's Maven repository and {@code cronet-fallback} to your test runtime classpath:
 *
 * <pre>{@code
 * repositories {
 *   google()
 *   mavenCentral()
 * }
 *
 * dependencies {
 *   testRuntimeOnly("org.chromium.net:cronet-fallback:119.6045.31")
 * }
 * }</pre>
 *
 * <p>Supported APIs:
 *
 * <ul>
 *   <li>{@link HttpEngine.Builder} configuration and {@link HttpEngine.Builder#build()} (with QUIC,
 *       DNS, connection migration, and API 37+ proxy options accepted as no-ops beyond null
 *       validation where required).
 *   <li>{@link HttpEngine#openConnection(URL)}, {@link HttpEngine#createUrlStreamHandlerFactory()},
 *       {@link HttpEngine#bindToNetwork(Network)}, and {@link HttpEngine#shutdown()}.
 *   <li>{@link HttpEngine#preload()} on API 36+, which updates preload state and preserves the
 *       single-preload {@link IllegalStateException} check without loading native Cronet libraries.
 * </ul>
 *
 * <p>{@link HttpEngine#newUrlRequestBuilder(String, Executor, UrlRequest.Callback)} and {@link
 * HttpEngine#newBidirectionalStreamBuilder(String, Executor, BidirectionalStream.Callback)} are not
 * yet implemented and throw {@link UnsupportedOperationException}.
 *
 * <p>Tests can alternatively supply a custom {@link org.chromium.net.CronetEngine.Builder} via
 * {@link #setCronetEngineBuilder(Object)}. Constructing {@link HttpEngine.Builder} without {@code
 * cronet-fallback} or a custom builder throws an {@link IllegalStateException} with setup
 * instructions.
 */
@SuppressWarnings("NewApi")
@Implements(value = HttpEngine.class, minSdk = UPSIDE_DOWN_CAKE, isInAndroidSdk = false)
public class ShadowHttpEngine {
  @Nullable private static Object cronetEngineBuilder;

  /**
   * Configures a custom {@code org.chromium.net.CronetEngine.Builder} to back subsequent {@link
   * HttpEngine.Builder} instances created via {@link HttpEngine.Builder#Builder(Context)}.
   *
   * <p>Pass {@code null} to clear any override and restore the default {@code
   * org.chromium.net.impl.JavaCronetProvider} fallback. Passing a non-null value requires {@code
   * org.chromium.net.CronetEngine$Builder} (from {@code org.chromium.net:cronet-fallback} or {@code
   * org.chromium.net:cronet-api}) to be present on the runtime classpath and throws {@link
   * IllegalStateException} if missing, or {@link IllegalArgumentException} if the object is not a
   * {@code CronetEngine.Builder}.
   */
  public static void setCronetEngineBuilder(@Nullable Object cronetEngineBuilder) {
    if (cronetEngineBuilder != null) {
      try {
        Class.forName(
            "org.chromium.net.CronetEngine$Builder",
            false,
            ShadowHttpEngine.class.getClassLoader());
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException(
            "ShadowHttpEngine.setCronetEngineBuilder requires"
                + " org.chromium.net.CronetEngine$Builder (from"
                + " org.chromium.net:cronet-fallback or org.chromium.net:cronet-api) on the test"
                + " runtime classpath.",
            e);
      }
      CronetBridge.requireCronetEngineBuilder(cronetEngineBuilder);
    }
    ShadowHttpEngine.cronetEngineBuilder = cronetEngineBuilder;
  }

  /**
   * Marks {@link HttpEngine} as preloaded without loading native Cronet libraries or classes, while
   * preserving the framework's single-preload check.
   */
  @Implementation(minSdk = BAKLAVA)
  protected static void preload() {
    HttpEngineReflector reflector = reflector(HttpEngineReflector.class);
    if (reflector.getPreloaded()) {
      throw new IllegalStateException("HttpEngine already preloaded");
    }
    reflector.setPreloaded(true);
  }

  @Resetter
  public static void reset() {
    cronetEngineBuilder = null;
    if (RuntimeEnvironment.getApiLevel() >= BAKLAVA) {
      reflector(HttpEngineReflector.class).setPreloaded(false);
    }
  }

  @ForType(HttpEngine.class)
  interface HttpEngineReflector {
    @Static
    @Accessor("sPreloaded")
    boolean getPreloaded();

    @Static
    @Accessor("sPreloaded")
    void setPreloaded(boolean preloaded);
  }

  interface BuilderDelegate {
    String getDefaultUserAgent();

    void setUserAgent(String userAgent);

    void setStoragePath(String value);

    void enableQuic(boolean value);

    void enableHttp2(boolean value);

    void enableBrotli(boolean value);

    void enableHttpCache(int cacheMode, long maxSize);

    void addQuicHint(String host, int port, int alternatePort);

    void addPublicKeyPins(
        String hostName,
        Set<byte[]> pinsSha256,
        boolean includeSubdomains,
        Instant expirationInstant);

    void enablePublicKeyPinningBypassForLocalTrustAnchors(boolean value);

    void setThreadPriority(int priority);

    void enableNetworkQualityEstimator(boolean value);

    HttpEngine build();
  }

  /**
   * Backing implementation for {@link HttpEngine.Builder} across API 34 and later.
   *
   * <p>Delegates builder calls to {@code org.chromium.net:cronet-fallback} or a custom {@code
   * CronetEngine.Builder} configured via {@link ShadowHttpEngine#setCronetEngineBuilder(Object)}.
   */
  @Implements(value = HttpEngine.Builder.class, minSdk = UPSIDE_DOWN_CAKE, isInAndroidSdk = false)
  public static class ShadowHttpEngineBuilder {
    @RealObject private HttpEngine.Builder realBuilder;
    private BuilderDelegate delegate;

    /** Returns {@code null} so SDK 34–36 does not load the native Cronet builder class. */
    @Implementation(maxSdk = BAKLAVA)
    protected static @ClassName("android.net.http.IHttpEngineBuilder") Object createBuilderDelegate(
        Context context) {
      return null;
    }

    /** No-op in SDK 34–36; initialization happens in {@link #__constructor__(Context)}. */
    @Implementation(maxSdk = BAKLAVA)
    protected void __constructor__(
        @ClassName("android.net.http.IHttpEngineBuilder") Object builderDelegate) {}

    /**
     * Initializes the builder with either the configured {@code CronetEngine.Builder} or a {@code
     * JavaCronetProvider} fallback builder, throwing an {@link IllegalStateException} with Gradle
     * setup instructions if {@code org.chromium.net:cronet-fallback} is absent from the runtime
     * classpath.
     */
    @Implementation
    protected void __constructor__(Context context) {
      Objects.requireNonNull(context, "Context must not be null");
      if (cronetEngineBuilder != null) {
        this.delegate = CronetBridge.forCustomBuilder(cronetEngineBuilder);
      } else {
        try {
          Class.forName(
              "org.chromium.net.impl.JavaCronetProvider",
              false,
              ShadowHttpEngine.class.getClassLoader());
        } catch (ClassNotFoundException e) {
          throw new IllegalStateException(
              "android.net.http.HttpEngine requires org.chromium.net:cronet-fallback on the test"
                  + " runtime classpath. Add"
                  + " testRuntimeOnly(\"org.chromium.net:cronet-fallback:119.6045.31\") (from"
                  + " Google's Maven repository, google()) to your Gradle dependencies, or configure"
                  + " a custom CronetEngine.Builder via"
                  + " ShadowHttpEngine.setCronetEngineBuilder(...).",
              e);
        }
        this.delegate = CronetBridge.forJavaCronetProvider(context);
      }
    }

    @Implementation
    protected String getDefaultUserAgent() {
      return delegate.getDefaultUserAgent();
    }

    @Implementation
    protected HttpEngine.Builder setUserAgent(String userAgent) {
      delegate.setUserAgent(userAgent);
      return realBuilder;
    }

    @Implementation
    protected HttpEngine.Builder setStoragePath(String value) {
      delegate.setStoragePath(value);
      return realBuilder;
    }

    @Implementation
    protected HttpEngine.Builder setEnableQuic(boolean value) {
      delegate.enableQuic(value);
      return realBuilder;
    }

    @Implementation
    protected HttpEngine.Builder setEnableHttp2(boolean value) {
      delegate.enableHttp2(value);
      return realBuilder;
    }

    @Implementation
    protected HttpEngine.Builder setEnableBrotli(boolean value) {
      delegate.enableBrotli(value);
      return realBuilder;
    }

    @Implementation
    protected HttpEngine.Builder setEnableHttpCache(int cacheMode, long maxSize) {
      delegate.enableHttpCache(cacheMode, maxSize);
      return realBuilder;
    }

    @Implementation
    protected HttpEngine.Builder addQuicHint(String host, int port, int alternatePort) {
      delegate.addQuicHint(host, port, alternatePort);
      return realBuilder;
    }

    @Implementation
    protected HttpEngine.Builder addPublicKeyPins(
        String hostName,
        Set<byte[]> pinsSha256,
        boolean includeSubdomains,
        Instant expirationInstant) {
      delegate.addPublicKeyPins(hostName, pinsSha256, includeSubdomains, expirationInstant);
      return realBuilder;
    }

    @Implementation
    protected HttpEngine.Builder setEnablePublicKeyPinningBypassForLocalTrustAnchors(
        boolean value) {
      delegate.enablePublicKeyPinningBypassForLocalTrustAnchors(value);
      return realBuilder;
    }

    /** No-op because QUIC options are not supported by the fallback {@code CronetEngine}. */
    @Implementation
    protected HttpEngine.Builder setQuicOptions(QuicOptions options) {
      return realBuilder;
    }

    /** No-op because DNS options are not supported by the fallback {@code CronetEngine}. */
    @Implementation
    protected HttpEngine.Builder setDnsOptions(DnsOptions options) {
      return realBuilder;
    }

    /** No-op because connection migration is not supported by the fallback {@code CronetEngine}. */
    @Implementation
    protected HttpEngine.Builder setConnectionMigrationOptions(ConnectionMigrationOptions options) {
      return realBuilder;
    }

    @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
    protected HttpEngine.Builder setThreadPriority(int priority) {
      delegate.setThreadPriority(priority);
      return realBuilder;
    }

    @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
    protected HttpEngine.Builder enableNetworkQualityEstimator(boolean value) {
      delegate.enableNetworkQualityEstimator(value);
      return realBuilder;
    }

    /** Validates non-null proxy options; proxy configuration is otherwise a no-op. */
    @Implementation(minSdk = CINNAMON_BUN)
    protected HttpEngine.Builder setProxyOptions(
        @ClassName("android.net.http.ProxyOptions") Object options) {
      Objects.requireNonNull(options);
      return realBuilder;
    }

    /** Builds a {@link HttpEngine} backed by the configured {@code CronetEngine}. */
    @Implementation
    protected HttpEngine build() {
      return delegate.build();
    }
  }

  /**
   * Isolates all {@code org.chromium.net.*} class references so {@code ShadowHttpEngine} and {@code
   * ShadowHttpEngineBuilder} can be loaded and wired by Robolectric without Cronet on the runtime
   * classpath.
   */
  static final class CronetBridge {
    private CronetBridge() {}

    static void requireCronetEngineBuilder(Object builder) {
      if (!(builder instanceof CronetEngine.Builder)) {
        throw new IllegalArgumentException(
            "Expected org.chromium.net.CronetEngine$Builder, got " + builder.getClass().getName());
      }
    }

    static BuilderDelegate forCustomBuilder(Object builder) {
      requireCronetEngineBuilder(builder);
      return new CronetBuilderDelegate((CronetEngine.Builder) builder);
    }

    static BuilderDelegate forJavaCronetProvider(Context context) {
      return new CronetBuilderDelegate(new JavaCronetProvider(context).createBuilder());
    }

    private static final class CronetBuilderDelegate implements BuilderDelegate {
      private final CronetEngine.Builder backend;

      CronetBuilderDelegate(CronetEngine.Builder backend) {
        this.backend = backend;
      }

      @Override
      public String getDefaultUserAgent() {
        return backend.getDefaultUserAgent();
      }

      @Override
      public void setUserAgent(String userAgent) {
        backend.setUserAgent(userAgent);
      }

      @Override
      public void setStoragePath(String value) {
        backend.setStoragePath(value);
      }

      @Override
      public void enableQuic(boolean value) {
        backend.enableQuic(value);
      }

      @Override
      public void enableHttp2(boolean value) {
        backend.enableHttp2(value);
      }

      @Override
      public void enableBrotli(boolean value) {
        backend.enableBrotli(value);
      }

      @Override
      public void enableHttpCache(int cacheMode, long maxSize) {
        backend.enableHttpCache(cacheMode, maxSize);
      }

      @Override
      public void addQuicHint(String host, int port, int alternatePort) {
        backend.addQuicHint(host, port, alternatePort);
      }

      @Override
      public void addPublicKeyPins(
          String hostName,
          Set<byte[]> pinsSha256,
          boolean includeSubdomains,
          Instant expirationInstant) {
        backend.addPublicKeyPins(
            hostName, pinsSha256, includeSubdomains, Date.from(expirationInstant));
      }

      @Override
      public void enablePublicKeyPinningBypassForLocalTrustAnchors(boolean value) {
        backend.enablePublicKeyPinningBypassForLocalTrustAnchors(value);
      }

      @Override
      public void setThreadPriority(int priority) {
        backend.setThreadPriority(priority);
      }

      @Override
      public void enableNetworkQualityEstimator(boolean value) {
        backend.enableNetworkQualityEstimator(value);
      }

      @Override
      public HttpEngine build() {
        return new CronetEngineWrapper(backend.build());
      }
    }

    private static final class CronetEngineWrapper extends HttpEngine {
      private final CronetEngine backend;

      CronetEngineWrapper(CronetEngine backend) {
        this.backend = backend;
      }

      @Override
      public void shutdown() {
        backend.shutdown();
      }

      @Override
      public URLConnection openConnection(URL url) throws IOException {
        return backend.openConnection(url);
      }

      @Override
      public URLStreamHandlerFactory createUrlStreamHandlerFactory() {
        return backend.createURLStreamHandlerFactory();
      }

      @Override
      public void bindToNetwork(@Nullable Network network) {
        long networkHandle = CronetEngine.UNBIND_NETWORK_HANDLE;
        if (network != null) {
          networkHandle = network.getNetworkHandle();
        }
        backend.bindToNetwork(networkHandle);
      }

      @Override
      public BidirectionalStream.Builder newBidirectionalStreamBuilder(
          String url, Executor executor, BidirectionalStream.Callback callback) {
        throw new UnsupportedOperationException("not implemented yet");
      }

      @Override
      public UrlRequest.Builder newUrlRequestBuilder(
          String url, Executor executor, UrlRequest.Callback callback) {
        throw new UnsupportedOperationException("not implemented yet");
      }
    }
  }
}
