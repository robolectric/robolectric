package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StaxAttrLoader extends StaxLoader {
  private String name;
  private String format;
  private List<AttrData.Pair> pairs;

  public StaxAttrLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType resType) {
    super(resourceTable, xpathExpr, attrType, resType);
  }

  @Override
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    name = xml.getAttributeValue(null, "name");
    format = xml.getAttributeValue(null, "format");
    pairs = new ArrayList<>();
  }

  @Override
  protected void addInnerHandlers(StaxDocumentLoader.NodeHandler nodeHandler) {
    nodeHandler.findMatchFor(null, null).addListener(new StaxDocumentLoader.NodeListener() {
      private String value;
      private String name;

      @Override
      public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        String type = xml.getLocalName();
        if (pairs.isEmpty()) {
          if (format == null) {
            format = type;
          } else {
            format = format + "|" + type;
          }
        }
        name = xml.getAttributeValue(null, "name");
        value = xml.getAttributeValue(null, "value");
        pairs.add(new AttrData.Pair(name, value));
      }

      @Override
      public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      }

      @Override
      public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
      }
    });
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    AttrData attrData = new AttrData(name, format, pairs);
//      xmlContext = xmlContext.withLineNumber(xml.getLocation().getLineNumber());
    if (attrData.getFormat() != null) {
      resourceTable.addResource(attrType, name, new TypedResource<>(attrData, resType, xmlContext));
    }
  }
}
