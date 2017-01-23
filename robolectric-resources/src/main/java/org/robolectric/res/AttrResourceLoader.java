package org.robolectric.res;

import java.util.ArrayList;
import java.util.List;

public class AttrResourceLoader extends XpathResourceXmlLoader {
  private final PackageResourceTable resourceTable;

  public AttrResourceLoader(PackageResourceTable resourceTable) {
    super("//attr");
    this.resourceTable = resourceTable;
  }

  @Override
  protected void processNode(String name, XmlNode xmlNode, XmlContext xmlContext) {
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
      pairs = new ArrayList<>();
      for (XmlNode enumNode : xmlNode.selectElements("enum")) {
        pairs.add(new AttrData.Pair(enumNode.getAttrValue("name"), enumNode.getAttrValue("value")));
      }
    } else if ("flag".equals(childFormat)) {
      pairs = new ArrayList<>();
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
    resourceTable.addResource("attr", name, new TypedResource<>(attrData, ResType.ATTR_DATA, xmlContext));
  }
}
