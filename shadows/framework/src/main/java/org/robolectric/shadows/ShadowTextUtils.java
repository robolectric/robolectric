package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Implement {@link TextUtils#ellipsize} by truncating the text. */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(TextUtils.class)
public class ShadowTextUtils {

  @Implementation
  protected static CharSequence ellipsize(
      CharSequence text, TextPaint p, float avail, TruncateAt where) {
    if (useRealEllipsize()) {
      return reflector(TextUtilsReflector.class).ellipsize(text, p, avail, where);
    }
    // This shadow follows the convention of ShadowPaint#measureText where each
    // characters width is 1.0.
    if (avail <= 0) {
      return "";
    } else if (text.length() < (int) (avail)) {
      return text;
    } else {
      return text.subSequence(0, (int) avail);
    }
  }

  private static boolean useRealEllipsize() {
    return ShadowView.useRealGraphics()
        && Boolean.parseBoolean(System.getProperty("robolectric.useRealEllipsize", "false"));
  }

  @ForType(TextUtils.class)
  interface TextUtilsReflector {
    @Direct
    @Static
    CharSequence ellipsize(CharSequence text, TextPaint p, float avail, TruncateAt where);
  }
}
