package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.net.http.HttpResponseCache;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowHttpResponseCacheTest {
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
    assertThat(cache.getHitCount()).isEqualTo(0);
    assertThat(cache.getNetworkCount()).isEqualTo(0);
    assertThat(cache.getRequestCount()).isEqualTo(0);
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
