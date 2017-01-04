package org.robolectric.res;

public class ValueResourceLoader extends XpathResourceXmlLoader {
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
  protected void processResourceXml(FsFile xmlFile, XmlNode xmlNode, XmlContext xmlContext) {
    super.processResourceXml(xmlFile, xmlNode, xmlContext);
  }

  @Override
  protected void processNode(String name, XmlNode xmlNode, XmlContext xmlContext) {
    resBunch.put(attrType, name, resType.getValueWithType(xmlNode, xmlContext));
  }
}
