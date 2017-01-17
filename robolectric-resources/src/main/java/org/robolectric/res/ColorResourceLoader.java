package org.robolectric.res;

import javax.xml.stream.XMLStreamReader;

public class ColorResourceLoader extends XpathResourceXmlLoader {
  private final ResBunch data;

  public ColorResourceLoader(ResBunch data) {
    super("/selector");
    this.data = data;
  }

  @Override
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) {
  }

  @Override
  protected void onStart(String name, XmlNode xmlNode, XmlContext xmlContext) {
    TypedResource value = new FileTypedResource(xmlContext.getXmlFile(), ResType.COLOR_STATE_LIST, xmlContext);
    data.put("color", xmlContext.getXmlFile().getBaseName(), value);
  }
}
