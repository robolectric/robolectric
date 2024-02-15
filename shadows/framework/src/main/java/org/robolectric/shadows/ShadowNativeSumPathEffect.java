package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.SumPathEffect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.SumPathEffectNatives;
import org.robolectric.shadows.ShadowNativeSumPathEffect.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link SumPathEffect} that is backed by native code */
@Implements(
    value = SumPathEffect.class,
    minSdk = O,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeSumPathEffect {

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nativeCreate(long first, long second) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return SumPathEffectNatives.nativeCreate(first, second);
  }

  /** Shadow picker for {@link SumPathEffect}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeSumPathEffect.class);
    }
  }
}
