package org.robolectric.res;

public class OpaqueFileLoader extends XmlLoader {
  private final ResBunch resBunch;
  private final String attrType;

  public OpaqueFileLoader(ResBunch resBunch, String attrType) {
    this.resBunch = resBunch;
    this.attrType = attrType;
  }

  @Override
  protected void processResourceXml(FsFile xmlFile, XpathResourceXmlLoader.XmlNode xmlNode, XmlContext xmlContext) throws Exception {
    resBunch.put(attrType, xmlFile.getBaseName(), new FileTypedResource<>(xmlFile.getPath(), ResType.LAYOUT), xmlContext);
  }
}
