package org.robolectric.res;

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class StaxArrayLoader extends StaxLoader {
  private String name;
  private List<TypedResource> items;
  private final StringBuilder buf = new StringBuilder();

  public StaxArrayLoader(PackageResourceTable resourceTable, String attrType, ResType arrayResType, final ResType scalarResType) {
    super(resourceTable, attrType, arrayResType);

    addHandler("item", new NodeHandler() {
      @Override
      public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        buf.setLength(0);
      }

      @Override
      public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        buf.append(xml.getText());
      }

      @Override
      public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        ResType resType = scalarResType == null ? ResType.inferType(buf.toString()) : scalarResType;
        items.add(new TypedResource<>(buf.toString(), resType, xmlContext));
      }

      @Override
      NodeHandler findMatchFor(XMLStreamReader xml) {
        return new TextCollectingNodeHandler(buf);
      }
    });
  }

  @Override
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    name = xml.getAttributeValue(null, "name");
    items = new ArrayList<>();
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    resourceTable.addResource(attrType, name, new TypedResource<>(items, resType, xmlContext));
  }
}
