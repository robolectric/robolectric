package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.net.wifi.WifiInfo;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(WifiInfo.class)
public class ShadowWifiInfo {
  @Implementation
  public static void __staticInitializer__() {}

  private String macAddress = "02:00:00:00:00:00"; // WifiInfo.DEFAULT_MAC_ADDRESS (@hide)
  private String ssid = "<unknown ssid>"; // WifiSsid.NONE (@hide)
  private String bssid;
  private int rssi = -127; // WifiInfo.INVALID_RSSI (@hide)
  private int linkSpeed = -1;
  private int frequency = -1;
  private int networkId = -1;

  @Implementation
  public String getMacAddress() {
    return macAddress;
  }

  @Implementation
  public String getSSID() {
    return ssid;
  }

  @Implementation
  public String getBSSID() {
    return bssid;
  }

  @Implementation
  public int getRssi() {
    return rssi;
  }

  @Implementation(minSdk = LOLLIPOP)
  public int getFrequency() {
    return frequency;
  }

  @Implementation
  public int getLinkSpeed() {
    return linkSpeed;
  }

  @Implementation
  public int getNetworkId() {
    return networkId;
  }

  @HiddenApi @Implementation
  public void setMacAddress(String newMacAddress) {
    macAddress = newMacAddress;
  }

  @HiddenApi @Implementation
  public void setSSID(String ssid) {
    this.ssid = ssid;
  }

  @HiddenApi @Implementation
  public void setBSSID(String bssid) {
    this.bssid = bssid;
  }

  @HiddenApi @Implementation
  public void setRssi(int rssi) {
    this.rssi = rssi;
  }

  @HiddenApi @Implementation
  public void setLinkSpeed(int linkSpeed) {
    this.linkSpeed = linkSpeed;
  }

  @HiddenApi
  @Implementation(minSdk = LOLLIPOP)
  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  @HiddenApi @Implementation
  public void setNetworkId(int id) {
    this.networkId = id;
  }
}
