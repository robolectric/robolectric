package com.xtremelabs.robolectric.shadows;

import android.content.res.Resources;
import android.content.res.TypedArray;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TypedArray.class)
public class ShadowTypedArray {
    private List<Object> values = new ArrayList<Object>();

    @Implementation
    public Resources getResources() {
        return Robolectric.application.getResources();
    }

    public void add(Object attributeValue) {
        values.add(attributeValue);
    }

    @Implementation
    public java.lang.String getString(int index) {
        return (String) values.get(index);
    }
}
