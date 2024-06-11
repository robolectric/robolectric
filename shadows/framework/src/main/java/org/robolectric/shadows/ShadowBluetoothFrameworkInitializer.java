package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.bluetooth.BluetoothFrameworkInitializer;
import android.os.BluetoothServiceManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Shadow for BluetoothFrameworkInitializer.
 *
 * <p>On real android, the BluetoothServiceManager is initializes and set by ActivityThread on
 * application startup. On Robolectric, this shadow is used to lazy load the BluetoothServiceManager
 * to save init costs if bluetooth is never used.
 */
@Implements(value = BluetoothFrameworkInitializer.class, isInAndroidSdk = false, minSdk = TIRAMISU)
public class ShadowBluetoothFrameworkInitializer {

  private static volatile BluetoothServiceManager bluetoothServiceManager = null;

  @Implementation(minSdk = TIRAMISU)
  protected static BluetoothServiceManager getBluetoothServiceManager() {
    if (bluetoothServiceManager == null) {
      bluetoothServiceManager = new BluetoothServiceManager();
    }
    return bluetoothServiceManager;
  }

  @Resetter
  public static void reset() {
    bluetoothServiceManager = null;
  }
}
