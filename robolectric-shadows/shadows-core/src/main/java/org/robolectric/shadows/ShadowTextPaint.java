package org.robolectric.shadows;

import android.text.TextPaint;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.text.TextPaint}.
 */
@Implements(TextPaint.class)
public class ShadowTextPaint extends ShadowPaint {
}
