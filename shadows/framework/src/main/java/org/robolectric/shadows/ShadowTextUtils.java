package org.robolectric.shadows;

import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Implement {@lint TextUtils#ellipsize} by truncating the text.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(TextUtils.class)
public class ShadowTextUtils {

  @Implementation
  protected static CharSequence ellipsize(
      CharSequence text, TextPaint p, float avail, TruncateAt where) {
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
}
