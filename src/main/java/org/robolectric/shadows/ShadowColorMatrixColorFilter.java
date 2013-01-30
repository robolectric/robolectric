package org.robolectric.shadows;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ColorMatrixColorFilter.class)
public class ShadowColorMatrixColorFilter {
    private ColorMatrix matrix;

    @Implementation
    public void __constructor__(ColorMatrix matrix) {
        this.matrix = matrix;
    }

    @Implementation
    public void __constructor__(float[] array) {
        this.matrix = new ColorMatrix(array);
    }

    @Override @Implementation
    public String toString() {
        return "ColorMatrixColorFilter<" + matrix + ">";
    }
}
