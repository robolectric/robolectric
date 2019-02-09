package org.robolectric.shadows;

import android.view.Choreographer;
import android.view.Display;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

import static org.robolectric.util.reflector.Reflector.reflector;

@Implements(value = Choreographer.class, shadowPicker = ShadowBaseChoreographer.Picker.class, isInAndroidSdk = false)
public class ShadowRealisticChoreographer extends ShadowBaseChoreographer {

  @Resetter
  public static void reset() {
    reflector(ChoregrapherReflector.class).getThreadInstance().remove();
  }

  @ForType(Choreographer.class)
  private interface ChoregrapherReflector {

    @Accessor("sThreadInstance") @Static
    ThreadLocal<Choreographer> getThreadInstance();
  }
}
