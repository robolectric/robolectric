package org.robolectric.shadows;

import android.annotation.NonNull;
import android.content.Context;
import android.net.Network;
import android.net.http.ExperimentalBidirectionalStream;
import android.net.http.ExperimentalHttpEngine;
import android.net.http.ExperimentalUrlRequest;
import android.net.http.HttpEngine;
import android.net.http.IHttpEngineBuilder;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandlerFactory;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Executor;
import org.chromium.net.CronetEngine;
import org.chromium.net.impl.JavaCronetProvider;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.versioning.AndroidVersions.U;

@SuppressWarnings({"NewApi"})
@Implements(value = HttpEngine.Builder.class, minSdk = U.SDK_INT)
public class ShadowHttpEngine {
  private static CronetEngine.Builder cronetEngineBuilder;

  @Implementation
  protected static IHttpEngineBuilder createBuilderDelegate(Context context) {
    if (cronetEngineBuilder != null) {
      return new CronetEngineBuilderWrapper(cronetEngineBuilder);
    } else {
      return new CronetEngineBuilderWrapper(new JavaCronetProvider(context).createBuilder());
    }
  }

  public static void setCronetEngineBuilder(CronetEngine.Builder cronetEngineBuilder) {
    ShadowHttpEngine.cronetEngineBuilder = cronetEngineBuilder;
  }

  @Resetter
  public static void reset() {
    cronetEngineBuilder = null;
  }

  static class CronetEngineBuilderWrapper extends IHttpEngineBuilder {
    private final CronetEngine.Builder backend;

    public CronetEngineBuilderWrapper(CronetEngine.Builder backend) {
      this.backend = backend;
    }

    @Override
    public String getDefaultUserAgent() {
      return backend.getDefaultUserAgent();
    }

    @Override
    public IHttpEngineBuilder setUserAgent(String userAgent) {
      backend.setUserAgent(userAgent);
      return this;
    }

    @Override
    public IHttpEngineBuilder setStoragePath(String value) {
      backend.setStoragePath(value);
      return this;
    }

    @Override
    public IHttpEngineBuilder addQuicHint(String host, int port, int alternatePort) {
      backend.addQuicHint(host, port, alternatePort);
      return this;
    }

    @Override
    public IHttpEngineBuilder addPublicKeyPins(
        String hostName,
        Set<byte[]> pinsSha256,
        boolean includeSubdomains,
        Instant expirationInstant) {
      backend.addPublicKeyPins(
          hostName, pinsSha256, includeSubdomains, Date.from(expirationInstant));
      return this;
    }

    @Override
    public IHttpEngineBuilder setQuicOptions(@NonNull android.net.http.QuicOptions options) {
      return this;
    }

    @Override
    public IHttpEngineBuilder setDnsOptions(@NonNull android.net.http.DnsOptions options) {
      return this;
    }

    @Override
    public IHttpEngineBuilder setConnectionMigrationOptions(
        @NonNull android.net.http.ConnectionMigrationOptions options) {
      return this;
    }

    @Override
    public IHttpEngineBuilder enableBrotli(boolean value) {
      return this;
    }

    @Override
    protected Set<Integer> getSupportedConfigOptions() {
      return Collections.emptySet();
    }

    @Override
    public IHttpEngineBuilder enableNetworkQualityEstimator(boolean value) {
      return this;
    }

    @Override
    public IHttpEngineBuilder setThreadPriority(int priority) {
      return this;
    }

    @Override
    public IHttpEngineBuilder enableHttp2(boolean b) {
      return this;
    }

    @Override
    public IHttpEngineBuilder enableHttpCache(int i, long l) {
      return this;
    }

    @Override
    public IHttpEngineBuilder enablePublicKeyPinningBypassForLocalTrustAnchors(boolean b) {
      return this;
    }

    @Override
    public IHttpEngineBuilder enableQuic(boolean b) {
      return this;
    }

    @Override
    public IHttpEngineBuilder enableSdch(boolean b) {
      return this;
    }

    @Override
    public IHttpEngineBuilder setExperimentalOptions(String s) {
      return this;
    }

    public ExperimentalHttpEngine build() {
      return new CronetEngineWrapper(backend.build());
    }
  }

  static class CronetEngineWrapper extends ExperimentalHttpEngine {

    private final CronetEngine backend;

    public CronetEngineWrapper(CronetEngine backend) {
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
    public void bindToNetwork(Network network) {
      long networkHandle = backend.UNBIND_NETWORK_HANDLE;
      if (network != null) {
        networkHandle = network.getNetworkHandle();
      }
      backend.bindToNetwork(networkHandle);
    }

    @Override
    public ExperimentalBidirectionalStream.Builder newBidirectionalStreamBuilder(
        String url, Executor executor, android.net.http.BidirectionalStream.Callback callback) {
      throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public ExperimentalUrlRequest.Builder newUrlRequestBuilder(
        String url, Executor executor, android.net.http.UrlRequest.Callback callback) {
      throw new UnsupportedOperationException("not implemented yet");
    }
  }
}
