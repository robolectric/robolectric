package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = BluetoothManager.class, minSdk = JELLY_BEAN_MR2)
public class ShadowBluetoothManager {

  @Implementation
  protected BluetoothAdapter getAdapter() {
      return BluetoothAdapter.getDefaultAdapter();
    }
}