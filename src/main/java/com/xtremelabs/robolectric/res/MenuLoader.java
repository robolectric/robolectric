package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import com.xtremelabs.robolectric.internal.TestAttributeSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuLoader extends XmlLoader {
    private Map<String, MenuNode> menuNodesByMenuName = new HashMap<String, MenuNode>();
    private AttrResourceLoader attrResourceLoader;

    public MenuLoader(ResourceExtractor resourceExtractor, AttrResourceLoader attrResourceLoader) {
        super(resourceExtractor);
        this.attrResourceLoader = attrResourceLoader;
    }

    @Override
    protected void processResourceXml(File xmlFile, Document document, boolean ignored) throws Exception {
        MenuNode topLevelNode = new MenuNode("top-level", new HashMap<String, String>());

        NodeList items = document.getChildNodes();
        if (items.getLength() != 1)
            throw new RuntimeException("Expected only one top-level item in menu file " + xmlFile.getName());
        if (items.item(0).getNodeName().compareTo("menu") != 0)
            throw new RuntimeException("Expected a top-level item called 'menu' in menu file " + xmlFile.getName());

        processChildren(items.item(0).getChildNodes(), topLevelNode);
        menuNodesByMenuName.put(
                "menu/" + xmlFile.getName().replace(".xml", ""),
                topLevelNode);
    }

    private void processChildren(NodeList childNodes, MenuNode parent) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            processNode(node, parent);
        }
    }

    private void processNode(Node node, MenuNode parent) {
        String name = node.getNodeName();
        NamedNodeMap attributes = node.getAttributes();
        Map<String, String> attrMap = new HashMap<String, String>();
        if (attributes != null) {
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = attributes.item(i);
                attrMap.put(attr.getNodeName(), attr.getNodeValue());
            }
        }

        if (!name.startsWith("#")) {
            MenuNode menuNode = new MenuNode(name, attrMap);
            parent.addChild(menuNode);
            if (node.getChildNodes().getLength() != 0)
                throw new RuntimeException(node.getChildNodes().toString());
        }
    }

    public void inflateMenu(Context context, String key, Menu root) {
        inflateMenu(context, key, null, root);
    }

    public void inflateMenu(Context context, int resourceId, Menu root) {
        inflateMenu(context, resourceExtractor.getResourceName(resourceId), root);
    }

    private void inflateMenu(Context context, String key, Map<String, String> attributes, Menu root) {
        MenuNode menuNode = menuNodesByMenuName.get(key);
        if (menuNode == null) {
            throw new RuntimeException("Could not find menu " + key);
        }
        try {
            if (attributes != null) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    if (!entry.getKey().equals("menu")) {
                        menuNode.attributes.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            menuNode.inflate(context, root);
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + key, e);
        }
    }

    public class MenuNode {
        private String name;
        private final TestAttributeSet attributes;

        private List<MenuNode> children = new ArrayList<MenuNode>();

        public MenuNode(String name, Map<String, String> attributes) {
            this.name = name;
            this .attributes = new TestAttributeSet(attributes, resourceExtractor, attrResourceLoader, null);
        }

        public List<MenuNode> getChildren() {
            return children;
        }

        public void addChild(MenuNode MenuNode) {
            children.add(MenuNode);
        }

        public void inflate(Context context, Menu root) throws Exception {
            for (MenuNode child : children) {
                MenuItem menuItem = root.add(0, child.attributes.getAttributeResourceValue("android", "id", 0),
                        0, child.attributes.getAttributeValue("android", "title"));
            }
        }
    }
}

