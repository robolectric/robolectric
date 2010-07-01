package com.xtremelabs.droidsugar.view;

import android.content.*;
import android.view.*;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class ViewLoader {
    public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    private DocumentBuilderFactory documentBuilderFactory;
    private Map<String, ViewNode> viewNodesByLayoutName = new HashMap<String, ViewNode>();
    private Map<String, Integer> resourceStringToId = new HashMap<String, Integer>();
    private Map<Integer, String> resourceIdToString = new HashMap<Integer, String>();

    public ViewLoader(Class rClass, File... layoutDirs) throws Exception {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);

        addRClass(rClass);
        for (File layoutDir : layoutDirs) {
            addXmlDir(layoutDir);
        }
    }

    private void addXmlDir(File layoutDir) throws Exception {
        for (File file1 : layoutDir.listFiles()) {
            processXml(file1);
        }
    }

    private void addRClass(Class rClass) throws Exception {
        for (Class innerClass : rClass.getClasses()) {
            for (Field field : innerClass.getDeclaredFields()) {
                if (field.getType().equals(Integer.TYPE) && Modifier.isStatic(field.getModifiers())) {
                    String name = innerClass.getSimpleName() + "/" + field.getName();
                    int value = field.getInt(null);
                    resourceStringToId.put(name, value);
                    resourceIdToString.put(value, name);
                }
            }
        }
    }

    private void processXml(File xmlFile) throws Exception {
        Document document = parse(xmlFile);

        ViewNode topLevelNode = new ViewNode("top-level", new HashMap<String, String>());
        processChildren(document.getChildNodes(), topLevelNode);
        viewNodesByLayoutName.put(
            "layout/" + xmlFile.getName().replace(".xml", ""),
            topLevelNode.getChildren().get(0)
        );
    }

    private void processChildren(NodeList childNodes, ViewNode parent) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            processNode(node, parent);
        }
    }

    private void processNode(Node node, ViewNode parent) {
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
            ViewNode viewNode = new ViewNode(name, attrMap);
            if (parent != null) parent.addChild(viewNode);

            String idAttr = getIdAttr(node);
            if (idAttr != null && idAttr.startsWith("@+id/")) {
                idAttr = idAttr.substring(5);

                Integer id = resourceStringToId.get("id/" + idAttr);
                if (id == null) {
                    throw new RuntimeException("unknown id " + getIdAttr(node));
                }
                viewNode.setId(id);
            }

            processChildren(node.getChildNodes(), viewNode);
        }
    }

    private String getIdAttr(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        Node idAttr = attributes.getNamedItemNS(ANDROID_NS, "id");
        return idAttr != null ? idAttr.getNodeValue() : null;
    }

    public View inflateView(Context context, String key) {
        ViewNode viewNode = viewNodesByLayoutName.get(key);
        try {
            return viewNode.inflate(context);
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + key, e);
        }
    }

    public View inflateView(Context context, int resourceId) {
        return inflateView(context, resourceIdToString.get(resourceId));
    }

    private Document parse(File xmlFile) throws Exception {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(xmlFile);
    }

    private class ViewNode {
        private String name;
        private final Map<String, String> attributes;

        private List<ViewNode> children = new ArrayList<ViewNode>();
        private Integer id;

        public ViewNode(String name, Map<String, String> attributes) {
            this.name = name;
            this.attributes = attributes;
        }

        public List<ViewNode> getChildren() {
            return children;
        }

        public void addChild(ViewNode viewNode) {
            children.add(viewNode);
        }

        public View inflate(Context context) throws Exception {
            View view = create(context);
            if (id != null) {
                view.setId(id);
            }
            for (ViewNode child : children) {
                ((ViewGroup) view).addView(child.inflate(context));
            }
            return view;
        }

        private View create(Context context) throws Exception {
            if (name.equals("include")) {
                String layout = attributes.get("layout");
                return inflateView(context, layout.substring(1));
            } else {
                Class<? extends View> clazz = loadClass(name);
                if (clazz == null) {
                    clazz = loadClass("android.view." + name);
                }
                if (clazz == null) {
                    clazz = loadClass("android.widget." + name);
                }

                if (clazz == null) {
                    throw new RuntimeException("couldn't find view class " + name);
                }
                Constructor<? extends View> constructor = clazz.getConstructor(Context.class);
                if (constructor == null) {
                    throw new RuntimeException("no constructor " + clazz.getName() + "(Context context);");
                }
                return constructor.newInstance(context);
            }
        }

        private Class<? extends View> loadClass(String className) {
            try {
                //noinspection unchecked
                return (Class<? extends View>) getClass().getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }
}
