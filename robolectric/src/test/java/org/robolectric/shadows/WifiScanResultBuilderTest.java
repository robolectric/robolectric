package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiSsid;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public final class WifiScanResultBuilderTest {
  @Test
  @Config(maxSdk = VERSION_CODES.S_V2)
  public void setSsid_withQuotedUtf8String_setsQuotedSsidField() {
    ScanResult scanResult = new WifiScanResultBuilder().setSsid("\"My Network\"").build();

    assertThat(scanResult.SSID).isEqualTo("\"My Network\"");
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void setSsid_withQuotedUtf8String_tiramisuPlus_setsUnquotedSsidField() {
    ScanResult scanResult = new WifiScanResultBuilder().setSsid("\"My Network\"").build();

    // https://cs.android.com/android/_/android/platform/packages/modules/Wifi/+/767deb99f14455262d364da2291cd93cc70db2a1
    // The behavior of the deprecated SSID field was changed such that it does not assign the
    // quoted UTF-8 string.
    assertThat(scanResult.SSID).isEqualTo("My Network");
  }

  @Test
  @Config(maxSdk = VERSION_CODES.S_V2)
  public void setSsid_withUnquotedHexString_setsSsidField() {
    ScanResult scanResult = new WifiScanResultBuilder().setSsid("0abcdef1").build();

    assertThat(scanResult.SSID).isEqualTo("0abcdef1");
  }

  @Test
  @Config(maxSdk = VERSION_CODES.S_V2)
  public void setSsid_toNull_setsNullSsidField() {
    ScanResult scanResult = new WifiScanResultBuilder().setSsid(null).build();

    assertThat(scanResult.SSID).isNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void setSsid_toNull_tiramisuPlus_setsUnknownSsidField() {
    ScanResult scanResult = new WifiScanResultBuilder().setSsid(null).build();

    assertThat(scanResult.SSID).isEqualTo("<unknown ssid>");
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void setWifiSsid_withQuotedUtf8String_setsBothSsidFields() {
    WifiSsid wifiSsid = WifiSsid.fromString("\"My Network\"");
    ScanResult scanResult = new WifiScanResultBuilder().setWifiSsid(wifiSsid).build();

    assertThat(scanResult.SSID).isEqualTo("My Network");
    assertThat(scanResult.getWifiSsid()).isEqualTo(wifiSsid);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void setWifiSsid_withUnquotedHexString_setsWifiSsidFields() {
    WifiSsid wifiSsid = WifiSsid.fromString("0abcdef1");

    ScanResult scanResult = new WifiScanResultBuilder().setWifiSsid(wifiSsid).build();

    // https://cs.android.com/android/_/android/platform/packages/modules/Wifi/+/767deb99f14455262d364da2291cd93cc70db2a1
    // The behavior of the deprecated SSID field was changed such that it only assigns the SSID
    // field for UTF-8.
    assertThat(scanResult.SSID).isEqualTo("<unknown ssid>");
    assertThat(scanResult.getWifiSsid()).isEqualTo(wifiSsid);
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void setCapabilities_setsCapabilitiesField() {
    String capabilities = "wpa2";
    ScanResult scanResult = new WifiScanResultBuilder().setCapabilities(capabilities).build();

    assertThat(scanResult.capabilities).isEqualTo(capabilities);
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void setBssid_setsBssidField() {
    String bssid = "01:AB:CD:EF:12";
    ScanResult scanResult = new WifiScanResultBuilder().setBssid(bssid).build();

    assertThat(scanResult.BSSID).isEqualTo(bssid);
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void setRssi_setsLevelField() {
    int rssi = -80;
    ScanResult scanResult = new WifiScanResultBuilder().setRssi(rssi).build();

    assertThat(scanResult.level).isEqualTo(rssi);
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void setFrequency_setsFrequencyField() {
    int frequency = 2412;
    ScanResult scanResult = new WifiScanResultBuilder().setFrequency(frequency).build();

    assertThat(scanResult.frequency).isEqualTo(frequency);
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void setTimestamp_setsTimestampField() {
    int durationMicros = 500000000;
    Duration timeSinceSeen = Duration.ofSeconds(TimeUnit.MICROSECONDS.toSeconds(durationMicros));
    ScanResult scanResult = new WifiScanResultBuilder().setTimeSinceSeen(timeSinceSeen).build();

    assertThat(scanResult.timestamp).isEqualTo(durationMicros);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void setChannelWidth_setsChannelWidthField() {
    ScanResult scanResult =
        new WifiScanResultBuilder().setChannelWidth(ScanResult.CHANNEL_WIDTH_80MHZ).build();

    assertThat(scanResult.channelWidth).isEqualTo(ScanResult.CHANNEL_WIDTH_80MHZ);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void setCenterFreq0_setsCenterFreq0Field() {
    int centerFreq0 = 2412;
    ScanResult scanResult = new WifiScanResultBuilder().setCenterFreq0(centerFreq0).build();

    assertThat(scanResult.centerFreq0).isEqualTo(centerFreq0);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void setCenterFreq1_setsCenterFreq1Field() {
    int centerFreq1 = 2412;
    ScanResult scanResult = new WifiScanResultBuilder().setCenterFreq1(centerFreq1).build();

    assertThat(scanResult.centerFreq1).isEqualTo(centerFreq1);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void setIs80211McRttResponder_returnsCorrectValue() {
    ScanResult scanResult = new WifiScanResultBuilder().setIs80211McRttResponder(true).build();

    assertThat(scanResult.is80211mcResponder()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  public void setIs80211azNtbRttResponder_returnsCorrectValue() {
    ScanResult scanResult = new WifiScanResultBuilder().setIs80211azNtbRttResponder(true).build();

    assertThat(scanResult.is80211azNtbResponder()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  public void setIsTwtResponder_returnsCorrectValue() {
    ScanResult scanResult = new WifiScanResultBuilder().setIsTwtResponder(true).build();

    assertThat(scanResult.isTwtResponder()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void buildFromEmpty_checkCommonDefaultValues() {
    ScanResult scanResult = new WifiScanResultBuilder().build();

    // In API 33, the constructor with WifiSsid treats a missing SSID with a
    // special <unknown ssid> string rather than null.
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
      assertThat(scanResult.SSID).isEqualTo("<unknown ssid>");
    } else {
      assertThat(scanResult.SSID).isNull();
    }

    assertThat(scanResult.BSSID).isNull();
    assertThat(scanResult.capabilities).isNull();
    assertThat(scanResult.level).isEqualTo(-1);
    assertThat(scanResult.frequency).isEqualTo(-1);
    assertThat(scanResult.timestamp).isEqualTo(0);

    if (VERSION.SDK_INT >= VERSION_CODES.VANILLA_ICE_CREAM) {
      // In API 35, ScanResult.Builder defaults to CHANNEL_WIDTH_20MHZ, while the previous
      // constructors default to UNSPECIFIED (-1).
      assertThat(scanResult.channelWidth).isEqualTo(ScanResult.CHANNEL_WIDTH_20MHZ);
    } else if (VERSION.SDK_INT >= VERSION_CODES.M) {
      assertThat(scanResult.channelWidth).isEqualTo(0);
    }

    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      assertThat(scanResult.centerFreq0).isEqualTo(-1);
      assertThat(scanResult.centerFreq1).isEqualTo(-1);
    }
  }
}
