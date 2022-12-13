package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.EmbossMaskFilter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.EmbossMaskFilterNatives;
import org.robolectric.shadows.ShadowNativeEmbossMaskFilter.Picker;

/** Shadow for {@link EmbossMaskFilter} that is backed by native code */
@Implements(value = EmbossMaskFilter.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeEmbossMaskFilter {

  @Implementation(minSdk = O)
  protected static long nativeConstructor(
      float[] direction, float ambient, float specular, float blurRadius) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return EmbossMaskFilterNatives.nativeConstructor(direction, ambient, specular, blurRadius);
  }

  /** Shadow picker for {@link EmbossMaskFilter}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeEmbossMaskFilter.class);
    }
  }
}
