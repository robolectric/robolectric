package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.net.wifi.ScanResult;
import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@Implements(ScanResult.class)
public class ShadowScanResult {

  @RealObject ScanResult realObject;

  public static ScanResult newInstance(String SSID, String BSSID, String caps, int level, int frequency) {
    return newInstance(SSID, BSSID, caps, level, frequency, false);
  }

  public static ScanResult newInstance(String SSID, String BSSID, String caps, int level, int frequency, boolean is80211McRTTResponder) {
    ScanResult scanResult = Shadow.newInstanceOf(ScanResult.class);
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

  @Override @Implementation
  public String toString() {
    StringBuilder sb = new StringBuilder()
        .append("SSID: ").append(valueOrNone(realObject.SSID))
        .append(", BSSID: ").append(valueOrNone(realObject.BSSID))
        .append(", capabilities: ").append(valueOrNone(realObject.capabilities))
        .append(", level: ").append(realObject.level)
        .append(", frequency: ").append(realObject.frequency);
    if (Build.VERSION.SDK_INT >= P) {
      sb.append(", flags: ").append(realObject.flags);
    }
    return sb.toString();
  }

  private String valueOrNone(String value) {
    return value == null ? "<none>" : value;
  }
}
