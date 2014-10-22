package org.robolectric.shadows;

import android.os.StatFs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class StatFsTest {
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
}
