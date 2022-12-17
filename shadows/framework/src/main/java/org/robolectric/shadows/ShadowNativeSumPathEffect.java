package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.SumPathEffect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.SumPathEffectNatives;
import org.robolectric.shadows.ShadowNativeSumPathEffect.Picker;

/** Shadow for {@link SumPathEffect} that is backed by native code */
@Implements(value = SumPathEffect.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeSumPathEffect {

  @Implementation(minSdk = O)
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
