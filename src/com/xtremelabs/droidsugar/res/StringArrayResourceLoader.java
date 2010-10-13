package com.xtremelabs.droidsugar.res;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringArrayResourceLoader extends XpathResourceXmlLoader {
    Map<String, String[]> stringArrayValues = new HashMap<String, String[]>();

    public StringArrayResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor, "/resources/string-array");
    }

    public String[] getArrayValue(int resourceId) {
        String resourceName = resourceExtractor.getResourceName(resourceId);
        return stringArrayValues.get(resourceName);
    }

    @Override protected void processNode(Node node, String name) throws XPathExpressionException {
        XPathExpression itemXPath = XPathFactory.newInstance().newXPath().compile("item");
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
