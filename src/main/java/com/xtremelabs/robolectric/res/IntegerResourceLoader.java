package com.xtremelabs.robolectric.res;

import org.w3c.dom.Node;

public class IntegerResourceLoader extends XTagXmlResourceLoader implements ResourceValueConverter {

	private final ResourceReferenceResolver< Integer > integerResolver = new ResourceReferenceResolver< Integer >( "integer" );

	public IntegerResourceLoader( ResourceExtractor resourceExtractor ) {
		super( resourceExtractor, "integer" );
	}

	public int getValue( int resourceId ) {
        final String resourceIdDebugString = String.valueOf(resourceId) + " (" + "0x" + Integer.toHexString(resourceId) + ")";
		String resourceName = resourceExtractor.getResourceName(resourceId);
        if (resourceName == null) {
            throw new IllegalArgumentException("No such resource: " + resourceId);
        }
        Integer value = integerResolver.getValue(resourceName);
        if (value == null) { // instead of auto-unboxing NPE
            throw new IllegalArgumentException("Got resource name " + resourceName + " from id " + resourceIdDebugString
                                                   + ", but found no resource by that name");
        }
        return value;
	}

	public int getValue( String resourceName, boolean isSystem ) {
        Integer resourceId = resourceExtractor.getResourceId(resourceName, isSystem);
        if (resourceName == null) {
            throw new IllegalArgumentException("No such resource (" + isSystem + "): " + resourceName);
        }
        return getValue(resourceId);
	}

	@Override
	public Object convertRawValue( String rawValue ) {
		try {
            // Decode into long, because there are some large hex values in the android resource files
            // (e.g. config_notificationsBatteryLowARGB = 0xFFFF0000 in sdk 14).
            // Integer.decode() does not support large, i.e. negative values in hex numbers.
            return (int) Long.decode(rawValue).longValue();
		} catch ( NumberFormatException nfe ) {
			throw new RuntimeException( rawValue + " is not an integer." , nfe );
		}
	}

	@Override
	protected void processNode( Node node, String name, boolean isSystem ) {
		integerResolver.processResource( name, node.getTextContent(), this, isSystem );
	}

}
