package org.robolectric.res;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class LayoutLoader extends XmlLoader {
  public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

  private final ResBundle<ViewNode> resBundle;

  public LayoutLoader(ResBundle<ViewNode> resBundle) {
    this.resBundle = resBundle;
  }

  @Override
  protected void processResourceXml(FsFile xmlFile, XpathResourceXmlLoader.XmlNode xmlNode, XmlContext xmlContext) throws Exception {
    Document document = parse(xmlFile);

    ViewNode topLevelNode = new ViewNode("top-level", new ArrayList<Attribute>(), xmlContext);
    processChildren(document.getChildNodes(), topLevelNode, xmlContext);
    String name = xmlFile.getName().replace(".xml", "");
    resBundle.put("layout", name, topLevelNode.getChildren().get(0), xmlContext);
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
