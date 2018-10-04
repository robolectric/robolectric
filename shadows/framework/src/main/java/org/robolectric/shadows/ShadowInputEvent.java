package org.robolectric.shadows;

import android.view.InputDevice;
import android.view.InputEvent;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(InputEvent.class)
public class ShadowInputEvent {
  protected InputDevice device;

  @Implementation
  protected InputDevice getDevice() {
    return device;
  }

  public void setDevice(InputDevice device) {
    this.device = device;
  }
}
