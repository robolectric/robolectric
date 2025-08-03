package org.robolectric.shadows;

import static org.robolectric.RuntimeEnvironment.getApiLevel;

import android.net.wifi.WifiUsabilityStatsEntry;
import android.net.wifi.WifiUsabilityStatsEntry.ContentionTimeStats;
import android.net.wifi.WifiUsabilityStatsEntry.RadioStats;
import android.net.wifi.WifiUsabilityStatsEntry.RateStats;
import android.os.Build.VERSION_CODES;
import android.util.SparseArray;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.versioning.AndroidVersions.Baklava;

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
    if (getApiLevel() <= VERSION_CODES.R) {
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
    } else if (getApiLevel() <= VERSION_CODES.TIRAMISU) {
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
          ClassParameter.from(int.class, timeSliceDutyCycleInPercent),
          ClassParameter.from(ContentionTimeStats[].class, new ContentionTimeStats[] {}),
          ClassParameter.from(RateStats[].class, new RateStats[] {}),
          ClassParameter.from(RadioStats[].class, new RadioStats[] {}),
          ClassParameter.from(int.class, CHANNEL_UTILIZATION_RATIO),
          ClassParameter.from(boolean.class, isThroughputSufficient),
          ClassParameter.from(boolean.class, isWifiScoringEnabled),
          ClassParameter.from(boolean.class, isCellularDataAvailable),
          ClassParameter.from(int.class, cellularDataNetworkType),
          ClassParameter.from(int.class, cellularSignalStrengthDbm),
          ClassParameter.from(int.class, cellularSignalStrengthDb),
          ClassParameter.from(boolean.class, isSameRegisteredCell));
    } else if (getApiLevel() <= VERSION_CODES.VANILLA_ICE_CREAM) {
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
          ClassParameter.from(int.class, timeSliceDutyCycleInPercent),
          ClassParameter.from(ContentionTimeStats[].class, new ContentionTimeStats[] {}),
          ClassParameter.from(RateStats[].class, new RateStats[] {}),
          ClassParameter.from(RadioStats[].class, new RadioStats[] {}),
          ClassParameter.from(int.class, CHANNEL_UTILIZATION_RATIO),
          ClassParameter.from(boolean.class, isThroughputSufficient),
          ClassParameter.from(boolean.class, isWifiScoringEnabled),
          ClassParameter.from(boolean.class, isCellularDataAvailable),
          ClassParameter.from(int.class, cellularDataNetworkType),
          ClassParameter.from(int.class, cellularSignalStrengthDbm),
          ClassParameter.from(int.class, cellularSignalStrengthDb),
          ClassParameter.from(boolean.class, isSameRegisteredCell),
          ClassParameter.from(SparseArray.class, new SparseArray<>())); // new in V
    } else if (getApiLevel() <= Baklava.SDK_INT) {
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
          ClassParameter.from(int.class, timeSliceDutyCycleInPercent),
          ClassParameter.from(ContentionTimeStats[].class, new ContentionTimeStats[] {}),
          ClassParameter.from(RateStats[].class, new RateStats[] {}),
          ClassParameter.from(RadioStats[].class, new RadioStats[] {}),
          ClassParameter.from(int.class, CHANNEL_UTILIZATION_RATIO),
          ClassParameter.from(boolean.class, isThroughputSufficient),
          ClassParameter.from(boolean.class, isWifiScoringEnabled),
          ClassParameter.from(boolean.class, isCellularDataAvailable),
          ClassParameter.from(int.class, cellularDataNetworkType),
          ClassParameter.from(int.class, cellularSignalStrengthDbm),
          ClassParameter.from(int.class, cellularSignalStrengthDb),
          ClassParameter.from(boolean.class, isSameRegisteredCell),
          ClassParameter.from(SparseArray.class, new SparseArray<>()), // linkStats
          /* new in B */
          ClassParameter.from(int.class, 0), // wifiLinkCount
          ClassParameter.from(int.class, 0), // mloMode
          ClassParameter.from(long.class, 0), // txTransmittedBytes
          ClassParameter.from(long.class, 0), // rxTransmittedBytes
          ClassParameter.from(int.class, 0), // labelBadEventCount
          ClassParameter.from(int.class, 0), // wifiFrameworkState
          ClassParameter.from(int.class, 0), // isNetworkCapabilitiesDownstreamSufficient
          ClassParameter.from(int.class, 0), // isNetworkCapabilitiesUpstreamSufficient
          ClassParameter.from(int.class, 0), // isThroughputPredictorDownstreamSufficient
          ClassParameter.from(int.class, 0), // isThroughputPredictorUpstreamSufficient
          ClassParameter.from(boolean.class, false), // isBluetoothConnected
          ClassParameter.from(int.class, 0), // uwbAdapterState
          ClassParameter.from(boolean.class, false), // isLowLatencyActivated
          ClassParameter.from(int.class, 0), // maxSupportedTxLinkSpeed
          ClassParameter.from(int.class, 0), // maxSupportedRxLinkSpeed
          ClassParameter.from(int.class, 0), // voipMode
          ClassParameter.from(int.class, 0), // threadDeviceRole
          ClassParameter.from(int.class, 0) // statusDataStall
          /* end new in B */
          );
    } else {
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
          ClassParameter.from(int.class, timeSliceDutyCycleInPercent),
          ClassParameter.from(ContentionTimeStats[].class, new ContentionTimeStats[] {}),
          ClassParameter.from(RateStats[].class, new RateStats[] {}),
          ClassParameter.from(RadioStats[].class, new RadioStats[] {}),
          ClassParameter.from(int.class, CHANNEL_UTILIZATION_RATIO),
          ClassParameter.from(boolean.class, isThroughputSufficient),
          ClassParameter.from(boolean.class, isWifiScoringEnabled),
          ClassParameter.from(boolean.class, isCellularDataAvailable),
          ClassParameter.from(int.class, cellularDataNetworkType),
          ClassParameter.from(int.class, cellularSignalStrengthDbm),
          ClassParameter.from(int.class, cellularSignalStrengthDb),
          ClassParameter.from(boolean.class, isSameRegisteredCell),
          ClassParameter.from(SparseArray.class, new SparseArray<>()),
          ClassParameter.from(int.class, 0), // wifiLinkCount
          ClassParameter.from(int.class, 0), // mloMode
          ClassParameter.from(long.class, 0), // txTransmittedBytes
          ClassParameter.from(long.class, 0), // rxTransmittedBytes
          ClassParameter.from(int.class, 0), // labelBadEventCount
          ClassParameter.from(int.class, 0), // wifiFrameworkState
          ClassParameter.from(int.class, 0), // isNetworkCapabilitiesDownstreamSufficient
          ClassParameter.from(int.class, 0), // isNetworkCapabilitiesUpstreamSufficient
          ClassParameter.from(int.class, 0), // isThroughputPredictorDownstreamSufficient
          ClassParameter.from(int.class, 0), // isThroughputPredictorUpstreamSufficient
          ClassParameter.from(boolean.class, false), // isBluetoothConnected
          ClassParameter.from(int.class, 0), // uwbAdapterState
          ClassParameter.from(boolean.class, false), // isLowLatencyActivated
          ClassParameter.from(int.class, 0), // maxSupportedTxLinkSpeed
          ClassParameter.from(int.class, 0), // maxSupportedRxLinkSpeed
          ClassParameter.from(int.class, 0), // voipMode
          ClassParameter.from(int.class, 0), // threadDeviceRole
          ClassParameter.from(int.class, 0), // statusDataStall
          /* new in post B */
          ClassParameter.from(int.class, 0),
          ClassParameter.from(int.class, 0)
          /* end new in post B */
          );
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
