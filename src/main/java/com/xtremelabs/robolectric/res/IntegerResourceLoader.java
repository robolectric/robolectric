package com.xtremelabs.robolectric.res;

import org.w3c.dom.Node;

/**
 *
 */
public class IntegerResourceLoader extends XpathResourceXmlLoader implements ResourceValueConverter {
    private ResourceReferenceResolver<Integer> intResolver = new ResourceReferenceResolver<Integer>("integer");

    public IntegerResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor, "/resources/integer");
    }

    public Integer getValue(int resourceId) {
        return intResolver.getValue(resourceExtractor.getResourceName(resourceId));
    }

    public Integer getValue(String resourceName, boolean isSystem) {
        return getValue(resourceExtractor.getResourceId(resourceName, isSystem));
    }

    @Override protected void processNode(Node node, String name, boolean isSystem) {
        final String textContent = node.getTextContent();
        try {
            intResolver.processResource(name, textContent, this, isSystem);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("For input string \"" + textContent + "\" and resource " + name);
        }
    }

    @Override public Object convertRawValue(String rawValue) {
        if (rawValue != null && rawValue.startsWith("0x")) { // Hex strings are apparently valid.
            long color = Long.parseLong(rawValue.substring(2), 16);
            return (int) color;
        }
        return Integer.parseInt(rawValue);
    }
}
