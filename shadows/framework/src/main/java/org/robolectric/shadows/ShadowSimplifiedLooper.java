package org.robolectric.shadows;

import android.os.Looper;
import org.robolectric.annotation.Implements;

/** A simpler variant of a Looper shadow that is active when ControlledLooper is enabled. */
@Implements(
    value = Looper.class,
    shadowPicker = ShadowBaseLooper.Picker.class,
    // TODO: turn off shadowOf generation. Figure out why this is needed
    isInAndroidSdk = false)
public class ShadowSimplifiedLooper extends ShadowBaseLooper {}
