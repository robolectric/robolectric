package com.xtremelabs.robolectric.res;

import android.view.Menu;
import android.content.Context;
import android.util.AttributeSet;
import com.xtremelabs.robolectric.util.TestAttributeSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class MenuLoader extends XmlLoader {
    private Map<String, MenuNode> menuNodesByMenuName = new HashMap<String, MenuNode>();
    private StringResourceLoader stringResourceLoader;
    private AttrResourceLoader attrResourceLoader;

    public MenuLoader(ResourceExtractor resourceExtractor, StringResourceLoader stringResourceLoader, AttrResourceLoader attrResourceLoader) {
        super(resourceExtractor);
        this.stringResourceLoader = stringResourceLoader;
        this.attrResourceLoader = attrResourceLoader;
    }

    @Override
    protected void processResourceXml(File xmlFile, Document document) throws Exception {
        MenuNode topLevelNode = new MenuNode("top-level", new HashMap<String, String>());
        processChildren(document.getChildNodes(), topLevelNode);
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
            processChildren(node.getChildNodes(), menuNode);
        }
    }

    public Menu inflateMenu(Context context, String key) {
        return inflateMenu(context, key, null);
    }

    public Menu inflateMenu(Context context, int resourceId) {
        return inflateMenu(context, resourceExtractor.getResourceName(resourceId));
    }

    private Menu inflateMenu(Context context, String key, Map<String, String> attributes) {
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
            return menuNode.inflate(context);
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + key, e);
        }
    }

    public class MenuNode {
        private String name;
        private final Map<String, String> attributes;

        private List<MenuNode> children = new ArrayList<MenuNode>();
        boolean requestFocusOverride = false;

        public MenuNode(String name, Map<String, String> attributes) {
            this.name = name;
            this.attributes = attributes;
        }

        public List<MenuNode> getChildren() {
            return children;
        }

        public void addChild(MenuNode MenuNode) {
            children.add(MenuNode);
        }

        public Menu inflate(Context context) throws Exception {
            Menu menu = create(context);

            for (MenuNode child : children) {
                child.inflate(context);
            }
            return menu;
        }

		private Menu create(Context context) throws Exception {
			Menu menu = constructMenu(context);
			return menu;
		}

		private Menu constructMenu(Context context) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			Class<? extends Menu> clazz = pickMenuClass();
			try {
				TestAttributeSet attributeSet = new TestAttributeSet(attributes, resourceExtractor, attrResourceLoader, clazz);
				return ((Constructor<? extends Menu>) clazz.getConstructor(Context.class, AttributeSet.class)).newInstance(context, attributeSet);
			} catch (NoSuchMethodException e) {
				try {
					return ((Constructor<? extends Menu>) clazz.getConstructor(Context.class)).newInstance(context);
				} catch (NoSuchMethodException e1) {
					return ((Constructor<? extends Menu>) clazz.getConstructor(Context.class, String.class)).newInstance(context, "");
				}
			}
		}

		private Class<? extends Menu> pickMenuClass() {
			Class<? extends Menu> clazz = loadClass(name);
			if (clazz == null) {
				clazz = loadClass("android.view." + name);
			}
			if (clazz == null) {
				clazz = loadClass("android.widget." + name);
			}
			if (clazz == null) {
				clazz = loadClass("android.webkit." + name);
			}
			if (clazz == null) {
				clazz = loadClass("com.google.android.maps." + name);
			}

			if (clazz == null) {
				throw new RuntimeException("couldn't find menu class " + name);
			}
			return clazz;
		}

        private Class<? extends Menu> loadClass(String className) {
            try {
                //noinspection unchecked
                return (Class<? extends Menu>) getClass().getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

	}
}

