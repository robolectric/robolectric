package org.robolectric.res;

public class ColorResourceLoader extends XpathResourceXmlLoader {
  private final PackageResourceTable resourceTable;

  public ColorResourceLoader(PackageResourceTable resourceTable) {
    super("/selector");
    this.resourceTable = resourceTable;
  }

  @Override
  protected void processNode(String name, XmlNode xmlNode, XmlContext xmlContext) {
    TypedResource value = new FileTypedResource(xmlContext.getXmlFile(), ResType.COLOR_STATE_LIST, xmlContext);
    resourceTable.addResource("color", xmlContext.getXmlFile().getBaseName(), value);
  }
}
