package com.xtremelabs.droidsugar.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringResourceLoader extends XmlLoader {
    Map<String, String> stringValues = new HashMap<String, String>();
    Map<String, String[]> stringArrayValues = new HashMap<String, String[]>();

    public StringResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor);
    }

    public String getValue(int resourceId) {
        String resourceName = resourceExtractor.getResourceName(resourceId);
        return stringValues.get(resourceName);
    }

    public String[] getArrayValue(int resourceId) {
        String resourceName = resourceExtractor.getResourceName(resourceId);
        return stringArrayValues.get(resourceName);
    }

    @Override
    protected void processResourceXml(File xmlFile, Document document) throws Exception {
        parseStrings(document);
        parseStringArrays(document);
    }

    private void parseStrings(Document document) throws XPathExpressionException {
        XPathExpression stringsXPath = XPathFactory.newInstance().newXPath().compile("/resources/string");
        NodeList stringNodes = (NodeList) stringsXPath.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < stringNodes.getLength(); i++) {
            Node node = stringNodes.item(i);
            String name = node.getAttributes().getNamedItem("name").getNodeValue();
            String value = node.getTextContent();
            stringValues.put("string/" + name, value);
        }
    }

    private void parseStringArrays(Document document) throws XPathExpressionException {
        XPathExpression stringsXPath = XPathFactory.newInstance().newXPath().compile("/resources/string-array");
        XPathExpression itemXPath = XPathFactory.newInstance().newXPath().compile("item");

        NodeList stringNodes = (NodeList) stringsXPath.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < stringNodes.getLength(); i++) {
            Node node = stringNodes.item(i);
            String name = node.getAttributes().getNamedItem("name").getNodeValue();

            NodeList childNodes = (NodeList) itemXPath.evaluate(node, XPathConstants.NODESET);
            List<String> arrayValues = new ArrayList<String>();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);

                String value = childNode.getTextContent();
                arrayValues.add(value);
            }
            stringArrayValues.put("array/" + name, arrayValues.toArray(new String[arrayValues.size()]));
        }
    }
}
