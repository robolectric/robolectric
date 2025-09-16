package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.graphics.NinePatch;
import android.graphics.Rect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.NinePatchNatives;
import org.robolectric.shadows.ShadowNativeNinePatch.Picker;

/** Shadow for {@link NinePatch} that is backed by native code */
@Implements(
    value = NinePatch.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeNinePatch {

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean isNinePatchChunk(byte[] chunk) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return NinePatchNatives.isNinePatchChunk(chunk);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long validateNinePatchChunk(byte[] chunk) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return NinePatchNatives.validateNinePatchChunk(chunk);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nativeFinalize(long chunk) {
    NinePatchNatives.nativeFinalize(chunk);
  }

  @Implementation(minSdk = Q, maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nativeGetTransparentRegion(long bitmapHandle, long chunk, Rect location) {
    return NinePatchNatives.nativeGetTransparentRegion(bitmapHandle, chunk, location);
  }

  /** Shadow picker for {@link NinePatch}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowNinePatch.class, ShadowNativeNinePatch.class);
    }
  }
}
