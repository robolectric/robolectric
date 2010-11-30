package com.xtremelabs.robolectric.res;

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

public class StringResourceLoader extends XpathResourceXmlLoader {
    Map<String, String> stringValues = new HashMap<String, String>();

    public StringResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor, "/resources/string");
    }

    public String getValue(int resourceId) {
        return stringValues.get(resourceExtractor.getResourceName(resourceId));
    }

    public String getValue(String resourceIdAsString) {
        return getValue(resourceExtractor.getResourceId(resourceIdAsString));
    }

    @Override protected void processNode(Node node, String name) {
        stringValues.put("string/" + name, node.getTextContent());
    }
}
