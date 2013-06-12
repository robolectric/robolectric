package org.robolectric.res;

import javax.xml.xpath.XPathExpressionException;

public class StyleResourceLoader extends XpathResourceXmlLoader {
  private final ResBunch data;

  public StyleResourceLoader(ResBunch data) {
    super("/resources/style");
    this.data = data;
  }

  @Override
  protected void processNode(String name, XmlNode xmlNode, XmlContext xmlContext) throws XPathExpressionException {
    String styleName = xmlNode.getAttrValue("name");
    String styleParent = xmlNode.getAttrValue("parent");
    if (styleParent == null) {
      int lastDot = styleName.lastIndexOf('.');
      if (lastDot != -1) {
        styleParent = styleName.substring(0, lastDot);
      }
    }

    String styleNameWithUnderscores = underscorize(styleName);
    StyleData styleData = new StyleData(xmlContext.packageName, styleNameWithUnderscores, underscorize(styleParent));

    for (XmlNode item : xmlNode.selectElements("item")) {
      String attrName = item.getAttrValue("name");
      String value = item.getTextContent();

      ResName attrResName = ResName.qualifyResName(attrName, xmlContext.packageName, "attr");
      styleData.add(attrResName, new Attribute(attrResName, value, xmlContext.packageName));
    }

    data.put("style", styleNameWithUnderscores, new TypedResource<StyleData>(styleData, ResType.STYLE), xmlContext);
  }

  private String underscorize(String s) {
    return s == null ? null : s.replace('.', '_');
  }
}
