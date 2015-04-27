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
    @Override public TypedResource getValueWithType(XpathResourceXmlLoader.XmlNode xmlNode) {
      throw new UnsupportedOperationException();
    }
  },

  CHAR_SEQUENCE {
    @Override
    public TypedResource getValueWithType(XpathResourceXmlLoader.XmlNode xmlNode) {
      return new TypedResource<>(StringResources.escape(xmlNode.getTextContent()), this);
    }
  },

  CHAR_SEQUENCE_ARRAY {
    @Override public TypedResource getValueWithType(XpathResourceXmlLoader.XmlNode xmlNode) {
      return extractScalarItems(xmlNode, CHAR_SEQUENCE_ARRAY, CHAR_SEQUENCE);
    }
  },

  INTEGER_ARRAY {
    @Override public TypedResource getValueWithType(XpathResourceXmlLoader.XmlNode xmlNode) {
      return extractScalarItems(xmlNode, INTEGER_ARRAY, INTEGER);
    }
  };

  private static TypedResource extractScalarItems(XpathResourceXmlLoader.XmlNode xmlNode, ResType arrayResType, ResType itemResType) {
    List<TypedResource> items = new ArrayList<>();
    for (XpathResourceXmlLoader.XmlNode item : xmlNode.selectElements("item")) {
      items.add(new TypedResource<>(item.getTextContent(), itemResType));
    }
    TypedResource[] typedResources = items.toArray(new TypedResource[items.size()]);
    return new TypedResource<>(typedResources, arrayResType);
  }

  public TypedResource getValueWithType(XpathResourceXmlLoader.XmlNode xmlNode) {
    return new TypedResource<>(xmlNode.getTextContent(), this);
  }
}
