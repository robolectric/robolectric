package com.xtremelabs.robolectric.shadows;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import static android.util.TypedValue.*;


/**
 * Portions of this file were copied from the Android source code,
 * licenced under the Apache License, Version 2.0
 *
 * http://www.google.com/codesearch/p?hl=en#uX1GffpyOZk/core/java/android/util/TypedValue.java
 */


@SuppressWarnings({"UnusedDeclaration"})
@Implements(TypedValue.class)
public class ShadowTypedValue {
    @Implementation
    public static float applyDimension(int unit, float value, DisplayMetrics metrics) {
        switch (unit) {
            case COMPLEX_UNIT_PX:
                return value;
            case COMPLEX_UNIT_DIP:
                return value * metrics.density;
            case COMPLEX_UNIT_SP:
                return value * metrics.scaledDensity;
            case COMPLEX_UNIT_PT:
                return value * metrics.xdpi * (1.0f / 72);
            case COMPLEX_UNIT_IN:
                return value * metrics.xdpi;
            case COMPLEX_UNIT_MM:
                return value * metrics.xdpi * (1.0f / 25.4f);
        }
        return 0;
    }
}
