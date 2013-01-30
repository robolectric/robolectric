package org.robolectric.shadows;

import android.text.TextPaint;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(TextPaint.class)
public class ShadowTextPaint extends ShadowPaint {
    @Implementation
    public float measureText(String text) {
    	return text.length();
    }
}
