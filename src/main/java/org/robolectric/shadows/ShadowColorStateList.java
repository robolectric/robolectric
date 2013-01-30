package com.xtremelabs.robolectric.shadows;

import android.content.res.ColorStateList;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ColorStateList.class)
public class ShadowColorStateList {
    @Implementation
    public static ColorStateList valueOf(int color) {
        return new ColorStateList(null, null);
    }
}
