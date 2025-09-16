package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;

import android.content.res.ApkAssets;
import android.graphics.PathIterator;
import dalvik.system.VMRuntime;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.PathIteratorNatives;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowNativePathIterator.Picker;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow for {@link PathIterator} that is backed by native code. */
@Implements(
    value = PathIterator.class,
    minSdk = UPSIDE_DOWN_CAKE,
    callNativeMethodsByDefault = true,
    shadowPicker = Picker.class,
    isInAndroidSdk = false /* disable shadowOf generation */)
public class ShadowNativePathIterator {

  /**
   * The {@link PathIterator} static initializer invokes its own native methods. This has to be
   * deferred starting in Android V.
   */
  @Implementation(minSdk = VANILLA_ICE_CREAM)
  protected static void __staticInitializer__() {
    // deferred
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE, maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nCreate(long nativePath) {
    return PathIteratorNatives.nCreate(nativePath);
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE, maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nGetFinalizer() {
    return PathIteratorNatives.nGetFinalizer();
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  protected static int nNext(long nativeIterator, long pointsAddress) {
    ShadowVMRuntime shadowVmRuntime = Shadow.extract(VMRuntime.getRuntime());
    int token;
    if (RuntimeEnvironment.getApiLevel() == UPSIDE_DOWN_CAKE) {
      token = PathIteratorNatives.nNext(nativeIterator, pointsAddress);
    } else {
      token =
          ReflectionHelpers.callStaticMethod(
              PathIterator.class,
              Shadow.directNativeMethodName(ApkAssets.class.getName(), "nNext"),
              ClassParameter.from(long.class, nativeIterator),
              ClassParameter.from(long.class, pointsAddress));
    }
    float[] points = (float[]) shadowVmRuntime.getObjectForAddress(pointsAddress);
    shadowVmRuntime.getBackingBuffer(pointsAddress).asFloatBuffer().get(points);
    return token;
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE, maxSdk = UPSIDE_DOWN_CAKE)
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
