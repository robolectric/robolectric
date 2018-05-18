package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiSsid;
import android.os.Build.VERSION_CODES;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(WifiInfo.class)
public class ShadowWifiInfo {

  @RealObject
  WifiInfo realObject;

  public void setMacAddress(String newMacAddress) {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setMacAddress(newMacAddress);
    } else {
      directlyOn(realObject, WifiInfo.class,
          "setMacAddress", ClassParameter.from(String.class, newMacAddress));
    }
  }

  public void setSSID(String ssid) {
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.JELLY_BEAN) {
      directlyOn(realObject, WifiInfo.class,
          "setSSID", ClassParameter.from(String.class, ssid));
    } else if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.KITKAT) {

      directlyOn(realObject, WifiInfo.class,
          "setSSID", ClassParameter.from(WifiSsid.class, getWifiSsid(ssid)));
    } else {
      directlyOn(realObject, WifiInfo.class).setSSID((WifiSsid) getWifiSsid(ssid));
    }
  }

  private static Object getWifiSsid(String ssid) {
    WifiSsid wifiSsid;
    if (ssid.startsWith("0x")) {
      wifiSsid = WifiSsid.createFromHex(ssid);
    } else {
      wifiSsid = WifiSsid.createFromAsciiEncoded(ssid);
    }
    return wifiSsid;
  }

  public void setBSSID(String bssid) {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setBSSID(bssid);
    } else {
      directlyOn(realObject, WifiInfo.class,
          "setBSSID", ClassParameter.from(String.class, bssid));
    }
  }

  public void setRssi(int rssi) {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setRssi(rssi);
    } else {
      directlyOn(realObject, WifiInfo.class,
          "setRssi", ClassParameter.from(int.class, rssi));
    }
  }

  public void setLinkSpeed(int linkSpeed) {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setLinkSpeed(linkSpeed);
    } else {
      directlyOn(realObject, WifiInfo.class,
          "setLinkSpeed", ClassParameter.from(int.class, linkSpeed));
    }
  }

  public void setFrequency(int frequency) {
    directlyOn(realObject, WifiInfo.class).setFrequency(frequency);
  }

  public void setNetworkId(int id) {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setNetworkId(id);
    } else {
      directlyOn(realObject, WifiInfo.class,
          "setNetworkId", ClassParameter.from(int.class, id));
    }
  }
}
