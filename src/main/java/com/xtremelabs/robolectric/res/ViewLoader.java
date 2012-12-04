package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.tester.android.util.Attribute;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewLoader extends XmlLoader {
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

    /**
     * Map of "package:layout/name" to the View nodes for that layout file
     */
    private final Map<String, ViewNode> viewNodesByLayoutName;

    public ViewLoader(ResourceExtractor resourceExtractor, Map<String, ViewNode> viewNodesByLayoutName) {
        super(resourceExtractor);
        this.viewNodesByLayoutName = viewNodesByLayoutName;
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
            parent.focusRequested();
        } else if (!name.startsWith("#")) {
            ViewNode viewNode = new ViewNode(name, attrList, parent.getXmlContext());
            parent.addChild(viewNode);

            processChildren(node.getChildNodes(), viewNode, xmlContext);
        }
    }
}
