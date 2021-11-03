package org.robolectric.shadows;

import android.net.wifi.WifiUsabilityStatsEntry;
import android.net.wifi.WifiUsabilityStatsEntry.ContentionTimeStats;
import android.net.wifi.WifiUsabilityStatsEntry.RadioStats;
import android.net.wifi.WifiUsabilityStatsEntry.RateStats;
import android.os.Build.VERSION_CODES;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link WifiUsabilityStatsEntry}. */
public class WifiUsabilityStatsEntryBuilder {
  private long timeStampMillis;
  private int rssi;
  private int linkSpeedMbps;
  private long totalTxSuccess;
  private long totalTxRetries;
  private long totalTxBad;
  private long totalRxSuccess;
  private long totalRadioOnTimeMillis;
  private long totalRadioTxTimeMillis;
  private long totalRadioRxTimeMillis;
  private long totalScanTimeMillis;
  private long totalNanScanTimeMillis;
  private long totalBackgroundScanTimeMillis;
  private long totalRoamScanTimeMillis;
  private long totalPnoScanTimeMillis;
  private long totalHotspot2ScanTimeMillis;
  private long totalCcaBusyFreqTimeMillis;
  private long totalRadioOnFreqTimeMillis;
  private long totalBeaconRx;
  private int probeStatusSinceLastUpdate;
  private int probeElapsedTimeSinceLastUpdateMillis;
  private int probeMcsRateSinceLastUpdate;
  private int rxLinkSpeedMbps;
  private int timeSliceDutyCycleInPercent;
  private static final int CHANNEL_UTILIZATION_RATIO = 0;
  private boolean isThroughputSufficient = true;
  private boolean isWifiScoringEnabled = true;
  private boolean isCellularDataAvailable = true;
  private int cellularDataNetworkType;
  private int cellularSignalStrengthDbm;
  private int cellularSignalStrengthDb;
  private boolean isSameRegisteredCell;

