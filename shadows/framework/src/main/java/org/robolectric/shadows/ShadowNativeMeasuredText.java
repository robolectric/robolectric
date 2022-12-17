package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.annotation.FloatRange;
import android.annotation.IntRange;
import android.graphics.Rect;
import android.graphics.text.MeasuredText;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.MeasuredTextBuilderNatives;
import org.robolectric.nativeruntime.MeasuredTextNatives;
import org.robolectric.shadows.ShadowNativeMeasuredText.Picker;

/** Shadow for {@link MeasuredText} that is backed by native code */
@Implements(value = MeasuredText.class, minSdk = Q, shadowPicker = Picker.class)
public class ShadowNativeMeasuredText {
  @Implementation
  protected static float nGetWidth(
      /* Non Zero */ long nativePtr, @IntRange(from = 0) int start, @IntRange(from = 0) int end) {
    return MeasuredTextNatives.nGetWidth(nativePtr, start, end);
  }

  @Implementation
  protected static /* Non Zero */ long nGetReleaseFunc() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return MeasuredTextNatives.nGetReleaseFunc();
  }

  @Implementation
  protected static int nGetMemoryUsage(/* Non Zero */ long nativePtr) {
    return MeasuredTextNatives.nGetMemoryUsage(nativePtr);
  }

  @Implementation
  protected static void nGetBounds(long nativePtr, char[] buf, int start, int end, Rect rect) {
    MeasuredTextNatives.nGetBounds(nativePtr, buf, start, end, rect);
  }

  @Implementation
  protected static float nGetCharWidthAt(long nativePtr, int offset) {
    return MeasuredTextNatives.nGetCharWidthAt(nativePtr, offset);
  }

  /** Shadow for {@link MeasuredText.Builder} that is backed by native code */
  @Implements(
      value = MeasuredText.Builder.class,
      minSdk = Q,
      shadowPicker = ShadowNativeMeasuredTextBuilder.Picker.class)
  public static class ShadowNativeMeasuredTextBuilder {
    @Implementation
    protected static /* Non Zero */ long nInitBuilder() {
      return MeasuredTextBuilderNatives.nInitBuilder();
    }

    @Implementation(maxSdk = S_V2)
    protected static void nAddStyleRun(
        /* Non Zero */ long nativeBuilderPtr,
        /* Non Zero */ long paintPtr,
        @IntRange(from = 0) int start,
        @IntRange(from = 0) int end,
        boolean isRtl) {
      MeasuredTextBuilderNatives.nAddStyleRun(nativeBuilderPtr, paintPtr, start, end, isRtl);
    }

    @Implementation(minSdk = TIRAMISU)
    protected static void nAddStyleRun(
        /* Non Zero */ long nativeBuilderPtr,
        /* Non Zero */ long paintPtr,
        int lineBreakStyle,
        int lineBreakWordStyle,
        int start,
        int end,
        boolean isRtl) {
      MeasuredTextBuilderNatives.nAddStyleRun(nativeBuilderPtr, paintPtr, start, end, isRtl);
    }

    @Implementation
    protected static void nAddReplacementRun(
        /* Non Zero */ long nativeBuilderPtr,
        /* Non Zero */ long paintPtr,
        @IntRange(from = 0) int start,
        @IntRange(from = 0) int end,
        @FloatRange(from = 0) float width) {
      MeasuredTextBuilderNatives.nAddReplacementRun(nativeBuilderPtr, paintPtr, start, end, width);
    }

    @Implementation(maxSdk = S_V2)
    protected static long nBuildMeasuredText(
        /* Non Zero */ long nativeBuilderPtr,
        long hintMtPtr,
        char[] text,
        boolean computeHyphenation,
        boolean computeLayout) {
      return MeasuredTextBuilderNatives.nBuildMeasuredText(
          nativeBuilderPtr, hintMtPtr, text, computeHyphenation, computeLayout);
    }

    @Implementation(minSdk = TIRAMISU)
    protected static long nBuildMeasuredText(
        /* Non Zero */ long nativeBuilderPtr,
        long hintMtPtr,
        char[] text,
        boolean computeHyphenation,
        boolean computeLayout,
        boolean fastHyphenationMode) {
      return MeasuredTextBuilderNatives.nBuildMeasuredText(
          nativeBuilderPtr, hintMtPtr, text, computeHyphenation, computeLayout);
    }

    @Implementation
    protected static void nFreeBuilder(/* Non Zero */ long nativeBuilderPtr) {
      MeasuredTextBuilderNatives.nFreeBuilder(nativeBuilderPtr);
    }

    /** Shadow picker for {@link MeasuredText.Builder}. */
    public static final class Picker extends GraphicsShadowPicker<Object> {
      public Picker() {
        super(
            org.robolectric.shadows.ShadowMeasuredTextBuilder.class,
            ShadowNativeMeasuredText.ShadowNativeMeasuredTextBuilder.class);
      }
    }
  }

  /** Shadow picker for {@link MeasuredText}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeMeasuredText.class);
    }
  }
}
