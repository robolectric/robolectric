package com.xtremelabs.robolectric.shadows;

import android.content.res.Resources;
import android.content.res.TypedArray;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TypedArray.class)
public class ShadowTypedArray implements UsesResources {
    private Resources resources;
    private List<Object> values = new ArrayList<Object>();

    public void injectResources(Resources resources) {
        this.resources = resources;
    }

    @Implementation
    public Resources getResources() {
        return resources;
    }

    public void add(Object attributeValue) {
        values.add(attributeValue);
    }

    @Implementation
    public java.lang.String getString(int index) {
        return (String) values.get(index);
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
