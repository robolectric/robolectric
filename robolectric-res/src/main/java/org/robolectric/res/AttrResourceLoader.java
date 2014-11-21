package org.robolectric.res;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;

public class AttrResourceLoader extends XpathResourceXmlLoader {
  private final ResBunch resBunch;

  public AttrResourceLoader(ResBunch resBunch) {
    super("//attr");
    this.resBunch = resBunch;
  }

  @Override
  protected void processNode(String name, XmlNode xmlNode, XmlContext xmlContext)
      throws XPathExpressionException {
    String format = xmlNode.getAttrValue("format");
    String childFormat = null;
    List<AttrData.Pair> pairs = null;

    XmlNode firstChild = xmlNode.getFirstChild();
    if (firstChild != null) {
      childFormat = firstChild.getElementName();
      if (format == null) {
        format = childFormat;
      } else {
        format = format + "|" + childFormat;
      }
    }

    if ("enum".equals(childFormat)) {
      pairs = new ArrayList<AttrData.Pair>();
      for (XmlNode enumNode : xmlNode.selectElements("enum")) {
        pairs.add(new AttrData.Pair(enumNode.getAttrValue("name"), enumNode.getAttrValue("value")));
      }
    } else if ("flag".equals(childFormat)) {
      pairs = new ArrayList<AttrData.Pair>();
      for (XmlNode flagNode : xmlNode.selectElements("flag")) {
        pairs.add(new AttrData.Pair(flagNode.getAttrValue("name"), flagNode.getAttrValue("value")));
      }
    }

    if (format == null) {
      return;
//            throw new IllegalStateException(
//                    "you need a format, enums, or flags for \"" + name + "\" in " + xmlContext);
    }
    AttrData attrData = new AttrData(name, format, pairs);
    resBunch.put("attr", name, new TypedResource<AttrData>(attrData, ResType.ATTR_DATA),
        xmlContext);
  }
}
