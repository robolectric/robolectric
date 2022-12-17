package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.annotation.FloatRange;
import android.annotation.IntRange;
import android.graphics.text.LineBreaker;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.LineBreakerNatives;
import org.robolectric.shadows.ShadowNativeLineBreaker.Picker;

/** Shadow for {@link LineBreaker} that is backed by native code */
@Implements(value = LineBreaker.class, minSdk = Q, shadowPicker = Picker.class)
public class ShadowNativeLineBreaker {
  @Implementation
  protected static long nInit(
      int breakStrategy, int hyphenationFrequency, boolean isJustified, int[] indents) {
    return LineBreakerNatives.nInit(breakStrategy, hyphenationFrequency, isJustified, indents);
  }

  @Implementation
  protected static long nGetReleaseFunc() {
    // Called first by the static initializer.
    DefaultNativeRuntimeLoader.injectAndLoad();
    return LineBreakerNatives.nGetReleaseFunc();
  }

  @Implementation
  protected static long nComputeLineBreaks(
      long nativePtr,
      char[] text,
      long measuredTextPtr,
      @IntRange(from = 0) int length,
      @FloatRange(from = 0.0f) float firstWidth,
      @IntRange(from = 0) int firstWidthLineCount,
      @FloatRange(from = 0.0f) float restWidth,
      float[] variableTabStops,
      float defaultTabStop,
      @IntRange(from = 0) int indentsOffset) {
    return LineBreakerNatives.nComputeLineBreaks(
        nativePtr,
        text,
        measuredTextPtr,
        length,
        firstWidth,
        firstWidthLineCount,
        restWidth,
        variableTabStops,
        defaultTabStop,
        indentsOffset);
  }

  // Result accessors
  @Implementation
  protected static int nGetLineCount(long ptr) {
    return LineBreakerNatives.nGetLineCount(ptr);
  }

  @Implementation
  protected static int nGetLineBreakOffset(long ptr, int idx) {
    return LineBreakerNatives.nGetLineBreakOffset(ptr, idx);
  }

  @Implementation
  protected static float nGetLineWidth(long ptr, int idx) {
    return LineBreakerNatives.nGetLineWidth(ptr, idx);
  }

  @Implementation
  protected static float nGetLineAscent(long ptr, int idx) {
    return LineBreakerNatives.nGetLineAscent(ptr, idx);
  }

  @Implementation
  protected static float nGetLineDescent(long ptr, int idx) {
    return LineBreakerNatives.nGetLineDescent(ptr, idx);
  }

  @Implementation
  protected static int nGetLineFlag(long ptr, int idx) {
    return LineBreakerNatives.nGetLineFlag(ptr, idx);
  }

  @Implementation
  protected static long nGetReleaseResultFunc() {
    return LineBreakerNatives.nGetReleaseResultFunc();
  }

  /** Shadow picker for {@link LineBreaker}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowLineBreaker.class, ShadowNativeLineBreaker.class);
    }
  }
}
