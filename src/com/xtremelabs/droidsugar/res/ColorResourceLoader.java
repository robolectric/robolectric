package com.xtremelabs.droidsugar.res;

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

public class ColorResourceLoader extends XpathResourceXmlLoader {
    private Map<String, Integer> colorValues = new HashMap<String, Integer>();

    public ColorResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor, "/resources/color");
    }

    public int getValue(int colorId) {
        String resourceName = resourceExtractor.getResourceName(colorId);
        return colorValues.get(resourceName);
    }

    @Override protected void processNode(Node node, String name) {
        String rawValue = node.getTextContent();
        long value = Long.valueOf(rawValue.replaceAll("#", ""), 16);
        colorValues.put("color/" + name, (int) value);
    }
}
