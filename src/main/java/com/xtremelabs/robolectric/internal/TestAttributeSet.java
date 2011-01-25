package com.xtremelabs.robolectric.internal;

import android.util.AttributeSet;
import android.view.View;
import com.xtremelabs.robolectric.res.AttrResourceLoader;
import com.xtremelabs.robolectric.res.ResourceExtractor;

import java.util.HashMap;
import java.util.Map;

public class TestAttributeSet implements AttributeSet {
    Map<String, String> attributes = new HashMap<String, String>();
    private ResourceExtractor resourceExtractor;
    private AttrResourceLoader attrResourceLoader;
    private Class<? extends View> viewClass;

    public TestAttributeSet(Map<String, String> attributes, ResourceExtractor resourceExtractor,
                            AttrResourceLoader attrResourceLoader, Class<? extends View> viewClass) {
        this.attributes = attributes;
        this.resourceExtractor = resourceExtractor;
        this.attrResourceLoader = attrResourceLoader;
        this.viewClass = viewClass;
    }

    @Override public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
        String value = getAttributeValueInMap(attribute);
        return (value != null) ? Boolean.valueOf(value) : defaultValue;
    }

    @Override public String getAttributeValue(String namespace, String attribute) {
        return getAttributeValueInMap(attribute);
    }

    @Override public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
        String value = getAttributeValueInMap(attribute);

        if (attrResourceLoader.hasAttributeFor(viewClass, "xxx", attribute)) {
            value = attrResourceLoader.convertValueToEnum(viewClass, "xxx", attribute, value);
        }

        return (value != null) ? Integer.valueOf(value) : defaultValue;
    }

    @Override public int getAttributeCount() {
        throw new UnsupportedOperationException();
    }

    @Override public String getAttributeName(int index) {
        throw new UnsupportedOperationException();
    }

    @Override public String getAttributeValue(int index) {
        throw new UnsupportedOperationException();
    }

    @Override public String getPositionDescription() {
        throw new UnsupportedOperationException();
    }

    @Override public int getAttributeNameResource(int index) {
        throw new UnsupportedOperationException();
    }

    @Override public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override public int getAttributeListValue(int index, String[] options, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean getAttributeBooleanValue(int resourceId, boolean defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
        String value = getAttributeValueInMap(attribute);
        return (value != null) ? resourceExtractor.getLocalResourceId(value) : defaultValue;
    }

    @Override public int getAttributeResourceValue(int resourceId, int defaultValue) {
        String attrName = resourceExtractor.getResourceName(resourceId);
        String value = getAttributeValueInMap(attrName);
        return (value == null) ? defaultValue : resourceExtractor.getLocalResourceId(value);
    }

    @Override public int getAttributeIntValue(int index, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override public int getAttributeUnsignedIntValue(int index, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override public float getAttributeFloatValue(int index, float defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override public String getIdAttribute() {
        throw new UnsupportedOperationException();
    }

    @Override public String getClassAttribute() {
        throw new UnsupportedOperationException();
    }

    @Override public int getIdAttributeResourceValue(int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override public int getStyleAttribute() {
        throw new UnsupportedOperationException();
    }

    private String getAttributeValueInMap(String attribute) {
        String value = null;
        for (String key : attributes.keySet()) {
            String mappedKey = key;
            if (key.contains(":")) {
                mappedKey = key.split(":")[1];
            }
            if (mappedKey.equals(attribute)) {
                value = attributes.get(key);
                break;
            }
        }
        return value;
    }
}
