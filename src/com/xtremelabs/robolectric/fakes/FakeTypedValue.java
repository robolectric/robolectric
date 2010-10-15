package com.xtremelabs.robolectric.fakes;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TypedValue.class)
public class FakeTypedValue {
    @Implementation
    public static float applyDimension(int unit, float value, DisplayMetrics metrics) {
        return value;
    }
}
