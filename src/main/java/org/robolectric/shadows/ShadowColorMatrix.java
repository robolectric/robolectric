package com.xtremelabs.robolectric.shadows;

import android.graphics.ColorMatrix;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.util.Join;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ColorMatrix.class)
public class ShadowColorMatrix {

    private float[] src;

    public void __constructor__(float[] src) {
        this.src = src;
    }

    public void __constructor__() {
        reset();
    }

    public void __constructor__(ColorMatrix src) {
        this.src = shadowOf(src).src;
    }

    @Implementation
    public void reset() {
        src = new float[20];
        src[0] = src[6] = src[12] = src[18] = 1;
    }

    @Implementation
    public void setSaturation(float sat) {
        reset();
        float[] m = src;

        final float invSat = 1 - sat;
        final float R = 0.213f * invSat;
        final float G = 0.715f * invSat;
        final float B = 0.072f * invSat;

        m[0] = R + sat;
        m[1] = G;
        m[2] = B;

        m[5] = R;
        m[6] = G + sat;
        m[7] = B;

        m[10] = R;
        m[11] = G;
        m[12] = B + sat;
    }

    @Override @Implementation
    public String toString() {
        List<String> floats = new ArrayList<String>();
        for (float f : src) {
            String format = String.format("%.2f", f);
            format = format.replace(".00", "");
            floats.add(format);
        }
        return Join.join(",", floats);
    }
}
