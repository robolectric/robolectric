package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.newInstanceOf;

import android.net.http.HttpResponseCache;
import java.io.File;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = HttpResponseCache.class)
public class ShadowHttpResponseCache {
  private static final Object LOCK = new Object();

  @RealObject private HttpResponseCache realCache;

  private File directory;
  private long maxSize;
  private int requestCount = 0;

  @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
  private int hitCount = 0;

  private int networkCount = 0;

  /**
   * Installs the cache the way the framework does, by making it the default {@link ResponseCache}.
   *
   * <p>Keeping it anywhere else would leave {@link ResponseCache#getDefault()} empty, which callers
   * reach through the inherited {@code HttpResponseCache.getDefault()}.
   */
  @Implementation
  protected static HttpResponseCache install(File directory, long maxSize) {
    synchronized (LOCK) {
      HttpResponseCache installed = getInstalled();
      if (installed != null) {
        ShadowHttpResponseCache shadowInstalled = Shadow.extract(installed);
        // An equivalent cache is already installed, so the framework keeps using it.
        if (shadowInstalled.maxSize == maxSize
            && Objects.equals(shadowInstalled.directory, directory)) {
          return installed;
        }
      }

      HttpResponseCache cache = newInstanceOf(HttpResponseCache.class);
      ShadowHttpResponseCache shadowCache = Shadow.extract(cache);
      shadowCache.directory = directory;
      shadowCache.maxSize = maxSize;
      ResponseCache.setDefault(cache);
      return cache;
    }
  }

  @Implementation
  protected static HttpResponseCache getInstalled() {
    ResponseCache installed = ResponseCache.getDefault();
    return installed instanceof HttpResponseCache ? (HttpResponseCache) installed : null;
  }

  @Implementation
  protected long maxSize() {
    return maxSize;
  }

  @Implementation
  protected long size() {
    return 0;
  }

  @Implementation
  protected void close() {
    // Uninstalls only this cache, and only while it is the installed one.
    synchronized (LOCK) {
      if (ResponseCache.getDefault() == realCache) {
        ResponseCache.setDefault(null);
      }
    }
  }

  @Implementation
  protected void delete() {
    close();
  }

  @Implementation
  protected int getHitCount() {
    return hitCount;
  }

  @Implementation
  protected int getNetworkCount() {
    return networkCount;
  }

  @Implementation
  protected int getRequestCount() {
    return requestCount;
  }

  @Implementation
  protected CacheResponse get(
      URI uri, String requestMethod, Map<String, List<String>> requestHeaders) {
    requestCount += 1;
    networkCount += 1; // Always pretend we had a cache miss and had to fall back to the network.
    return null;
  }

  @Implementation
  protected CacheRequest put(URI uri, URLConnection urlConnection) {
    // Do not cache any data. All requests will be a miss.
    return null;
  }

  @Implementation
  protected void flush() {
    // No-op as `mDelegate` is null.
  }

  /**
   * The default {@link ResponseCache} is process-wide state that outlives the sandbox, so a cache a
   * test installs has to be uninstalled again. Anything else installed there is left alone.
   */
  @Resetter
  public static void reset() {
    synchronized (LOCK) {
      if (ResponseCache.getDefault() instanceof HttpResponseCache) {
        ResponseCache.setDefault(null);
      }
    }
  }
}
