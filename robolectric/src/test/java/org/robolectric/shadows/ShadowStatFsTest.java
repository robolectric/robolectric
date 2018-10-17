package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static com.google.common.truth.Truth.assertThat;

import android.os.StatFs;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowStatFsTest {
  @Test
  public void shouldRegisterStats() throws Exception {
    ShadowStatFs.registerStats("/tmp", 100, 20, 10);
    StatFs statsFs = new StatFs("/tmp");

    assertThat(statsFs.getBlockCount()).isEqualTo(100);
    assertThat(statsFs.getFreeBlocks()).isEqualTo(20);
    assertThat(statsFs.getAvailableBlocks()).isEqualTo(10);
    assertThat(statsFs.getBlockSize()).isEqualTo(ShadowStatFs.BLOCK_SIZE);
  }

  @Test
  public void shouldRegisterStatsWithFile() throws Exception {
    ShadowStatFs.registerStats(new File("/tmp"), 100, 20, 10);
    StatFs statsFs = new StatFs(new File("/tmp").getAbsolutePath());

    assertThat(statsFs.getBlockCount()).isEqualTo(100);
    assertThat(statsFs.getFreeBlocks()).isEqualTo(20);
    assertThat(statsFs.getAvailableBlocks()).isEqualTo(10);
    assertThat(statsFs.getBlockSize()).isEqualTo(ShadowStatFs.BLOCK_SIZE);
  }

  @Test
  public void shouldUseBestMatch() throws Exception {
    ShadowStatFs.registerStats("/tmp", 101, 21, 11);
    ShadowStatFs.registerStats("/tmp/a", 102, 22, 12);
    StatFs statsFsForTmp = new StatFs("/tmp");
    StatFs statsFsForA = new StatFs("/tmp/a");
    StatFs statsFsForB = new StatFs("/tmp/b");
    StatFs statsFsForAC = new StatFs("/tmp/a/c");

    assertThat(statsFsForTmp.getBlockCount()).isEqualTo(101);
    assertThat(statsFsForTmp.getFreeBlocks()).isEqualTo(21);
    assertThat(statsFsForTmp.getAvailableBlocks()).isEqualTo(11);
    assertThat(statsFsForTmp.getBlockSize()).isEqualTo(ShadowStatFs.BLOCK_SIZE);

    assertThat(statsFsForA.getBlockCount()).isEqualTo(102);
    assertThat(statsFsForA.getFreeBlocks()).isEqualTo(22);
    assertThat(statsFsForA.getAvailableBlocks()).isEqualTo(12);
    assertThat(statsFsForA.getBlockSize()).isEqualTo(ShadowStatFs.BLOCK_SIZE);

    assertThat(statsFsForB.getBlockCount()).isEqualTo(101);
    assertThat(statsFsForB.getFreeBlocks()).isEqualTo(21);
    assertThat(statsFsForB.getAvailableBlocks()).isEqualTo(11);
    assertThat(statsFsForB.getBlockSize()).isEqualTo(ShadowStatFs.BLOCK_SIZE);

    assertThat(statsFsForAC.getBlockCount()).isEqualTo(102);
    assertThat(statsFsForAC.getFreeBlocks()).isEqualTo(22);
    assertThat(statsFsForAC.getAvailableBlocks()).isEqualTo(12);
    assertThat(statsFsForAC.getBlockSize()).isEqualTo(ShadowStatFs.BLOCK_SIZE);

    StatFs statsFsForSlash = new StatFs("/");
    assertThat(statsFsForSlash.getFreeBlocks()).isEqualTo(0);
  }

  @Test
  public void shouldResetStateBetweenTests() throws Exception {
    StatFs statsFs = new StatFs("/tmp");
    assertThat(statsFs.getBlockCount()).isEqualTo(0);
    assertThat(statsFs.getFreeBlocks()).isEqualTo(0);
    assertThat(statsFs.getAvailableBlocks()).isEqualTo(0);
    assertThat(statsFs.getBlockSize()).isEqualTo(ShadowStatFs.BLOCK_SIZE);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void withApi18_shouldRegisterStats() {
    ShadowStatFs.registerStats("/tmp", 100, 20, 10);
    StatFs statsFs = new StatFs("/tmp");

    assertThat(statsFs.getBlockCountLong()).isEqualTo(100L);
    assertThat(statsFs.getFreeBlocksLong()).isEqualTo(20L);
    assertThat(statsFs.getFreeBytes()).isEqualTo(20L * ShadowStatFs.BLOCK_SIZE);
    assertThat(statsFs.getAvailableBlocksLong()).isEqualTo(10L);
    assertThat(statsFs.getAvailableBytes()).isEqualTo(10L * ShadowStatFs.BLOCK_SIZE);
    assertThat(statsFs.getBlockSizeLong()).isEqualTo(ShadowStatFs.BLOCK_SIZE);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void withApi18_shouldRegisterStatsWithFile() {
    ShadowStatFs.registerStats(new File("/tmp"), 100, 20, 10);
    StatFs statsFs = new StatFs(new File("/tmp").getAbsolutePath());

    assertThat(statsFs.getBlockCountLong()).isEqualTo(100L);
    assertThat(statsFs.getFreeBlocksLong()).isEqualTo(20L);
    assertThat(statsFs.getFreeBytes()).isEqualTo(20L * ShadowStatFs.BLOCK_SIZE);
    assertThat(statsFs.getAvailableBlocksLong()).isEqualTo(10L);
    assertThat(statsFs.getAvailableBytes()).isEqualTo(10L * ShadowStatFs.BLOCK_SIZE);
    assertThat(statsFs.getBlockSizeLong()).isEqualTo(ShadowStatFs.BLOCK_SIZE);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void withApi18_shouldResetStateBetweenTests() {
    StatFs statsFs = new StatFs("/tmp");
    assertThat(statsFs.getBlockCountLong()).isEqualTo(0L);
    assertThat(statsFs.getFreeBlocksLong()).isEqualTo(0L);
    assertThat(statsFs.getFreeBytes()).isEqualTo(0L);
    assertThat(statsFs.getAvailableBlocksLong()).isEqualTo(0L);
    assertThat(statsFs.getAvailableBytes()).isEqualTo(0L);
    assertThat(statsFs.getBlockSizeLong()).isEqualTo(ShadowStatFs.BLOCK_SIZE);
  }

  @Test
  public void shouldRestat() throws Exception {
    ShadowStatFs.registerStats("/tmp", 100, 20, 10);
    StatFs statsFs = new StatFs("/tmp");

    assertThat(statsFs.getBlockCount()).isEqualTo(100);
    assertThat(statsFs.getFreeBlocks()).isEqualTo(20);
    assertThat(statsFs.getAvailableBlocks()).isEqualTo(10);

    ShadowStatFs.registerStats("/tmp", 3, 2, 1);

    statsFs.restat("/tmp");
    assertThat(statsFs.getBlockCount()).isEqualTo(3);
    assertThat(statsFs.getFreeBlocks()).isEqualTo(2);
    assertThat(statsFs.getAvailableBlocks()).isEqualTo(1);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void withApi18_shouldRestat() throws Exception {
    ShadowStatFs.registerStats("/tmp", 100, 20, 10);
    StatFs statsFs = new StatFs("/tmp");

    assertThat(statsFs.getBlockCountLong()).isEqualTo(100L);
    assertThat(statsFs.getFreeBlocksLong()).isEqualTo(20L);
    assertThat(statsFs.getFreeBytes()).isEqualTo(20L * ShadowStatFs.BLOCK_SIZE);
    assertThat(statsFs.getAvailableBlocksLong()).isEqualTo(10L);
    assertThat(statsFs.getAvailableBytes()).isEqualTo(10L * ShadowStatFs.BLOCK_SIZE);

    ShadowStatFs.registerStats("/tmp", 3, 2, 1);

    statsFs.restat("/tmp");
    assertThat(statsFs.getBlockCountLong()).isEqualTo(3L);
    assertThat(statsFs.getFreeBlocksLong()).isEqualTo(2L);
    assertThat(statsFs.getFreeBytes()).isEqualTo(2L * ShadowStatFs.BLOCK_SIZE);
    assertThat(statsFs.getAvailableBlocksLong()).isEqualTo(1L);
    assertThat(statsFs.getAvailableBytes()).isEqualTo(1L * ShadowStatFs.BLOCK_SIZE);
  }
}
