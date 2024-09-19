package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.StatsManager;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowStatsManager}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Build.VERSION_CODES.P)
public final class ShadowStatsManagerTest {

  @Test
  public void testGetMetadata() throws Exception {
    StatsManager statsManager =
        ApplicationProvider.getApplicationContext().getSystemService(StatsManager.class);
    byte[] metadataBytes = new byte[] {1, 2, 3, 4, 5};
    ShadowStatsManager.setStatsMetadata(metadataBytes);

    assertThat(statsManager.getMetadata()).isEqualTo(metadataBytes);
  }

  @Test
  public void testGetStatsMetadata() throws Exception {
    StatsManager statsManager =
        ApplicationProvider.getApplicationContext().getSystemService(StatsManager.class);
    byte[] metadataBytes = new byte[] {1, 2, 3, 4, 5};
    ShadowStatsManager.setStatsMetadata(metadataBytes);

    assertThat(statsManager.getStatsMetadata()).isEqualTo(metadataBytes);
  }

  @Test
  public void testGetReports_multipleReports() throws Exception {
    StatsManager statsManager =
        ApplicationProvider.getApplicationContext().getSystemService(StatsManager.class);
    long reportId1 = 1L;
    long reportId2 = 2L;
    byte[] report1Bytes = new byte[] {1, 2, 3, 4, 5};
    byte[] report2Bytes = new byte[] {1, 2, 3};
    ShadowStatsManager.addReportData(reportId1, report1Bytes);
    ShadowStatsManager.addReportData(reportId2, report2Bytes);

    assertThat(statsManager.getReports(reportId1)).isEqualTo(report1Bytes);
    assertThat(statsManager.getReports(reportId2)).isEqualTo(report2Bytes);
  }

  @Test
  public void testGetReports_clearsExistingReport() throws Exception {
    StatsManager statsManager =
        ApplicationProvider.getApplicationContext().getSystemService(StatsManager.class);
    long reportId1 = 1L;
    byte[] report1Bytes = new byte[] {1, 2, 3, 4, 5};
    ShadowStatsManager.addReportData(reportId1, report1Bytes);

    assertThat(statsManager.getReports(reportId1)).isEqualTo(report1Bytes);
    assertThat(statsManager.getReports(reportId1)).isEqualTo(new byte[] {});
  }

  @Test
  public void testAddConfig() throws Exception {
    StatsManager statsManager =
        ApplicationProvider.getApplicationContext().getSystemService(StatsManager.class);
    long configId1 = 1L;
    long configId2 = 2L;
    byte[] config1Bytes = new byte[] {1, 2, 3, 4, 5};
    byte[] config2Bytes = new byte[] {1, 2, 3};
    statsManager.addConfig(configId1, config1Bytes);
    statsManager.addConfig(configId2, config2Bytes);

    assertThat(ShadowStatsManager.getConfigData(configId1)).isEqualTo(config1Bytes);
    assertThat(ShadowStatsManager.getConfigData(configId2)).isEqualTo(config2Bytes);
  }

  @Test
  public void testRemoveConfig() throws Exception {
    StatsManager statsManager =
        ApplicationProvider.getApplicationContext().getSystemService(StatsManager.class);
    long configId1 = 1L;
    long configId2 = 2L;
    byte[] config1Bytes = new byte[] {1, 2, 3, 4, 5};
    byte[] config2Bytes = new byte[] {1, 2, 3};
    statsManager.addConfig(configId1, config1Bytes);
    statsManager.addConfig(configId2, config2Bytes);
    statsManager.removeConfig(configId1);

    assertThat(ShadowStatsManager.getConfigData(configId1)).isEqualTo(new byte[] {});
    assertThat(ShadowStatsManager.getConfigData(configId2)).isEqualTo(config2Bytes);
  }

  @Test
  public void testReset_clearsReports() throws Exception {
    StatsManager statsManager =
        ApplicationProvider.getApplicationContext().getSystemService(StatsManager.class);
    long reportId1 = 1L;
    byte[] report1Bytes = new byte[] {1, 2, 3, 4, 5};
    ShadowStatsManager.addReportData(reportId1, report1Bytes);

    ShadowStatsManager.reset();

    assertThat(statsManager.getReports(reportId1)).isEqualTo(new byte[] {});
  }

  @Test
  public void testReset_clearsMetadata() throws Exception {
    StatsManager statsManager =
        ApplicationProvider.getApplicationContext().getSystemService(StatsManager.class);
    byte[] metadataBytes = new byte[] {1, 2, 3, 4, 5};
    ShadowStatsManager.setStatsMetadata(metadataBytes);

    ShadowStatsManager.reset();

    assertThat(statsManager.getMetadata()).isEqualTo(new byte[] {});
  }

  @Test
  public void testReset_clearsConfigs() throws Exception {
    StatsManager statsManager =
        ApplicationProvider.getApplicationContext().getSystemService(StatsManager.class);
    long config1Id = 1L;
    byte[] config1Bytes = new byte[] {1, 2, 3, 4, 5};
    statsManager.addConfig(config1Id, config1Bytes);

    ShadowStatsManager.reset();

    assertThat(ShadowStatsManager.getConfigData(config1Id)).isEqualTo(new byte[] {});
  }
}
