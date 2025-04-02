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

/**
 * Implement {@link TextUtils#ellipsize} by truncating the text.
 *
 * <p>Ideally this would use {@link GraphicsShadowPicker} to get disabled when native graphics are
 * enabled, but TextUtils is used by {@link android.os.Build}, which is often referenced in static
 * initializers, and shadow pickers referencing {@link org.robolectric.config.ConfigurationRegistry}
 * are not supported in static initializers.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(TextUtils.class)
public class ShadowTextUtils {

  @Implementation
  protected static CharSequence ellipsize(
      CharSequence text, TextPaint p, float avail, TruncateAt where) {
    if (ShadowView.useRealGraphics()) {
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

  @ForType(TextUtils.class)
  interface TextUtilsReflector {
    @Direct
    @Static
    CharSequence ellipsize(CharSequence text, TextPaint p, float avail, TruncateAt where);
  }
}
