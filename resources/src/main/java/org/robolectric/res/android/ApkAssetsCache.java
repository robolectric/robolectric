package org.robolectric.res.android;

import com.google.auto.value.AutoValue;
import java.util.HashMap;

/**
 * A global cache for ApkAssets (framework and app resources). This cache is shared between
 * sandboxes to avoid having to store multiple copies of system resources for the same SDK level
 * (i.e. different looper, graphics modes, or instrumentation configuration).
 *
 * <p>Previously there was a per-sandbox cache for system resources, but with the resource tables
 * growing in size, and an increasing number of sandbox parameters, the memory usage did not scale
 * well as test suites increased in size.
 *
 * <p>Currently the items in this cache are not made available for GC. However, it would definitely
 * be possible to have a per-sdk-level reference counting mechanism where an item could be freed if
 * all sandboxes referencing that item are collected.
 */
@SuppressWarnings("AndroidJdkLibsChecker")
public final class ApkAssetsCache {

  private static final HashMap<Key, Long> cachedApkAssetsPtrs = new HashMap<>();

  public static void put(String path, boolean system, int apiLevel, long ptr) {
    cachedApkAssetsPtrs.put(Key.newInstance(path, system, system ? apiLevel : 0), ptr);
  }

  public static long get(String path, boolean system, int apiLevel) {
    return cachedApkAssetsPtrs.getOrDefault(
        Key.newInstance(path, system, system ? apiLevel : 0), -1L);
  }

  /** The cache key for this cache. */
  @AutoValue
  public abstract static class Key {
    public abstract String path();

    public abstract boolean system();

    public abstract int apiLevel();

    public static Key newInstance(String path, boolean system, int apiLevel) {
      return new AutoValue_ApkAssetsCache_Key(path, system, apiLevel);
    }
  }

  private ApkAssetsCache() {}
}
