package org.robolectric.android.internal;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.platform.device.DeviceController;
import androidx.test.platform.device.UnsupportedDeviceOperationException;
import org.robolectric.RuntimeEnvironment;

/**
 * A {@link DeviceController} that runs on a local JVM with Robolectric, so that Espresso Device can
 * rotate the screen.
 */
public class LocalDeviceController implements DeviceController {

  @Override
  public void setDeviceMode(int deviceMode) {
    throw new UnsupportedDeviceOperationException(
        "Robolectric can't fold the device. Use androidx.window:window-testing to publish folding"
            + " features instead.");
  }

  @Override
  public void setScreenOrientation(int screenOrientation) {
    String qualifiers;
    if (screenOrientation == ScreenOrientation.LANDSCAPE.ordinal()) {
      qualifiers = "+land";
    } else if (screenOrientation == ScreenOrientation.PORTRAIT.ordinal()) {
      qualifiers = "+port";
    } else {
      throw new IllegalArgumentException("Unknown screen orientation: " + screenOrientation);
    }
    InstrumentationRegistry.getInstrumentation()
        .runOnMainSync(() -> RuntimeEnvironment.setQualifiers(qualifiers));
  }
}
