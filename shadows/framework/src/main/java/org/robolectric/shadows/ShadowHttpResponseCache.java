package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.newInstanceOf;

import android.net.http.HttpResponseCache;
import java.io.File;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = HttpResponseCache.class, callThroughByDefault = false)
public class ShadowHttpResponseCache {
  private static final Object LOCK = new Object();

  static ShadowHttpResponseCache installed = null;

  private HttpResponseCache originalObject;
  private File directory;
  private long maxSize;
  private int requestCount = 0;
  private int hitCount = 0;
  private int networkCount = 0;

  @Implementation
  protected static HttpResponseCache install(File directory, long maxSize) {
    HttpResponseCache cache = newInstanceOf(HttpResponseCache.class);
    ShadowHttpResponseCache shadowCache = Shadow.extract(cache);
    shadowCache.originalObject = cache;
    shadowCache.directory = directory;
    shadowCache.maxSize = maxSize;
    synchronized (LOCK) {
      installed = shadowCache;
      return cache;
    }
  }

  @Implementation
  protected static HttpResponseCache getInstalled() {
    synchronized (LOCK) {
      return (installed != null) ? installed.originalObject : null;
    }
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
    synchronized (LOCK) {
      installed = null;
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
}
