package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.graphics.RadialGradient;
import androidx.annotation.ColorLong;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.RadialGradientNatives;
import org.robolectric.shadows.ShadowNativeRadialGradient.Picker;

/** Shadow for {@link RadialGradient} that is backed by native code */
@Implements(value = RadialGradient.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeRadialGradient {

  @Implementation(minSdk = S)
  protected static long nativeCreate(
      long matrix,
      float startX,
      float startY,
      float startRadius,
      float endX,
      float endY,
      float endRadius,
      @ColorLong long[] colors,
      float[] positions,
      int tileMode,
      long colorSpaceHandle) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RadialGradientNatives.nativeCreate(
        matrix,
        startX,
        startY,
        startRadius,
        endX,
        endY,
        endRadius,
        colors,
        positions,
        tileMode,
        colorSpaceHandle);
  }

  @Implementation(minSdk = Q, maxSdk = R)
  protected static long nativeCreate(
      long matrix,
      float x,
      float y,
      float radius,
      @ColorLong long[] colors,
      float[] positions,
      int tileMode,
      long colorSpaceHandle) {
    return nativeCreate(
        matrix, x, y, 0, x, y, radius, colors, positions, tileMode, colorSpaceHandle);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static long nativeCreate1(
      long matrix, float x, float y, float radius, int[] colors, float[] positions, int tileMode) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RadialGradientNatives.nativeCreate1(matrix, x, y, radius, colors, positions, tileMode);
  }

  @Implementation(minSdk = O, maxSdk = P)
  protected static long nativeCreate2(
      long matrix, float x, float y, float radius, int color0, int color1, int tileMode) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RadialGradientNatives.nativeCreate2(matrix, x, y, radius, color0, color1, tileMode);
  }

  /** Shadow picker for {@link RadialGradient}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeRadialGradient.class);
    }
  }
}
