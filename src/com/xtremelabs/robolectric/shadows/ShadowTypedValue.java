package com.xtremelabs.robolectric.shadows;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TypedValue.class)
public class ShadowTypedValue {
    @Implementation
    public static float applyDimension(int unit, float value, DisplayMetrics metrics) {
        return value;
    }
}
