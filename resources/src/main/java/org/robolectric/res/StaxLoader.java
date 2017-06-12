package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public abstract class StaxLoader extends NodeHandler {

  protected final PackageResourceTable resourceTable;
  protected final String attrType;
  protected final ResType resType;

  public StaxLoader(PackageResourceTable resourceTable, String attrType, ResType resType) {
    this.resourceTable = resourceTable;
    this.attrType = attrType;
    this.resType = resType;
  }

  @Override
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
  }

  @Override
  public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
  }
}
