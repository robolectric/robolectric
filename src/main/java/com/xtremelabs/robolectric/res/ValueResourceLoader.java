package com.xtremelabs.robolectric.res;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ValueResourceLoader extends XpathResourceXmlLoader {
    private final ResBundle<String> resBundle;
    private final String attrType;
    private final boolean arraysToo;

    public ValueResourceLoader(ResBundle<String> resBundle, String attrType, boolean arraysToo) {
        super("/resources/" + attrType, attrType);
        this.resBundle = resBundle;
        this.attrType = attrType;
        this.arraysToo = arraysToo;
    }

    @Override
    protected void processResourceXml(File xmlFile, Document document, XmlContext xmlContext) throws Exception {
        super.processResourceXml(xmlFile, document, xmlContext);

        if (arraysToo) {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPathExpression arrayXPath = xPathFactory.newXPath().compile("/resources/" + attrType + "-array");
            NodeList arrayNodes = (NodeList) arrayXPath.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < arrayNodes.getLength(); i++) {
                Node arrayNode = arrayNodes.item(i);
                String name = arrayNode.getAttributes().getNamedItem("name").getNodeValue();

                List<String> itemStrings = new ArrayList<String>();
                XPathExpression itemXpath = xPathFactory.newXPath().compile(".//item");
                NodeList itemNodes = (NodeList) itemXpath.evaluate(arrayNode, XPathConstants.NODESET);
                for (int j = 0; j < itemNodes.getLength(); j++) {
                    Node itemNode = itemNodes.item(j);
                    itemStrings.add(itemNode.getTextContent());
                }

                resBundle.putArray(attrType + "-array", name, itemStrings, xmlContext);
            }
        }
    }

    @Override
    protected void processNode(Node node, String name, XmlContext xmlContext, String attrType) throws XPathExpressionException {
        resBundle.put(attrType, name, node.getTextContent(), xmlContext);
    }
}
