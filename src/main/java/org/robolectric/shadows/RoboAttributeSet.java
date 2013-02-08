package org.robolectric.shadows;

import android.util.AttributeSet;
import android.view.View;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceExtractor;
import org.robolectric.res.ResourceIndex;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.I18nException;

import java.util.List;

public class RoboAttributeSet implements AttributeSet {
    private final List<Attribute> attributes;
    private final ResourceLoader resourceLoader;
    private Class<? extends View> viewClass;

    /**
     * Names of attributes to be validated for i18n-safe values.
     */
    private static final ResName strictI18nAttrs[] = {
            new ResName("android:attr/text"),
            new ResName("android:attr/title"),
            new ResName("android:attr/titleCondensed"),
            new ResName("android:attr/summary")
    };

    public RoboAttributeSet(List<Attribute> attributes, ResourceLoader resourceLoader, Class<? extends View> viewClass) {
        this.attributes = attributes;
        this.resourceLoader = resourceLoader;
        this.viewClass = viewClass;
    }

    public RoboAttributeSet put(String fullyQualifiedName, String value, String valuePackage) {
        return put(new Attribute(fullyQualifiedName, value, valuePackage));
    }

    public RoboAttributeSet put(Attribute attribute) {
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

        if (isEnum(namespace, attribute)) {
            return getEnumValue(namespace, attribute, value);
        }

        return extractInt(value, defaultValue);
    }

    private int extractInt(String value, int defaultValue) {
        if (value == null) return defaultValue;
        if (value.startsWith("0x")) return Integer.parseInt(value.substring(2), 16);
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        System.out.println("WARN: couldn't parse \"" + value + "\" as an integer");
        return defaultValue;
      }
    }

    public boolean isEnum(String namespace, String attribute) {
        return resourceLoader.hasAttributeFor(viewClass, namespace, attribute);
    }

    public int getEnumValue(String namespace, String attribute, String value) {
        int intValue = 0;
        for (String part : value.split("\\|")) {
            intValue |= extractInt(resourceLoader.convertValueToEnum(viewClass, namespace, attribute, part), 0);
        }
        return intValue;
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

        Integer resourceId = ResName.getResourceId(resourceLoader.getResourceExtractor(), attr.value, attr.contextPackageName);
        return resourceId == null ? defaultValue : resourceId;
    }

    @Override
    public int getAttributeResourceValue(int resourceId, int defaultValue) {
        String attrName = resourceLoader.getResourceExtractor().getResourceName(resourceId);
        Attribute attr = findByName(null, attrName);
        if (attr == null) return defaultValue;
        Integer extracted = ResName.getResourceId(resourceLoader.getResourceExtractor(), attr.value, attr.contextPackageName);
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
        Attribute styleAttribute = Attribute.find(attributes, new ResName("", "attr", "style"));
        if (styleAttribute == null) {
            // Per Android specifications, return 0 if there is no style.
            return 0;
        }
        Integer i = ResName.getResourceId(resourceLoader.getResourceExtractor(), styleAttribute.value, styleAttribute.contextPackageName);
        return i != null ? i : 0;
    }


    public void validateStrictI18n() {
        for (ResName key : strictI18nAttrs) {
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
        return findByName(new ResName(packageName, "attr", attrName));
    }

    private Attribute findByName(ResName resName) {
        ResourceIndex resourceIndex = resourceLoader.getResourceExtractor();
        Integer resourceId = resourceIndex.getResourceId(resName);
        // canonicalize the attr name if we can, otherwise don't...
        // todo: this is awful; fix it.
        if (resourceId == null) {
            return Attribute.find(attributes, resName);
        } else {
            return Attribute.find(attributes, resourceId, resourceIndex);
        }
    }
}