  public WifiUsabilityStatsEntry build() {
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.R) {
      return ReflectionHelpers.callConstructor(
          WifiUsabilityStatsEntry.class,
          ClassParameter.from(long.class, timeStampMillis),
          ClassParameter.from(int.class, rssi),
          ClassParameter.from(int.class, linkSpeedMbps),
          ClassParameter.from(long.class, totalTxSuccess),
          ClassParameter.from(long.class, totalTxRetries),
          ClassParameter.from(long.class, totalTxBad),
          ClassParameter.from(long.class, totalRxSuccess),
          ClassParameter.from(long.class, totalRadioOnTimeMillis),
          ClassParameter.from(long.class, totalRadioTxTimeMillis),
          ClassParameter.from(long.class, totalRadioRxTimeMillis),
          ClassParameter.from(long.class, totalScanTimeMillis),
          ClassParameter.from(long.class, totalNanScanTimeMillis),
          ClassParameter.from(long.class, totalBackgroundScanTimeMillis),
          ClassParameter.from(long.class, totalRoamScanTimeMillis),
          ClassParameter.from(long.class, totalPnoScanTimeMillis),
          ClassParameter.from(long.class, totalHotspot2ScanTimeMillis),
          ClassParameter.from(long.class, totalCcaBusyFreqTimeMillis),
          ClassParameter.from(long.class, totalRadioOnFreqTimeMillis),
          ClassParameter.from(long.class, totalBeaconRx),
          ClassParameter.from(int.class, probeStatusSinceLastUpdate),
          ClassParameter.from(int.class, probeElapsedTimeSinceLastUpdateMillis),
          ClassParameter.from(int.class, probeMcsRateSinceLastUpdate),
          ClassParameter.from(int.class, rxLinkSpeedMbps),
          ClassParameter.from(int.class, cellularDataNetworkType),
          ClassParameter.from(int.class, cellularSignalStrengthDbm),
          ClassParameter.from(int.class, cellularSignalStrengthDb),
          ClassParameter.from(boolean.class, isSameRegisteredCell));
    } else {
      return new WifiUsabilityStatsEntry(
          timeStampMillis,
          rssi,
          linkSpeedMbps,
          totalTxSuccess,
          totalTxRetries,
          totalTxBad,
          totalRxSuccess,
          totalRadioOnTimeMillis,
          totalRadioTxTimeMillis,
          totalRadioRxTimeMillis,
          totalScanTimeMillis,
          totalNanScanTimeMillis,
          totalBackgroundScanTimeMillis,
          totalRoamScanTimeMillis,
          totalPnoScanTimeMillis,
          totalHotspot2ScanTimeMillis,
          totalCcaBusyFreqTimeMillis,
          totalRadioOnFreqTimeMillis,
          totalBeaconRx,
          probeStatusSinceLastUpdate,
          probeElapsedTimeSinceLastUpdateMillis,
          probeMcsRateSinceLastUpdate,
          rxLinkSpeedMbps,
          timeSliceDutyCycleInPercent,
          new ContentionTimeStats[] {},
          new RateStats[] {},
          new RadioStats[] {},
          CHANNEL_UTILIZATION_RATIO,
          isThroughputSufficient,
          isWifiScoringEnabled,
          isCellularDataAvailable,
          cellularDataNetworkType,
          cellularSignalStrengthDbm,
          cellularSignalStrengthDb,
          isSameRegisteredCell);
    }
  }

  public WifiUsabilityStatsEntryBuilder setTimeStampMillis(long timeStampMillis) {
    this.timeStampMillis = timeStampMillis;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setRssi(int rssi) {
    this.rssi = rssi;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setLinkSpeedMbps(int linkSpeedMbps) {
    this.linkSpeedMbps = linkSpeedMbps;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalTxSuccess(long totalTxSuccess) {
    this.totalTxSuccess = totalTxSuccess;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalTxRetries(long totalTxRetries) {
    this.totalTxRetries = totalTxRetries;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalTxBad(long totalTxBad) {
    this.totalTxBad = totalTxBad;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalRxSuccess(long totalRxSuccess) {
    this.totalRxSuccess = totalRxSuccess;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalRadioOnTimeMillis(long totalRadioOnTimeMillis) {
    this.totalRadioOnTimeMillis = totalRadioOnTimeMillis;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalRadioTxTimeMillis(long totalRadioTxTimeMillis) {
    this.totalRadioTxTimeMillis = totalRadioTxTimeMillis;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalRadioRxTimeMillis(long totalRadioRxTimeMillis) {
    this.totalRadioRxTimeMillis = totalRadioRxTimeMillis;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalScanTimeMillis(long totalScanTimeMillis) {
    this.totalScanTimeMillis = totalScanTimeMillis;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalNanScanTimeMillis(long totalNanScanTimeMillis) {
    this.totalNanScanTimeMillis = totalNanScanTimeMillis;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalBackgroundScanTimeMillis(
      long totalBackgroundScanTimeMillis) {
    this.totalBackgroundScanTimeMillis = totalBackgroundScanTimeMillis;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalRoamScanTimeMillis(long totalRoamScanTimeMillis) {
    this.totalRoamScanTimeMillis = totalRoamScanTimeMillis;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalPnoScanTimeMillis(long totalPnoScanTimeMillis) {
    this.totalPnoScanTimeMillis = totalPnoScanTimeMillis;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalHotspot2ScanTimeMillis(
      long totalHotspot2ScanTimeMillis) {
    this.totalHotspot2ScanTimeMillis = totalHotspot2ScanTimeMillis;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalCcaBusyFreqTimeMillis(
      long totalCcaBusyFreqTimeMillis) {
    this.totalCcaBusyFreqTimeMillis = totalCcaBusyFreqTimeMillis;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalRadioOnFreqTimeMillis(
      long totalRadioOnFreqTimeMillis) {
    this.totalRadioOnFreqTimeMillis = totalRadioOnFreqTimeMillis;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTotalBeaconRx(long totalBeaconRx) {
    this.totalBeaconRx = totalBeaconRx;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setProbeStatusSinceLastUpdate(
      int probeStatusSinceLastUpdate) {
    this.probeStatusSinceLastUpdate = probeStatusSinceLastUpdate;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setProbeElapsedTimeSinceLastUpdateMillis(
      int probeElapsedTimeSinceLastUpdateMillis) {
    this.probeElapsedTimeSinceLastUpdateMillis = probeElapsedTimeSinceLastUpdateMillis;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setProbeMcsRateSinceLastUpdate(
      int probeMcsRateSinceLastUpdate) {
    this.probeMcsRateSinceLastUpdate = probeMcsRateSinceLastUpdate;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setRxLinkSpeedMbps(int rxLinkSpeedMbps) {
    this.rxLinkSpeedMbps = rxLinkSpeedMbps;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setCellularDataNetworkType(int cellularDataNetworkType) {
    this.cellularDataNetworkType = cellularDataNetworkType;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setCellularSignalStrengthDbm(
      int cellularSignalStrengthDbm) {
    this.cellularSignalStrengthDbm = cellularSignalStrengthDbm;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setCellularSignalStrengthDb(int cellularSignalStrengthDb) {
    this.cellularSignalStrengthDb = cellularSignalStrengthDb;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setSameRegisteredCell(boolean sameRegisteredCell) {
    isSameRegisteredCell = sameRegisteredCell;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setTimeSliceDutyCycleInPercent(int percent) {
    this.timeSliceDutyCycleInPercent = percent;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setIsCellularDataAvailable(boolean avail) {
    this.isCellularDataAvailable = avail;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setIsThroughputSufficient(boolean sufficient) {
    this.isThroughputSufficient = sufficient;
    return this;
  }

  public WifiUsabilityStatsEntryBuilder setIsWifiScoringEnabled(boolean enabled) {
    this.isWifiScoringEnabled = enabled;
    return this;
  }
}
