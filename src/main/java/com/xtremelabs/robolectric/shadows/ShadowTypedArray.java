package com.xtremelabs.robolectric.shadows;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.res.ResourceExtractor;
import com.xtremelabs.robolectric.tester.android.util.Attribute;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TypedArray.class)
public class ShadowTypedArray implements UsesResources {
    private Resources resources;
    private AttributeSet values;
    private int[] attrs;
    private ResourceExtractor resourceExtractor;

    public void injectResources(Resources resources) {
        this.resources = resources;
        resourceExtractor = shadowOf(resources).getResourceLoader().getResourceExtractor();
    }

    @Implementation
    public Resources getResources() {
        return resources;
    }

    @Implementation
    public CharSequence getText(int index) {
        CharSequence str = values.getAttributeValue(namespace(index), name(index));
        return str == null ? "" : str;
    }

    @Implementation
    public String getString(int index) {
        String str = values.getAttributeValue(namespace(index), name(index));
        return str == null ? "" : str;
    }

    @Implementation
    public boolean getBoolean(int index, boolean defValue) {
        return values.getAttributeBooleanValue(namespace(index), name(index), defValue);
    }

    @Implementation
    public int getInt(int index, int defValue) {
        return values.getAttributeIntValue(namespace(index), name(index), defValue);
    }

    @Implementation
    public float getFloat(int index, float defValue) {
        return values.getAttributeFloatValue(namespace(index), name(index), defValue);
    }

    @Implementation
    public int getInteger(int index, int defValue) {
        return values.getAttributeIntValue(namespace(index), name(index), defValue);
    }

    @Implementation
    public float getDimension(int index, float defValue) {
        return defValue;
    }

    @Implementation
    public int getResourceId(int index, int defValue) {
        return defValue;
    }

    private String namespace(int index) {
        return Attribute.getNamespace(resourceExtractor.getFullyQualifiedResourceName(attrs[index]));
    }

    private String name(int index) {
        return Attribute.getName(resourceExtractor.getFullyQualifiedResourceName(attrs[index]));
    }

    public void populate(AttributeSet set, int[] attrs) {
        if (this.values != null || this.attrs != null) throw new IllegalStateException();
        this.values = set;
        this.attrs = attrs;
    }
}
