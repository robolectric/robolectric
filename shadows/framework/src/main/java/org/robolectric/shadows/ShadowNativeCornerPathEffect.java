package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.CornerPathEffect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.CornerPathEffectNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadows.ShadowNativeCornerPathEffect.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link CornerPathEffect} that is backed by native code */
@Implements(
    value = CornerPathEffect.class,
    minSdk = O,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeCornerPathEffect {

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nativeCreate(float radius) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return CornerPathEffectNatives.nativeCreate(radius);
  }

  /** Shadow picker for {@link CornerPathEffect}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeCornerPathEffect.class);
    }
  }
}
