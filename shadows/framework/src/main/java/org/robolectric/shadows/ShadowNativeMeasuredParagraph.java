package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.graphics.Rect;
import android.text.MeasuredParagraph;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.MeasuredTextBuilderNatives;
import org.robolectric.nativeruntime.MeasuredTextNatives;
import org.robolectric.shadows.ShadowNativeMeasuredParagraph.Picker;

/** Shadow for {@link MeasuredParagraph} that is backed by native code */
@Implements(value = MeasuredParagraph.class, minSdk = P, maxSdk = P, shadowPicker = Picker.class)
public class ShadowNativeMeasuredParagraph {
  @Implementation
  protected static long nInitBuilder() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return MeasuredTextBuilderNatives.nInitBuilder();
  }

  @Implementation
  protected static void nAddStyleRun(
      long nativeBuilderPtr, long paintPtr, int start, int end, boolean isRtl) {
    MeasuredTextBuilderNatives.nAddStyleRun(nativeBuilderPtr, paintPtr, start, end, isRtl);
  }

  @Implementation
  protected static void nAddReplacementRun(
      long nativeBuilderPtr, long paintPtr, int start, int end, float width) {
    MeasuredTextBuilderNatives.nAddReplacementRun(nativeBuilderPtr, paintPtr, start, end, width);
  }

  @Implementation
  protected static long nBuildNativeMeasuredParagraph(
      long nativeBuilderPtr, char[] text, boolean computeHyphenation, boolean computeLayout) {
    return MeasuredTextBuilderNatives.nBuildMeasuredText(
        nativeBuilderPtr, 0, text, computeHyphenation, computeLayout);
  }

  @Implementation
  protected static void nFreeBuilder(long nativeBuilderPtr) {
    MeasuredTextBuilderNatives.nFreeBuilder(nativeBuilderPtr);
  }

  @Implementation
  protected static float nGetWidth(long nativePtr, int start, int end) {
    return MeasuredTextNatives.nGetWidth(nativePtr, start, end);
  }

  @Implementation
  protected static long nGetReleaseFunc() {
    return MeasuredTextNatives.nGetReleaseFunc();
  }

  @Implementation
  protected static int nGetMemoryUsage(long nativePtr) {
    return MeasuredTextNatives.nGetMemoryUsage(nativePtr);
  }

  @Implementation
  protected static void nGetBounds(long nativePtr, char[] buf, int start, int end, Rect rect) {
    MeasuredTextNatives.nGetBounds(nativePtr, buf, start, end, rect);
  }

  /** Shadow picker for {@link MeasuredParagraph}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowMeasuredParagraph.class, ShadowNativeMeasuredParagraph.class);
    }
  }
}
