package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.PathDashPathEffect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.PathDashPathEffectNatives;
import org.robolectric.shadows.ShadowNativePathDashPathEffect.Picker;

/** Shadow for {@link PathDashPathEffect} that is backed by native code */
@Implements(value = PathDashPathEffect.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativePathDashPathEffect {

  @Implementation(minSdk = O)
  protected static long nativeCreate(long nativePath, float advance, float phase, int nativeStyle) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return PathDashPathEffectNatives.nativeCreate(nativePath, advance, phase, nativeStyle);
  }

  /** Shadow picker for {@link PathDashPathEffect}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativePathDashPathEffect.class);
    }
  }
}
