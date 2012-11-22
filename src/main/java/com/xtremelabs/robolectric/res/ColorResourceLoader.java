package com.xtremelabs.robolectric.res;

import org.w3c.dom.Node;

public class ColorResourceLoader extends XpathResourceXmlLoader implements ResourceValueConverter {
    private ResourceReferenceResolver<Integer> colorResolver = new ResourceReferenceResolver<Integer>("color");

    public ColorResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor, "/resources/color");
    }

    public int getValue(int colorId) {
        String resourceName = resourceExtractor.getResourceName(colorId);
        if (resourceName == null) return -1;
        Integer resolved = colorResolver.getValue(resourceName);
        return resolved == null ? -1 : resolved;
    }

    @Override
    protected void processNode(Node node, String name, XmlContext xmlContext) {
        colorResolver.processResource(name, node.getTextContent(), this, xmlContext.packageName);
    }

    @Override
    public Integer convertRawValue(String rawValue) {
        if (rawValue.startsWith("#")) {
            long color = Long.parseLong(rawValue.substring(1), 16);
            return (int) color;
        }
        return null;
    }
}
