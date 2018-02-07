package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.assertj.core.api.Assertions.assertThat;

import android.os.StatFs;
import java.io.File;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
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
