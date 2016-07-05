package org.robolectric.res;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class PreferenceLoader extends XmlLoader {
  private final ResBundle<PreferenceNode> resBundle;

  PreferenceLoader(ResBundle<PreferenceNode> resBundle) {
    this.resBundle = resBundle;
  }

  @Override
  protected void processResourceXml(FsFile xmlFile, XpathResourceXmlLoader.XmlNode xmlNode, XmlContext xmlContext) throws Exception {
    PreferenceNode topLevelNode = new PreferenceNode();
    processChildren(parse(xmlFile).getChildNodes(), topLevelNode);
    resBundle.put("xml", xmlFile.getName().replace(".xml", ""), topLevelNode.getChildren().get(0), xmlContext);
  }

  private void processChildren(NodeList childNodes, PreferenceNode parent) {
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      String name = node.getNodeName();

      if (!name.startsWith("#")) {
        PreferenceNode prefNode = new PreferenceNode();
        if (parent != null) parent.addChild(prefNode);

        processChildren(node.getChildNodes(), prefNode);
      }
    }
  }

}
