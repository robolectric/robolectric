package com.xtremelabs.robolectric.res;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ResourceReferenceResolver<T> {
    private Map<String, T> attributeNamesToValues = new HashMap<String, T>();
    private Map<String, List<String>> unresolvedReferences = new HashMap<String, List<String>>();
    private String prefix;

    ResourceReferenceResolver(String prefix) {
        this.prefix = prefix;
    }

    public T getValue(String resourceName) {
        return attributeNamesToValues.get(resourceName);
    }

    public void processResource(String name, String rawValue, ResourceValueConverter loader, boolean isSystem) {
        String valuePointer = prefix + "/" + name;
        if (rawValue.startsWith("@" + prefix) || rawValue.startsWith("@android:" + prefix)) {
            addAttributeReference(rawValue, valuePointer);
        } else {
            if (isSystem) {
                valuePointer = "android:" + valuePointer;
            }
            addAttribute(valuePointer, (T) loader.convertRawValue(rawValue));
        }
    }

    public void addAttribute(String valuePointer, T value) {
        attributeNamesToValues.put(valuePointer, value);
        resolveUnresolvedReferences(valuePointer, value);
    }

    private void resolveUnresolvedReferences(String attributeName, T value) {
        List<String> references = unresolvedReferences.remove(attributeName);
        if (references == null) {
            return;
        }
        for (String reference : references) {
            attributeNamesToValues.put(reference, value);
        }
    }

    private void addUnresolvedReference(String valuePointer, String attributeName) {
        List<String> references = unresolvedReferences.get(attributeName);
        if (references == null) {
            references = new ArrayList<String>();
            unresolvedReferences.put(attributeName, references);
        }
        references.add(valuePointer);
    }

    private void addAttributeReference(String rawValue, String valuePointer) {
        String attributeName = rawValue.substring(1);
        T value = attributeNamesToValues.get(attributeName);
        if (value == null) {
            addUnresolvedReference(valuePointer, attributeName);
        } else {
            attributeNamesToValues.put(valuePointer, value);
        }
    }
}
