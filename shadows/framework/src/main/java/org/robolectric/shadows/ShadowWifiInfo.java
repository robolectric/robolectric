package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiSsid;
import java.net.InetAddress;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(WifiInfo.class)
public class ShadowWifiInfo {

  public static WifiInfo newInstance() {
    return ReflectionHelpers.callConstructor(WifiInfo.class);
  }

  @RealObject
  WifiInfo realObject;

  public void setInetAddress(InetAddress address) {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setInetAddress(address);
    } else {
      directlyOn(
          realObject,
          WifiInfo.class,
          "setInetAddress",
          ClassParameter.from(InetAddress.class, address));
    }
  }

  public void setMacAddress(String newMacAddress) {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setMacAddress(newMacAddress);
    } else {
      directlyOn(realObject, WifiInfo.class,
          "setMacAddress", ClassParameter.from(String.class, newMacAddress));
    }
  }

  public void setSSID(String ssid) {
    if (RuntimeEnvironment.getApiLevel() <= JELLY_BEAN) {
      directlyOn(realObject, WifiInfo.class,
          "setSSID", ClassParameter.from(String.class, ssid));
    } else if (RuntimeEnvironment.getApiLevel() <= KITKAT) {

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
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setBSSID(bssid);
    } else {
      directlyOn(realObject, WifiInfo.class,
          "setBSSID", ClassParameter.from(String.class, bssid));
    }
  }

  public void setRssi(int rssi) {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setRssi(rssi);
    } else {
      directlyOn(realObject, WifiInfo.class,
          "setRssi", ClassParameter.from(int.class, rssi));
    }
  }

  public void setLinkSpeed(int linkSpeed) {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
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
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setNetworkId(id);
    } else {
      directlyOn(realObject, WifiInfo.class,
          "setNetworkId", ClassParameter.from(int.class, id));
    }
  }
}
