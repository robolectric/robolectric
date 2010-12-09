package com.xtremelabs.robolectric.res;

import org.w3c.dom.Node;

public class ColorResourceLoader extends XpathResourceXmlLoader implements ResourceValueConverter {
    private ResourceReferenceResolver<Integer> colorResolver = new ResourceReferenceResolver<Integer>("color");

    public ColorResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor, "/resources/color");
    }

    public int getValue(int colorId) {
        return colorResolver.getValue(resourceExtractor.getResourceName(colorId));
    }

    @Override protected void processNode(Node node, String name) {
        colorResolver.processResource(name, node.getTextContent(), this);
    }

    @Override
    public Integer convertRawValue(String rawValue) {
        return Integer.valueOf(rawValue.replaceAll("#", ""), 16);
    }
}
