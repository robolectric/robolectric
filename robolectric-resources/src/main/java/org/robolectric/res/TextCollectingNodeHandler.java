package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class TextCollectingNodeHandler extends NodeHandler {
  private final StringBuilder buf;

  public TextCollectingNodeHandler(StringBuilder buf) {
    this.buf = buf;
  }

  @Override
  public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    buf.append(xml.getText());
  }

  @Override
  NodeHandler findMatchFor(XMLStreamReader xml) {
    return this;
  }
}
