package com.xtremelabs.robolectric.res;

import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;

class ValueResourceLoader extends XpathResourceXmlLoader {
    private final ResBundle<String> resBundle;

    public ValueResourceLoader(ResourceExtractor resourceExtractor, String xpathString, ResBundle<String> resBundle, String attrType) {
        super(resourceExtractor, xpathString, attrType);
        this.resBundle = resBundle;
    }

    @Override
    protected void processNode(Node node, String name, XmlContext xmlContext, String attrType) throws XPathExpressionException {
        resBundle.put(attrType, name, node.getTextContent(), xmlContext);
    }
}
