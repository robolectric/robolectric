package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.shadow.api.Shadow.extract;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ScanData;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.N)
public class ShadowWifiScannerTest {
  private static final ImmutableList<ScanResult> SCAN_RESULTS = createFakeScanResults();

  /** Verifies that WifiScanner and its shadow are available without throwing exceptions. */
  @Test
  public void init() {
    WifiScanner scanner =
        (WifiScanner)
            RuntimeEnvironment.getApplication().getSystemService(Context.WIFI_SCANNING_SERVICE);
    assertNotNull(scanner);

    ShadowWifiScanner shadowWifiScanner = extract(scanner);
    assertNotNull(shadowWifiScanner);
  }

  /**
   * Verifies that a listener registered via WifiScanner#registerScanListener receives results when
   * extract(scanner).setScanResults is called.
   *
   * <p>WifiScanner#registerScanListener was added in Nougat.
   */
  @Test
  @Config(minSdk = VERSION_CODES.N_MR1, maxSdk = VERSION_CODES.Q)
  public void setScanResults_invokesListeners() {
    ScanDataListener listener = new ScanDataListener();

    WifiScanner scanner =
        (WifiScanner)
            RuntimeEnvironment.getApplication().getSystemService(Context.WIFI_SCANNING_SERVICE);

    scanner.registerScanListener(listener);
    ((ShadowWifiScanner) extract(scanner)).setScanResults(SCAN_RESULTS);

    ScanData[] scanData = listener.scanData;
    assertThat(scanData).hasLength(1);
    assertThat(Arrays.asList(scanData[0].getResults())).containsExactlyElementsIn(SCAN_RESULTS);
  }

  /**
   * Verifies that a listener registered via WifiScanner#registerScanListener receives results when
   * extract(scanner).setScanResults is called.
   *
   * <p>Specifying an executor is only supported in R+.
   */
  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void setScanResultsR_invokesListeners() {
    ScanDataListener listener = new ScanDataListener();

    WifiScanner scanner =
        (WifiScanner)
            RuntimeEnvironment.getApplication().getSystemService(Context.WIFI_SCANNING_SERVICE);

    scanner.registerScanListener(MoreExecutors.directExecutor(), listener);
    ((ShadowWifiScanner) extract(scanner)).setScanResults(SCAN_RESULTS);

    ScanData[] scanData = listener.scanData;
    assertThat(scanData).hasLength(1);
    assertThat(Arrays.asList(scanData[0].getResults())).containsExactlyElementsIn(SCAN_RESULTS);
  }

  /**
   * Verifies that WifiScanner#getSingleScanResults returns the latest scan results.
   *
   * <p>WifiScanner#getSingleScanResults was added in R.
   */
  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void getSingleScanResults_returnsResults() {
    WifiScanner scanner =
        (WifiScanner)
            RuntimeEnvironment.getApplication().getSystemService(Context.WIFI_SCANNING_SERVICE);

    ((ShadowWifiScanner) extract(scanner)).setScanResults(SCAN_RESULTS);

    assertThat(scanner.getSingleScanResults()).containsExactlyElementsIn(SCAN_RESULTS);
  }

  private static ImmutableList<ScanResult> createFakeScanResults() {
    ScanResult scanResult = null;

    if (Build.VERSION.SDK_INT >= VERSION_CODES.R) {
      // informationElements added in R
      ScanResult.InformationElement mobileInformationElement =
          InformationElementBuilder.newBuilder()
              .setId(107) // EID_INTERWORKING
              .setBytes(new byte[] {1, 2, 3})
              .build();
      ImmutableList<ScanResult.InformationElement> informationElements =
          ImmutableList.of(mobileInformationElement);
      scanResult =
          ShadowScanResult.newInstance(
              "SSID",
              "BSSID",
              "Some capabilities",
              /* level= */ -57,
              /* frequency= */ 5,
              /* is80211McRttResponder= */ true,
              informationElements);
    } else {
      scanResult =
          ShadowScanResult.newInstance(
              "SSID",
              "BSSID",
              "Some capabilities",
              /* level= */ -57,
              /* frequency= */ 5,
              /* is80211McRTTResponder= */ true);
    }

    scanResult.timestamp = 1000L;

    return ImmutableList.of(scanResult);
  }

  private static class ScanDataListener implements WifiScanner.ScanListener {
    public ScanData[] scanData = null;

    @Override
    public void onSuccess() {}

    @Override
    public void onFailure(int reason, String description) {}

    @Override
    public void onPeriodChanged(int periodInMs) {}

    @Override
    public void onResults(ScanData[] results) {
      scanData = results;
    }

    @Override
    public void onFullResult(ScanResult fullScanResult) {}
  }
}
