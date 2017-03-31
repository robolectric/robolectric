package org.robolectric.shadows;

import android.bluetooth.BluetoothDevice;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.bluetooth.BluetoothDevice}.
 */
@Implements(BluetoothDevice.class)
public class ShadowBluetoothDevice {

  private String name;

  public void setName(String name) {
    this.name = name;
  }

  @Implementation
  public String getName() {
    return name;
  }
}
