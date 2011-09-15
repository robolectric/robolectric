package com.xtremelabs.robolectric.res;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import com.xtremelabs.robolectric.util.I18nException;

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
        menuNodesByMenuName.put("menu/" + xmlFile.getName().replace(".xml", ""), topLevelNode);
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
            NodeList children = node.getChildNodes();
            if (children != null && children.getLength() != 0) {
                for (int i = 0; i < children.getLength(); i++) {
                    Node nodei = children.item(i);
                    if (childToIgnore(nodei)) {
                        continue;
                    } else if (validChildren(nodei)) {
                        // recursively add all nodes
                        processNode(nodei, menuNode);
                    } else {
                        throw new RuntimeException("Unknown menu node"
                                + nodei.getNodeName());
                    }
                }
            }
        }
    }

    private static boolean childToIgnore(Node nodei) {
        return TextUtils.isEmpty(nodei.getNodeName())
                || nodei.getNodeName().startsWith("#");
    }

    private static boolean validChildren(Node nodei) {
        return nodei.getNodeName().equals("item")
                || nodei.getNodeName().equals("menu")
                || nodei.getNodeName().equals("group");
    }

    public void inflateMenu(Context context, String key, Menu root) {
        inflateMenu(context, key, null, root);
    }

    public void inflateMenu(Context context, int resourceId, Menu root) {
        inflateMenu(context, resourceExtractor.getResourceName(resourceId),
                root);
    }

    private void inflateMenu(Context context, String key,
                             Map<String, String> attributes, Menu root) {
        MenuNode menuNode = menuNodesByMenuName.get(key);
        if (menuNode == null) {
            throw new RuntimeException("Could not find menu " + key);
        }
        try {
            if (attributes != null) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    if (!entry.getKey().equals("menu")) {
                        menuNode.attributes.put(entry.getKey(),
                                entry.getValue());
                    }
                }
            }
            menuNode.inflate(context, root);
        } catch (I18nException e) {
            throw e;
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
            this.attributes = new TestAttributeSet(attributes,
                    resourceExtractor, attrResourceLoader, null, false);
        }

        public List<MenuNode> getChildren() {
            return children;
        }

        public void addChild(MenuNode MenuNode) {
            children.add(MenuNode);
        }

        private boolean isSubMenuItem(MenuNode child) {
            List<MenuLoader.MenuNode> ch = child.children;
            return ch != null && ch.size() == 1 && "menu".equals(ch.get(0).name);
        }

        private void addChildrenInGroup(MenuNode source, int groupId, Menu root) {
            for (MenuNode child : source.children) {
                String name = child.name;
                TestAttributeSet attributes = child.attributes;
                if (strictI18n) {
                    attributes.validateStrictI18n();
                }
                if (name.equals("item")) {
                    if (isSubMenuItem(child)) {
                        SubMenu sub = root.addSubMenu(groupId, attributes
                                .getAttributeResourceValue("android", "id", 0),
                                0, attributes.getAttributeValue("android", "title"));
                        MenuNode subMenuNode = child.children.get(0);
                        addChildrenInGroup(subMenuNode, groupId, sub);
                    } else {
                        MenuItem menuItem = root.add(groupId, attributes
                                .getAttributeResourceValue("android", "id", 0),
                                0, attributes.getAttributeValue("android", "title"));
                    }
                } else if (name.equals("group")) {
                    int newGroupId = attributes.getAttributeResourceValue("android", "id", 0);
                    addChildrenInGroup(child, newGroupId, root);
                }
            }
        }

        public void inflate(Context context, Menu root) throws Exception {
            addChildrenInGroup(this, 0, root);
        }
    }
}
