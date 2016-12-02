package org.robolectric.internal;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruCacheHashMap<K,V> extends LinkedHashMap<K,V> {

  private final int cacheSize;

  public LruCacheHashMap(int cacheSize) {
    this.cacheSize = cacheSize;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry eldest) {
    return size() > cacheSize;
  }
}
