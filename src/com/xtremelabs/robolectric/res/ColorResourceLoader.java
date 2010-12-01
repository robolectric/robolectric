package com.xtremelabs.robolectric.res;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorResourceLoader extends XpathResourceXmlLoader {
    private Map<String, Integer> colorValues = new HashMap<String, Integer>();
    private Map<String, List<String>> unresolvedReferences = new HashMap<String, List<String>>();

    public ColorResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor, "/resources/color");
    }

    public int getValue(int colorId) {
        String resourceName = resourceExtractor.getResourceName(colorId);
        return colorValues.get(resourceName);
    }

    @Override protected void processNode(Node node, String name) {
        String rawValue = node.getTextContent();
        if (rawValue.startsWith("#")) {
            long value = Long.valueOf(rawValue.replaceAll("#", ""), 16);
            String colorAttributeName = "color/" + name;
            colorValues.put(colorAttributeName, (int) value);
            resolveUnresolvedReferences(colorAttributeName, (int) value);
        } else if (rawValue.startsWith("@color")) {
            String colorPointer = "color/" + name;
            String colorAttributeName = rawValue.substring(1);
            Integer value = colorValues.get(colorAttributeName);
            if (value == null) {
                addUnresolvedReference(colorPointer, colorAttributeName);
            } else {
                colorValues.put(colorPointer, value);
            }
        }
    }

    private void resolveUnresolvedReferences(String colorAttributeName, int value) {
        List<String> references = unresolvedReferences.remove(colorAttributeName);
        if (references == null) {
            return;
        }
        for (String reference : references) {
            colorValues.put(reference, value);
        }
    }

    private void addUnresolvedReference(String colorPointer, String colorAttributeName) {
        List<String> references = unresolvedReferences.get(colorAttributeName);
        if (references == null) {
            references = new ArrayList<String>();
            unresolvedReferences.put(colorAttributeName, references);
        }
        references.add(colorPointer);
    }
}
