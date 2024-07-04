package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.net.wifi.ScanResult;
import android.os.Build;
import java.util.List;
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
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        scanResult.informationElements = new ScanResult.InformationElement[0];
      }
    }
    return scanResult;
  }

  public static ScanResult newInstance(
      String ssid,
      String bssid,
      String caps,
      int level,
      int frequency,
      boolean is80211McRttResponder,
      List<ScanResult.InformationElement> informationElements) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      ScanResult scanResult = new ScanResult();
      scanResult.SSID = ssid;
      scanResult.BSSID = bssid;
      scanResult.capabilities = caps;
      scanResult.level = level;
      scanResult.frequency = frequency;
      scanResult.informationElements =
          informationElements.toArray(new ScanResult.InformationElement[0]);
      if (is80211McRttResponder) {
        scanResult.setFlag(ScanResult.FLAG_80211mc_RESPONDER);
      } else {
        scanResult.setFlag(0);
      }

      return scanResult;
    } else {
      throw new UnsupportedOperationException(
          "InformationElement not available on API " + Build.VERSION.SDK_INT);
    }
  }
}
