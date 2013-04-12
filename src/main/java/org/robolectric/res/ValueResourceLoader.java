package org.robolectric.res;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ValueResourceLoader extends XpathResourceXmlLoader {
    private final ResBundle<String> resBundle;
    private final String attrType;
    private final boolean arraysToo;

    public ValueResourceLoader(ResBundle<String> resBundle, String attrType, boolean arraysToo) {
        super("/resources/" + attrType, attrType);
        this.resBundle = resBundle;
        this.attrType = attrType;
        this.arraysToo = arraysToo;
    }

    @Override
    protected void processResourceXml(File xmlFile, XmlNode xmlNode, XmlContext xmlContext) throws Exception {
        super.processResourceXml(xmlFile, xmlNode, xmlContext);

        if (arraysToo) {
            for (XmlNode arrayNode : xmlNode.selectByXpath("/resources/" + attrType + "-array")) {
                String name = arrayNode.getAttrValue("name");

                List<String> itemStrings = new ArrayList<String>();
                for (XmlNode itemNode : arrayNode.selectByXpath(".//item")) {
                    itemStrings.add(itemNode.getTextContent());
                }
                resBundle.putArray(attrType + "-array", name, itemStrings, xmlContext);
            }
        }
    }

    @Override
    protected void processNode(String name, XmlNode xmlNode, XmlContext xmlContext, String attrType) throws XPathExpressionException {
        resBundle.put(attrType, name, xmlNode.getTextContent(), xmlContext);
    }
}
