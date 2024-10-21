package org.robolectric.shadows;

import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Implement {@link TextUtils#ellipsize} by truncating the text. */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(TextUtils.class)
public class ShadowTextUtils {
  // See
  // https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/text/TextUtils.java;l=96?q=TextUtils.java&ss=android%2Fplatform%2Fsuperproject%2Fmain
  private static final String ELLIPSIS_NORMAL = "\u2026"; // HORIZONTAL ELLIPSIS (…)

  @Implementation
  protected static CharSequence ellipsize(
      CharSequence text, TextPaint p, float avail, TruncateAt where) {
    // The AOSP implementation doesn't check text's null, so we don't check it here either.
    // This shadow follows the convention of ShadowPaint#measureText where each
    // characters width is 1.0.
    int len = text.length();
    float measuredWidth = p.measureText(text, 0, len);
    if (measuredWidth <= avail) {
      return text;
    }
    float ellipsisWidth = p.measureText(ELLIPSIS_NORMAL);
    // Calculate available width after removing ellipsis.
    avail -= ellipsisWidth;
    // Current Robolectric's breakText doesn't work as expected, the workaround is to think
    // every character has the same width, although it is not always true.
    int widthPerChar = (int) measuredWidth / len;
    // If available length is less than or equals to ellipsis size, we should return empty string.
    if (avail <= 0) {
      return "";
    } else if (where == TruncateAt.START) {
      int right = (int) (len - avail / widthPerChar);
      if (right >= len) {
        return "";
      } else {
        return TextUtils.concat(ELLIPSIS_NORMAL, text.subSequence(right, len));
      }
    } else if (where == TruncateAt.END) {
      int left = (int) (avail / widthPerChar);
      if (left > 0) {
        return TextUtils.concat(text.subSequence(0, left), ELLIPSIS_NORMAL);
      } else {
        return "";
      }
    } else {
      // See TextUtilTest.java, the Android default implementation prefers the starting has more
      // characters than the ending if it is not even.
      int right = len - (int) (avail / 2 / widthPerChar);
      avail -= p.measureText(text, right, len);
      int left = (int) (avail / widthPerChar);
      return TextUtils.concat(
          text.subSequence(0, left), ELLIPSIS_NORMAL, text.subSequence(right, len));
    }
  }
}
