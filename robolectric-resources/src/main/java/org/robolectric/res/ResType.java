package org.robolectric.res;

import org.jetbrains.annotations.NotNull;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
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
    @Override
    public TypedResource getValueWithType(String text, XmlLoader.XmlContext xmlContext) {
      throw new UnsupportedOperationException();
    }
  },

  CHAR_SEQUENCE,

  CHAR_SEQUENCE_ARRAY {
//    @Override
//    public TypedResource getValueWithType(String text, XmlLoader.XmlContext xmlContext) {
//      return extractScalarItems(text, CHAR_SEQUENCE_ARRAY, CHAR_SEQUENCE, xmlContext);
//    }
  },

  INTEGER_ARRAY {
//    @Override
//    public TypedResource getValueWithType(String text, XmlLoader.XmlContext xmlContext) {
//      return extractScalarItems(text, INTEGER_ARRAY, INTEGER, xmlContext);
//    }
  },

  TYPED_ARRAY {
//    @Override
//    public TypedResource getValueWithType(String text, XmlLoader.XmlContext xmlContext) throws XMLStreamException {
//      return extractTypedItems(text, TYPED_ARRAY, xmlContext);
//    }
  },

  NULL;

  private static TypedResource extractScalarItems(XMLStreamReader xml, ResType arrayResType, ResType itemResType, XmlLoader.XmlContext xmlContext) {
    List<TypedResource> items = new ArrayList<>();

    try {
      int nestLevel = 1;
      String tagName = null;
      while (nestLevel > 0 && xml.hasNext()) {
        switch (xml.next()) {
          case XMLStreamConstants.START_ELEMENT:
            nestLevel++;
            tagName = xml.getLocalName();
            break;

          case XMLStreamConstants.CHARACTERS:
            if ("item".equals(tagName)) {
              items.add(new TypedResource<>(xml.getText(), itemResType, xmlContext));
            }
            break;

          case XMLStreamConstants.END_ELEMENT:
            nestLevel--;
            break;
        }
      }
    } catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }
    TypedResource[] typedResources = items.toArray(new TypedResource[items.size()]);
    return new TypedResource<>(typedResources, arrayResType, xmlContext);
  }

  private static TypedResource extractTypedItems(XMLStreamReader xml, ResType arrayResType, XmlLoader.XmlContext xmlContext) throws XMLStreamException {
    final List<TypedResource> items = new ArrayList<>();
    int nestLevel = 1;
    String tagName = null;
    StringBuilder buf = new StringBuilder();
    while (nestLevel > 0 && xml.hasNext()) {
      switch (xml.next()) {
        case XMLStreamConstants.START_ELEMENT:
          tagName = xml.getLocalName();
          if ("item".equals(tagName)) {
            buf.setLength(0);
          }
          nestLevel++;
          break;
        case XMLStreamConstants.CHARACTERS:
          buf.append(xml.getText().trim());
          break;
        case XMLStreamConstants.END_ELEMENT:
          String itemString = buf.toString();

          if ("item".equals(tagName)) {
            ResType itemResType = inferFromValue(itemString);
            if (itemResType == ResType.CHAR_SEQUENCE) {
              if (AttributeResource.isStyleReference(itemString)) {
                itemResType = ResType.STYLE;
              } else if (itemString.equals("@null")) {
                itemResType = ResType.NULL;
              } else if (AttributeResource.isResourceReference(itemString)) {
                // This is a reference; no type info needed.
                itemResType = null;
              }
            }
            items.add(new TypedResource<>(itemString, itemResType, xmlContext));

            tagName = null;
            buf.setLength(0);
          }
          nestLevel--;
          break;
      }
    }

    final TypedResource[] typedResources = items.toArray(new TypedResource[items.size()]);
    return new TypedResource<>(typedResources, arrayResType, xmlContext);
  }

  public TypedResource getValueWithType(String text, XmlLoader.XmlContext xmlContext) throws XMLStreamException {
    return new TypedResource<>(text, this, xmlContext);
  }

  @NotNull
  protected String collectString(XMLStreamReader xml) throws XMLStreamException {
    StringBuilder buf = new StringBuilder();
    int nestLevel = 1;
    while (nestLevel > 0 && xml.hasNext()) {
      switch (xml.next()) {
        case XMLStreamConstants.START_ELEMENT:
          nestLevel++;
          break;
        case XMLStreamConstants.CHARACTERS:
          buf.append(xml.getText().trim());
          break;
        case XMLStreamConstants.END_ELEMENT:
          nestLevel--;
          break;
      }
    }
    return buf.toString();
  }

  /**
   * Parses a resource value to infer the type
   */
  public static ResType inferFromValue(String value) {
    if (value.startsWith("#")) {
      return ResType.COLOR;
    } else if ("true".equals(value) || "false".equals(value)) {
      return ResType.BOOLEAN;
    } else if (value.endsWith("dp") || value.endsWith("sp") || value.endsWith("pt") || value.endsWith("px") || value.endsWith("mm") || value.endsWith("in") || value.endsWith("dip")) {
      return ResType.DIMEN;
    } else {
      try {
        Integer.parseInt(value);
        return ResType.INTEGER;
      } catch (NumberFormatException nfe) {
      }

      try {
        Float.parseFloat(value);
        return ResType.FRACTION;
      } catch (NumberFormatException nfe) {
      }


      return ResType.CHAR_SEQUENCE;
    }
  }
}
