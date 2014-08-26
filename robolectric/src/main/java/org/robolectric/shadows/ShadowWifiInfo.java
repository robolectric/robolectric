package org.robolectric.shadows;

import android.net.wifi.WifiInfo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.HiddenApi;

@Implements(WifiInfo.class)
public class ShadowWifiInfo {
  public static void __staticInitializer__() {
  }

  private String macAddress;

  @Implementation
  public String getMacAddress() {
    return macAddress;
  }

  @HiddenApi @Implementation
  public void setMacAddress(String newMacAddress) {
    macAddress = newMacAddress;
  }
}
