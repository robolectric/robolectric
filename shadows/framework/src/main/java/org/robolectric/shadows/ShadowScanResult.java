package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.net.wifi.ScanResult;
import android.os.Build;
import org.robolectric.shadow.api.Shadow;

public class ShadowScanResult {
  /**
   * @deprecated use ScanResult() instead
   */
  @Deprecated
  public static ScanResult newInstance(
      String SSID, String BSSID, String caps, int level, int frequency) {
    return newInstance(SSID, BSSID, caps, level, frequency, false);
  }

  /**
   * @deprecated use ScanResult() instead
   */
  @Deprecated
  public static ScanResult newInstance(
      String SSID,
      String BSSID,
      String caps,
      int level,
      int frequency,
      boolean is80211McRTTResponder) {
    ScanResult scanResult;
    if (Build.VERSION.SDK_INT >= 30) {
      // ScanResult() was introduced as public API in 30
      scanResult = new ScanResult();
    } else {
      scanResult = Shadow.newInstanceOf(ScanResult.class);
    }
    scanResult.SSID = SSID;
    scanResult.BSSID = BSSID;
    scanResult.capabilities = caps;
    scanResult.level = level;
    scanResult.frequency = frequency;
    if (Build.VERSION.SDK_INT >= P) {
      if (is80211McRTTResponder) {
        scanResult.setFlag(ScanResult.FLAG_80211mc_RESPONDER);
      } else {
      scanResult.setFlag(0);
      }
    }
    return scanResult;
  }
}
