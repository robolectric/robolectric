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

public class StringArrayResourceLoader extends XpathResourceXmlLoader {
    Map<String, String[]> stringArrayValues = new HashMap<String, String[]>();
    private StringResourceLoader stringResourceLoader;

    public StringArrayResourceLoader(ResourceExtractor resourceExtractor, StringResourceLoader stringResourceLoader) {
        super(resourceExtractor, "/resources/string-array");
        this.stringResourceLoader = stringResourceLoader;
    }

    public String[] getArrayValue(int resourceId) {
        String resourceName = resourceExtractor.getResourceName(resourceId);
        return stringArrayValues.get(resourceName);
    }

    @Override protected void processNode(Node node, String name, boolean isSystem) throws XPathExpressionException {
        XPathExpression itemXPath = XPathFactory.newInstance().newXPath().compile("item");
        NodeList childNodes = (NodeList) itemXPath.evaluate(node, XPathConstants.NODESET);
        List<String> arrayValues = new ArrayList<String>();
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node childNode = childNodes.item(j);

            String value = childNode.getTextContent();
            if (value.startsWith("@")) {
                value = value.substring(1);
                arrayValues.add(stringResourceLoader.getValue(value , isSystem));
            } else {
                arrayValues.add(value);
            }
        }
        String valuePointer = (isSystem ? "android:" : "") + "array/" + name;
        stringArrayValues.put(valuePointer, arrayValues.toArray(new String[arrayValues.size()]));
    }
}
