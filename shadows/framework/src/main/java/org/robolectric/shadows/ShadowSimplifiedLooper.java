package org.robolectric.shadows;


import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.os.Looper;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;

/** A simpler variant of a Looper shadow that is active when ControlledLooper is enabled. */
@Implements(
    value = Looper.class,
    shadowPicker = ShadowBaseLooper.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowSimplifiedLooper extends ShadowBaseLooper {

  @Resetter
  public static synchronized void reset() {
    ControlledLooper.reset();
  }
}
