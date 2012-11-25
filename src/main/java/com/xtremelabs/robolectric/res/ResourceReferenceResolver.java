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

    public void processResource(String name, String rawValue, ResourceValueConverter loader, String packageName) {
        String valuePointer = prefix + "/" + name;
        // todo: seems wrong
        if (rawValue.startsWith("@" + prefix) || rawValue.startsWith("@android:" + prefix)) {
            addAttributeReference(rawValue, valuePointer, packageName);
        } else {
            addAttribute(packageName + ":" + valuePointer, (T) loader.convertRawValue(rawValue));
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

    private void addAttributeReference(String rawValue, String valuePointer, String packageName) {
        String attributeName = ResourceExtractor.qualifyResourceName(rawValue.substring(1), packageName);
        String qualifiedValuePointer = ResourceExtractor.qualifyResourceName(valuePointer, packageName);
        T value = attributeNamesToValues.get(attributeName);
        if (value == null) {
            addUnresolvedReference(qualifiedValuePointer, attributeName);
        } else {
            attributeNamesToValues.put(qualifiedValuePointer, value);
        }
    }
}
