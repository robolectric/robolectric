package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.DashPathEffect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DashPathEffectNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadows.ShadowNativeDashPathEffect.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link DashPathEffect} that is backed by native code */
@Implements(
    value = DashPathEffect.class,
    minSdk = O,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeDashPathEffect {

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nativeCreate(float[] intervals, float phase) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return DashPathEffectNatives.nativeCreate(intervals, phase);
  }

  /** Shadow picker for {@link DashPathEffect}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeDashPathEffect.class);
    }
  }
}
