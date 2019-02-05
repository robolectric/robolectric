package org.robolectric.shadows;

import android.view.Choreographer;
import android.view.Display;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

import static org.robolectric.util.reflector.Reflector.reflector;

@Implements(value = Choreographer.class, shadowPicker = ShadowBaseChoreographer.Picker.class, isInAndroidSdk = false)
public class ShadowNewChoreographer extends ShadowBaseChoreographer {

  @Resetter
  public static void reset() {
    // Choreographer is exposed as a static reference, which other code could reference
    // So for safety, don't clear that static reference and instead reset all the field values
    ChoregrapherReflector choregrapherReflector = reflector(ChoregrapherReflector.class,
        Choreographer.getInstance());
    choregrapherReflector.setLastFrameTimeNanos(Long.MIN_VALUE);
  }

  @ForType(Choreographer.class)
  private interface ChoregrapherReflector {

    @Accessor("mLastFrameTimeNanos")
    void setLastFrameTimeNanos(long val);
  }
}
