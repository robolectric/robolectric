package org.robolectric.shadows;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

/**
 * Shadow for {@link android.bluetooth.BluetoothManager}.
 */
@Implements(value = BluetoothManager.class, minSdk = JELLY_BEAN_MR2)
public class ShadowBluetoothManager {

    @Implementation
    public BluetoothAdapter getAdapter() {
      return BluetoothAdapter.getDefaultAdapter();
    }
}