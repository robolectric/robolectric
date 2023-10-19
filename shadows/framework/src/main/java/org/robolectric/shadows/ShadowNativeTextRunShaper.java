package org.robolectric.shadows;

import android.graphics.text.MeasuredText;
import android.graphics.text.TextRunShaper;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.TextRunShaperNatives;
import org.robolectric.shadows.ShadowNativeTextRunShaper.Picker;
import org.robolectric.versioning.AndroidVersions.S;

/** Shadow for {@link TextRunShaper} that is backed by native code */
@Implements(value = TextRunShaper.class, minSdk = S.SDK_INT, shadowPicker = Picker.class)
public class ShadowNativeTextRunShaper {

  @Implementation
  protected static long nativeShapeTextRun(
      char[] text,
      int start,
      int count,
      int contextStart,
      int contextCount,
      boolean isRtl,
      long nativePaint) {
    return TextRunShaperNatives.nativeShapeTextRun(
        text, start, count, contextStart, contextCount, isRtl, nativePaint);
  }

  @Implementation
  protected static long nativeShapeTextRun(
      String text,
      int start,
      int count,
      int contextStart,
      int contextCount,
      boolean isRtl,
      long nativePaint) {
    return TextRunShaperNatives.nativeShapeTextRun(
        text, start, count, contextStart, contextCount, isRtl, nativePaint);
  }

  /** Shadow picker for {@link MeasuredText}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeTextRunShaper.class);
    }
  }
}
