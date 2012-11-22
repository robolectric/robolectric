package com.xtremelabs.robolectric.shadows;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.res.ResourceExtractor;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

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
    public String getString(int index) {
      String str = (String) values.get(index);
      return str == null ? "" : str;
    }

    @Implementation
    public CharSequence getText(int index) {
      CharSequence str = (CharSequence) values.get(index);
      return str == null ? "" : str;
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

    public void populate(AttributeSet set, int[] attrs) {
        ResourceExtractor resourceExtractor = shadowOf(resources).getResourceLoader().getResourceExtractor();
        if (attrs == null) return;
        for (int attr : attrs) {
          String value = null;
            String attrName = resourceExtractor.getFullyQualifiedResourceName(attr);
            for (int setIndex = 0; setIndex < set.getAttributeCount(); setIndex++) {
                if (set.getAttributeName(setIndex).equals(attrName)) {
                    value = set.getAttributeValue(setIndex);
                }
            }
            values.add(value);
        }
    }
}
