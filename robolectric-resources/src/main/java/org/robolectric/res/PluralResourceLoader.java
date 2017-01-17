package org.robolectric.res;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluralResourceLoader extends XpathResourceXmlLoader {
  private final ResBunch resBunch;

  private String name;
  private List<Plural> rules;

  public PluralResourceLoader(ResBunch resBunch) {
    super("/resources/plurals");
    this.resBunch = resBunch;
  }

  @Override
  public DocumentLoader.NodeHandler addTo(DocumentLoader.NodeHandler nodeHandler) {
    DocumentLoader.NodeHandler pluralsHandler = super.addTo(nodeHandler);

    DocumentLoader.NodeHandler itemHandler = pluralsHandler.addHandler("item", Collections.<String, String>emptyMap());
    itemHandler.addListener(new DocumentLoader.NodeListener() {
      private final StringBuilder buf = new StringBuilder();

      private String quantity;

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
        rules.add(new Plural(quantity, buf.toString()));
      }
    });
    return pluralsHandler;
  }

  @Override
  public void onStart(XMLStreamReader xml, XmlContext xmlContext) {
    name = xml.getAttributeValue(null, "name");
    rules = new ArrayList<>();
  }

  @Override protected void onStart(String name, XmlNode xmlNode, XmlContext xmlContext) {
  }

  @Override
  public void onEnd(XMLStreamReader xml, XmlContext xmlContext) throws XMLStreamException {
    resBunch.put("plurals", name, new PluralRules(rules, ResType.CHAR_SEQUENCE, xmlContext));
    name = null;
    rules = null;
  }

  public static class PluralRules extends TypedResource<List<Plural>> {
    public PluralRules(List<Plural> data, ResType resType, XmlContext xmlContext) {
      super(data, resType, xmlContext);
    }

    public Plural find(int quantity) {
      for (Plural p : getData()) {
        if (p.num == quantity) return p;
      }
      for (Plural p : getData()) {
        if (p.num == -1) return p;
      }
      return null;
    }
  }
}
