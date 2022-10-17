package org.robolectric.shadows;

import android.graphics.text.LineBreaker;
import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.res.android.NativeObjRegistry;

/** Shadow for android.graphics.text.LineBreaker */
@Implements(value = LineBreaker.class, isInAndroidSdk = false, minSdk = Build.VERSION_CODES.Q)
public class ShadowLineBreaker {

  static class NativeLineBreakerResult {
    char[] chars;
  }

  static final NativeObjRegistry<NativeLineBreakerResult> nativeObjectRegistry =
      new NativeObjRegistry<>(NativeLineBreakerResult.class);

  @Implementation
  protected static long nComputeLineBreaks(
      /* non zero */ long nativePtr,
      // Inputs
      char[] text,
      long measuredTextPtr,
      int length,
      float firstWidth,
      int firstWidthLineCount,
      float restWidth,
      float[] variableTabStops,
      float defaultTabStop,
      int indentsOffset) {
    NativeLineBreakerResult nativeLineBreakerResult = new NativeLineBreakerResult();
    nativeLineBreakerResult.chars = text;
    return nativeObjectRegistry.register(nativeLineBreakerResult);
  }

  @Implementation
  protected static int nGetLineCount(long ptr) {
    return 1;
  }

  @Implementation
  protected static int nGetLineBreakOffset(long ptr, int idx) {
    NativeLineBreakerResult nativeLineBreakerResult = nativeObjectRegistry.getNativeObject(ptr);
    return (nativeLineBreakerResult != null) ? nativeLineBreakerResult.chars.length : 1;
  }
}
