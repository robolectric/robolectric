package com.xtremelabs.droidsugar.util;

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

public class StringResourceLoader extends XpathResourceXmlLoader {
    Map<String, String> stringValues = new HashMap<String, String>();

    public StringResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor, "/resources/string");
    }

    public String getValue(int resourceId) {
        String resourceName = resourceExtractor.getResourceName(resourceId);
        return stringValues.get(resourceName);
    }

    public String getValue(String resourceIdAsString) {
        int key = resourceExtractor.getResourceStringToId().get(resourceIdAsString);
        return getValue(key);
    }

    @Override protected void processNode(Node node, String name) {
        String value = node.getTextContent();
        stringValues.put("string/" + name, value);
    }
}
