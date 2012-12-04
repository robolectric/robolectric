package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.view.View;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.ResName;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewLoader extends XmlLoader {
    public static final ResName ATTR_LAYOUT = new ResName(":attr/layout");
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

    /**
     * Map of "layout/foo" to the View nodes for that layout file
     */
    protected Map<String, ViewNode> viewNodesByLayoutName = new HashMap<String, ViewNode>();
    private List<String> qualifierSearchPath = new ArrayList<String>();

    public ViewLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor);
    }

    @Override
    protected void processResourceXml(File xmlFile, Document document, XmlContext xmlContext) throws Exception {
        ViewNode topLevelNode = new ViewNode("top-level", new ArrayList<Attribute>(), xmlContext);
        processChildren(document.getChildNodes(), topLevelNode, xmlContext);
        String parentDir = xmlFile.getParentFile().getName();
        String layoutName = xmlContext.packageName + ":layout/" + xmlFile.getName().replace(".xml", "");
        String specificLayoutName = xmlContext.packageName + ":" + parentDir + "/" + xmlFile.getName().replace(".xml", "");
        // Check to see if the generic "layout/foo" is already in the map.  If not, add it.
        if (!viewNodesByLayoutName.containsKey(layoutName)) {
            viewNodesByLayoutName.put(layoutName, topLevelNode.getChildren().get(0));
        }
        // Add the specific "layout-land/foo" to the map.  If this happens to be "layout/foo", it's a no-op.
        viewNodesByLayoutName.put(specificLayoutName, topLevelNode.getChildren().get(0));
    }

    private void processChildren(NodeList childNodes, ViewNode parent, XmlContext xmlContext) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            processNode(node, parent, xmlContext);
        }
    }

    private void processNode(Node node, ViewNode parent, XmlContext xmlContext) {
        String name = node.getNodeName();
        NamedNodeMap attributes = node.getAttributes();
        List<Attribute> attrList = new ArrayList<Attribute>();
        if (attributes != null) {
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = attributes.item(i);
                if (!XMLNS_URI.equals(attr.getNamespaceURI())) {
                    attrList.add(new Attribute(attr, xmlContext));
                }
            }
        }

        if (name.equals("requestFocus")) {
            parent.focusRequested(xmlContext);
        } else if (!name.startsWith("#")) {
            ViewNode viewNode = new ViewNode(name, attrList, parent.getXmlContext());
            parent.addChild(viewNode);

            processChildren(node.getChildNodes(), viewNode, xmlContext);
        }
    }

    public View inflateView(Context context, String key, View parent) {
        return inflateView(context, key, new ArrayList<Attribute>(), parent);
    }

    public View inflateView(Context context, int resourceId, View parent) {
        return inflateView(context, resourceExtractor.getResourceName(resourceId), parent);
    }

    public View inflateView(Context context, String layoutName, List<Attribute> attributes, View parent) {
        ViewNode viewNode = getViewNodeByLayoutName(layoutName);
        if (viewNode == null) {
            throw new RuntimeException("Could not find layout " + layoutName);
        }

        return viewNode.inflate(context, layoutName, attributes, parent);
    }

    private ViewNode getViewNodeByLayoutName(String layoutName) {
        String[] parts = layoutName.split("/");
        if (parts[0].endsWith(":layout") && !qualifierSearchPath.isEmpty()) {
            String rawLayoutName = parts[1];
            for (String location : qualifierSearchPath) {
                ViewNode foundNode = viewNodesByLayoutName.get(parts[0] + "-" + location + "/" + rawLayoutName);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return viewNodesByLayoutName.get(layoutName);
    }

    public void setLayoutQualifierSearchPath(String... locations) {
        qualifierSearchPath = Arrays.asList(locations);
    }
}
