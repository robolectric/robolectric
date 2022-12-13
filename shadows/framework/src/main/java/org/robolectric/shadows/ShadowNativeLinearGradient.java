package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;

import android.graphics.LinearGradient;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.LinearGradientNatives;
import org.robolectric.shadows.ShadowNativeLinearGradient.Picker;

/** Shadow for {@link LinearGradient} that is backed by native code */
@Implements(value = LinearGradient.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeLinearGradient {
  @Implementation(minSdk = Q)
  protected long nativeCreate(
      long matrix,
      float x0,
      float y0,
      float x1,
      float y1,
      long[] colors,
      float[] positions,
      int tileMode,
      long colorSpaceHandle) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return LinearGradientNatives.nativeCreate(
        matrix, x0, y0, x1, y1, colors, positions, tileMode, colorSpaceHandle);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected long nativeCreate1(
      long matrix,
      float x0,
      float y0,
      float x1,
      float y1,
      int[] colors,
      float[] positions,
      int tileMode) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return LinearGradientNatives.nativeCreate1(matrix, x0, y0, x1, y1, colors, positions, tileMode);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected long nativeCreate2(
      long matrix, float x0, float y0, float x1, float y1, int color0, int color1, int tileMode) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return LinearGradientNatives.nativeCreate2(matrix, x0, y0, x1, y1, color0, color1, tileMode);
  }

  /** Shadow picker for {@link LinearGradient}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeLinearGradient.class);
    }
  }
}
