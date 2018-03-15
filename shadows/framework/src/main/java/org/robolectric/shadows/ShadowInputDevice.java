package org.robolectric.shadows;

import android.view.InputDevice;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(InputDevice.class)
public class ShadowInputDevice {
  private String deviceName;

  public static InputDevice makeInputDeviceNamed(String deviceName) {
    InputDevice inputDevice = Shadow.newInstanceOf(InputDevice.class);
    Shadows.shadowOf(inputDevice).setDeviceName(deviceName);
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
