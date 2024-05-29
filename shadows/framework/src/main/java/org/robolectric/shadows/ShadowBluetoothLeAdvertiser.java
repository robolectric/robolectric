package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.BluetoothLeAdvertiser;
import java.util.Map;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow implementation of {@link BluetoothLeAdvertiser}. */
@Implements(value = BluetoothLeAdvertiser.class, minSdk = O)
public class ShadowBluetoothLeAdvertiser {
  @ReflectorObject protected BluetoothLeAdvertiserReflector bluetoothLeAdvertiserReflector;

  /** Returns the count of current ongoing Bluetooth LE advertising requests. */
  public int getAdvertisementRequestCount() {
    return bluetoothLeAdvertiserReflector.getLegacyAdvertisers().size();
  }

  /** Returns the count of current ongoing Bluetooth LE advertising set requests. */
  public int getAdvertisingSetRequestCount() {
    return bluetoothLeAdvertiserReflector.getAdvertisingSets().size();
  }

  @ForType(BluetoothLeAdvertiser.class)
  private interface BluetoothLeAdvertiserReflector {
    @Accessor("mLegacyAdvertisers")
    Map<AdvertiseCallback, AdvertisingSetCallback> getLegacyAdvertisers();

    @Accessor("mAdvertisingSets")
    Map<?, ?> getAdvertisingSets();
  }
}
