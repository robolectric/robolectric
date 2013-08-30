package org.robolectric.shadows;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import org.robolectric.res.AttrData;
import org.robolectric.res.Attribute;
import org.robolectric.res.DrawableNode;
import org.robolectric.res.DrawableResourceLoader;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResName;
import org.robolectric.res.ResType;
import org.robolectric.res.ResourceIndex;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.TypedResource;
import org.robolectric.util.Util;

import java.util.LinkedHashMap;
import java.util.Map;

public class Converter<T> {
  private static int nextStringCookie = 0xbaaa5;
  private static final Map<String, ResType> ATTR_TYPE_MAP = new LinkedHashMap<String, ResType>();

  static {
    ATTR_TYPE_MAP.put("boolean", ResType.BOOLEAN);
    ATTR_TYPE_MAP.put("color", ResType.COLOR);
    ATTR_TYPE_MAP.put("dimension", ResType.DIMEN);
    ATTR_TYPE_MAP.put("float", ResType.FLOAT);
    ATTR_TYPE_MAP.put("integer", ResType.INTEGER);
    ATTR_TYPE_MAP.put("string", ResType.CHAR_SEQUENCE);
  }

  synchronized private static int getNextStringCookie() {
    return nextStringCookie++;
  }

  public static void convertAndFill(Attribute attribute, TypedValue outValue, ResourceLoader resourceLoader, String qualifiers) {
    if (attribute == null || attribute.isNull()) {
      outValue.type = TypedValue.TYPE_NULL;
      return;
    }

    TypedResource attrTypeData = resourceLoader.getValue(attribute.resName, qualifiers);
    if (attrTypeData == null) {
      throw new Resources.NotFoundException("Couldn't find " + attribute.resName + " attr data");
    }

    AttrData attrData = (AttrData) attrTypeData.getData();
    convertAndFill(attribute, outValue, resourceLoader, qualifiers, attrData);
  }

  public static void convertAndFill(Attribute attribute, TypedValue outValue, ResourceLoader resourceLoader, String qualifiers, AttrData attrData) {
    // short-circuit Android caching of loaded resources cuz our string positions don't remain stable...
    outValue.assetCookie = getNextStringCookie();
    String format = attrData.getFormat();
    String[] types = format.split("\\|");

    // dereference resource and style references...
    if (attribute.isStyleReference()) {
      ResName resName = attribute.getStyleReference();
      // todo
      System.out.println("TODO: Not handling " + attribute.value + " yet!");
      return;
    }

    ResourceIndex resourceIndex = resourceLoader.getResourceIndex();
    while (attribute.isResourceReference()) {
      ResName resName = attribute.getResourceReference();
      Integer resourceId = resourceIndex.getResourceId(resName);
      if (resourceId == null) {
        throw new Resources.NotFoundException("unknown resource " + resName);
      }
      outValue.type = TypedValue.TYPE_REFERENCE;
      outValue.resourceId = resourceId;
      TypedResource dereferencedRef = resourceLoader.getValue(resName, qualifiers);

      if (dereferencedRef == null) {
        if (resName.type.equals("id")) {
          return;
        } else if (resName.type.equals("layout")) {
          return; // resourceId is good enough, right?
        } else if (resName.type.equals("dimen")) {
          return;
        } else if (DrawableResourceLoader.isStillHandledHere(resName)) {
          // wtf. color and drawable references reference are all kinds of stupid.
          DrawableNode drawableNode = resourceLoader.getDrawableNode(resName, qualifiers);
          if (drawableNode == null) {
            throw new Resources.NotFoundException("can't find file for " + resName);
          } else {
            outValue.type = TypedValue.TYPE_STRING;
            outValue.data = 0;
            outValue.assetCookie = getNextStringCookie();
            outValue.string = drawableNode.getFsFile().getPath();
            return;
          }
        } else {
          throw new RuntimeException("huh? " + resName);
        }
      } else {
        if (dereferencedRef.isFile()) {
          outValue.type = TypedValue.TYPE_STRING;
          outValue.data = 0;
          outValue.assetCookie = getNextStringCookie();
          outValue.string = dereferencedRef.asString();
          return;
        } else if (dereferencedRef.getData() instanceof String) {
          attribute = new Attribute(attribute.resName, dereferencedRef.asString(), resName.packageName);
          if (attribute.isResourceReference()) {
            continue;
          }
        }
      }
      break;
    }


    if (attribute.isNull()) {
      outValue.type = TypedValue.TYPE_NULL;
      return;
    }

    // Special case for attrs that can be integers or enums, like numColumns.
    // todo: generalize this!
    if (format.equals("integer|enum") || format.equals("dimension|enum")) {
      if (attribute.value.matches("^\\d.*")) {
        types = new String[] { types[0] };
      } else {
        types = new String[] { "enum" };
      }
    }

    for (String type : types) {
      if ("reference".equals(type)) continue; // already handled above

      Converter converter = ATTR_TYPE_MAP.containsKey(type)
          ? getConverter(ATTR_TYPE_MAP.get(type))
          : null;

      if (converter == null) {
        if (type.equals("enum")) {
          converter = new EnumConverter(attrData);
        } else if (type.equals("flag")) {
          converter = new FlagConverter(attrData);
        }
      }

      if (converter != null) {
        try {
          converter.fillTypedValue(attribute.value, outValue);
        } catch (Exception e) {
          throw new RuntimeException("error converting " + attribute.value + " using " + converter.getClass().getSimpleName(), e);
        }
        return;
      }
    }
  }

