package com.xtremelabs.robolectric.res;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluralResourceLoader extends XpathResourceXmlLoader implements ResourceValueConverter {
    Map<String, PluralRules> plurals = new HashMap<String, PluralRules>();
    private StringResourceLoader stringResourceLoader;

    public PluralResourceLoader(ResourceExtractor resourceExtractor, StringResourceLoader stringResourceLoader) {
        super(resourceExtractor, "/resources/plurals");
        this.stringResourceLoader = stringResourceLoader;
    }

    public String getValue(int resourceId, int quantity) {
        String name = resourceExtractor.getResourceName(resourceId);
        PluralRules rules = plurals.get(name);
        if (rules != null) {
            Plural p = rules.find(quantity);
            if (p != null) {
                return p.string;
            }
        }
        return null;
    }

    @Override protected void processNode(Node node, String name, boolean isSystem) throws XPathExpressionException {
        XPathExpression itemXPath = XPathFactory.newInstance().newXPath().compile("item");
        NodeList childNodes = (NodeList) itemXPath.evaluate(node, XPathConstants.NODESET);
        PluralRules rules = new PluralRules();
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node childNode = childNodes.item(j);
            String value = childNode.getTextContent();
            String quantity = childNode.getAttributes().getNamedItem("quantity").getTextContent();
            if (value.startsWith("@")) {
                value = value.substring(1);
                rules.add(new Plural(quantity, stringResourceLoader.getValue(value, isSystem)));
            } else {
                rules.add(new Plural(quantity, value));
            }
        }
        plurals.put("plurals/" + name, rules);
    }

    @Override public Object convertRawValue(String rawValue) {
        return rawValue;
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
