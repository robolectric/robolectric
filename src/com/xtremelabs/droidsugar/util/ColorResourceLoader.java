package com.xtremelabs.droidsugar.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ColorResourceLoader extends XmlLoader {
    private Map<String, Integer> colorValues = new HashMap<String, Integer>();

    public ColorResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor);
    }

    public int getValue(int colorId) {
        String resourceName = resourceExtractor.getResourceName(colorId);
        return colorValues.get(resourceName);
    }

    @Override protected void processResourceXml(File xmlFile, Document document) throws Exception {
        XPathExpression stringsXPath = XPathFactory.newInstance().newXPath().compile("/resources/color");
        NodeList colorNodes = (NodeList) stringsXPath.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < colorNodes.getLength(); i++) {
            Node node = colorNodes.item(i);
            String name = node.getAttributes().getNamedItem("name").getNodeValue();
            String rawValue = node.getTextContent();
            long value = Long.valueOf(rawValue.replaceAll("#", ""), 16);
            colorValues.put("color/" + name, (int) value);
        }
    }
}
