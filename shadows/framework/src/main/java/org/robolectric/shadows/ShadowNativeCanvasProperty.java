package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.CanvasProperty;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.CanvasPropertyNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadows.ShadowNativeCanvasProperty.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link CanvasProperty} that is backed by native code */
@Implements(
    value = CanvasProperty.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeCanvasProperty<T> {

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nCreateFloat(float initialValue) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return CanvasPropertyNatives.nCreateFloat(initialValue);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nCreatePaint(long initialValuePaintPtr) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return CanvasPropertyNatives.nCreatePaint(initialValuePaintPtr);
  }

  /** Shadow picker for {@link CanvasProperty}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeCanvasProperty.class);
    }
  }
}
