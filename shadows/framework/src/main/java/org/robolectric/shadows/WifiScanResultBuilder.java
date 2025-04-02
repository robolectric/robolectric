package org.robolectric.shadows;

import android.annotation.RequiresApi;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiSsid;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * Builder class for {@link ScanResult} allowing for more accurate construction of Wi-Fi scan
 * results in test code.
 */
public final class WifiScanResultBuilder {
  private static final int UNSPECIFIED = -1;
  private static final int NATIVE_BUILDER_MIN_SDK = VERSION_CODES.VANILLA_ICE_CREAM;
  private static final boolean USE_NATIVE_BUILDER = Build.VERSION.SDK_INT >= NATIVE_BUILDER_MIN_SDK;

  // Available in or before API 21+
  @Nullable private String ssid;
  @Nullable private String bssid;
  @Nullable private String capabilities;
  private int rssi = UNSPECIFIED;
  private int frequency = UNSPECIFIED;
  private Duration timeSinceSeen = Duration.ZERO;

  // Added in API 23
  private int channelWidth = ScanResult.CHANNEL_WIDTH_20MHZ;
  private int centerFreq0 = UNSPECIFIED;
  private int centerFreq1 = UNSPECIFIED;
  private boolean is80211McRttResponder = false;

  // Added in API 33
  @Nullable private WifiSsid wifiSsid;

  // Added in API 35. Past this API level all setters are delegated to the real ScanResult.Builder.
  @Nullable private ScanResult.Builder realBuilder;

  public WifiScanResultBuilder() {}

  /**
   * Sets the value of the {@link ScanResult#capabilities} field.
   *
   * @return this builder, for chaining
   */
  @CanIgnoreReturnValue
  public WifiScanResultBuilder setCapabilities(@Nullable String capabilities) {
    if (USE_NATIVE_BUILDER) {
      ensureRealBuilder().setCaps(capabilities);
    } else {
      this.capabilities = capabilities;
    }
    return this;
  }

  /**
   * Sets the value of the {@link ScanResult#BSSID} field.
   *
   * @return this builder, for chaining
   */
  @CanIgnoreReturnValue
  public WifiScanResultBuilder setBssid(@Nullable String bssid) {
    if (USE_NATIVE_BUILDER) {
      ensureRealBuilder().setBssid(bssid);
    } else {
      this.bssid = bssid;
    }
    return this;
  }

  /**
   * Sets the value of the {@link ScanResult#level} field (aka RSSI).
   *
   * @return this builder, for chaining
   */
  @CanIgnoreReturnValue
  public WifiScanResultBuilder setRssi(int rssi) {
    if (USE_NATIVE_BUILDER) {
      ensureRealBuilder().setRssi(rssi);
    } else {
      this.rssi = rssi;
    }
    return this;
  }

  /**
   * Sets the value of the frequency, in MHz, returned by the {@link ScanResult#frequency} field.
   *
   * @return this builder, for chaining
   */
  @CanIgnoreReturnValue
  public WifiScanResultBuilder setFrequency(int frequency) {
    if (USE_NATIVE_BUILDER) {
      ensureRealBuilder().setFrequency(frequency);
    } else {
      this.frequency = frequency;
    }
    return this;
  }

