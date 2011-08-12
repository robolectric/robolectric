package com.xtremelabs.robolectric.res;

import org.w3c.dom.Node;

public class StringResourceLoader extends XpathResourceXmlLoader implements ResourceValueConverter {
    private ResourceReferenceResolver<String> stringResolver = new ResourceReferenceResolver<String>("string");

    public StringResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor, "/resources/string");
    }

    public String getValue(int resourceId) {
        return stringResolver.getValue(resourceExtractor.getResourceName(resourceId));
    }

    public String getValue(String resourceName, boolean isSystem) {
        return getValue(resourceExtractor.getResourceId(resourceName, isSystem));
    }

    @Override protected void processNode(Node node, String name, boolean isSystem) {
        stringResolver.processResource(name, node.getTextContent(), this, isSystem);
    }

    @Override public Object convertRawValue(String rawValue) {
        return rawValue;
    }
}
