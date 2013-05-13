package org.robolectric.shadows;

import android.net.http.HttpResponseCache;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.File;
import java.net.CacheResponse;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import static org.robolectric.Robolectric.newInstanceOf;

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
  public static HttpResponseCache install(File directory, long maxSize) {
    HttpResponseCache cache = newInstanceOf(HttpResponseCache.class);
    ShadowHttpResponseCache shadowCache = Robolectric.shadowOf(cache);
    shadowCache.originalObject = cache;
    shadowCache.directory = directory;
    shadowCache.maxSize = maxSize;
    synchronized (LOCK) {
      installed = shadowCache;
      return cache;
    }
  }

  @Implementation
  public static HttpResponseCache getInstalled() {
    synchronized (LOCK) {
      return (installed != null) ? installed.originalObject : null;
    }
  }

  @Implementation
  public long maxSize() {
    return maxSize;
  }

  @Implementation
  public long size() {
    return 0;
  }

  @Implementation
  public void close() {
    synchronized (LOCK) {
      installed = null;
    }
  }

  @Implementation
  public void delete() {
    close();
  }

  @Implementation
  public int getHitCount() {
    return hitCount;
  }

  @Implementation
  public int getNetworkCount() {
    return networkCount;
  }

  @Implementation
  public int getRequestCount() {
    return requestCount;
  }

  @Implementation
  public CacheResponse get(URI uri, String requestMethod, Map<String, List<String>> requestHeaders) {
    requestCount += 1;
    networkCount += 1; // Always pretend we had a cache miss and had to fall back to the network.
    return null;
  }

  @Implementation
  public CacheResponse put(URI uri, URLConnection urlConnection) {
    // Do not cache any data. All requests will be a miss.
    return null;
  }
}
