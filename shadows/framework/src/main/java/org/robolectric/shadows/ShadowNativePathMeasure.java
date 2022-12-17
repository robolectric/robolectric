package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.PathMeasure;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.PathMeasureNatives;
import org.robolectric.shadows.ShadowNativePathMeasure.Picker;

/** Shadow for {@link PathMeasure} that is backed by native code */
@Implements(
    value = PathMeasure.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false)
public class ShadowNativePathMeasure {

  @Implementation(minSdk = O)
  protected static long native_create(long nativePath, boolean forceClosed) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return PathMeasureNatives.native_create(nativePath, forceClosed);
  }

  @Implementation(minSdk = O)
  protected static void native_setPath(long nativeInstance, long nativePath, boolean forceClosed) {
    PathMeasureNatives.native_setPath(nativeInstance, nativePath, forceClosed);
  }

  @Implementation(minSdk = O)
  protected static float native_getLength(long nativeInstance) {
    return PathMeasureNatives.native_getLength(nativeInstance);
  }

  @Implementation(minSdk = O)
  protected static boolean native_getPosTan(
      long nativeInstance, float distance, float[] pos, float[] tan) {
    return PathMeasureNatives.native_getPosTan(nativeInstance, distance, pos, tan);
  }

  @Implementation(minSdk = O)
  protected static boolean native_getMatrix(
      long nativeInstance, float distance, long nativeMatrix, int flags) {
    return PathMeasureNatives.native_getMatrix(nativeInstance, distance, nativeMatrix, flags);
  }

  @Implementation(minSdk = O)
  protected static boolean native_getSegment(
      long nativeInstance, float startD, float stopD, long nativePath, boolean startWithMoveTo) {
    return PathMeasureNatives.native_getSegment(
        nativeInstance, startD, stopD, nativePath, startWithMoveTo);
  }

  @Implementation(minSdk = O)
  protected static boolean native_isClosed(long nativeInstance) {
    return PathMeasureNatives.native_isClosed(nativeInstance);
  }

  @Implementation(minSdk = O)
  protected static boolean native_nextContour(long nativeInstance) {
    return PathMeasureNatives.native_nextContour(nativeInstance);
  }

  @Implementation(minSdk = O)
  protected static void native_destroy(long nativeInstance) {
    PathMeasureNatives.native_destroy(nativeInstance);
  }

  /** Shadow picker for {@link PathMeasure}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowPathMeasure.class, ShadowNativePathMeasure.class);
    }
  }
}
