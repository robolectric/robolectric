package org.robolectric.shadows;

import android.graphics.text.MeasuredText;
import android.graphics.text.TextRunShaper;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.TextRunShaperNatives;
import org.robolectric.shadows.ShadowNativeTextRunShaper.Picker;
import org.robolectric.versioning.AndroidVersions.S;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link TextRunShaper} that is backed by native code */
@Implements(
    value = TextRunShaper.class,
    minSdk = S.SDK_INT,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeTextRunShaper {

  @Implementation(maxSdk = U.SDK_INT)
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

  @Implementation(maxSdk = U.SDK_INT)
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
