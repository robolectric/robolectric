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

    @Override
    protected void processResourceXml(File xmlFile, Document document, boolean isSystem) throws Exception {
        XPathExpression stringsXPath = XPathFactory.newInstance().newXPath().compile("/resources/declare-styleable/attr/enum");
        NodeList stringNodes = (NodeList) stringsXPath.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < stringNodes.getLength(); i++) {
            Node node = stringNodes.item(i);
            String viewName = node.getParentNode().getParentNode().getAttributes().getNamedItem("name").getNodeValue();
            String enumName = node.getParentNode().getAttributes().getNamedItem("name").getNodeValue();
            String name = node.getAttributes().getNamedItem("name").getNodeValue();
            String value = node.getAttributes().getNamedItem("value").getNodeValue();

            classAttrEnumToValue.put(key(viewName, enumName, name, isSystem), value);
            knownClassAttrs.add(key(viewName, enumName, isSystem));
        }
    }

    public String convertValueToEnum(Class<? extends View> viewClass, String namespace, String attrName, String attrValue) {
        boolean isSystem = "android".equals(namespace);
        String className = findKnownAttrClass(attrName, viewClass, isSystem);
        return classAttrEnumToValue.get(key(className, attrName, attrValue, isSystem));
    }

    public boolean hasAttributeFor(Class<? extends View> viewClass, String namespace, String attrName) {
        boolean isSystem = "android".equals(namespace);
        return findKnownAttrClass(attrName, viewClass, isSystem) != null;
    }

    private String findKnownAttrClass(String attrName, Class<?> clazz, boolean isSystem) {
        while (clazz != null) {
            String className = clazz.getName();
            if (isSystem) {
                className = clazz.getSimpleName();
            }
            if (knownClassAttrs.contains(key(className, attrName, isSystem))) {
                return className;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private String key(String viewName, String attrName, String name, boolean isSystem) {
        return key(viewName, attrName, isSystem) + "#" + name;
    }

    private String key(String viewName, String attrName, boolean isSystem) {
        return (isSystem ? "android:" : "") + viewName + "#" + attrName;
    }
}
