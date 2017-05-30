package org.robolectric.shadows;

import android.bluetooth.BluetoothDevice;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(BluetoothDevice.class)
public class ShadowBluetoothDevice {

  @RealObject private BluetoothDevice realObject;
  private String name;

  public void __constructor__(String address) {
    // Call getService() like the original constructor would, but ignore any Exceptions. This allows
    // us to easily create BluetoothDevices for testing purposes without having any actual Bluetooth
    // capability.
    try {
      ReflectionHelpers.callStaticMethod(BluetoothDevice.class, "getService");
    } catch (Exception e) {
      // no-op.
    }
    ReflectionHelpers.setField(realObject, "mAddress", address);
  }

  public void setName(String name) {
    this.name = name;
  }

  @Implementation
  public String getName() {
    return name;
  }
}
