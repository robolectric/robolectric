package org.robolectric.shadows;

import android.graphics.PathIterator;
import dalvik.system.VMRuntime;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.PathIteratorNatives;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowNativePathIterator.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/**
 * Shadow for {@link PathIterator} that is backed by native code
 *
 * <p>TODO(hoisie): support on Android V
 */
@Implements(
    value = PathIterator.class,
    minSdk = U.SDK_INT,
    maxSdk = U.SDK_INT,
    shadowPicker = Picker.class)
public class ShadowNativePathIterator {
  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected static long nCreate(long nativePath) {
    return PathIteratorNatives.nCreate(nativePath);
  }

  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected static long nGetFinalizer() {
    return PathIteratorNatives.nGetFinalizer();
  }

  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected static int nNext(long nativeIterator, long pointsAddress) {
    ShadowVMRuntime shadowVmRuntime = Shadow.extract(VMRuntime.getRuntime());
    int token = PathIteratorNatives.nNext(nativeIterator, pointsAddress);
    float[] points = (float[]) shadowVmRuntime.getObjectForAddress(pointsAddress);
    shadowVmRuntime.getBackingBuffer(pointsAddress).asFloatBuffer().get(points);
    return token;
  }

  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected static int nPeek(long nativeIterator) {
    return PathIteratorNatives.nPeek(nativeIterator);
  }

  /** Shadow picker for {@link PathIterator}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowPathIterator.class, ShadowNativePathIterator.class);
    }
  }
}
