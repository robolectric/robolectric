package org.robolectric.shadows;

import android.view.InputDevice;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static org.robolectric.Robolectric.shadowOf;

@Implements(InputDevice.class)
public class ShadowInputDevice {
  private String deviceName;

  public static InputDevice makeInputDeviceNamed(String deviceName) {
    InputDevice inputDevice = Robolectric.newInstanceOf(InputDevice.class);
    shadowOf(inputDevice).setDeviceName(deviceName);
    return inputDevice;
  }

  @Implementation
  public String getName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }
}
