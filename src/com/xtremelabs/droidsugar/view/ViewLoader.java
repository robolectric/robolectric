package com.xtremelabs.droidsugar.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewLoader {
    public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    private DocumentBuilderFactory documentBuilderFactory;
    private Map<String, ViewNode> viewNodesById = new HashMap<String, ViewNode>();
    private Map<String, Integer> resourceStringToId = new HashMap<String, Integer>();
    private Map<Integer, String> resourceIdToString = new HashMap<Integer, String>();

    public ViewLoader(Class rClass, File layoutDir) throws Exception {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);

        addRClass(rClass);
        addXmlDir(layoutDir);
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

        ViewNode topLevelNode = new ViewNode("top-level");
        processChildren(document.getChildNodes(), topLevelNode);
        viewNodesById.put("layout/" + xmlFile.getName().replace(".xml", ""),
                topLevelNode.getChildren().get(0));
    }

    private void processChildren(NodeList childNodes, ViewNode parent) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            processNode(node, parent);
        }
    }

    private void processNode(Node node, ViewNode parent) {
        String name = node.getNodeName();

        if (!name.startsWith("#")) {
            ViewNode viewNode = new ViewNode(name);
            if (parent != null) parent.addChild(viewNode);

            String idAttr = getIdAttr(node);
            if (idAttr != null && idAttr.startsWith("@+id/")) {
                idAttr = idAttr.substring(5);
                viewNodesById.put("id/" + idAttr, viewNode);

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
        ViewNode viewNode = viewNodesById.get(key);
        try {
            return viewNode.inflate(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        private List<ViewNode> children = new ArrayList<ViewNode>();
        private Integer id;

        public ViewNode(String name) {
            this.name = name;
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
            Class<? extends View> clazz = loadClass(name);
            if (clazz == null) {
                clazz = loadClass("android.view." + name);
            }
            if (clazz == null) {
                clazz = loadClass("android.widget." + name);
            }

            return clazz.getConstructor(Context.class).newInstance(context);
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
