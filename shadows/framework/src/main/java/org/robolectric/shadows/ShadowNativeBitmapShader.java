package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.BitmapShaderNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadows.ShadowNativeBitmapShader.Picker;

/** Shadow for {@link BitmapShader} that is backed by native code */
@Implements(value = BitmapShader.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeBitmapShader {

  @Implementation(minSdk = O, maxSdk = P)
  protected static long nativeCreate(
      long nativeMatrix, Bitmap bitmap, int shaderTileModeX, int shaderTileModeY) {
    return nativeCreate(
        nativeMatrix,
        bitmap != null ? bitmap.getNativeInstance() : 0,
        shaderTileModeX,
        shaderTileModeY,
        false);
  }

  @Implementation(minSdk = Q, maxSdk = R)
  protected static long nativeCreate(
      long nativeMatrix, long bitmapHandle, int shaderTileModeX, int shaderTileModeY) {
    return nativeCreate(nativeMatrix, bitmapHandle, shaderTileModeX, shaderTileModeY, false);
  }

  @Implementation(minSdk = S, maxSdk = S_V2)
  protected static long nativeCreate(
      long nativeMatrix,
      long bitmapHandle,
      int shaderTileModeX,
      int shaderTileModeY,
      boolean filter) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return BitmapShaderNatives.nativeCreate(
        nativeMatrix, bitmapHandle, shaderTileModeX, shaderTileModeY, filter);
  }

  @Implementation(minSdk = TIRAMISU)
  protected static long nativeCreate(
      long nativeMatrix,
      long bitmapHandle,
      int shaderTileModeX,
      int shaderTileModeY,
      boolean filter,
      boolean isDirectSampled) {
    return nativeCreate(nativeMatrix, bitmapHandle, shaderTileModeX, shaderTileModeY, filter);
  }

  /** Shadow picker for {@link BitmapShader}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeBitmapShader.class);
    }
  }
}
