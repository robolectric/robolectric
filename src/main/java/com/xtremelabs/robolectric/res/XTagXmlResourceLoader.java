package com.xtremelabs.robolectric.res;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * XTagXmlResourceLoader is dedicated for mixed tags xml files.
 * 
 */
public abstract class XTagXmlResourceLoader extends XmlLoader {

    private String tag;

	private static List< String > xPathXmlFiles = new ArrayList< String >( 6 );

	static {
		xPathXmlFiles.add( "values/attrs" );
		xPathXmlFiles.add( "values/colors" );
		xPathXmlFiles.add( "values/strings" );
		xPathXmlFiles.add( "values/string_arrays" );
		xPathXmlFiles.add( "values/plurals" );
		xPathXmlFiles.add( "values/dimens" );
	}

	public XTagXmlResourceLoader(String tag) {
        this.tag = tag;
	}

	@Override
	protected void processResourceXml( File xmlFile, Document document, XmlContext xmlContext) throws Exception {

		String resourceName = toResourceName( xmlFile );
		if ( xPathXmlFiles.contains( resourceName ) )
			return;

		NodeList items = document.getElementsByTagName( tag );
		for ( int i = 0; i < items.getLength(); i++ ) {
			Node node = items.item( i );
			String name = node.getAttributes().getNamedItem( "name" ).getNodeValue();
			processNode(node, name, xmlContext);
		}

	}

	/**
	 * Convert file name to resource name.
	 * 
	 * @param xmlFile
	 *            Xml File
	 * @return Resource name
	 */
	private String toResourceName( File xmlFile ) {
		try {
			return xmlFile.getCanonicalPath().replaceAll( "[/\\\\\\\\]", "/" ).replaceAll( "^.*?/res/", "" )
					.replaceAll( "\\..+$", "" );
		} catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	protected abstract void processNode(Node node, String name, XmlContext xmlContext);

}
