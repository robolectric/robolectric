package org.robolectric.res;

import java.util.ArrayList;
import java.util.List;

public enum ResType {
  DRAWABLE,
  ATTR_DATA,
  BOOLEAN,
  COLOR,
  COLOR_STATE_LIST,
  DIMEN,
  FILE,
  FLOAT,
  FRACTION,
  INTEGER,
  LAYOUT,
  STYLE {
    @Override public TypedResource getValueWithType(XpathResourceXmlLoader.XmlNode xmlNode, XmlLoader.XmlContext xmlContext) {
      throw new UnsupportedOperationException();
    }
  },

  CHAR_SEQUENCE {
    @Override
    public TypedResource getValueWithType(XpathResourceXmlLoader.XmlNode xmlNode, XmlLoader.XmlContext xmlContext) {
      return new TypedResource<>(StringResources.proccessStringResources(xmlNode.getTextContent()), this, xmlContext);
    }
  },

  CHAR_SEQUENCE_ARRAY {
    @Override public TypedResource getValueWithType(XpathResourceXmlLoader.XmlNode xmlNode, XmlLoader.XmlContext xmlContext) {
      return extractScalarItems(xmlNode, CHAR_SEQUENCE_ARRAY, CHAR_SEQUENCE, xmlContext);
    }
  },

  INTEGER_ARRAY {
    @Override public TypedResource getValueWithType(XpathResourceXmlLoader.XmlNode xmlNode, XmlLoader.XmlContext xmlContext) {
      return extractScalarItems(xmlNode, INTEGER_ARRAY, INTEGER, xmlContext);
    }
  };

  private static TypedResource extractScalarItems(XpathResourceXmlLoader.XmlNode xmlNode, ResType arrayResType, ResType itemResType, XmlLoader.XmlContext xmlContext) {
    List<TypedResource> items = new ArrayList<>();
    for (XpathResourceXmlLoader.XmlNode item : xmlNode.selectElements("item")) {
      items.add(new TypedResource<>(item.getTextContent(), itemResType, xmlContext));
    }
    TypedResource[] typedResources = items.toArray(new TypedResource[items.size()]);
    return new TypedResource<>(typedResources, arrayResType, xmlContext);
  }

  public TypedResource getValueWithType(XpathResourceXmlLoader.XmlNode xmlNode, XmlLoader.XmlContext xmlContext) {
    return new TypedResource<>(xmlNode.getTextContent(), this, xmlContext);
  }

  /**
   * Parses a resource value to infer the type
   */
  public static ResType inferFromValue(String value) {
    if (value.startsWith("#")) {
      return ResType.COLOR;
    } else if ("true".equals(value) || "false".equals(value)) {
      return ResType.BOOLEAN;
    } else if (value.endsWith("dp") || value.endsWith("sp") || value.endsWith("pt") || value.endsWith("px") || value.endsWith("mm") || value.endsWith("in")) {
      return ResType.DIMEN;
    } else {
      try {
        Integer.parseInt(value);
        return ResType.INTEGER;
      } catch (NumberFormatException nfe) {}

      try {
        Float.parseFloat(value);
        return ResType.FRACTION;
      } catch (NumberFormatException nfe) {}


      return ResType.CHAR_SEQUENCE;
    }
  }
}
