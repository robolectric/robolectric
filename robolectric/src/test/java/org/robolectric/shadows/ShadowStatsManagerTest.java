package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.PendingIntent;
import android.app.StatsCursor;
import android.app.StatsManager;
import android.app.StatsManager.StatsQueryException;
import android.app.StatsQuery;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Looper;
import android.os.OutcomeReceiver;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import com.google.common.collect.ImmutableList;
import java.util.concurrent.CountDownLatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowStatsManager} */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Build.VERSION_CODES.P)
public final class ShadowStatsManagerTest {

  private long[] metricIds;
  private static CountDownLatch countDownLatch;

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

  @Before
  public void setUp() {
    IntentFilter filter = new IntentFilter(RestrictedMetricsChangedReceiver.ACTION);
    getApplicationContext().registerReceiver(new RestrictedMetricsChangedReceiver(), filter);
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void statsManagerQuery_throwsExceptionWhenResultOrExceptionNotSet() throws Exception {
    StatsManager statsManager = getApplicationContext().getSystemService(StatsManager.class);
    StatsQuery query =
        new StatsQuery.Builder("sqlQuery")
            .setSqlDialect(StatsQuery.DIALECT_SQLITE)
            .setMinSqlClientVersion(0)
            .build();
    StubOutcomeReceiver stubOutcomeReceiver = new StubOutcomeReceiver();
    statsManager.query(123L, "package", query, null, stubOutcomeReceiver);
    assertThat(stubOutcomeReceiver.error)
        .hasMessageThat()
        .isEqualTo("Failed to query statsd: Stats result was not configured.");
  }

  private static class StubOutcomeReceiver
      implements OutcomeReceiver<StatsCursor, StatsQueryException> {
    StatsCursor result;
    StatsQueryException error;

    @Override
    public void onResult(StatsCursor result) {
      this.result = result;
    }

    @Override
    public void onError(@NonNull StatsQueryException error) {
      this.error = error;
    }
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void statsManagerAddMetric_invokesPassedPendingIntent() throws Exception {
    StatsManager statsManager = getApplicationContext().getSystemService(StatsManager.class);
    PendingIntent pi =
        PendingIntent.getBroadcast(
            getApplicationContext(),
            1,
            new Intent(RestrictedMetricsChangedReceiver.ACTION),
            PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    ShadowStatsManager shadowStatsManager = Shadow.extract(statsManager);
    shadowStatsManager.addMetricIds(ImmutableList.of(123L, 12345L), getApplicationContext());

    countDownLatch = new CountDownLatch(1);

    metricIds = statsManager.setRestrictedMetricsChangedOperation(123, "package", pi);
    assertThat(metricIds).asList().containsExactly(123L, 12345L);

    shadowStatsManager.addMetricIds(ImmutableList.of(123456L), getApplicationContext());
    shadowOf(Looper.getMainLooper()).idle();
    countDownLatch.await();

    assertThat(metricIds).asList().containsExactly(123L, 12345L, 123456L);
  }

  public final class RestrictedMetricsChangedReceiver extends BroadcastReceiver {
    public static final String ACTION =
        "org.robolectric.shadows.RestrictedMetricsChangedReceiver.ACTION";

    @Override
    public void onReceive(Context mContext, Intent intent) {
      metricIds = intent.getLongArrayExtra(StatsManager.EXTRA_STATS_RESTRICTED_METRIC_IDS);
      countDownLatch.countDown();
    }
  }
}
