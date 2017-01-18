package org.robolectric.res;

public class OpaqueFileLoader implements XmlLoader {
  private final PackageResourceTable resourceTable;
  private final String attrType;
  private final ResType resType;

  public OpaqueFileLoader(PackageResourceTable resourceTable, String attrType) {
    this(resourceTable, attrType, ResType.LAYOUT);
  }

  public OpaqueFileLoader(PackageResourceTable resourceTable, String attrType, ResType resType) {
    this.resourceTable = resourceTable;
    this.attrType = attrType;
    this.resType = resType;
  }

  @Override
  public void processResourceXml(XpathResourceXmlLoader.XmlNode xmlNode, XmlContext xmlContext) {
    resourceTable.addResource(attrType, xmlContext.getXmlFile().getBaseName(), new FileTypedResource(xmlContext.getXmlFile(), resType, xmlContext));
  }
}