  /**
   * Sets the value of the {@link ScanResult#SSID} field. On API 33 and above, this also sets the
   * return value of {@link ScanResult#getWifiSsid()}.
   *
   * @param ssid the name of the network, or null; this should be either a UTF-8 encoded string,
   *     surrounded by quotes (e.g. '"Network name"'), or an unquoted hex-encoded string (e.g.
   *     '0a8b2c1f').
   * @return this builder, for chaining
   */
  @CanIgnoreReturnValue
  public WifiScanResultBuilder setSsid(@Nullable String ssid) {
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
      setWifiSsid(ssid != null ? WifiSsid.fromString(ssid) : null);
    } else {
      this.ssid = ssid;
    }
    return this;
  }

  /**
   * Sets the duration since boot since the result was seen.
   *
   * @see ScanResult#timestamp
   * @return this builder, for chaining
   */
  @CanIgnoreReturnValue
  public WifiScanResultBuilder setTimeSinceSeen(Duration timeSinceSeen) {
    if (USE_NATIVE_BUILDER) {
      ensureRealBuilder().setTsf(TimeUnit.MILLISECONDS.toMicros(timeSinceSeen.toMillis()));
    } else {
      this.timeSinceSeen = timeSinceSeen;
    }
    return this;
  }

  /**
   * Sets the value returned by {@link ScanResult#getWifiSsid()}, additionally setting the
   * deprecated {@link ScanResult#SSID} field.
   *
   * @return this builder, for chaining
   */
  @RequiresApi(VERSION_CODES.TIRAMISU)
  @CanIgnoreReturnValue
  public WifiScanResultBuilder setWifiSsid(@Nullable WifiSsid wifiSsid) {
    if (USE_NATIVE_BUILDER) {
      ensureRealBuilder().setWifiSsid(wifiSsid);
    } else {
      this.wifiSsid = wifiSsid;
      this.ssid = wifiSsid == null ? null : wifiSsid.toString();
    }
    return this;
  }

  /**
   * Sets the value returned by {@link ScanResult#channelWidth}. This should be one of the constants
   * in {@link ScanResult} such as {@link ScanResult#CHANNEL_WIDTH_20MHZ}.
   *
   * @return this builder, for chaining
   */
  @RequiresApi(VERSION_CODES.M)
  @CanIgnoreReturnValue
  public WifiScanResultBuilder setChannelWidth(int channelWidth) {
    if (USE_NATIVE_BUILDER) {
      ensureRealBuilder().setChannelWidth(channelWidth);
    } else {
      this.channelWidth = channelWidth;
    }
    return this;
  }

  /**
   * Sets the center frequency, in MHz, of the first segment (see {@link ScanResult#centerFreq0}).
   *
   * @return this builder, for chaining
   */
  @RequiresApi(VERSION_CODES.M)
  @CanIgnoreReturnValue
  public WifiScanResultBuilder setCenterFreq0(int centerFreq0) {
    if (USE_NATIVE_BUILDER) {
      ensureRealBuilder().setCenterFreq0(centerFreq0);
    } else {
      this.centerFreq0 = centerFreq0;
    }
    return this;
  }

  /**
   * Sets the center frequency, in MHz, of the second segment (see {@link ScanResult#centerFreq1}).
   *
   * @return this builder, for chaining
   */
  @CanIgnoreReturnValue
  @RequiresApi(VERSION_CODES.M)
  public WifiScanResultBuilder setCenterFreq1(int centerFreq1) {
    if (USE_NATIVE_BUILDER) {
      ensureRealBuilder().setCenterFreq1(centerFreq1);
    } else {
      this.centerFreq1 = centerFreq1;
    }
    return this;
  }

  /**
   * Sets the return value of {@link ScanResult#is80211mcResponder()}.
   *
   * @return this builder, for chaining
   */
  @CanIgnoreReturnValue
  @RequiresApi(VERSION_CODES.M)
  public WifiScanResultBuilder setIs80211McRttResponder(boolean is80211McRttResponder) {
    if (USE_NATIVE_BUILDER) {
      ensureRealBuilder().setIs80211McRTTResponder(is80211McRttResponder);
    } else {
      this.is80211McRttResponder = is80211McRttResponder;
    }
    return this;
  }

  /**
   * Sets the return value of {@link ScanResult#is80211azNtbResponder()}.
   *
   * @return this builder, for chaining
   */
  @CanIgnoreReturnValue
  @RequiresApi(VERSION_CODES.VANILLA_ICE_CREAM)
  public WifiScanResultBuilder setIs80211azNtbRttResponder(boolean is80211azNtbRttResponder) {
    ensureRealBuilder().setIs80211azNtbRTTResponder(is80211azNtbRttResponder);
    return this;
  }

  /**
   * Sets the return value of {@link ScanResult#isTwtResponder()}.
   *
   * @return this builder, for chaining
   */
  @CanIgnoreReturnValue
  @RequiresApi(VERSION_CODES.VANILLA_ICE_CREAM)
  public WifiScanResultBuilder setIsTwtResponder(boolean isTwtResponder) {
    ensureRealBuilder().setIsTwtResponder(isTwtResponder);
    return this;
  }

  /** Returns a new {@link ScanResult} instance as configured in this builder. */
  public ScanResult build() {
    if (USE_NATIVE_BUILDER) {
      return ensureRealBuilder().build();
    }

    long timestampMicros = TimeUnit.MILLISECONDS.toMicros(timeSinceSeen.toMillis());
    ScanResult scanResult;
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
      scanResult =
          new ScanResult(
              wifiSsid,
              bssid,
              /* hessid= */ UNSPECIFIED,
              /* anqpDomainId= */ UNSPECIFIED,
              /* osuProviders= */ null,
              capabilities,
              rssi,
              frequency,
              timestampMicros);
    } else {
      scanResult = new ScanResult();
      scanResult.SSID = ssid;
      scanResult.BSSID = bssid;
      scanResult.capabilities = capabilities;
      scanResult.level = rssi;
      scanResult.frequency = frequency;
      scanResult.timestamp = timestampMicros;
    }

    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      scanResult.channelWidth = channelWidth;
      scanResult.centerFreq0 = centerFreq0;
      scanResult.centerFreq1 = centerFreq1;
      if (is80211McRttResponder) {
        scanResult.setFlag(ScanResult.FLAG_80211mc_RESPONDER);
      }
    }

    return scanResult;
  }

  @RequiresApi(NATIVE_BUILDER_MIN_SDK)
  private ScanResult.Builder ensureRealBuilder() {
    if (realBuilder == null) {
      realBuilder = new ScanResult.Builder();
    }
    return realBuilder;
  }
}
