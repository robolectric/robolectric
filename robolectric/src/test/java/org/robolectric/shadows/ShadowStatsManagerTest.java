package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.StatsManager;
import android.content.Context;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowStatsManager} */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Build.VERSION_CODES.P)
public final class ShadowStatsManagerTest {

  @Test
  public void testGetMetadata() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    StatsManager statsManager = context.getSystemService(StatsManager.class);
    byte[] metadataBytes = new byte[] {1, 2, 3, 4, 5};
    ShadowStatsManager.setStatsMetadata(metadataBytes);

    assertThat(statsManager.getMetadata()).isEqualTo(metadataBytes);
  }

  @Test
  public void testGetReports_multipleReports() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    StatsManager statsManager = context.getSystemService(StatsManager.class);
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
    Context context = ApplicationProvider.getApplicationContext();
    StatsManager statsManager = context.getSystemService(StatsManager.class);
    long reportId1 = 1L;
    byte[] report1Bytes = new byte[] {1, 2, 3, 4, 5};
    ShadowStatsManager.addReportData(reportId1, report1Bytes);

    assertThat(statsManager.getReports(reportId1)).isEqualTo(report1Bytes);
    assertThat(statsManager.getReports(reportId1)).isEqualTo(new byte[] {});
  }

  @Test
  public void testReset_clearsReports() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    StatsManager statsManager = context.getSystemService(StatsManager.class);
    long reportId1 = 1L;
    byte[] report1Bytes = new byte[] {1, 2, 3, 4, 5};
    ShadowStatsManager.addReportData(reportId1, report1Bytes);

    ShadowStatsManager.reset();

    assertThat(statsManager.getReports(reportId1)).isEqualTo(new byte[] {});
  }

  @Test
  public void testReset_clearsMetadata() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    StatsManager statsManager = context.getSystemService(StatsManager.class);
    byte[] metadataBytes = new byte[] {1, 2, 3, 4, 5};
    ShadowStatsManager.setStatsMetadata(metadataBytes);

    ShadowStatsManager.reset();

    assertThat(statsManager.getMetadata()).isEqualTo(new byte[] {});
  }
}
