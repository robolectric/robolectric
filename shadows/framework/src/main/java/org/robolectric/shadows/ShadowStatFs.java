package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

import android.os.StatFs;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Robolectic doesn't provide actual filesystem stats; rather, it provides the ability to specify
 * stats values in advance.
 *
 * @see #registerStats(File, int, int, int)
 */
@Implements(StatFs.class)
public class ShadowStatFs {
  public static final int BLOCK_SIZE = 4096;
  private static final Stats DEFAULT_STATS = new Stats(0, 0, 0);
  private static TreeMap<String, Stats> stats = new TreeMap<>();
  private Stats stat;

  @Implementation
  protected void __constructor__(String path) {
    restat(path);
  }

  @Implementation
  protected int getBlockSize() {
    return BLOCK_SIZE;
  }

  @Implementation
  protected int getBlockCount() {
    return stat.blockCount;
  }

  @Implementation
  protected int getFreeBlocks() {
    return stat.freeBlocks;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected long getFreeBlocksLong() {
    return stat.freeBlocks;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected long getFreeBytes() {
    return getBlockSizeLong() * getFreeBlocksLong();
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected long getAvailableBytes() {
    return getBlockSizeLong() * getAvailableBlocksLong();
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected long getTotalBytes() {
    return getBlockSizeLong() * getBlockCountLong();
  }

  @Implementation
  protected int getAvailableBlocks() {
    return stat.availableBlocks;
  }

  @Implementation
  protected void restat(String path) {
    Map.Entry<String, Stats> mapEntry = stats.floorEntry(path);
    for (;;) {
      // We will hit all matching paths, longest one first. We may hit non-matching paths before we
      // find the right one.
      if (mapEntry == null) {
        stat = DEFAULT_STATS;
        return;
      }
      String key = mapEntry.getKey();
      if (path.startsWith(key)) {
        stat = mapEntry.getValue();
        return;
      }
      mapEntry = stats.lowerEntry(key);
    }
  }

  /** Robolectric always uses a block size of `4096`. */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected long getBlockSizeLong() {
    return BLOCK_SIZE;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected long getBlockCountLong() {
    return stat.blockCount;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected long getAvailableBlocksLong() {
    return stat.availableBlocks;
  }

  /**
   * Register stats for a path, which will be used when a matching {@link StatFs} instance is
   * created.
   *
   * @param path path to the file
   * @param blockCount number of blocks
   * @param freeBlocks number of free blocks
   * @param availableBlocks number of available blocks
   */
  public static void registerStats(File path, int blockCount, int freeBlocks, int availableBlocks) {
    registerStats(path.getAbsolutePath(), blockCount, freeBlocks, availableBlocks);
  }

  /**
   * Register stats for a path, which will be used when a matching {@link StatFs} instance is
   * created.  A {@link StatFs} instance matches if it extends path. If several registered paths
   * match, we pick the longest one.
   *
   * @param path path to the file
   * @param blockCount number of blocks
   * @param freeBlocks number of free blocks
   * @param availableBlocks number of available blocks
   */
  public static void registerStats(String path, int blockCount, int freeBlocks,
      int availableBlocks) {
    stats.put(path, new Stats(blockCount, freeBlocks, availableBlocks));
  }

  @Resetter
  public static void reset() {
    stats.clear();
  }

  private static class Stats {
    Stats(int blockCount, int freeBlocks, int availableBlocks) {
      this.blockCount = blockCount;
      this.freeBlocks = freeBlocks;
      this.availableBlocks = availableBlocks;
    }
    int blockCount, freeBlocks, availableBlocks;
  }
}
