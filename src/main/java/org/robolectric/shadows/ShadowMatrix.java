package com.xtremelabs.robolectric.shadows;

import android.graphics.Matrix;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Matrix.class)
public class ShadowMatrix {
    private float scaleX = 1;
    private float transX;

    private float scaleY = 1;
    private float transY;

    // scaleX=0, skewX=1,  transX=2
    // skewY=3,  scaleY=4, transY=5
    // persp0=6, persp1=7, persp2=8

    // identity: Matrix{[1.0, 0.0, 0.0][0.0, 1.0, 0.0][0.0, 0.0, 1.0]}

    // drag down: Matrix{[1.0, 0.0, -1.3872986][0.0, 1.0, 0.37722778][0.0, 0.0, 1.0]}


    public void __constructor__(Matrix src) {
        set(src);
    }

    @Implementation
    public void set(Matrix src) {
        transX = shadowOf(src).transX;
        scaleX = shadowOf(src).scaleX;

        transY = shadowOf(src).transY;
        scaleY = shadowOf(src).scaleY;
    }

    @Implementation
    public void setTranslate(float dx, float dy) {
        transX = dx;
        transY = dy;
    }

    @Implementation
    public void postTranslate(float dx, float dy) {
        transX += dx;
        transY += dy;
    }

    public float getTransX() {
        return transX;
    }

    public float getTransY() {
        return transY;
    }

    @Implementation
    public boolean postScale(float sx, float sy, float px, float py) {
        scaleX *= sx;
        scaleY *= sy;
        return true;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }
}
