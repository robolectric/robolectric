package com.xtremelabs.robolectric.res;

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

public class IntegerArrayResourceLoader extends XpathResourceXmlLoader {
    private final Map<String, Integer[]> integerArrayValues = new HashMap<String, Integer[]>();
    
    private final IntegerResourceLoader integerResourceLoader;

    public IntegerArrayResourceLoader(ResourceExtractor resourceExtractor, IntegerResourceLoader integerResourceLoader) {
        super(resourceExtractor, "/resources/integer-array");
        this.integerResourceLoader = integerResourceLoader;
    }

    public int[] getArrayValue(int resourceId) {
        String resourceName = resourceExtractor.getResourceName(resourceId);
        Integer[] values = integerArrayValues.get(resourceName);
        int[] results = new int[values.length];
        for (int i = 0; i < values.length; i++) {
        	results[i] = values[i];
        }
        return results;
    }

    @Override protected void processNode(Node node, String name, boolean isSystem) throws XPathExpressionException {
        XPathExpression itemXPath = XPathFactory.newInstance().newXPath().compile("item");
        NodeList childNodes = (NodeList) itemXPath.evaluate(node, XPathConstants.NODESET);
        List<Integer> arrayValues = new ArrayList<Integer>();
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node childNode = childNodes.item(j);

            String value = childNode.getTextContent();
            if (value.startsWith("@")) {
                value = value.substring(1);
                arrayValues.add(integerResourceLoader.getValue(value , isSystem));
            } else {
                arrayValues.add(Integer.parseInt(value));
            }
        }
        String valuePointer = (isSystem ? "android:" : "") + "array/" + name;
        integerArrayValues.put(valuePointer, arrayValues.toArray(new Integer[arrayValues.size()]));
    }
}
