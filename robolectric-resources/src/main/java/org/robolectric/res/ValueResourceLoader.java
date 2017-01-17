package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class ValueResourceLoader extends XpathResourceXmlLoader {
  private final ResBunch resBunch;
  private final String attrType;
  private final ResType resType;

  private String name;
  private final StringBuilder buf = new StringBuilder();

  public ValueResourceLoader(ResBunch resBunch, String xpathExpr, String attrType, ResType resType) {
    super(xpathExpr);
    this.resBunch = resBunch;
    this.attrType = attrType;
    this.resType = resType;
  }

  @Override
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    name = xml.getAttributeValue(null, "name");
    buf.setLength(0);
  }

  @Override
  public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    buf.append(xml.getText());
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    resBunch.put(attrType, name, resType.getValueWithType(buf.toString(), xmlContext));
  }

  @Override
  protected void processResourceXml(FsFile xmlFile, XmlNode xmlNode, XmlContext xmlContext) {
    super.processResourceXml(xmlFile, xmlNode, xmlContext);
  }

  @Override
  protected void onStart(String name, XmlNode xmlNode, XmlContext xmlContext) {
//    resBunch.put(attrType, name, resType.getValueWithType(xmlNode, xmlContext));
  }
}
