package com.xtremelabs.robolectric.res;

import org.w3c.dom.Node;
public class DimenResourceLoader extends XpathResourceXmlLoader implements ResourceValueConverter {

    private static final String[] UNITS = { "dp", "dip", "pt", "px", "sp" };
	
    private ResourceReferenceResolver<Float> dimenResolver = new ResourceReferenceResolver<Float>("dimen");

    public DimenResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor, "/resources/dimen");
    }

    public float getValue(int resourceId) {
        return dimenResolver.getValue(resourceExtractor.getResourceName(resourceId));
    }

    public float getValue(String resourceName, boolean isSystem) {
        return getValue(resourceExtractor.getResourceId(resourceName, isSystem));
    }

    @Override
    protected void processNode(Node node, String name, boolean isSystem) {
        dimenResolver.processResource(name, node.getTextContent(), this, isSystem);
    }

    @Override
    public Object convertRawValue(String rawValue) {
    	int end = rawValue.length();
    	for ( int i = 0; i < UNITS.length; i++ ) {
    		int index = rawValue.indexOf(UNITS[i]);
    		if ( index >= 0 && end > index ) {
    			end = index;
    		}
    	}
    	
        return Float.parseFloat(rawValue.substring(0, end));
    }
}

