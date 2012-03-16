package com.xtremelabs.robolectric.res;

import org.w3c.dom.Node;

public class IntegerResourceLoader extends XTagXmlResourceLoader implements ResourceValueConverter {

	private ResourceReferenceResolver< Integer > integerResolver = new ResourceReferenceResolver< Integer >( "integer" );

	public IntegerResourceLoader( ResourceExtractor resourceExtractor ) {
		super( resourceExtractor, "integer" );
	}

	public int getValue( int resourceId ) {
		String resourceName = resourceExtractor.getResourceName( resourceId );
		return integerResolver.getValue( resourceName );
	}

	public int getValue( String resourceName, boolean isSystem ) {
		return getValue( resourceExtractor.getResourceId( resourceName, isSystem ) );
	}

	@Override
	public Object convertRawValue( String rawValue ) {

		try {
			return Integer.parseInt( rawValue );
		} catch ( NumberFormatException nfe ) {
			throw new RuntimeException( rawValue + " is not an integer." );
		}

	}

	@Override
	protected void processNode( Node node, String name, boolean isSystem ) {
		integerResolver.processResource( name, node.getTextContent(), this, isSystem );
	}

}
