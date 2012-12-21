package com.xtremelabs.robolectric.res;

import android.content.res.Resources;
import com.xtremelabs.robolectric.tester.android.util.ResName;
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
    Map<String, Value[]> stringArrayValues = new HashMap<String, Value[]>();
    private ResourceLoader.Resolver<String> stringResolver;

    public StringArrayResourceLoader(ResourceExtractor resourceExtractor, ResourceLoader.Resolver<String> stringResolver) {
        super(resourceExtractor, "/resources/string-array", "array");
        this.stringResolver = stringResolver;
    }

    public String[] getArrayValue(int resourceId) {
        ResName resName = resourceExtractor.getResName(resourceId);
        if (resName == null) return null;
        Value[] values = stringArrayValues.get(resName.getFullyQualifiedName());
        if (values == null) throw new Resources.NotFoundException(resName.getFullyQualifiedName());
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            Value value = values[i];
            result[i] = stringResolver.resolveValue("", value.text, resName.namespace);
        }
        return result;
    }

    @Override protected void processNode(Node node, String name, XmlContext xmlContext, String attrType) throws XPathExpressionException {
        XPathExpression itemXPath = XPathFactory.newInstance().newXPath().compile("item");
        NodeList childNodes = (NodeList) itemXPath.evaluate(node, XPathConstants.NODESET);
        List<Value> arrayValues = new ArrayList<Value>();
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node childNode = childNodes.item(j);
            arrayValues.add(new Value(childNode.getTextContent(), xmlContext.packageName));
        }
        String valuePointer = xmlContext.packageName + ":array/" + name;
        stringArrayValues.put(valuePointer, arrayValues.toArray(new Value[arrayValues.size()]));
    }

    private static class Value {
        String text;
        String packageName;

        private Value(String text, String packageName) {
            this.text = text;
            this.packageName = packageName;
        }
    }
}
