package org.robolectric.res;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

public class PluralResourceLoader extends XpathResourceXmlLoader {
    private ResBundle<PluralRules> pluralRulesResBundle;

    public PluralResourceLoader(ResourceIndex resourceIndex, ResBundle<PluralRules> pluralRulesResBundle) {
        super("/resources/plurals", "plurals");
        this.pluralRulesResBundle = pluralRulesResBundle;
    }

    @Override protected void processNode(Node node, String name, XmlContext xmlContext, String attrType) throws XPathExpressionException {
        XPathExpression itemXPath = XPathFactory.newInstance().newXPath().compile("item");
        NodeList childNodes = (NodeList) itemXPath.evaluate(node, XPathConstants.NODESET);
        PluralRules rules = new PluralRules();
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node childNode = childNodes.item(j);
            String value = childNode.getTextContent();
            String quantity = childNode.getAttributes().getNamedItem("quantity").getTextContent();
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
