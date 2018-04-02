package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class StaxValueLoader extends StaxLoader {
  private final StringBuilder buf = new StringBuilder();
  protected String name;

  public StaxValueLoader(PackageResourceTable resourceTable, String attrType, ResType resType) {
    super(resourceTable, attrType, resType);

    if (resType == ResType.CHAR_SEQUENCE) {
      addHandler("*", new TextCollectingNodeHandler(buf));
    }
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
    String s = buf.toString();
    if (resType == ResType.CHAR_SEQUENCE) {
      s = StringResources.processStringResources(s);
    } else {
      s = s.trim();
    }
    resourceTable.addResource(attrType, name, new TypedResource<>(s, resType, xmlContext));
  }
}
