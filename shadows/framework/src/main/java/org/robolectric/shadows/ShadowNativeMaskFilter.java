package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.MaskFilter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.MaskFilterNatives;
import org.robolectric.shadows.ShadowNativeMaskFilter.Picker;

/** Shadow for {@link MaskFilter} that is backed by native code */
@Implements(value = MaskFilter.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeMaskFilter {

  @Implementation(minSdk = O)
  protected static void nativeDestructor(long nativeFilter) {
    MaskFilterNatives.nativeDestructor(nativeFilter);
  }

  /** Shadow picker for {@link MaskFilter}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeMaskFilter.class);
    }
  }
}
