package org.robolectric.shadows;

import static org.robolectric.internal.Shadow.directlyOn;

import android.net.wifi.WifiInfo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Shadow for {@link android.net.wifi.WifiInfo}.
 */
@Implements(WifiInfo.class)
public class ShadowWifiInfo {
  @RealObject private WifiInfo realObject;

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

  @HiddenApi @Implementation
  public void setRssi(int rssi) {
    directlyOn(realObject, WifiInfo.class, "setRssi", ClassParameter.from(int.class, rssi));
  }
}
