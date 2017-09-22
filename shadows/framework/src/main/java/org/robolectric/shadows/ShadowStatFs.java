package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

import android.os.StatFs;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Robolectic doesn't provide actual filesystem stats; rather, it provides the ability to specify stats values in advance.
 *
 * @see #registerStats(File, int, int, int)
 */
@Implements(StatFs.class)
public class ShadowStatFs {
  public static final int BLOCK_SIZE = 4096;
  private static final Stats DEFAULT_STATS = new Stats(0, 0, 0);
  private static Map<String, Stats> stats = new HashMap<String, Stats>();
  private Stats stat;

  @Implementation
  public void __constructor__(String path) {
    restat(path);
  }

  @Implementation
  public int getBlockSize() {
    return BLOCK_SIZE;
  }

  @Implementation
  public int getBlockCount() {
    return stat.blockCount;
  }

  @Implementation
  public int getFreeBlocks() {
    return stat.freeBlocks;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public long getFreeBlocksLong() {
    return stat.freeBlocks;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public long getFreeBytes() {
    return getBlockSizeLong() * getFreeBlocksLong();
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public long getAvailableBytes() {
    return getBlockSizeLong() * getAvailableBlocksLong();
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public long getTotalBytes() {
    return getBlockSizeLong() * getBlockCountLong();
  }
  @Implementation
  public int getAvailableBlocks() {
    return stat.availableBlocks;
  }

  @Implementation
  public void restat(String path) {
    stat = stats.get(path);
    if (stat == null) {
      stat = DEFAULT_STATS;
    }
  }

  /**
   * Robolectric always uses a block size of `4096`.
   * @return
   */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  public long getBlockSizeLong() {
    return BLOCK_SIZE;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public long getBlockCountLong() {
    return stat.blockCount;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public long getAvailableBlocksLong() {
    return stat.availableBlocks;
  }

  /**
   * Register stats for a path, which will be used when a matching {@link StatFs} instance is created.
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
   * Register stats for a path, which will be used when a matching {@link StatFs} instance is created.
   *
   * @param path path to the file
   * @param blockCount number of blocks
   * @param freeBlocks number of free blocks
   * @param availableBlocks number of available blocks
   */
  public static void registerStats(String path, int blockCount, int freeBlocks, int availableBlocks) {
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