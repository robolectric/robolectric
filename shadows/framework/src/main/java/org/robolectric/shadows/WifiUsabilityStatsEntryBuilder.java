package org.robolectric.shadows;

import static org.robolectric.RuntimeEnvironment.getApiLevel;

import android.net.wifi.WifiUsabilityStatsEntry;
import org.robolectric.versioning.AndroidVersions.Baklava;

/** A Builder for {@link WifiUsabilityStatsEntry} */
public interface WifiUsabilityStatsEntryBuilder {

  WifiUsabilityStatsEntry build();

  WifiUsabilityStatsEntryBuilder setTimeStampMillis(long timeStampMillis);

  WifiUsabilityStatsEntryBuilder setRssi(int rssi);

  WifiUsabilityStatsEntryBuilder setLinkSpeedMbps(int linkSpeedMbps);

  WifiUsabilityStatsEntryBuilder setTotalTxSuccess(long totalTxSuccess);

  WifiUsabilityStatsEntryBuilder setTotalTxRetries(long totalTxRetries);

  WifiUsabilityStatsEntryBuilder setTotalTxBad(long totalTxBad);

  WifiUsabilityStatsEntryBuilder setTotalRxSuccess(long totalRxSuccess);

  WifiUsabilityStatsEntryBuilder setTotalRadioOnTimeMillis(long totalRadioOnTimeMillis);

  WifiUsabilityStatsEntryBuilder setTotalRadioTxTimeMillis(long totalRadioTxTimeMillis);

  WifiUsabilityStatsEntryBuilder setTotalRadioRxTimeMillis(long totalRadioRxTimeMillis);

  WifiUsabilityStatsEntryBuilder setTotalScanTimeMillis(long totalScanTimeMillis);

  WifiUsabilityStatsEntryBuilder setTotalNanScanTimeMillis(long totalNanScanTimeMillis);

  WifiUsabilityStatsEntryBuilder setTotalBackgroundScanTimeMillis(
      long totalBackgroundScanTimeMillis);

  WifiUsabilityStatsEntryBuilder setTotalRoamScanTimeMillis(long totalRoamScanTimeMillis);

  WifiUsabilityStatsEntryBuilder setTotalPnoScanTimeMillis(long totalPnoScanTimeMillis);

  WifiUsabilityStatsEntryBuilder setTotalHotspot2ScanTimeMillis(long totalHotspot2ScanTimeMillis);

  WifiUsabilityStatsEntryBuilder setTotalCcaBusyFreqTimeMillis(long totalCcaBusyFreqTimeMillis);

  WifiUsabilityStatsEntryBuilder setTotalRadioOnFreqTimeMillis(long totalRadioOnFreqTimeMillis);

  WifiUsabilityStatsEntryBuilder setTotalBeaconRx(long totalBeaconRx);

  WifiUsabilityStatsEntryBuilder setProbeStatusSinceLastUpdate(int probeStatusSinceLastUpdate);

  WifiUsabilityStatsEntryBuilder setProbeElapsedTimeSinceLastUpdateMillis(
      int probeElapsedTimeSinceLastUpdateMillis);

  WifiUsabilityStatsEntryBuilder setProbeMcsRateSinceLastUpdate(int probeMcsRateSinceLastUpdate);

  WifiUsabilityStatsEntryBuilder setRxLinkSpeedMbps(int rxLinkSpeedMbps);

  WifiUsabilityStatsEntryBuilder setCellularDataNetworkType(int cellularDataNetworkType);

  WifiUsabilityStatsEntryBuilder setCellularSignalStrengthDbm(int cellularSignalStrengthDbm);

  WifiUsabilityStatsEntryBuilder setCellularSignalStrengthDb(int cellularSignalStrengthDb);

  /**
   * @deprecated Use {@link #setIsSameRegisteredCell(boolean)} instead.
   */
  @Deprecated
  default WifiUsabilityStatsEntryBuilder setSameRegisteredCell(boolean sameRegisteredCell) {
    return setIsSameRegisteredCell(sameRegisteredCell);
  }

  WifiUsabilityStatsEntryBuilder setIsSameRegisteredCell(boolean sameRegisteredCell);

  WifiUsabilityStatsEntryBuilder setTimeSliceDutyCycleInPercent(int percent);

  WifiUsabilityStatsEntryBuilder setIsCellularDataAvailable(boolean avail);

  WifiUsabilityStatsEntryBuilder setIsThroughputSufficient(boolean sufficient);

  WifiUsabilityStatsEntryBuilder setIsWifiScoringEnabled(boolean enabled);

  static WifiUsabilityStatsEntryBuilder newBuilder() {
    if (getApiLevel() <= Baklava.SDK_INT) {
      return new WifiUsabilityStatsEntryBuilderLegacyImpl();
    } else {
      // For newer APIs, delegate to the framework's WifiUsabilityStatsEntry.Builder
      return new WifiUsabilityStatsEntryBuilderImpl();
    }
  }
}
