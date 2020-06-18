package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;

import android.hardware.input.InputManager;
import android.view.InputDevice;
import android.view.InputEvent;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = InputManager.class)
public class ShadowInputManager {

  @Implementation
  protected boolean injectInputEvent(InputEvent event, int mode) {
    // ignore
    return true;
  }

  @Implementation(minSdk = KITKAT)
  protected boolean[] deviceHasKeys(int id, int[] keyCodes) {
    return new boolean[keyCodes.length];
  }

  /** Used in {@link InputDevice#getDeviceIds()} */
  @Implementation
  protected int[] getInputDeviceIds() {
    return new int[0];
  }

  @Resetter
  public static void reset() {
    ReflectionHelpers.setStaticField(InputManager.class, "sInstance", null);
  }
}
