package org.robolectric.manifest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.TypedResource;
import org.robolectric.res.android.ResTable_config;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class MetaData {
  private final Map<String, Object> valueMap = new LinkedHashMap<>();
  private final Map<String, VALUE_TYPE> typeMap = new LinkedHashMap<>();
  private boolean initialised;

  public MetaData(List<Node> nodes) {
    for (Node metaNode : nodes) {
      NamedNodeMap attributes = metaNode.getAttributes();
      Node nameAttr = attributes.getNamedItem("android:name");
      Node valueAttr = attributes.getNamedItem("android:value");
      Node resourceAttr = attributes.getNamedItem("android:resource");

      if (valueAttr != null) {
        valueMap.put(nameAttr.getNodeValue(), valueAttr.getNodeValue());
        typeMap.put(nameAttr.getNodeValue(), VALUE_TYPE.VALUE);
      } else if (resourceAttr != null) {
        valueMap.put(nameAttr.getNodeValue(), resourceAttr.getNodeValue());
        typeMap.put(nameAttr.getNodeValue(), VALUE_TYPE.RESOURCE);
      }
    }
  }

  public void init(ResourceTable resourceTable, String packageName) throws RoboNotFoundException {
    if (!initialised) {
      for (Map.Entry<String,VALUE_TYPE> entry : typeMap.entrySet()) {
        String value = valueMap.get(entry.getKey()).toString();
        if (value.startsWith("@")) {
          ResName resName = ResName.qualifyResName(value.substring(1), packageName, null);

          switch (entry.getValue()) {
            case RESOURCE:
              // Was provided by resource attribute, store resource ID
              valueMap.put(entry.getKey(), resourceTable.getResourceId(resName));
              break;
            case VALUE:
              // Was provided by value attribute, need to inferFromValue it
              TypedResource<?> typedRes = resourceTable.getValue(resName, new ResTable_config());
              // The typed resource's data is always a String, so need to inferFromValue the value.
              if (typedRes == null) {
                throw new RoboNotFoundException(resName.getFullyQualifiedName());
              }
              switch (typedRes.getResType()) {
                case BOOLEAN: case COLOR: case INTEGER: case FLOAT:
                  valueMap.put(entry.getKey(), parseValue(typedRes.getData().toString()));
                  break;
                default:
                  valueMap.put(entry.getKey(),typedRes.getData());
              }
              break;
          }
        } else if (entry.getValue() == VALUE_TYPE.VALUE) {
          // Raw value, so inferFromValue it in to the appropriate type and store it
          valueMap.put(entry.getKey(), parseValue(value));
        }
      }
      // Finished parsing, mark as initialised
      initialised = true;
    }
  }

  public Map<String, Object> getValueMap() {
    return valueMap;
  }

  private enum VALUE_TYPE {
    RESOURCE,
    VALUE
  }

  private Object parseValue(String value) {
    if (value == null) {
      return null;
    } else if ("true".equals(value)) {
      return true;
    } else if ("false".equals(value)) {
      return false;
    } else if (value.startsWith("#")) {
      // if it's a color, add it and continue
      try {
        return getColor(value);
      } catch (NumberFormatException e) {
            /* Not a color */
      }
    } else if (value.contains(".")) {
      // most likely a float
      try {
        return Float.parseFloat(value);
      } catch (NumberFormatException e) {
        // Not a float
      }
    } else {
      // if it's an int, add it and continue
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException ei) {
        // Not an int
      }
    }

    // Not one of the above types, keep as String
    return value;
  }

  // todo: this is copied from ResourceHelper, dedupe
  /**
   * Returns the color value represented by the given string value
   * @param value the color value
   * @return the color as an int
   * @throws NumberFormatException if the conversion failed.
   */
  public static int getColor(String value) {
    if (value != null) {
      if (value.startsWith("#") == false) {
        throw new NumberFormatException(
            String.format("Color value '%s' must start with #", value));
      }

      value = value.substring(1);

      // make sure it's not longer than 32bit
      if (value.length() > 8) {
        throw new NumberFormatException(String.format(
            "Color value '%s' is too long. Format is either" +
                "#AARRGGBB, #RRGGBB, #RGB, or #ARGB",
            value));
      }

      if (value.length() == 3) { // RGB format
        char[] color = new char[8];
        color[0] = color[1] = 'F';
        color[2] = color[3] = value.charAt(0);
        color[4] = color[5] = value.charAt(1);
        color[6] = color[7] = value.charAt(2);
        value = new String(color);
      } else if (value.length() == 4) { // ARGB format
        char[] color = new char[8];
        color[0] = color[1] = value.charAt(0);
        color[2] = color[3] = value.charAt(1);
        color[4] = color[5] = value.charAt(2);
        color[6] = color[7] = value.charAt(3);
        value = new String(color);
      } else if (value.length() == 6) {
        value = "FF" + value;
      }

      // this is a RRGGBB or AARRGGBB value

      // Integer.parseInt will fail to inferFromValue strings like "ff191919", so we use
      // a Long, but cast the result back into an int, since we know that we're only
      // dealing with 32 bit values.
      return (int)Long.parseLong(value, 16);
    }

    throw new NumberFormatException();
  }

}
