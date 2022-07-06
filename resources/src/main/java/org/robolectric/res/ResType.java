package org.robolectric.res;

import java.util.regex.Pattern;
import javax.annotation.Nullable;

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
  STYLE,
  CHAR_SEQUENCE,
  CHAR_SEQUENCE_ARRAY,
  INTEGER_ARRAY,
  TYPED_ARRAY,
  NULL;

  private static final Pattern DIMEN_RE = Pattern.compile("^\\d+(dp|dip|sp|pt|px|mm|in)$");

  @Nullable
  public static ResType inferType(String itemString) {
    ResType itemResType = ResType.inferFromValue(itemString);
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
    return itemResType;
  }

  /**
   * Parses a resource value to infer the type
   */
  public static ResType inferFromValue(String value) {
    if (value.startsWith("#")) {
      return COLOR;
    } else if ("true".equals(value) || "false".equals(value)) {
      return BOOLEAN;
    } else if (DIMEN_RE.matcher(value).find()) {
      return DIMEN;
    } else {
      try {
        Integer.parseInt(value);
        return INTEGER;
      } catch (NumberFormatException nfe) {}

      try {
        Float.parseFloat(value);
        return FRACTION;
      } catch (NumberFormatException nfe) {}


      return CHAR_SEQUENCE;
    }
  }
}
