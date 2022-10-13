package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiSsid;
import java.net.InetAddress;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.util.reflector.WithType;

@Implements(WifiInfo.class)
public class ShadowWifiInfo {

  public static WifiInfo newInstance() {
    return ReflectionHelpers.callConstructor(WifiInfo.class);
  }

  @RealObject WifiInfo realObject;

  @Implementation
  public void setInetAddress(InetAddress address) {
    reflector(WifiInfoReflector.class, realObject).setInetAddress(address);
  }

  @Implementation
  public void setMacAddress(String newMacAddress) {
    reflector(WifiInfoReflector.class, realObject).setMacAddress(newMacAddress);
  }

  @Implementation(maxSdk = JELLY_BEAN)
  public void setSSID(String ssid) {
    if (RuntimeEnvironment.getApiLevel() <= JELLY_BEAN) {
      reflector(WifiInfoReflector.class, realObject).setSSID(ssid);
    } else {
      reflector(WifiInfoReflector.class, realObject).setSSID(getWifiSsid(ssid));
    }
  }

  private static Object getWifiSsid(String ssid) {
    WifiSsid wifiSsid;
    if (ssid.startsWith("0x")) {
      wifiSsid = reflector(WifiSsidReflector.class).createFromHex(ssid);
    } else {
      wifiSsid = reflector(WifiSsidReflector.class).createFromAsciiEncoded(ssid);
    }
    return wifiSsid;
  }

  @Implementation
  public void setBSSID(String bssid) {
    reflector(WifiInfoReflector.class, realObject).setBSSID(bssid);
  }

  @Implementation
  public void setSupplicantState(SupplicantState state) {
    reflector(WifiInfoReflector.class, realObject).setSupplicantState(state);
  }

  @Implementation
  public void setRssi(int rssi) {
    reflector(WifiInfoReflector.class, realObject).setRssi(rssi);
  }

  @Implementation
  public void setLinkSpeed(int linkSpeed) {
    reflector(WifiInfoReflector.class, realObject).setLinkSpeed(linkSpeed);
  }

  @Implementation(minSdk = LOLLIPOP)
  public void setFrequency(int frequency) {
    reflector(WifiInfoReflector.class, realObject).setFrequency(frequency);
  }

  @Implementation
  public void setNetworkId(int id) {
    reflector(WifiInfoReflector.class, realObject).setNetworkId(id);
  }

  @ForType(WifiInfo.class)
  interface WifiInfoReflector {

    @Direct
    void setInetAddress(InetAddress address);

    @Direct
    void setMacAddress(String newMacAddress);

    @Direct
    void setSSID(String ssid);

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

    @Direct
    void setFrequency(int frequency);
  }

  @ForType(WifiSsid.class)
  interface WifiSsidReflector {
    // pre-T
    @Static
    WifiSsid createFromHex(String hexStr);

    // pre-T
    @Static
    WifiSsid createFromAsciiEncoded(String asciiEncoded);
  }
}
