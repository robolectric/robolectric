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

    public void processResource(String name, String rawValue, ResourceValueConverter loader) {
        if (rawValue.startsWith("@" + prefix)) {
           addAttributeReference(name, rawValue);
        } else {
            addAttribute(prefix + "/" + name, (T) loader.convertRawValue(rawValue));
        }
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

    public void addAttribute(String attributeName, T value) {
        attributeNamesToValues.put(attributeName, value);
        resolveUnresolvedReferences(attributeName, value);
    }

    private void addAttributeReference(String name, String rawValue) {
        String valuePointer = prefix + "/" + name;
        String attributeName = rawValue.substring(1);
        T value = attributeNamesToValues.get(attributeName);
        if (value == null) {
            addUnresolvedReference(valuePointer, attributeName);
        } else {
            attributeNamesToValues.put(valuePointer, value);
        }
    }
}
