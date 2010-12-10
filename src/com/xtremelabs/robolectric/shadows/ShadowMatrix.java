package com.xtremelabs.robolectric.shadows;

import android.graphics.Matrix;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Matrix.class)
public class ShadowMatrix {
    private float x;
    private float y;

    public void __constructor__(Matrix src) {
        x = shadowOf(src).x;
        y = shadowOf(src).y;
    }

    @Implementation
    public void setTranslate(float dx, float dy) {
        x = dx;
        y = dy;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
