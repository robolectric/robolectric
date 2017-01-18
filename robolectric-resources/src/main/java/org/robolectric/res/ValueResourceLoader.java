package org.robolectric.res;

public class ValueResourceLoader extends XpathResourceXmlLoader {
  private final PackageResourceTable resourceTable;
  private final String attrType;
  private final ResType resType;

  public ValueResourceLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType resType) {
    super(xpathExpr);
    this.resourceTable = resourceTable;
    this.attrType = attrType;
    this.resType = resType;
  }

  @Override
  protected void processNode(String name, XmlNode xmlNode, XmlContext xmlContext) {
    resourceTable.addResource(attrType, name, resType.getValueWithType(xmlNode, xmlContext));
  }
}
