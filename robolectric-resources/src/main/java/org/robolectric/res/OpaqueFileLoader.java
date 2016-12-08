package org.robolectric.res;

public class OpaqueFileLoader extends XmlLoader {
  private final ResBunch resBunch;
  private final String attrType;
  private final ResType resType;

  public OpaqueFileLoader(ResBunch resBunch, String attrType) {
    this(resBunch, attrType, ResType.LAYOUT);
  }

  public OpaqueFileLoader(ResBunch resBunch, String attrType, ResType resType) {
    this.resBunch = resBunch;
    this.attrType = attrType;
    this.resType = resType;
  }

  @Override
  protected void processResourceXml(XpathResourceXmlLoader.XmlNode xmlNode, XmlContext xmlContext) throws Exception {
    FileTypedResource value = new FileTypedResource(xmlContext.getXmlFile(), resType, xmlContext);
    resBunch.put(attrType, xmlContext.getXmlFile().getBaseName(), value);
  }
}
