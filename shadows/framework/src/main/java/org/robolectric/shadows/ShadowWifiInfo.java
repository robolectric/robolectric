package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.net.wifi.WifiInfo;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(WifiInfo.class)
public class ShadowWifiInfo {
  @Implementation
  protected static void __staticInitializer__() {}

  private String macAddress = "02:00:00:00:00:00"; // WifiInfo.DEFAULT_MAC_ADDRESS (@hide)
  private String ssid = "<unknown ssid>"; // WifiSsid.NONE (@hide)
  private String bssid;
  private int rssi = -127; // WifiInfo.INVALID_RSSI (@hide)
  private int linkSpeed = -1;
  private int frequency = -1;
  private int networkId = -1;

  @Implementation
  protected String getMacAddress() {
    return macAddress;
  }

  @Implementation
  protected String getSSID() {
    return ssid;
  }

  @Implementation
  protected String getBSSID() {
    return bssid;
  }

  @Implementation
  protected int getRssi() {
    return rssi;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected int getFrequency() {
    return frequency;
  }

  @Implementation
  protected int getLinkSpeed() {
    return linkSpeed;
  }

  @Implementation
  protected int getNetworkId() {
    return networkId;
  }

  @HiddenApi @Implementation
  protected void setMacAddress(String newMacAddress) {
    macAddress = newMacAddress;
  }

  @HiddenApi @Implementation
  protected void setSSID(String ssid) {
    this.ssid = ssid;
  }

  @HiddenApi @Implementation
  protected void setBSSID(String bssid) {
    this.bssid = bssid;
  }

  @HiddenApi @Implementation
  protected void setRssi(int rssi) {
    this.rssi = rssi;
  }

  @HiddenApi @Implementation
  protected void setLinkSpeed(int linkSpeed) {
    this.linkSpeed = linkSpeed;
  }

  @HiddenApi
  @Implementation(minSdk = LOLLIPOP)
  protected void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  @HiddenApi @Implementation
  protected void setNetworkId(int id) {
    this.networkId = id;
  }
}