  public static Converter getConverter(ResType resType) {
    switch (resType) {
      case ATTR_DATA:
        return new FromAttrData();
      case BOOLEAN:
        return new FromBoolean();
      case CHAR_SEQUENCE:
        return new FromCharSequence();
      case COLOR:
        return new FromColor();
      case COLOR_STATE_LIST:
        return new FromFilePath();
      case DIMEN:
        return new FromDimen();
      case FILE:
        return new FromFile();
      case FLOAT:
        return new FromFloat();
      case INTEGER:
        return new FromInt();
      case LAYOUT:
        return new FromFilePath();

      case CHAR_SEQUENCE_ARRAY:
      case INTEGER_ARRAY:
        return new FromArray();
      default:
        throw new UnsupportedOperationException(resType.name());
    }
  }

  public CharSequence asCharSequence(TypedResource typedResource) {
    throw cantDo("asCharSequence");
  }

  public int asInt(TypedResource typedResource) {
    throw cantDo("asInt");
  }

  public TypedResource[] getItems(TypedResource typedResource) {
    throw cantDo("getItems");
  }

  public void fillTypedValue(T data, TypedValue typedValue) {
    throw cantDo("fillTypedValue");
  }

  private UnsupportedOperationException cantDo(String operation) {
    return new UnsupportedOperationException(getClass().getName() + " doesn't support " + operation);
  }

  public static class FromAttrData extends Converter<AttrData> {
    @Override public CharSequence asCharSequence(TypedResource typedResource) {
      return typedResource.asString();
    }

    @Override public void fillTypedValue(AttrData data, TypedValue typedValue) {
      typedValue.type = TypedValue.TYPE_STRING;
      throw new RuntimeException("huh?");
    }
  }

  public static class FromCharSequence extends Converter<String> {
    @Override public CharSequence asCharSequence(TypedResource typedResource) {
      return typedResource.asString();
    }

    @Override public int asInt(TypedResource typedResource) {
      String rawValue = typedResource.asString();
      return convertInt(rawValue);
    }

    @Override public void fillTypedValue(String data, TypedValue typedValue) {
      typedValue.type = TypedValue.TYPE_STRING;
      typedValue.data = 0;
      typedValue.assetCookie = getNextStringCookie();
      typedValue.string = data;
    }
  }

  public static class FromColor extends Converter<String> {
    @Override public void fillTypedValue(String data, TypedValue typedValue) {
      typedValue.type = TypedValue.TYPE_INT_COLOR_ARGB8;
      typedValue.data = Color.parseColor(data);
      typedValue.assetCookie = 0;
    }
  }

  private static class FromFilePath extends Converter<String> {
    @Override public void fillTypedValue(String data, TypedValue typedValue) {
      typedValue.type = TypedValue.TYPE_STRING;
      typedValue.data = 0;
      typedValue.string = data;
      typedValue.assetCookie = getNextStringCookie();
    }
  }

