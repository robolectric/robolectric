/**
 * 
 */
package com.xtremelabs.robolectric.res;

import org.w3c.dom.Node;

public class BoolResourceLoader extends XTagXmlResourceLoader
		implements ResourceValueConverter {

	private static final String BOOL = "bool";
	private final ResourceReferenceResolver<Boolean> boolResolver = 
			new ResourceReferenceResolver<Boolean>(BOOL);
	
	public BoolResourceLoader(ResourceExtractor resourceExtractor) {
		super(resourceExtractor, BOOL);
	}
	
	public boolean getValue(int resourceId) {
        final String resourceIdDebugString = String.valueOf(resourceId) + " (" + "0x" + Integer.toHexString(resourceId) + ")";
		String resourceName = resourceExtractor.getResourceName(resourceId);
        if (resourceName == null) {
            throw new IllegalArgumentException("No such resource: " + resourceId);
        }
        Boolean value = boolResolver.getValue(resourceName);
        if (value == null) { // instead of auto-unboxing NPE
            throw new IllegalArgumentException("Got resource name " + resourceName + " from id " + resourceIdDebugString
                                                   + ", but found no resource by that name");
        }
        return value;
	}

	public boolean getValue( String resourceName, boolean isSystem ) {
        Integer resourceId = resourceExtractor.getResourceId(resourceName, isSystem);
        if (resourceName == null) {
            throw new IllegalArgumentException("No such resource (" + isSystem + "): " + resourceName);
        }
        return getValue(resourceId);
	}
	
	@Override
	public Object convertRawValue(String rawValue) {
		// If null just let it throw a null pointer exception
		rawValue = rawValue.toLowerCase();
		if ("true".equals(rawValue)) {
			return true;
		} else if ("false".equals(rawValue)) {
			return false;
		}
		// Convert to int
		// If is not a number this will throw an exception
		int intValue = Integer.parseInt(rawValue);
		if (intValue == 0) {
			return false;
		} 
		return true;
	}

	@Override
	protected void processNode(Node node, String name, boolean isSystem) {
		boolResolver.processResource(
				name, node.getTextContent(), this, isSystem);
	}

}
