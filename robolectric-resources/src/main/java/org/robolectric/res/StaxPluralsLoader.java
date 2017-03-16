package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StaxPluralsLoader extends StaxLoader {
  protected String name;
  private String quantity;
  private final List<Plural> plurals = new ArrayList<>();

  public StaxPluralsLoader(InMemoryPackageResourceTable resourceTable, String attrType, ResType charSequence) {
    super(resourceTable, attrType, charSequence);

    addHandler("item", new NodeHandler() {
      private final StringBuilder buf = new StringBuilder();

      @Override
      public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        quantity = xml.getAttributeValue(null, "quantity");
        buf.setLength(0);
      }

      @Override
      public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        buf.append(xml.getText());
      }

      @Override
      public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        plurals.add(new Plural(quantity, buf.toString()));
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
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    resourceTable.addResource(attrType, name, new TypedResource(new ArrayList<>(plurals), resType, xmlContext));
    plurals.clear();
  }
}
