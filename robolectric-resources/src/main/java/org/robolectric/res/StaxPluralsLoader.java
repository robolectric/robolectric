package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StaxPluralsLoader extends StaxLoader {
  protected String name;
  final StringBuilder buf;
  List<Plural> plurals;
  String quantity;

  public StaxPluralsLoader(PackageResourceTable resourceTable, String xpathExpr, String attrType, ResType charSequence) {
    super(resourceTable, xpathExpr, attrType, charSequence);
    buf = new StringBuilder();
  }

  @Override
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    name = xml.getAttributeValue(null, "name");
    plurals = new ArrayList<>();
  }

  @Override
  protected void addInnerHandlers(final StaxDocumentLoader.NodeHandler nodeHandler) {
    final StaxDocumentLoader.NodeHandler itemNodeHandler = nodeHandler.findMatchFor("item", null);
    itemNodeHandler.addListener(new StaxDocumentLoader.NodeListener() {
      @Override
      public void onStart(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        quantity = xml.getAttributeValue(null, "quantity");
        buf.setLength(0);

        addInnerHandler(itemNodeHandler, buf);
      }

      @Override
      public void onCharacters(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        buf.append(xml.getText());
      }

      @Override
      public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
        plurals.add(new Plural(quantity, buf.toString()));
      }
    });
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    resourceTable.addResource(attrType, name, new PluralRules(plurals, resType, xmlContext));
  }
}
