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
        // This string could contain hex values, so we're going to use the "decode" method.  Since Android can send us
        // an unsigned int larger than what Integer.decode is ok with, we parse it as a Long and convert down.
        return Long.decode(rawValue).intValue();
    }
}
