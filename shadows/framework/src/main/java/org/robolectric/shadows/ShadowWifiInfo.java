package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiSsid;
import java.net.InetAddress;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

@Implements(WifiInfo.class)
public class ShadowWifiInfo {

  public static WifiInfo newInstance() {
    return ReflectionHelpers.callConstructor(WifiInfo.class);
  }

  @RealObject WifiInfo realObject;

  public void setInetAddress(InetAddress address) {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setInetAddress(address);
    } else {
      reflector(WifiInfoReflector.class, realObject).setInetAddress(address);
    }
  }

  public void setMacAddress(String newMacAddress) {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setMacAddress(newMacAddress);
    } else {
      reflector(WifiInfoReflector.class, realObject).setMacAddress(newMacAddress);
    }
  }

  public void setSSID(String ssid) {
    if (RuntimeEnvironment.getApiLevel() <= JELLY_BEAN) {
      reflector(WifiInfoReflector.class, realObject).setSSID(ssid);
    } else if (RuntimeEnvironment.getApiLevel() <= KITKAT) {

      reflector(WifiInfoReflector.class, realObject).setSSID(getWifiSsid(ssid));
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
      reflector(WifiInfoReflector.class, realObject).setBSSID(bssid);
    }
  }

  public void setSupplicantState(SupplicantState state) {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setSupplicantState(state);
    } else {
      reflector(WifiInfoReflector.class, realObject).setSupplicantState(state);
    }
  }

  public void setRssi(int rssi) {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setRssi(rssi);
    } else {
      reflector(WifiInfoReflector.class, realObject).setRssi(rssi);
    }
  }

  public void setLinkSpeed(int linkSpeed) {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setLinkSpeed(linkSpeed);
    } else {
      reflector(WifiInfoReflector.class, realObject).setLinkSpeed(linkSpeed);
    }
  }

  public void setFrequency(int frequency) {
    directlyOn(realObject, WifiInfo.class).setFrequency(frequency);
  }

  public void setNetworkId(int id) {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      directlyOn(realObject, WifiInfo.class).setNetworkId(id);
    } else {
      reflector(WifiInfoReflector.class, realObject).setNetworkId(id);
    }
  }

  @ForType(WifiInfo.class)
  interface WifiInfoReflector {

    @Direct
    void setInetAddress(InetAddress address);

    @Direct
    void setMacAddress(String newMacAddress);

    @Direct
    void setSSID(String ssid);

    @Direct
    void setSSID(@WithType("android.net.wifi.WifiSsid") Object ssid);

    @Direct
    void setBSSID(String bssid);

    @Direct
    void setSupplicantState(SupplicantState state);

    @Direct
    void setRssi(int rssi);

    @Direct
    void setLinkSpeed(int linkSpeed);

    @Direct
    void setNetworkId(int id);
  }
}
