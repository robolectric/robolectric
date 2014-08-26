package org.robolectric.res;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;

public class PluralResourceLoader extends XpathResourceXmlLoader {
  private ResBundle<PluralRules> pluralRulesResBundle;

  public PluralResourceLoader(ResBundle<PluralRules> pluralRulesResBundle) {
    super("/resources/plurals");
    this.pluralRulesResBundle = pluralRulesResBundle;
  }

  @Override protected void processNode(String name, XmlNode xmlNode, XmlContext xmlContext) throws XPathExpressionException {
    PluralRules rules = new PluralRules();
    for (XmlNode item : xmlNode.selectElements("item")) {
      String value = item.getTextContent();
      String quantity = item.getAttrValue("quantity");
      rules.add(new Plural(quantity, value));
    }
    pluralRulesResBundle.put("plurals", name, rules, xmlContext);
  }

  static class PluralRules {
    List<Plural> plurals = new ArrayList<Plural>();

    Plural find(int quantity) {
      for (Plural p : plurals) {
        if (p.num == quantity) return p;
      }
      for (Plural p : plurals) {
        if (p.num == -1) return p;
      }
      return null;
    }

    void add(Plural p) {
      plurals.add(p);
    }
  }
}
