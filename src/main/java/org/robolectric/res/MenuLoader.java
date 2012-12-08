package org.robolectric.res;

import org.robolectric.tester.android.util.Attribute;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MenuLoader extends XmlLoader {
    private final ResBundle<MenuNode> menuNodes;

    public MenuLoader(ResBundle<MenuNode> menuNodes) {
        this.menuNodes = menuNodes;
    }

    @Override
    protected void processResourceXml(File xmlFile, Document document, XmlContext xmlContext) throws Exception {
        MenuNode topLevelNode = new MenuNode("top-level", new ArrayList<Attribute>());

        NodeList items = document.getChildNodes();
        if (items.getLength() != 1)
            throw new RuntimeException("Expected only one top-level item in menu file " + xmlFile.getName());
        if (items.item(0).getNodeName().compareTo("menu") != 0)
            throw new RuntimeException("Expected a top-level item called 'menu' in menu file " + xmlFile.getName());

        processChildren(items.item(0).getChildNodes(), topLevelNode, xmlContext);
        menuNodes.put("menu", xmlFile.getName().replace(".xml", ""), topLevelNode, xmlContext);
    }

    private void processChildren(NodeList childNodes, MenuNode parent, XmlContext xmlContext) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            processNode(node, parent, xmlContext);
        }
    }

    private void processNode(Node node, MenuNode parent, XmlContext xmlContext) {
        String name = node.getNodeName();
        NamedNodeMap attributesNodes = node.getAttributes();
        List<Attribute> attributes = new ArrayList<Attribute>();
        if (attributesNodes != null) {
            int length = attributesNodes.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = attributesNodes.item(i);
                String resourceName = ResourceExtractor.qualifyResourceName(attr.getNodeName(), xmlContext.packageName);
                attributes.add(new Attribute(Attribute.addType(resourceName, "attr"), attr.getNodeValue(), xmlContext.packageName));
            }
        }

        if (!name.startsWith("#")) {
            MenuNode menuNode = new MenuNode(name, attributes);
            parent.addChild(menuNode);
            NodeList children = node.getChildNodes();
            if (children != null && children.getLength() != 0) {
                for (int i = 0; i < children.getLength(); i++) {
                    Node nodei = children.item(i);
                    if (childToIgnore(nodei)) {
                        continue;
                    } else if (validChildren(nodei)) {
                        // recursively add all nodes
                        processNode(nodei, menuNode, xmlContext);
                    } else {
                        throw new RuntimeException("Unknown menu node"
                                + nodei.getNodeName());
                    }
                }
            }
        }
    }

    private static boolean childToIgnore(Node nodei) {
        return isEmpty(nodei.getNodeName()) || nodei.getNodeName().startsWith("#");
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    private static boolean validChildren(Node nodei) {
        return nodei.getNodeName().equals("item")
                || nodei.getNodeName().equals("menu")
                || nodei.getNodeName().equals("group");
    }
}
