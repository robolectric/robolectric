package org.robolectric.shadows;

import android.view.Choreographer;
import org.robolectric.annotation.Implements;

@Implements(value = Choreographer.class, shadowPicker = ShadowBaseChoreographer.Picker.class, isInAndroidSdk = false)
public class ShadowSimplifiedChoreographer extends ShadowBaseChoreographer {

}
