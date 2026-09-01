package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.net.http.HttpResponseCache;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.net.ResponseCache;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowHttpResponseCacheTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void installedCacheIsReturned() throws Exception {
    // Also checks that the previous test left no cache behind.
    assertThat(HttpResponseCache.getInstalled()).isNull();
    HttpResponseCache cache = HttpResponseCache.install(temporaryFolder.newFolder(), 42);
    HttpResponseCache installed = HttpResponseCache.getInstalled();
    assertThat(installed).isSameInstanceAs(cache);
    assertThat(installed.maxSize()).isEqualTo(42);
  }

  @Test
  public void installedCacheIsTheDefaultResponseCache() throws Exception {
    assertThat(ResponseCache.getDefault()).isNull();

    HttpResponseCache cache = HttpResponseCache.install(temporaryFolder.newFolder(), 42);

    assertThat(HttpResponseCache.getDefault()).isSameInstanceAs(cache);
  }

  @Test
  public void countsStartAtZero() throws Exception {
    HttpResponseCache cache = HttpResponseCache.install(temporaryFolder.newFolder(), 42);
    assertThat(cache.getHitCount()).isEqualTo(0);
    assertThat(cache.getNetworkCount()).isEqualTo(0);
    assertThat(cache.getRequestCount()).isEqualTo(0);
  }

  @Test
  public void deleteRemovesReference() throws Exception {
    HttpResponseCache cache = HttpResponseCache.install(temporaryFolder.newFolder(), 42);
    cache.delete();
    assertThat(HttpResponseCache.getInstalled()).isNull();
    assertThat(HttpResponseCache.getDefault()).isNull();
  }

  @Test
  public void closeRemovesReference() throws Exception {
    HttpResponseCache cache = HttpResponseCache.install(temporaryFolder.newFolder(), 42);
    cache.close();
    assertThat(HttpResponseCache.getInstalled()).isNull();
    assertThat(HttpResponseCache.getDefault()).isNull();
  }

  @Test
  public void closeLeavesADifferentInstalledCacheAlone() throws Exception {
    HttpResponseCache uninstalled = HttpResponseCache.install(temporaryFolder.newFolder(), 42);
    HttpResponseCache installed = HttpResponseCache.install(temporaryFolder.newFolder(), 42);

    uninstalled.close();

    assertThat(HttpResponseCache.getInstalled()).isSameInstanceAs(installed);
  }

  @Test
  public void installKeepsAnEquivalentCache() throws Exception {
    File directory = temporaryFolder.newFolder();
    HttpResponseCache cache = HttpResponseCache.install(directory, 42);

    assertThat(HttpResponseCache.install(directory, 42)).isSameInstanceAs(cache);
  }

  @Test
  public void installReplacesACacheWithADifferentMaxSize() throws Exception {
    File directory = temporaryFolder.newFolder();
    HttpResponseCache cache = HttpResponseCache.install(directory, 42);

    HttpResponseCache replacement = HttpResponseCache.install(directory, 43);

    assertThat(replacement).isNotSameInstanceAs(cache);
    assertThat(HttpResponseCache.getInstalled()).isSameInstanceAs(replacement);
    assertThat(replacement.maxSize()).isEqualTo(43);
  }

  @Test
  public void installReplacesACacheWithADifferentDirectory() throws Exception {
    HttpResponseCache cache = HttpResponseCache.install(temporaryFolder.newFolder(), 42);

    HttpResponseCache replacement = HttpResponseCache.install(temporaryFolder.newFolder(), 42);

    assertThat(replacement).isNotSameInstanceAs(cache);
    assertThat(HttpResponseCache.getInstalled()).isSameInstanceAs(replacement);
  }
}
