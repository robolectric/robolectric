package com.xtremelabs.robolectric.res;

import android.view.View;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AttrResourceLoader extends XmlLoader {
    Map<String, EnumDef> enums = new HashMap<String, EnumDef>();
    Map<String, EnumRef> enumRefs = new HashMap<String, EnumRef>();
    boolean resolved = false;

    Map<String, String> classEnumToValue = new HashMap<String, String>();
    Set<String> knownClassEnums = new HashSet<String>();
  
    static class EnumDef {
        final String name;
        final Map<String, String> values = new HashMap<String, String>();

        EnumDef(String name) { this.name = name; }
    }

    static class EnumRef {
        final String viewName;
        final String enumName;

        EnumRef(String viewName, String enumName) {
            this.enumName = enumName;
            this.viewName = viewName;
        }
    }

    @Override protected void processResourceXml(File xmlFile, Document document, XmlContext xmlContext) throws Exception {

        // Pick up inline enum definitions
        {
            NodeList nodeList = findNodes(document, "/resources/declare-styleable/attr/enum|/resources/declare-styleable/attr/flag");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String viewName = node.getParentNode().getParentNode().getAttributes().getNamedItem("name").getNodeValue();
                String enumName = enumName(node.getParentNode().getAttributes().getNamedItem("name").getNodeValue(), xmlContext.packageName);
                String name = node.getAttributes().getNamedItem("name").getNodeValue();
                String value = node.getAttributes().getNamedItem("value").getNodeValue();

                classEnumToValue.put(key(viewName, enumName, name), value);
                knownClassEnums.add(key(viewName, enumName));
            }
        }

        // Look for any global enum definitions.
        {
            NodeList nodeList = findNodes(document, "/resources/attr/enum|/resources/attr/flag");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                String enumName = enumName(node.getParentNode().getAttributes().getNamedItem("name").getNodeValue(), xmlContext.packageName);
                EnumDef enumDef = enums.get(enumName);
                if (enumDef == null) {
                    enumDef = new EnumDef(enumName);
                    enums.put(enumName, enumDef);
                }
                enumDef.values.put(node.getAttributes().getNamedItem("name").getNodeValue(),
                        node.getAttributes().getNamedItem("value").getNodeValue());
            }
        }

        // Note uses of system enums and top level local enums by childless attr nodes
        {
            NodeList nodeList = findNodes(document, "/resources/declare-styleable/attr[not(node())]");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                String viewName = node.getParentNode().getAttributes().getNamedItem("name").getNodeValue();
                String enumName = enumName(node.getAttributes().getNamedItem("name").getNodeValue(), xmlContext.packageName);

                enumRefs.put(key(viewName, enumName), new EnumRef(viewName, enumName));
            }
        }
    }

    public String convertValueToEnum(Class<? extends View> viewClass, String namespace, String attrName, String attrValue) {
        resolveReferences();
        String qualifiedAttrName = namespace + ":" + attrName;
        String className = findKnownAttrClass(qualifiedAttrName, viewClass).getSimpleName();
        String value = classEnumToValue.get(key(className, qualifiedAttrName, attrValue));
        if (value == null) {
            throw new RuntimeException("Unknown value \"" + attrValue + "\" for enum " + className + "." + qualifiedAttrName);
        }
        return value;
    }

    public boolean hasAttributeFor(Class<? extends View> viewClass, String namespace, String attrName) {
        resolveReferences();
        String qualifiedAttrName = namespace + ":" + attrName;
        return findKnownAttrClass(qualifiedAttrName, viewClass) != null;
    }

    private String enumName(String name, String packageName) {
        return packageName + ":" + name;
    }

    private NodeList findNodes(Document document, String path) throws XPathExpressionException {
        XPathExpression nestedEnumsXPath = XPathFactory.newInstance().newXPath().compile(path);
        return (NodeList) nestedEnumsXPath.evaluate(document, XPathConstants.NODESET);
    }

    private void resolveReferences() {
        if (!resolved) {
            for (EnumRef enumRef : enumRefs.values()) {
                noteEnumUses(enumRef.viewName, enumRef.enumName);
            }
            resolved = true;
        }
    }

    private void noteEnumUses(String viewName, String enumName) {
        EnumDef enumDef = enums.get(enumName);
        if (enumDef == null) return;

        for (Map.Entry<String, String> entry : enumDef.values.entrySet()) {
            classEnumToValue.put(key(viewName, enumName, entry.getKey()), entry.getValue());
        }
        knownClassEnums.add(key(viewName, enumName));
    }

    private Class<?> findKnownAttrClass(String attrName, Class<?> clazz) {
        while (clazz != null) {
            if (knownClassEnums.contains(key(clazz.getSimpleName(), attrName))) {
                return clazz;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private String key(String viewName, String enumName, String name) {
        return viewName + "#" + enumName + "#" + name;
    }

    private String key(String viewName, String enunName) {
        return viewName + "#" + enunName;
    }
}
