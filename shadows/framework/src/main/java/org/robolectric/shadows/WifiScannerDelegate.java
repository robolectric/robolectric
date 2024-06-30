package org.robolectric.shadows;

import android.net.wifi.WifiScanner;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Messenger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A system service interface delegate for IWifiScanner.
 *
 * <p>By default, Robolectric will provide stub return results for any IWifiScanner calls. This
 * delegate is used whenever more substantial logic is needed.
 */
class WifiScannerDelegate {
  public Messenger getMessenger() {
    return new Messenger(new Handler(Looper.getMainLooper()));
  }

  public Bundle getAvailableChannels(int band, String packageName, String featureId) {
    Bundle bundle = new Bundle();

    // Mock available WiFi channels. See https://en.wikipedia.org/wiki/List_of_WLAN_channels
    List<Integer> availableChannels = new ArrayList<>();

    switch (band) {
      case WifiScanner.WIFI_BAND_24_GHZ:
        availableChannels =
            Arrays.asList(
                2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447, 2452, 2457, 2462, 2467, 2472, 2477);
        break;
      case WifiScanner.WIFI_BAND_5_GHZ_WITH_DFS:
        availableChannels =
            Arrays.asList(
                5180, 5200, 5220, 5240, 5260, 5280, 5300, 5320, 5500, 5520, 5540, 5560, 5580, 5600,
                5620, 5640, 5660, 5680, 5700, 5720, 5745, 5765, 5785, 5805, 5825, 5845, 5865, 5885,
                5905, 5925, 5945, 5965, 5985);
        break;
      case WifiScanner.WIFI_BAND_BOTH_WITH_DFS:
        availableChannels =
            Arrays.asList(
                2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447, 2452, 2457, 2462, 2467, 2472, 2477,
                5180, 5200, 5220, 5240, 5260, 5280, 5300, 5320, 5500, 5520, 5540, 5560, 5580, 5600,
                5620, 5640, 5660, 5680, 5700, 5720, 5745, 5765, 5785, 5805, 5825, 5845, 5865, 5885,
                5905, 5925, 5945, 5965, 5985);
        break;
      case WifiScanner.WIFI_BAND_6_GHZ:
        availableChannels =
            Arrays.asList(
                5975, 5995, 6015, 6035, 6055, 6075, 6095, 6115, 6135, 6155, 6175, 6195, 6215, 6235,
                6255, 6275, 6295, 6315, 6335, 6355, 6375, 6395, 6415, 6435);
        break;
      case WifiScanner.WIFI_BAND_24_5_WITH_DFS_6_GHZ:
        availableChannels =
            Arrays.asList(
                2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447, 2452, 2457, 2462, 2467, 2472, 2477,
                5180, 5200, 5220, 5240, 5260, 5280, 5300, 5320, 5500, 5520, 5540, 5560, 5580, 5600,
                5620, 5640, 5660, 5680, 5700, 5720, 5745, 5765, 5785, 5805, 5825, 5845, 5865, 5885,
                5905, 5925, 5945, 5965, 5985, 6015, 6035, 6055, 6075, 6095, 6115, 6135, 6155, 6175,
                6195, 6215, 6235, 6255, 6275, 6295, 6315, 6335, 6355, 6375, 6395, 6415, 6435);
        break;
      case WifiScanner.WIFI_BAND_60_GHZ:
        availableChannels =
            Arrays.asList(
                58320, 58340, 58360, 58380, 58400, 58420, 58440, 58460, 58480, 58500, 58520, 58540,
                58560, 58580, 58600, 58620, 58640, 58660, 58680, 58700, 58720, 58740, 58760, 58780,
                58800, 58820, 58840, 58860, 58880, 58900, 58920, 58940, 58960, 58980, 59000, 59020,
                59040, 59060, 59080, 59100, 59120, 59140, 59160, 59180, 59200, 59220, 59240, 59260,
                59280, 59300, 59320, 59340, 59360, 59380, 59400, 59420, 59440, 59460, 59480, 59500,
                59520, 59540, 59560, 59580, 59600, 59620, 59640, 59660, 59680, 59700, 59720, 59740,
                59760, 59780, 59800, 59820, 59840, 59860, 59880, 59900, 59920, 59940, 59960, 59980);
        break;
      case WifiScanner.WIFI_BAND_24_5_WITH_DFS_6_60_GHZ:
      case WifiScanner.WIFI_BAND_UNSPECIFIED:
      default:
        availableChannels =
            Arrays.asList(
                2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447, 2452, 2457, 2462, 2467, 2472, 2477,
                5180, 5200, 5220, 5240, 5260, 5280, 5300, 5320, 5500, 5520, 5540, 5560, 5580, 5600,
                5620, 5640, 5660, 5680, 5700, 5720, 5745, 5765, 5785, 5805, 5825, 5845, 5865, 5885,
                5905, 5925, 5945, 5965, 5985, 6015, 6035, 6055, 6075, 6095, 6115, 6135, 6155, 6175,
                6195, 6215, 6235, 6255, 6275, 6295, 6315, 6335, 6355, 6375, 6395, 6415, 6435, 58320,
                58340, 58360, 58380, 58400, 58420, 58440, 58460, 58480, 58500, 58520, 58540, 58560,
                58580, 58600, 58620, 58640, 58660, 58680, 58700, 58720, 58740, 58760, 58780, 58800,
                58820, 58840, 58860, 58880, 58900, 58920, 58940, 58960, 58980, 59000, 59020, 59040,
                59060, 59080, 59100, 59120, 59140, 59160, 59180, 59200, 59220, 59240, 59260, 59280,
                59300, 59320, 59340, 59360, 59380, 59400, 59420, 59440, 59460, 59480, 59500, 59520,
                59540, 59560, 59580, 59600, 59620, 59640, 59660, 59680, 59700, 59720, 59740, 59760,
                59780, 59800, 59820, 59840, 59860, 59880, 59900, 59920, 59940, 59960, 59980);
        break;
    }

    bundle.putIntegerArrayList(
        WifiScanner.GET_AVAILABLE_CHANNELS_EXTRA, new ArrayList<>(availableChannels));

    return bundle;
  }

  public Bundle getAvailableChannels(
      int band, String packageName, String featureId, Bundle extras) {
    return getAvailableChannels(band, packageName, featureId);
  }
}