  public static class FromArray extends Converter {
    @Override public TypedResource[] getItems(TypedResource typedResource) {
      return (TypedResource[]) typedResource.getData();
    }
  }

  private static class FromInt extends Converter<String> {
    @Override public void fillTypedValue(String data, TypedValue typedValue) {
      typedValue.type = TypedValue.TYPE_INT_HEX;
      typedValue.data = convertInt(data);
      typedValue.assetCookie = 0;
    }

    @Override public int asInt(TypedResource typedResource) {
      String rawValue = typedResource.asString();
      return convertInt(rawValue);
    }
  }

  private static class FromFile extends Converter<FsFile> {
    @Override public void fillTypedValue(FsFile data, TypedValue typedValue) {
      typedValue.type = TypedValue.TYPE_STRING;
      typedValue.data = 0;
      typedValue.string = data.getPath();
      typedValue.assetCookie = getNextStringCookie();
    }
  }

  private static class FromFloat extends Converter<String> {
    @Override public void fillTypedValue(String data, TypedValue typedValue) {
      ResourceHelper.parseFloatAttribute(null, data, typedValue, false);
    }
  }

  private static class FromBoolean extends Converter<String> {
    @Override public void fillTypedValue(String data, TypedValue typedValue) {
      typedValue.type = TypedValue.TYPE_INT_BOOLEAN;
      typedValue.data = convertBool(data) ? 1 : 0;
      typedValue.assetCookie = 0;
    }
  }

  private static class FromDimen extends Converter<String> {
    @Override public void fillTypedValue(String data, TypedValue typedValue) {
      ResourceHelper.parseFloatAttribute(null, data, typedValue, false);
    }
  }

  ///////////////////////

  private static int convertInt(String rawValue) {
    try {
      // Decode into long, because there are some large hex values in the android resource files
      // (e.g. config_notificationsBatteryLowARGB = 0xFFFF0000 in sdk 14).
      // Integer.decode() does not support large, i.e. negative values in hex numbers.
      // try parsing decimal number
      return (int) Long.parseLong(rawValue);
    } catch (NumberFormatException nfe) {
      // try parsing hex number
      try {
        return Long.decode(rawValue).intValue();
      } catch (NumberFormatException e) {
        throw new RuntimeException(rawValue + " is not an integer.", nfe);
      }
    }
  }

  private static boolean convertBool(String rawValue) {
    if ("true".equalsIgnoreCase(rawValue)) {
      return true;
    } else if ("false".equalsIgnoreCase(rawValue)) {
      return false;
    }

    try {
      int intValue = Integer.parseInt(rawValue);
      return intValue != 0;
    } catch (NumberFormatException e) {
      throw new RuntimeException(e);
    }
  }

  private static class EnumConverter extends EnumOrFlagConverter {
    public EnumConverter(AttrData attrData) {
      super(attrData);
    }

    @Override public void fillTypedValue(String data, TypedValue typedValue) {
      typedValue.type = TypedValue.TYPE_INT_HEX;
      typedValue.data = findValueFor(data);
      typedValue.assetCookie = 0;
    }
  }

  private static class FlagConverter extends EnumOrFlagConverter {
    public FlagConverter(AttrData attrData) {
      super(attrData);
    }

    @Override public void fillTypedValue(String data, TypedValue typedValue) {
      int flags = 0;
      for (String key : data.split("\\|")) {
        flags |= findValueFor(key);
      }

      typedValue.type = TypedValue.TYPE_INT_HEX;
      typedValue.data = flags;
      typedValue.assetCookie = 0;
    }
  }

  private static class EnumOrFlagConverter extends Converter<String> {
    private final AttrData attrData;

    public EnumOrFlagConverter(AttrData attrData) {
      this.attrData = attrData;
    }

    protected int findValueFor(String key) {
      String valueFor = attrData.getValueFor(key);
      if (valueFor == null) {
        throw new RuntimeException("no value found for " + key);
      }
      return Util.parseInt(valueFor);
    }
  }
}
