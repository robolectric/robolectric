package com.xtremelabs.robolectric.res;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;

public abstract class XpathResourceXmlLoader extends XmlLoader {
    private String expression;
    private String attrType;

    public XpathResourceXmlLoader(String expression, String attrType) {
        this.expression = expression;
        this.attrType = attrType;
    }

    @Override protected void processResourceXml(File xmlFile, Document document, XmlContext xmlContext) throws Exception {
        XPathExpression xPath = XPathFactory.newInstance().newXPath().compile(expression);
        NodeList nodes = (NodeList) xPath.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String name = node.getAttributes().getNamedItem("name").getNodeValue();
            processNode(node, name, xmlContext, attrType);
        }
    }

    protected abstract void processNode(Node node, String name, XmlContext xmlContext, String attrType) throws XPathExpressionException;
}
