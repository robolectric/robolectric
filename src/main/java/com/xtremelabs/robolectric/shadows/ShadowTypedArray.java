package com.xtremelabs.robolectric.shadows;

import android.content.res.Resources;
import android.content.res.TypedArray;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TypedArray.class)
public class ShadowTypedArray implements UsesResources {
    private Resources resources;

    public void injectResources(Resources resources) {
        this.resources = resources;
    }

    @Implementation
    public Resources getResources() {
        return resources;
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

    @Implementation
    public float getDimension(int index, float defValue) {
        return defValue;
    }
}
