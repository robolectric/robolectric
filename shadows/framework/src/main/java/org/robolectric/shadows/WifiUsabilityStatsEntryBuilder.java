package org.robolectric.shadows;

import android.net.wifi.WifiUsabilityStatsEntry;

/** Builder for {@link WifiUsabilityStatsEntry}. */
public class WifiUsabilityStatsEntryBuilder {
  protected long timeStampMillis;
  protected int rssi;
  protected int linkSpeedMbps;
  protected long totalTxSuccess;
  protected long totalTxRetries;
  protected long totalTxBad;
  protected long totalRxSuccess;
  protected long totalRadioOnTimeMillis;
  protected long totalRadioTxTimeMillis;
  protected long totalRadioRxTimeMillis;
  protected long totalScanTimeMillis;
  protected long totalNanScanTimeMillis;
  protected long totalBackgroundScanTimeMillis;
  protected long totalRoamScanTimeMillis;
  protected long totalPnoScanTimeMillis;
  protected long totalHotspot2ScanTimeMillis;
  protected long totalCcaBusyFreqTimeMillis;
  protected long totalRadioOnFreqTimeMillis;
  protected long totalBeaconRx;
  protected int probeStatusSinceLastUpdate;
  protected int probeElapsedTimeSinceLastUpdateMillis;
  protected int probeMcsRateSinceLastUpdate;
  protected int rxLinkSpeedMbps;
  protected int cellularDataNetworkType;
  protected int cellularSignalStrengthDbm;
  protected int cellularSignalStrengthDb;
  protected boolean isSameRegisteredCell;

  public WifiUsabilityStatsEntry build() {
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
        cellularDataNetworkType,
        cellularSignalStrengthDbm,
        cellularSignalStrengthDb,
        isSameRegisteredCell);
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
}
