package org.robolectric.shadows;

import android.os.StatFs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class StatFsTest {
  /* http://rationalpi.wordpress.com/2007/01/26/javaiotmpdir-inconsitency/  */
  private static final String TMP_DIR = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath();

  @Test
  public void shouldRegisterStats() throws Exception {
    ShadowStatFs.registerStats(TMP_DIR, 100, 20, 10);
    StatFs statsFs = new StatFs(TMP_DIR);

    assertThat(statsFs.getBlockCount()).isEqualTo(100);
    assertThat(statsFs.getFreeBlocks()).isEqualTo(20);
    assertThat(statsFs.getAvailableBlocks()).isEqualTo(10);
    assertThat(statsFs.getBlockSize()).isEqualTo(ShadowStatFs.BLOCK_SIZE);
  }

  @Test
  public void shouldRegisterStatsWithFile() throws Exception {
    ShadowStatFs.registerStats(new File(TMP_DIR), 100, 20, 10);
    StatFs statsFs = new StatFs(TMP_DIR);

    assertThat(statsFs.getBlockCount()).isEqualTo(100);
    assertThat(statsFs.getFreeBlocks()).isEqualTo(20);
    assertThat(statsFs.getAvailableBlocks()).isEqualTo(10);
    assertThat(statsFs.getBlockSize()).isEqualTo(ShadowStatFs.BLOCK_SIZE);
  }

  @Test
  public void shouldResetStateBetweenTests() throws Exception {
    StatFs statsFs = new StatFs(TMP_DIR);
    assertThat(statsFs.getBlockCount()).isEqualTo(0);
    assertThat(statsFs.getFreeBlocks()).isEqualTo(0);
    assertThat(statsFs.getAvailableBlocks()).isEqualTo(0);
    assertThat(statsFs.getBlockSize()).isEqualTo(ShadowStatFs.BLOCK_SIZE);
  }

  @Test
  public void shouldRestat() throws Exception {
    ShadowStatFs.registerStats(TMP_DIR, 100, 20, 10);
    StatFs statsFs = new StatFs(TMP_DIR);

    assertThat(statsFs.getBlockCount()).isEqualTo(100);
    assertThat(statsFs.getFreeBlocks()).isEqualTo(20);
    assertThat(statsFs.getAvailableBlocks()).isEqualTo(10);

    ShadowStatFs.registerStats(TMP_DIR, 3, 2, 1);

    statsFs.restat(TMP_DIR);
    assertThat(statsFs.getBlockCount()).isEqualTo(3);
    assertThat(statsFs.getFreeBlocks()).isEqualTo(2);
    assertThat(statsFs.getAvailableBlocks()).isEqualTo(1);
  }
}
