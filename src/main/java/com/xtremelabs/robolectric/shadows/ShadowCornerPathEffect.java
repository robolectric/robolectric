package com.xtremelabs.robolectric.shadows;

import android.graphics.CornerPathEffect;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(CornerPathEffect.class)

public class ShadowCornerPathEffect {
    private float radius;

    public void __constructor__(float radius) {
        this.radius = radius;
     }

    public float getRadius() {
        return radius;
    }
}
