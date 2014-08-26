package org.robolectric.shadows;

import android.util.LruCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class LruTest {

  @Test
  public void shouldLru() throws Exception {
    LruCache<Integer, String> lruCache = new LruCache<Integer, String>(2);
    lruCache.put(1, "one");
    lruCache.put(2, "two");
    lruCache.put(3, "three");

    assertThat(lruCache.size()).isEqualTo(2);
    assertThat(lruCache.get(1)).isNull();
    assertThat(lruCache.get(2)).isEqualTo("two");
    assertThat(lruCache.get(3)).isEqualTo("three");
  }
}
