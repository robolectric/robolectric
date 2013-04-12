package org.robolectric.res;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;

public class PluralResourceLoader extends XpathResourceXmlLoader {
    private ResBundle<PluralRules> pluralRulesResBundle;

    public PluralResourceLoader(ResourceIndex resourceIndex, ResBundle<PluralRules> pluralRulesResBundle) {
        super("/resources/plurals", "plurals");
        this.pluralRulesResBundle = pluralRulesResBundle;
    }

    @Override protected void processNode(String name, XmlNode xmlNode, XmlContext xmlContext, String attrType) throws XPathExpressionException {
        PluralRules rules = new PluralRules();
        for (XmlNode item : xmlNode.selectElements("item")) {
            String value = item.getTextContent();
            String quantity = item.getAttrValue("quantity");
            rules.add(new Plural(quantity, value));
        }
        pluralRulesResBundle.put(attrType, name, rules, xmlContext);
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

    static class Plural {
        final String quantity, string;
        final int num;

        Plural(String quantity, String string) {
            this.quantity = quantity;
            this.string = string;
            if ("zero".equals(quantity)) {
                num = 0;
            } else if ("one".equals(quantity)) {
                num = 1;
            } else if ("two".equals(quantity)) {
                num = 2;
            } else if ("other".equals(quantity)) {
                num = -1;
            } else {
                num = -1;
            }
        }
    }
}
