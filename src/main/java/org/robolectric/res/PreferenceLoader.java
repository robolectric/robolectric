package org.robolectric.res;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class PreferenceLoader extends XmlLoader {
  private final ResBundle<PreferenceNode> resBundle;

  public PreferenceLoader(ResBundle<PreferenceNode> resBundle) {
    this.resBundle = resBundle;
  }

  @Override
  protected void processResourceXml(FsFile xmlFile, XpathResourceXmlLoader.XmlNode xmlNode, XmlContext xmlContext) throws Exception {
    PreferenceNode topLevelNode = new PreferenceNode("top-level", new ArrayList<Attribute>());
    processChildren(parse(xmlFile).getChildNodes(), topLevelNode, xmlContext);
    resBundle.put("xml", xmlFile.getName().replace(".xml", ""), topLevelNode.getChildren().get(0), xmlContext);
  }

  private void processChildren(NodeList childNodes, PreferenceNode parent, XmlContext xmlContext) {
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      processNode(node, parent, xmlContext);
    }
  }

  private void processNode(Node node, PreferenceNode parent, XmlContext xmlContext) {
    String name = node.getNodeName();
    NamedNodeMap attributes = node.getAttributes();
    List<Attribute> attrList = new ArrayList<Attribute>();

    if (attributes != null) {
      int length = attributes.getLength();
      for (int i = 0; i < length; i++) {
        Node attr = attributes.item(i);
        String attrName = Attribute.qualifyName(attr.getNodeName(), xmlContext.packageName);
        if (attrName.startsWith("xmlns:")) {
          // ignore
        } else {
          attrList.add(new Attribute(Attribute.addType(attrName, "attr"), attr.getNodeValue(), xmlContext.packageName));
        }
      }
    }

    if (!name.startsWith("#")) {
      PreferenceNode prefNode = new PreferenceNode(name, attrList);
      if (parent != null) parent.addChild(prefNode);

      processChildren(node.getChildNodes(), prefNode, xmlContext);
    }
  }
}
