package org.robolectric.manifest;

import org.robolectric.res.ResName;
import org.robolectric.res.ResourceIndex;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.TypedResource;
import org.robolectric.shadows.ResourceHelper;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

  public void init(ResourceLoader resLoader, String packageName) {
    ResourceIndex resIndex = resLoader.getResourceIndex();

    if (!initialised) {
      for (Map.Entry<String,VALUE_TYPE> entry : typeMap.entrySet()) {
        String value = valueMap.get(entry.getKey()).toString();
        if (value.startsWith("@")) {
          ResName resName = ResName.qualifyResName(value.substring(1), packageName, null);

          switch (entry.getValue()) {
            case RESOURCE:
              // Was provided by resource attribute, store resource ID
              valueMap.put(entry.getKey(), resIndex.getResourceId(resName));
              break;
            case VALUE:
              // Was provided by value attribute, need to parse it
              TypedResource<?> typedRes = resLoader.getValue(resName, "");
              // The typed resource's data is always a String, so need to parse the value.
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
          // Raw value, so parse it in to the appropriate type and store it
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
        return ResourceHelper.getColor(value);
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
}
