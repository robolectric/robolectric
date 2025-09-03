package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.wifi.WifiUsabilityStatsEntry;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/**
 * Implementation of {@link WifiUsabilityStatsEntryBuilder} that delegates to {@link
 * android.net.wifi.WifiUsabilityStatsEntry.Builder}.
 */
final class WifiUsabilityStatsEntryBuilderImpl implements WifiUsabilityStatsEntryBuilder {

  // TODO: replace with direct reference once compiling against post-Baklava
  private final WifiUsabilityStatsEntryBuilderReflector delegate;

  public WifiUsabilityStatsEntryBuilderImpl() {
    Object builderObj = reflector(WifiUsabilityStatsEntryBuilderReflector.class).newInstance();
    this.delegate = reflector(WifiUsabilityStatsEntryBuilderReflector.class, builderObj);
  }

  @Override
  public WifiUsabilityStatsEntry build() {
    return delegate.build();
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTimeStampMillis(long timeStampMillis) {
    delegate.setTimeStampMillis(timeStampMillis);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setRssi(int rssi) {
    delegate.setRssi(rssi);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setLinkSpeedMbps(int linkSpeedMbps) {
    delegate.setLinkSpeedMbps(linkSpeedMbps);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalTxSuccess(long totalTxSuccess) {
    delegate.setTotalTxSuccess(totalTxSuccess);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalTxRetries(long totalTxRetries) {
    delegate.setTotalTxRetries(totalTxRetries);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalTxBad(long totalTxBad) {
    delegate.setTotalTxBad(totalTxBad);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalRxSuccess(long totalRxSuccess) {
    delegate.setTotalRxSuccess(totalRxSuccess);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalRadioOnTimeMillis(long totalRadioOnTimeMillis) {
    delegate.setTotalRadioOnTimeMillis(totalRadioOnTimeMillis);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalRadioTxTimeMillis(long totalRadioTxTimeMillis) {
    delegate.setTotalRadioTxTimeMillis(totalRadioTxTimeMillis);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalRadioRxTimeMillis(long totalRadioRxTimeMillis) {
    delegate.setTotalRadioRxTimeMillis(totalRadioRxTimeMillis);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalScanTimeMillis(long totalScanTimeMillis) {
    delegate.setTotalScanTimeMillis(totalScanTimeMillis);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalNanScanTimeMillis(long totalNanScanTimeMillis) {
    delegate.setTotalNanScanTimeMillis(totalNanScanTimeMillis);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalBackgroundScanTimeMillis(
      long totalBackgroundScanTimeMillis) {
    delegate.setTotalBackgroundScanTimeMillis(totalBackgroundScanTimeMillis);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalRoamScanTimeMillis(long totalRoamScanTimeMillis) {
    delegate.setTotalRoamScanTimeMillis(totalRoamScanTimeMillis);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalPnoScanTimeMillis(long totalPnoScanTimeMillis) {
    delegate.setTotalPnoScanTimeMillis(totalPnoScanTimeMillis);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalHotspot2ScanTimeMillis(
      long totalHotspot2ScanTimeMillis) {
    delegate.setTotalHotspot2ScanTimeMillis(totalHotspot2ScanTimeMillis);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalCcaBusyFreqTimeMillis(
      long totalCcaBusyFreqTimeMillis) {
    delegate.setTotalCcaBusyFreqTimeMillis(totalCcaBusyFreqTimeMillis);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalRadioOnFreqTimeMillis(
      long totalRadioOnFreqTimeMillis) {
    delegate.setTotalRadioOnFreqTimeMillis(totalRadioOnFreqTimeMillis);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTotalBeaconRx(long totalBeaconRx) {
    delegate.setTotalBeaconRx(totalBeaconRx);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setProbeStatusSinceLastUpdate(
      int probeStatusSinceLastUpdate) {
    delegate.setProbeStatusSinceLastUpdate(probeStatusSinceLastUpdate);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setProbeElapsedTimeSinceLastUpdateMillis(
      int probeElapsedTimeSinceLastUpdateMillis) {
    delegate.setProbeElapsedTimeSinceLastUpdateMillis(probeElapsedTimeSinceLastUpdateMillis);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setProbeMcsRateSinceLastUpdate(
      int probeMcsRateSinceLastUpdate) {
    delegate.setProbeMcsRateSinceLastUpdate(probeMcsRateSinceLastUpdate);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setRxLinkSpeedMbps(int rxLinkSpeedMbps) {
    delegate.setRxLinkSpeedMbps(rxLinkSpeedMbps);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setCellularDataNetworkType(int cellularDataNetworkType) {
    delegate.setCellularDataNetworkType(cellularDataNetworkType);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setCellularSignalStrengthDbm(
      int cellularSignalStrengthDbm) {
    delegate.setCellularSignalStrengthDbm(cellularSignalStrengthDbm);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setCellularSignalStrengthDb(int cellularSignalStrengthDb) {
    delegate.setCellularSignalStrengthDb(cellularSignalStrengthDb);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setIsSameRegisteredCell(boolean sameRegisteredCell) {
    delegate.setIsSameRegisteredCell(sameRegisteredCell);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setTimeSliceDutyCycleInPercent(int percent) {
    delegate.setTimeSliceDutyCycleInPercent(percent);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setIsCellularDataAvailable(boolean avail) {
    delegate.setIsCellularDataAvailable(avail);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setIsThroughputSufficient(boolean sufficient) {
    delegate.setIsThroughputSufficient(sufficient);
    return this;
  }

  @Override
  public WifiUsabilityStatsEntryBuilder setIsWifiScoringEnabled(boolean enabled) {
    delegate.setIsWifiScoringEnabled(enabled);
    return this;
  }

  @ForType(className = "android.net.wifi.WifiUsabilityStatsEntry$Builder")
  private interface WifiUsabilityStatsEntryBuilderReflector {
    WifiUsabilityStatsEntry build();

    @Constructor
    Object newInstance();

    Object setTimeStampMillis(long timeStampMillis);

    Object setRssi(int rssi);

    Object setLinkSpeedMbps(int linkSpeedMbps);

    Object setTotalTxSuccess(long totalTxSuccess);

    Object setTotalTxRetries(long totalTxRetries);

    Object setTotalTxBad(long totalTxBad);

    Object setTotalRxSuccess(long totalRxSuccess);

    Object setTotalRadioOnTimeMillis(long totalRadioOnTimeMillis);

    Object setTotalRadioTxTimeMillis(long totalRadioTxTimeMillis);

    Object setTotalRadioRxTimeMillis(long totalRadioRxTimeMillis);

    Object setTotalScanTimeMillis(long totalScanTimeMillis);

    Object setTotalNanScanTimeMillis(long totalNanScanTimeMillis);

    Object setTotalBackgroundScanTimeMillis(long totalBackgroundScanTimeMillis);

    Object setTotalRoamScanTimeMillis(long totalRoamScanTimeMillis);

    Object setTotalPnoScanTimeMillis(long totalPnoScanTimeMillis);

    Object setTotalHotspot2ScanTimeMillis(long totalHotspot2ScanTimeMillis);

    Object setTotalCcaBusyFreqTimeMillis(long totalCcaBusyFreqTimeMillis);

    Object setTotalRadioOnFreqTimeMillis(long totalRadioOnFreqTimeMillis);

    Object setTotalBeaconRx(long totalBeaconRx);

    Object setProbeStatusSinceLastUpdate(int probeStatusSinceLastUpdate);

    Object setProbeElapsedTimeSinceLastUpdateMillis(int probeElapsedTimeSinceLastUpdateMillis);

    Object setProbeMcsRateSinceLastUpdate(int probeMcsRateSinceLastUpdate);

    Object setRxLinkSpeedMbps(int rxLinkSpeedMbps);

    Object setCellularDataNetworkType(int cellularDataNetworkType);

    Object setCellularSignalStrengthDbm(int cellularSignalStrengthDbm);

    Object setCellularSignalStrengthDb(int cellularSignalStrengthDb);

    Object setIsSameRegisteredCell(boolean sameRegisteredCell);

    Object setTimeSliceDutyCycleInPercent(int percent);

    Object setIsCellularDataAvailable(boolean avail);

    Object setIsThroughputSufficient(boolean sufficient);

    Object setIsWifiScoringEnabled(boolean enabled);
  }
}
