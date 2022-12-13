package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;

import android.graphics.SweepGradient;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.SweepGradientNatives;
import org.robolectric.shadows.ShadowNativeSweepGradient.Picker;

/** Shadow for {@link SweepGradient} that is backed by native code */
@Implements(value = SweepGradient.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeSweepGradient {

  @Implementation(minSdk = Q)
  protected static long nativeCreate(
      long matrix, float x, float y, long[] colors, float[] positions, long colorSpaceHandle) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return SweepGradientNatives.nativeCreate(matrix, x, y, colors, positions, colorSpaceHandle);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static long nativeCreate1(
      long matrix, float x, float y, int[] colors, float[] positions) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return SweepGradientNatives.nativeCreate1(matrix, x, y, colors, positions);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static long nativeCreate2(long matrix, float x, float y, int color0, int color1) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return SweepGradientNatives.nativeCreate2(matrix, x, y, color0, color1);
  }

  /** Shadow picker for {@link SweepGradient}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeSweepGradient.class);
    }
  }
}
