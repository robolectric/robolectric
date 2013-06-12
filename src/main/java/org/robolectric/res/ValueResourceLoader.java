package org.robolectric.res;

import javax.xml.xpath.XPathExpressionException;

class ValueResourceLoader extends XpathResourceXmlLoader {
  private final ResBunch resBunch;
  private final String attrType;
  private final ResType resType;

  public ValueResourceLoader(ResBunch resBunch, String xpathExpr, String attrType, ResType resType) {
    super(xpathExpr);
    this.resBunch = resBunch;
    this.attrType = attrType;
    this.resType = resType;
  }

  @Override
  protected void processResourceXml(FsFile xmlFile, XmlNode xmlNode, XmlContext xmlContext) throws Exception {
    super.processResourceXml(xmlFile, xmlNode, xmlContext);

//    if (arraysToo) {
//      for (XmlNode arrayNode : xmlNode.selectByXpath("/resources/" + attrType + "-array")) {
//        String name = arrayNode.getAttrValue("name");
//
//        List<String> itemStrings = new ArrayList<String>();
//        for (XmlNode itemNode : arrayNode.selectByXpath(".//item")) {
//          itemStrings.add(itemNode.getTextContent());
//        }
//        resBunch.putArray(attrType + "-array", name, itemStrings, xmlContext);
//      }
//    }
  }

  @Override
  protected void processNode(String name, XmlNode xmlNode, XmlContext xmlContext) throws XPathExpressionException {
    resBunch.put(attrType, name, resType.getValueWithType(xmlNode), xmlContext);
  }
}
