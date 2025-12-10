package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiUsabilityStatsEntry;
import android.os.Build.VERSION_CODES;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.Q)
public class WifiUsabilityStatsEntryBuilderTest {

  @Test
  public void newBuilder_buildsCorrectEntry() {
    WifiUsabilityStatsEntryBuilder builder = WifiUsabilityStatsEntryBuilder.newBuilder();

    builder
        .setTimeStampMillis(1000L)
        .setRssi(-60)
        .setLinkSpeedMbps(866)
        .setTotalTxSuccess(100)
        .setTotalTxRetries(10)
        .setTotalTxBad(1)
        .setTotalRxSuccess(200)
        .setTotalRadioOnTimeMillis(5000)
        .setTotalRadioTxTimeMillis(2000)
        .setTotalRadioRxTimeMillis(1500)
        .setTotalScanTimeMillis(100)
        .setTotalNanScanTimeMillis(10)
        .setTotalBackgroundScanTimeMillis(20)
        .setTotalRoamScanTimeMillis(5)
        .setTotalPnoScanTimeMillis(15)
        .setTotalHotspot2ScanTimeMillis(3)
        .setTotalCcaBusyFreqTimeMillis(4000)
        .setTotalRadioOnFreqTimeMillis(5000)
        .setTotalBeaconRx(50)
        .setProbeStatusSinceLastUpdate(1)
        .setProbeElapsedTimeSinceLastUpdateMillis(500)
        .setProbeMcsRateSinceLastUpdate(9)
        .setRxLinkSpeedMbps(780)
        .setCellularDataNetworkType(13) // LTE
        .setCellularSignalStrengthDbm(-80)
        .setCellularSignalStrengthDb(10)
        .setIsSameRegisteredCell(true);

    if (RuntimeEnvironment.getApiLevel() >= S) {
      builder
          .setTimeSliceDutyCycleInPercent(80)
          .setIsCellularDataAvailable(true)
          .setIsThroughputSufficient(true)
          .setIsWifiScoringEnabled(true);
    }

    WifiUsabilityStatsEntry entry = builder.build();

    assertThat(entry.getTimeStampMillis()).isEqualTo(1000L);
    assertThat(entry.getRssi()).isEqualTo(-60);
    assertThat(entry.getLinkSpeedMbps()).isEqualTo(866);
    assertThat(entry.getTotalTxSuccess()).isEqualTo(100);
    assertThat(entry.getTotalTxRetries()).isEqualTo(10);
    assertThat(entry.getTotalTxBad()).isEqualTo(1);
    assertThat(entry.getTotalRxSuccess()).isEqualTo(200);
    assertThat(entry.getTotalRadioOnTimeMillis()).isEqualTo(5000);
    assertThat(entry.getTotalRadioTxTimeMillis()).isEqualTo(2000);
    assertThat(entry.getTotalRadioRxTimeMillis()).isEqualTo(1500);
    assertThat(entry.getTotalScanTimeMillis()).isEqualTo(100);
    assertThat(entry.getTotalNanScanTimeMillis()).isEqualTo(10);
    assertThat(entry.getTotalBackgroundScanTimeMillis()).isEqualTo(20);
    assertThat(entry.getTotalRoamScanTimeMillis()).isEqualTo(5);
    assertThat(entry.getTotalPnoScanTimeMillis()).isEqualTo(15);
    assertThat(entry.getTotalHotspot2ScanTimeMillis()).isEqualTo(3);
    assertThat(entry.getTotalCcaBusyFreqTimeMillis()).isEqualTo(4000);
    assertThat(entry.getTotalRadioOnFreqTimeMillis()).isEqualTo(5000);
    assertThat(entry.getTotalBeaconRx()).isEqualTo(50);
    assertThat(entry.getProbeStatusSinceLastUpdate()).isEqualTo(1);
    assertThat(entry.getProbeElapsedTimeSinceLastUpdateMillis()).isEqualTo(500);
    assertThat(entry.getProbeMcsRateSinceLastUpdate()).isEqualTo(9);
    assertThat(entry.getRxLinkSpeedMbps()).isEqualTo(780);
    assertThat(entry.getCellularDataNetworkType()).isEqualTo(13);
    assertThat(entry.getCellularSignalStrengthDbm()).isEqualTo(-80);
    assertThat(entry.getCellularSignalStrengthDb()).isEqualTo(10);
    assertThat(entry.isSameRegisteredCell()).isTrue();

    if (RuntimeEnvironment.getApiLevel() >= S) {
      assertThat(entry.getTimeSliceDutyCycleInPercent()).isEqualTo(80);
      assertThat(entry.isCellularDataAvailable()).isTrue();
      assertThat(entry.isThroughputSufficient()).isTrue();
      assertThat(entry.isWifiScoringEnabled()).isTrue();
    }
  }

  @Test
  public void newBuilder_defaultValues() {
    WifiUsabilityStatsEntry entry = WifiUsabilityStatsEntryBuilder.newBuilder().build();

    assertThat(entry.getTimeStampMillis()).isEqualTo(0L);
    assertThat(entry.getRssi()).isEqualTo(WifiInfo.INVALID_RSSI);
    assertThat(entry.getLinkSpeedMbps()).isEqualTo(WifiInfo.LINK_SPEED_UNKNOWN);
    // ... all other fields should be their default values (usually 0 or false)
  }
}
