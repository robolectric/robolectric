package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.Interpolator;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.InterpolatorNatives;
import org.robolectric.shadows.ShadowNativeInterpolator.Picker;

/** Shadow for {@link Interpolator} that is backed by native code */
@Implements(value = Interpolator.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeInterpolator {

  @Implementation
  protected static long nativeConstructor(int valueCount, int frameCount) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return InterpolatorNatives.nativeConstructor(valueCount, frameCount);
  }

  @Implementation
  protected static void nativeDestructor(long nativeInstance) {
    InterpolatorNatives.nativeDestructor(nativeInstance);
  }

  @Implementation
  protected static void nativeReset(long nativeInstance, int valueCount, int frameCount) {
    InterpolatorNatives.nativeReset(nativeInstance, valueCount, frameCount);
  }

  @Implementation
  protected static void nativeSetKeyFrame(
      long nativeInstance, int index, int msec, float[] values, float[] blend) {
    InterpolatorNatives.nativeSetKeyFrame(nativeInstance, index, msec, values, blend);
  }

  @Implementation
  protected static void nativeSetRepeatMirror(
      long nativeInstance, float repeatCount, boolean mirror) {
    InterpolatorNatives.nativeSetRepeatMirror(nativeInstance, repeatCount, mirror);
  }

  @Implementation
  protected static int nativeTimeToValues(long nativeInstance, int msec, float[] values) {
    return InterpolatorNatives.nativeTimeToValues(nativeInstance, msec, values);
  }

  /** Shadow picker for {@link Interpolator}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeInterpolator.class);
    }
  }
}
