package com.xtremelabs.robolectric.res;

import android.view.View;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AttrResourceLoader extends XmlLoader {
    Map<String, String> classAttrEnumToValue = new HashMap<String, String>();
    Set<String> knownClassAttrs = new HashSet<String>();

    public AttrResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor);
    }

    @Override protected void processResourceXml(File xmlFile, Document document, boolean ignored) throws Exception {
        XPathExpression stringsXPath = XPathFactory.newInstance().newXPath().compile("/resources/declare-styleable/attr/enum");
        NodeList stringNodes = (NodeList) stringsXPath.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < stringNodes.getLength(); i++) {
            Node node = stringNodes.item(i);
            String viewName = node.getParentNode().getParentNode().getAttributes().getNamedItem("name").getNodeValue();
            String enumName = node.getParentNode().getAttributes().getNamedItem("name").getNodeValue();
            String name = node.getAttributes().getNamedItem("name").getNodeValue();
            String value = node.getAttributes().getNamedItem("value").getNodeValue();

            classAttrEnumToValue.put(key(viewName, enumName, name), value);
            knownClassAttrs.add(key(viewName, enumName));
        }
    }

    public String convertValueToEnum(Class<? extends View> viewClass, String namespace, String attrName, String attrValue) {
        String className = findKnownAttrClass(attrName, viewClass).getName();
        return classAttrEnumToValue.get(key(className, attrName, attrValue));
    }

    public boolean hasAttributeFor(Class<? extends View> viewClass, String namespace, String attrName) {
        return findKnownAttrClass(attrName, viewClass) != null;
    }

    private Class<?> findKnownAttrClass(String attrName, Class<?> clazz) {
        while (clazz != null) {
            if (knownClassAttrs.contains(key(clazz.getName(), attrName))) {
                return clazz;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private String key(String viewName, String attrName, String name) {
        return viewName + "#" + attrName + "#" + name;
    }

    private String key(String viewName, String attrName) {
        return viewName + "#" + attrName;
    }
}
