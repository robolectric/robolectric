package org.robolectric.shadows;

import android.util.TypedValue;
import org.robolectric.res.AttrData;
import org.robolectric.util.Util;

public class Converter2<T> {
  private static int nextStringCookie = 0xbaaa5;

  synchronized static int getNextStringCookie() {
    return nextStringCookie++;
  }

  public static Converter2 getConverterFor(AttrData attrData, String type) {
    switch (type) {
      case "enum":
        return new EnumConverter(attrData);
      case "flag":
      case "flags": // because {@link ResourceTable#gFormatFlags} uses "flags"
        return new FlagConverter(attrData);
      case "boolean":
        return new FromBoolean();
      case "color":
        return new FromColor();
      case "dimension":
        return new FromDimen();
      case "float":
        return new FromFloat();
      case "integer":
        return new FromInt();
      case "string":
        return new FromCharSequence();
      case "fraction":
        return new FromFraction();
      default:
        throw new UnsupportedOperationException("Type not supported: " + type);
    }
  }

  public boolean fillTypedValue(T data, TypedValue typedValue, boolean throwOnFailure) {
    return false;
  }

  public static class FromCharSequence extends Converter2<String> {

    @Override
    public boolean fillTypedValue(String data, TypedValue typedValue, boolean throwOnFailure) {
      typedValue.type = TypedValue.TYPE_STRING;
      typedValue.data = 0;
      typedValue.assetCookie = getNextStringCookie();
      typedValue.string = data;
      return true;
    }
  }

  public static class FromColor extends Converter2<String> {
    @Override
    public boolean fillTypedValue(String data, TypedValue typedValue, boolean throwOnFailure) {
      try {
        typedValue.type = ResourceHelper.getColorType(data);
        typedValue.data = ResourceHelper.getColor(data);
        typedValue.assetCookie = 0;
        typedValue.string = null;
        return true;
      } catch (NumberFormatException nfe) {
        return false;
      }
    }

  }


  private static class FromInt extends Converter2<String> {
    @Override
    public boolean fillTypedValue(String data, TypedValue typedValue, boolean throwOnFailure) {
      try {
        if (data.startsWith("0x")) {
          typedValue.type = TypedValue.TYPE_INT_HEX;
        } else {
          typedValue.type = TypedValue.TYPE_INT_DEC;
        }
        typedValue.data = convertInt(data);
        typedValue.assetCookie = 0;
        typedValue.string = null;
        return true;
      } catch (NumberFormatException nfe) {
        return false;
      }
    }

  }

  private static class FromFraction extends Converter2<String> {
    @Override
    public boolean fillTypedValue(String data, TypedValue typedValue, boolean throwOnFailure) {
      return ResourceHelper2.parseFloatAttribute(null, data, typedValue, false);
    }
  }

  private static class FromFloat extends Converter2<String> {
    @Override
    public boolean fillTypedValue(String data, TypedValue typedValue, boolean throwOnFailure) {
      return ResourceHelper2.parseFloatAttribute(null, data, typedValue, false);
    }
  }

  private static class FromBoolean extends Converter2<String> {
    @Override
    public boolean fillTypedValue(String data, TypedValue typedValue, boolean throwOnFailure) {
      typedValue.type = TypedValue.TYPE_INT_BOOLEAN;
      typedValue.assetCookie = 0;
      typedValue.string = null;

      if ("true".equalsIgnoreCase(data)) {
        typedValue.data = 1;
        return true;
      } else if ("false".equalsIgnoreCase(data)) {
        typedValue.data = 0;
        return true;
      }
      return false;
    }
  }

  private static class FromDimen extends Converter2<String> {
    @Override
    public boolean fillTypedValue(String data, TypedValue typedValue, boolean throwOnFailure) {
      return ResourceHelper2.parseFloatAttribute(null, data, typedValue, true);
    }
  }

  private static int convertInt(String rawValue) {
    try {
      // Decode into long, because there are some large hex values in the android resource files
      // (e.g. config_notificationsBatteryLowARGB = 0xFFFF0000 in sdk 14).
      // Integer.decode() does not support large, i.e. negative values in hex numbers.
      // try parsing decimal number
      return (int) Long.parseLong(rawValue);
    } catch (NumberFormatException nfe) {
      // try parsing hex number
      return Long.decode(rawValue).intValue();
    }
  }

  private static class EnumConverter extends EnumOrFlagConverter {
    EnumConverter(AttrData attrData) {
      super(attrData);
    }

    @Override
    public boolean fillTypedValue(String data, TypedValue typedValue, boolean throwOnFailure) {
      typedValue.type = TypedValue.TYPE_INT_HEX;
      if (throwOnFailure) {
        typedValue.data = findValueFor(data);
      } else {
        try {
          typedValue.data = findValueFor(data);
        } catch (Exception e) {
          return false;
        }
      }
      typedValue.assetCookie = 0;
      typedValue.string = null;
      return true;
    }
  }

  private static class FlagConverter extends EnumOrFlagConverter {
    FlagConverter(AttrData attrData) {
      super(attrData);
    }

    @Override
    public boolean fillTypedValue(String data, TypedValue typedValue, boolean throwOnFailure) {
      int flags = 0;
      for (String key : data.split("\\|", 0)) {
        if (throwOnFailure) {
          flags |= findValueFor(key);
        } else {
          try {
            flags |= findValueFor(key);
          } catch (Exception e) {
            return false;
          }
        }
      }

      typedValue.type = TypedValue.TYPE_INT_HEX;
      typedValue.data = flags;
      typedValue.assetCookie = 0;
      typedValue.string = null;
      return true;
    }
  }

  private static class EnumOrFlagConverter extends Converter2<String> {
    private final AttrData attrData;

    EnumOrFlagConverter(AttrData attrData) {
      this.attrData = attrData;
    }

    int findValueFor(String key) {
      String valueFor = attrData.getValueFor(key);
      if (valueFor == null) {
        // Maybe they have passed in the value directly, rather than the name.
        if (attrData.isValue(key)) {
          valueFor = key;
        } else {
          throw new RuntimeException("no value found for " + key);
        }
      }
      return Util.parseInt(valueFor);
    }
  }
}
