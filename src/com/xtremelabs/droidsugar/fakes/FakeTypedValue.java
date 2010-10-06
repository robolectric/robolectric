package com.xtremelabs.droidsugar.fakes;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TypedValue.class)
public class FakeTypedValue {
    public static float applyDimension(int unit, float value, DisplayMetrics metrics) {
        return value;
    }
}
