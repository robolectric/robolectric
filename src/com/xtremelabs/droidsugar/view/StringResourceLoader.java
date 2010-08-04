package com.xtremelabs.droidsugar.view;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class StringResourceLoader extends XmlLoader {
    Map<String, String> values = new HashMap<String, String>();

    public StringResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor);
    }

    @Override
    protected void processResourceXml(File xmlFile, Document document) throws Exception {
        XPathExpression stringsXPath = XPathFactory.newInstance().newXPath().compile("/resources/string");
        NodeList stringNodes = (NodeList) stringsXPath.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < stringNodes.getLength(); i++) {
            Node node = stringNodes.item(i);
            String name = node.getAttributes().getNamedItem("name").getNodeValue();
            String value = node.getTextContent();
            values.put("string/" + name, value);
        }
    }

    public String getValue(int resourceId) {
        String resourceName = resourceExtractor.getResourceName(resourceId);
        return values.get(resourceName);
    }
}
