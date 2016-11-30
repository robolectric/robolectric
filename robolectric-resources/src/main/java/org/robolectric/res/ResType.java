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
}
