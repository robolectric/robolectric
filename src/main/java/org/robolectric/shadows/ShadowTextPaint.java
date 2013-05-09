package org.robolectric.shadows;

import android.text.TextPaint;
import org.robolectric.annotation.Implements;

@Implements(TextPaint.class)
public class ShadowTextPaint extends ShadowPaint {
}
