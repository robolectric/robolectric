package org.robolectric.res;

import java.util.ArrayList;
import java.util.List;

public class PluralResourceLoader extends XpathResourceXmlLoader {
  private ResBunch resBunch;

  public PluralResourceLoader(ResBunch resBunch) {
    super("/resources/plurals");
    this.resBunch = resBunch;
  }

  @Override protected void processNode(String name, XmlNode xmlNode, XmlContext xmlContext) {
    List<Plural> rules = new ArrayList<>();
    for (XmlNode item : xmlNode.selectElements("item")) {
      String value = item.getTextContent();
      String quantity = item.getAttrValue("quantity");
      rules.add(new Plural(quantity, value));
    }
    resBunch.put("plurals", name, new PluralRules(rules, ResType.CHAR_SEQUENCE, xmlContext));
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
