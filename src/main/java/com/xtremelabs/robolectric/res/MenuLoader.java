package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.text.TextUtils;
import android.view.Menu;
import android.view.SubMenu;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import com.xtremelabs.robolectric.util.I18nException;
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
    protected void processResourceXml(File xmlFile, Document document, XmlContext xmlContext) throws Exception {
        MenuNode topLevelNode = new MenuNode("top-level", new ArrayList<Attribute>());

        NodeList items = document.getChildNodes();
        if (items.getLength() != 1)
            throw new RuntimeException("Expected only one top-level item in menu file " + xmlFile.getName());
        if (items.item(0).getNodeName().compareTo("menu") != 0)
            throw new RuntimeException("Expected a top-level item called 'menu' in menu file " + xmlFile.getName());

        processChildren(items.item(0).getChildNodes(), topLevelNode, xmlContext);
        String resourceName = ResourceExtractor.qualifyResourceName("menu/" + xmlFile.getName().replace(".xml", ""), xmlContext.packageName);
        menuNodesByMenuName.put(resourceName, topLevelNode);
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
        return TextUtils.isEmpty(nodei.getNodeName())
                || nodei.getNodeName().startsWith("#");
    }

    private static boolean validChildren(Node nodei) {
        return nodei.getNodeName().equals("item")
                || nodei.getNodeName().equals("menu")
                || nodei.getNodeName().equals("group");
    }

    public boolean inflateMenu(Context context, String key, Menu root) {
        return inflateMenu(context, key, null, root);
    }

    public boolean inflateMenu(Context context, int resourceId, Menu root) {
        return inflateMenu(context, resourceExtractor.getResourceName(resourceId), root);
    }

    private boolean inflateMenu(Context context, String key, 
                             List<Attribute> attributes, Menu root) {
        MenuNode menuNode = menuNodesByMenuName.get(key);
        if (menuNode == null) return false;

        try {
            if (attributes != null) {
                Attribute attribute = Attribute.find(attributes, "android:attr/menu");
                if (attribute != null) {
                    menuNode.attributes.put(attribute);
                }
            }
            menuNode.inflate(context, root);
            return true;
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

        public MenuNode(String name, List<Attribute> attributes) {
            this.name = name;
            this.attributes = new TestAttributeSet(attributes, resourceExtractor, attrResourceLoader, null);
        }

        public void addChild(MenuNode MenuNode) {
            children.add(MenuNode);
        }

        private boolean isSubMenuItem(MenuNode child) {
            List<MenuNode> ch = child.children;
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
                        SubMenu sub = root.addSubMenu(groupId,
                                attributes.getAttributeResourceValue("android", "id", 0),
                                0, attributes.getAttributeValue("android", "title"));
                        MenuNode subMenuNode = child.children.get(0);
                        addChildrenInGroup(subMenuNode, groupId, sub);
                    } else {
                        root.add(groupId,
                                attributes.getAttributeResourceValue("android", "id", 0),
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
