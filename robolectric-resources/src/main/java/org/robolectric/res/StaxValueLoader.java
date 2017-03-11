package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.robolectric.res.StaxLoader.addInnerHandler;

public class StaxValueLoader extends StaxLoader {
  private final StringBuilder buf = new StringBuilder();
  protected String name;

  public StaxValueLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType resType) {
    super(resourceTable, xpathExpr, attrType, resType);
  }

  @Override
  protected void addInnerHandlers(StaxDocumentLoader.NodeHandler nodeHandler) {
    if (resType == ResType.CHAR_SEQUENCE) {
      addInnerHandler(nodeHandler, buf);
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
      s = StringResources.proccessStringResources(s);
    }
    resourceTable.addResource(attrType, name, new TypedResource<>(s, resType, xmlContext));
  }
}
