package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class OpaqueLoader extends StaxLoader {
  public OpaqueLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType resType) {
    super(resourceTable, xpathExpr, attrType, resType);
  }

  @Override
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
  }

  @Override
  public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    resourceTable.addResource(attrType, xmlContext.getXmlFile().getBaseName(), new FileTypedResource(xmlContext.getXmlFile(), resType, xmlContext));
  }
}
