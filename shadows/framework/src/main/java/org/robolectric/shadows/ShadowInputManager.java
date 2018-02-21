package org.robolectric.shadows;

import android.hardware.input.InputManager;
import android.view.InputEvent;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

/*
 * Shadow for InputManager.
 */
@Implements(value = InputManager.class)
public class ShadowInputManager {

  @Implementation
  protected boolean injectInputEvent(InputEvent event, int mode) {
    // ignore
    return true;
  }

  @Resetter
  public static void reset() {
    ReflectionHelpers.setStaticField(InputManager.class, "sInstance", null);
  }
}
