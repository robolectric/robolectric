package com.xtremelabs.robolectric.shadows;

import android.content.res.Resources;
import android.content.res.TypedArray;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TypedArray.class)
public class ShadowTypedArray {
    @Implementation
    public Resources getResources() {
        return Robolectric.application.getResources();
    }

    @Implementation
    public int getInt(int index, int defValue) {
        return defValue;
    }

    @Implementation
    public int getInteger(int index, int defValue) {
        return defValue;
    }

    @Implementation
    public int getResourceId(int index, int defValue) {
        return defValue;
    }
}
