package org.robolectric.shadows;

import android.net.wifi.WifiScanner;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Messenger;
import java.util.ArrayList;
import java.util.Arrays;

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

    // Insert some mocked channels, taken from https://en.wikipedia.org/wiki/List_of_WLAN_channels
    bundle.putIntegerArrayList(
        WifiScanner.GET_AVAILABLE_CHANNELS_EXTRA,
        new ArrayList<>(
            Arrays.asList(2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447, 2452, 2457, 2462)));

    return bundle;
  }
}
