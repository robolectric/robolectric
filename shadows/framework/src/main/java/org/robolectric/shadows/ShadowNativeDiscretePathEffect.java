package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.graphics.DiscretePathEffect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.DiscretePathEffectNatives;
import org.robolectric.shadows.ShadowNativeDiscretePathEffect.Picker;

/** Shadow for {@link DiscretePathEffect} that is backed by native code */
@Implements(
    value = DiscretePathEffect.class,
    minSdk = O,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeDiscretePathEffect {

  @Implementation(minSdk = O, maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nativeCreate(float length, float deviation) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return DiscretePathEffectNatives.nativeCreate(length, deviation);
  }

  /** Shadow picker for {@link DiscretePathEffect}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeDiscretePathEffect.class);
    }
  }
}
