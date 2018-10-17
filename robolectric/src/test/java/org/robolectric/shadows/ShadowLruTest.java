package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.util.LruCache;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowLruTest {

  @Test
  public void shouldLru() throws Exception {
    LruCache<Integer, String> lruCache = new LruCache<>(2);
    lruCache.put(1, "one");
    lruCache.put(2, "two");
    lruCache.put(3, "three");

    assertThat(lruCache.size()).isEqualTo(2);
    assertThat(lruCache.get(1)).isNull();
    assertThat(lruCache.get(2)).isEqualTo("two");
    assertThat(lruCache.get(3)).isEqualTo("three");
  }
}
