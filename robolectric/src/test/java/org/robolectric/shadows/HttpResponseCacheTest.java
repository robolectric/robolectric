package org.robolectric.shadows;

import android.net.http.HttpResponseCache;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class HttpResponseCacheTest {
  @Before
  public void setUp() {
    // If someone else installed a cache, clear it.
    ShadowHttpResponseCache.installed = null;
  }

  @After
  public void tearDown() {
    // Ensure we don't leak an installed cache from a test.
    ShadowHttpResponseCache.installed = null;
  }

  @Test
  public void installedCacheIsReturned() throws Exception {
    assertThat(HttpResponseCache.getInstalled()).isNull();
    HttpResponseCache cache = HttpResponseCache.install(File.createTempFile("foo", "bar"), 42);
    HttpResponseCache installed = HttpResponseCache.getInstalled();
    assertThat(installed).isSameAs(cache);
    assertThat(installed.maxSize()).isEqualTo(42);
  }

  @Test
  public void countsStartAtZero() throws Exception {
    HttpResponseCache cache = HttpResponseCache.install(File.createTempFile("foo", "bar"), 42);
    assertThat(cache.getHitCount()).isZero();
    assertThat(cache.getNetworkCount()).isZero();
    assertThat(cache.getRequestCount()).isZero();
  }

  @Test
  public void deleteRemovesReference() throws Exception {
    HttpResponseCache cache = HttpResponseCache.install(File.createTempFile("foo", "bar"), 42);
    cache.delete();
    assertThat(HttpResponseCache.getInstalled()).isNull();
  }

  @Test
  public void closeRemovesReference() throws Exception {
    HttpResponseCache cache = HttpResponseCache.install(File.createTempFile("foo", "bar"), 42);
    cache.close();
    assertThat(HttpResponseCache.getInstalled()).isNull();
  }
}
