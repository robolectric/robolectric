package com.xtremelabs.robolectric.shadows;

import android.text.style.ScaleXSpan;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ScaleXSpan.class)
public class ShadowScaleXSpan {
    private float scaleX;

    public void __constructor__(float scaleX) {
        this.scaleX = scaleX;
    }

    @Implementation
    public float getScaleX() {
        return scaleX;
    }
}
