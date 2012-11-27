package com.xtremelabs.robolectric.tester.android.util;

import android.util.AttributeSet;
import android.view.View;
import com.xtremelabs.robolectric.res.AttrResourceLoader;
import com.xtremelabs.robolectric.res.ResourceExtractor;
import com.xtremelabs.robolectric.res.ResourcePath;
import com.xtremelabs.robolectric.util.I18nException;

import java.util.ArrayList;
import java.util.List;

public class TestAttributeSet implements AttributeSet {
    private final List<Attribute> attributes;
    private final ResourceExtractor resourceExtractor;
    private AttrResourceLoader attrResourceLoader;
    private Class<? extends View> viewClass;

    /**
     * Names of attributes to be validated for i18n-safe values.
     */
    private static final String strictI18nAttrs[] = {
            "android:attr/text",
            "android:attr/title",
            "android:attr/titleCondensed",
            "android:attr/summary"
    };

    public TestAttributeSet() {
        this(new ArrayList<Attribute>());
    }

    public TestAttributeSet(List<Attribute> attributes, ResourceExtractor resourceExtractor,
                            AttrResourceLoader attrResourceLoader, Class<? extends View> viewClass) {
        this.attributes = attributes;
        this.resourceExtractor = resourceExtractor;
        this.attrResourceLoader = attrResourceLoader;
        this.viewClass = viewClass;
    }

    public TestAttributeSet(List<Attribute> attributes) {
        this(attributes, new ResourceExtractor());
    }

    public TestAttributeSet(ResourceExtractor resourceExtractor) {
        this.attributes = new ArrayList<Attribute>();
        this.resourceExtractor = resourceExtractor;
        this.attrResourceLoader = new AttrResourceLoader(resourceExtractor);
    }

    public TestAttributeSet(List<Attribute> attributes, ResourceExtractor resourceExtractor) {
        this(attributes, resourceExtractor, new AttrResourceLoader(resourceExtractor), null);
    }

    public TestAttributeSet(List<Attribute> attributes, Class<?> rFileClass) throws Exception {
        this(attributes, new ResourceExtractor(new ResourcePath(rFileClass, null, null)));
    }

    public TestAttributeSet put(String fullyQualifiedName, String value, String valuePackage) {
        return put(new Attribute(fullyQualifiedName, value, valuePackage));
    }

    public TestAttributeSet put(Attribute attribute) {
        attributes.add(attribute);
        return this;
    }

    @Override
    public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
        Attribute attr = findByName(namespace, attribute);
        return (attr != null) ? Boolean.valueOf(attr.value) : defaultValue;
    }

    @Override
    public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
        Attribute attr = findByName(namespace, attribute);
        if (attr == null) return defaultValue;
        String value = attr.value;

        if (attrResourceLoader.hasAttributeFor(viewClass, namespace, attribute)) {
            value = attrResourceLoader.convertValueToEnum(viewClass, namespace, attribute, value);
            if (value != null && value.startsWith("0x")) value = value.substring(2);

            return (value != null) ? Integer.valueOf(value) : defaultValue;
        } else {
            if (value.startsWith("0x")) {
                value = value.substring(2);
                return (value != null) ? Integer.valueOf(value) : defaultValue;
            } else {
                return Integer.valueOf(value);
            }
        }
    }

    @Override
    public int getAttributeCount() {
        return attributes.size();
    }

    @Override
    public String getAttributeName(int index) {
        return attributes.get(index).resName.getFullyQualifiedName();
    }

    @Override
    public String getAttributeValue(String namespace, String attribute) {
        Attribute attr = findByName(namespace, attribute);
        return (attr != null) ? attr.value : null;
    }

    @Override
    public String getAttributeValue(int index) {
        try {
            return attributes.get(index).value;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

  @Override
    public String getPositionDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getAttributeNameResource(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
        Attribute attr = findByName(namespace, attribute);
        return (attr != null) ? Float.valueOf(attr.value) : defaultValue;
    }

    @Override
    public int getAttributeListValue(int index, String[] options, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getAttributeBooleanValue(int resourceId, boolean defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override public int getAttributeResourceValue(String namespace, String attribute, int defaultValue) {
        Attribute attr = findByName(namespace, attribute);
        if (attr == null) return defaultValue;

        Integer resourceId = resourceExtractor.getResourceId(attr.value, attr.contextPackageName);
        return resourceId == null ? defaultValue : resourceId;
    }

    @Override
    public int getAttributeResourceValue(int resourceId, int defaultValue) {
        String attrName = resourceExtractor.getResourceName(resourceId);
        Attribute attr = findByName(null, attrName);
        if (attr == null) return defaultValue;
        Integer extracted = resourceExtractor.getResourceId(attr.value, attr.contextPackageName);
        return (extracted == null) ? defaultValue : extracted;
    }

    @Override
    public int getAttributeIntValue(int index, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getAttributeUnsignedIntValue(int index, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getAttributeFloatValue(int index, float defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getIdAttribute() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getClassAttribute() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIdAttributeResourceValue(int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override public int getStyleAttribute() {
        Attribute styleAttribute = findByName(":attr/style");
        if (styleAttribute == null) {
            // Per Android specifications, return 0 if there is no style.
            return 0;
        }
        if (resourceExtractor != null) {
            Integer i = resourceExtractor.getResourceId(styleAttribute.value, styleAttribute.contextPackageName);
            if (i != null) return i;
        }
        return 0;
    }


    public void validateStrictI18n() {
        for (String key : strictI18nAttrs) {
            Attribute attribute = findByName(key);
            if (attribute != null) {
                if (!attribute.value.startsWith("@string/")) {
                    throw new I18nException("View class: " + (viewClass != null ? viewClass.getName() : "") +
                            " has attribute: " + key + " with hardcoded value: \"" + attribute.value + "\" and is not i18n-safe.");
                }
            }
        }
    }

    private Attribute findByName(String packageName, String attrName) {
        return findByName(packageName + ":attr/" + attrName);
    }

    private Attribute findByName(String fullyQualifiedName) {
        return Attribute.find(attributes, fullyQualifiedName);
    }
}
