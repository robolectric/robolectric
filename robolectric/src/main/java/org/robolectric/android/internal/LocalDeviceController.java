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
    String qualifiers =
        screenOrientation == ScreenOrientation.LANDSCAPE.ordinal() ? "+land" : "+port";
    InstrumentationRegistry.getInstrumentation()
        .runOnMainSync(() -> RuntimeEnvironment.setQualifiers(qualifiers));
  }
}
