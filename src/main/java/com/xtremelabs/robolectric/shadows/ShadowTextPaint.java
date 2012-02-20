package com.xtremelabs.robolectric.shadows;

import android.text.TextPaint;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(TextPaint.class)
public class ShadowTextPaint extends ShadowPaint {
    @Implementation
    public float measureText(String text) {
    	return text.length();
    }
}
