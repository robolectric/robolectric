package com.xtremelabs.robolectric.res;

import org.w3c.dom.Node;

public class ColorResourceLoader extends XpathResourceXmlLoader {

    private ResourceReferenceResolver<Integer> resolver = new ResourceReferenceResolver<Integer>();

    public ColorResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor, "/resources/color");
    }

    public int getValue(int colorId) {
        return resolver.getValue(resourceExtractor.getResourceName(colorId));
    }

    @Override protected void processNode(Node node, String name) {
        String rawValue = node.getTextContent();
        if (rawValue.startsWith("#")) {
            long value = Long.valueOf(rawValue.replaceAll("#", ""), 16);
            resolver.addAttribute("color/" + name, (int) value);
        } else if (rawValue.startsWith("@color")) {
            String colorPointer = "color/" + name;
            String colorAttributeName = rawValue.substring(1);
            resolver.addAttributeReference(colorPointer, colorAttributeName);
        }
    }
}
