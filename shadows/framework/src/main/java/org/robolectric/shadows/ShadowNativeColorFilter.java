package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;

import android.graphics.ColorFilter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.ColorFilterNatives;
import org.robolectric.shadows.ShadowNativeColorFilter.Picker;

/** Shadow for {@link ColorFilter} that is backed by native code */
@Implements(value = ColorFilter.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeColorFilter {

  @Implementation(minSdk = O_MR1)
  protected static long nativeGetFinalizer() {
    return ColorFilterNatives.nativeGetFinalizer();
  }

  @Implementation(minSdk = O, maxSdk = O)
  protected static void nSafeUnref(long nativeInstance) {
    ColorFilterNatives.nSafeUnref(nativeInstance);
  }

  /** Shadow picker for {@link ColorFilter}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeColorFilter.class);
    }
  }
}
