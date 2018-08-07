package org.robolectric.android;

import static org.robolectric.res.android.ResourceTypes.ANDROID_NS;
import static org.robolectric.res.android.ResourceTypes.AUTO_NS;
import static org.robolectric.res.android.ResourceTypes.RES_XML_END_ELEMENT_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_RESOURCE_MAP_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_START_ELEMENT_TYPE;
import static org.robolectric.res.android.ResourceTypes.ResTable_map.ATTR_TYPE;
import static org.robolectric.shadows.ShadowLegacyAssetManager.ATTRIBUTE_TYPE_PRECIDENCE;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import com.google.common.collect.ImmutableMap;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import org.robolectric.res.AttrData;
import org.robolectric.res.AttrData.Pair;
import org.robolectric.res.AttributeResource;
import org.robolectric.res.ResName;
import org.robolectric.res.ResType;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.TypedResource;
import org.robolectric.res.android.DataType;
import org.robolectric.res.android.ResTable;
import org.robolectric.res.android.ResTable.ResourceName;
import org.robolectric.res.android.ResourceTable.flag_entry;
import org.robolectric.res.android.ResourceTypes.ResChunk_header;
import org.robolectric.res.android.ResourceTypes.ResStringPool_header.Writer;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_attrExt;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_endElementExt;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_header;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_node;
import org.robolectric.res.android.ResourceTypes.Res_value;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.Converter;
import org.robolectric.shadows.Converter2;
import org.robolectric.shadows.ShadowArscAssetManager;
import org.robolectric.shadows.ShadowAssetManager;
import org.robolectric.shadows.ShadowLegacyAssetManager;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

public class AttributeSetBuilderImpl implements AttributeSetBuilder {
  private static final int STYLE_RES_ID = Integer.MAX_VALUE - 2;
  private static final int CLASS_RES_ID = Integer.MAX_VALUE - 1;
  private static final int ID_RES_ID = Integer.MAX_VALUE;

  private static final ImmutableMap<Integer, String> MAGIC_ATTRS = ImmutableMap.of(
      STYLE_RES_ID, "style",
      CLASS_RES_ID, "class",
      ID_RES_ID, "id"
  );

  private final ResourceResolver resourceResolver;
  private final Map<Integer, String> attrToValue = new TreeMap<>();

  public interface ResourceResolver {

    String getPackageName();

    String getResourceName(Integer attrId);

    Integer getIdentifier(String name, String type, String packageName);

    void parseValue(Integer attrId, ResName attrResName, AttributeResource attribute,
        TypedValue outValue);
  }

  public static class ArscResourceResolver implements ResourceResolver {

    private final Context context;
    private final ResTable resTable;

    public ArscResourceResolver(Context context) {
      this.context = context;

      ShadowAssetManager.ArscBase shadowArscAssetManager = Shadow.extract(context.getAssets());
      this.resTable = shadowArscAssetManager.getCompileTimeResTable();
    }

    @Override
    public String getPackageName() {
      return context.getPackageName();
    }

    @Override
    public String getResourceName(Integer attrId) {
      ResourceName name = new ResourceName();
      if (!resTable.getResourceName(attrId, true, name)) {
        return null;
      }

      StringBuilder str = new StringBuilder();
      if (name.packageName != null) {
        str.append(name.packageName.trim());
      }
      if (name.type != null) {
        if (str.length() > 0) {
          char div = ':';
          str.append(div);
        }
        str.append(name.type);
      }
      if (name.name != null) {
        if (str.length() > 0) {
          char div = '/';
          str.append(div);
        }
        str.append(name.name);
      }
      return str.toString();
    }

    @Override
    public Integer getIdentifier(String name, String type, String packageName) {
      return resTable.identifierForName(name, type, packageName);
    }

    @Override
    public void parseValue(Integer attrId, ResName attrResName, AttributeResource attribute,
        TypedValue outValue) {
      arscParse(attrId, attrResName, attribute, outValue);
    }

    private void arscParse(Integer attrId, ResName attrResName, AttributeResource attribute,
        TypedValue outValue) {
      String format = ShadowArscAssetManager.getResourceBagValue(attrId, ATTR_TYPE, resTable);
      Map<String, Integer> map = ShadowArscAssetManager.getResourceBagValues(attrId, resTable);
      ArrayList<Pair> pairs = new ArrayList<>();
      for (Entry<String, Integer> e : map.entrySet()) {
        pairs.add(new Pair(e.getKey(), Integer.toString(e.getValue())));
      }

      int formatFlags = Integer.parseInt(format);
      TreeSet<flag_entry> sortedFlags = new TreeSet<>(
          (a, b) -> ATTRIBUTE_TYPE_PRECIDENCE.compare(a.name, b.name));
      Collections.addAll(sortedFlags,
          org.robolectric.res.android.ResourceTable.gFormatFlags);

      for (flag_entry flag : sortedFlags) {
        if ((formatFlags & flag.value) != 0) {
          if ("reference".equals(flag.name)) {
            continue;
          }

          AttrData attrData = new AttrData(attrResName.getFullyQualifiedName(), flag.name, pairs);
          Converter2 converter = Converter2.getConverterFor(attrData, flag.name);
          if (converter.fillTypedValue(attribute.value, outValue, true)) {
            break;
          }
        }
      }
    }
  }

  public static class LegacyResourceResolver implements ResourceResolver {

    private final Context context;
    private final ResourceTable resourceTable;

    public LegacyResourceResolver(Context context, ResourceTable compileTimeResourceTable) {
      this.context = context;
      resourceTable = compileTimeResourceTable;
    }

    @Override
    public String getPackageName() {
      return context.getPackageName();
    }

    @Override
    public String getResourceName(Integer attrId) {
      return resourceTable.getResName(attrId).getFullyQualifiedName();
    }

    @Override
    public Integer getIdentifier(String name, String type, String packageName) {
      Integer resourceId = resourceTable.getResourceId(new ResName(packageName, type, name));
      if (resourceId == 0) {
        resourceId = resourceTable.getResourceId(
            new ResName(packageName, type, name.replace('.', '_')));
      }
      return resourceId;
    }

    @Override
    public void parseValue(Integer attrId, ResName attrResName, AttributeResource attribute,
        TypedValue outValue) {
      ShadowLegacyAssetManager shadowAssetManager = Shadow
          .extract(context.getResources().getAssets());
      TypedResource attrTypeData = shadowAssetManager.getAttrTypeData(attribute.resName);
      if (attrTypeData != null) {
        AttrData attrData = (AttrData) attrTypeData.getData();
        String format = attrData.getFormat();
        String[] types = format.split("\\|");
        Arrays.sort(types, ATTRIBUTE_TYPE_PRECIDENCE);
        for (String type : types) {
          if ("reference".equals(type)) continue; // already handled above
          Converter2 converter = Converter2.getConverterFor(attrData, type);

          if (converter != null) {
            if (converter.fillTypedValue(attribute.value, outValue, true)) {
              break;
            }
          }

        }
        // throw new IllegalArgumentException("wha? " + format);
      } else {
      /* In cases where the runtime framework doesn't know this attribute, e.g: viewportHeight (added in 21) on a
       * KitKat runtine, then infer the attribute type from the value.
       *
       * TODO: When we are able to pass the SDK resources from the build environment then we can remove this
       * and replace the NullResourceLoader with simple ResourceProvider that only parses attribute type information.
       */
        ResType resType = ResType.inferFromValue(attribute.value);
        Converter.getConverter(resType).fillTypedValue(attribute.value, outValue);
      }
    }
  }

  protected AttributeSetBuilderImpl(ResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
  }

  // todo rename to setAttribute(), or just set()?
  @Override
  public AttributeSetBuilder addAttribute(int resId, String value) {
    attrToValue.put(resId, value);
    return this;
  }

  // todo rename to setStyle()?
  @Override
  public AttributeSetBuilder setStyleAttribute(String value) {
    attrToValue.put(STYLE_RES_ID, value);
    return this;
  }

  // todo rename to setClass()?
  @Override
  public AttributeSetBuilder setClassAttribute(String value) {
    attrToValue.put(CLASS_RES_ID, value);
    return this;
  }

  // todo rename to setId()?
  @Override
  public AttributeSetBuilder setIdAttribute(String value) {
    attrToValue.put(ID_RES_ID, value);
    return this;
  }

  @Override
  public AttributeSet build() {
    Class<?> xmlBlockClass = ReflectionHelpers
        .loadClass(this.getClass().getClassLoader(), "android.content.res.XmlBlock");

    ByteBuffer buf = ByteBuffer.allocate(16 * 1024).order(ByteOrder.LITTLE_ENDIAN);
    Writer resStringPoolWriter = new Writer();

    final SparseArray<Integer> resIds = new SparseArray<>();
    final int[] maxAttrNameIndex = new int[] { 0 };

    ResXMLTree_attrExt.Writer dummyStart = new ResXMLTree_attrExt.Writer(buf, resStringPoolWriter,
        null, "dummy") {
      {
        String packageName = resourceResolver.getPackageName();

        for (Entry<Integer, String> entry : attrToValue.entrySet()) {
          Integer attrId = entry.getKey();
          String attrNs = "";
          String attrName;
          ResName attrResName = null;

          String magicAttr = MAGIC_ATTRS.get(attrId);
          if (magicAttr != null) {
            attrId = null;
            attrName = magicAttr;
          } else {
            String attrNameStr = resourceResolver.getResourceName(attrId);
            attrResName = ResName.qualifyResName(attrNameStr, packageName, "attr");
            attrNs = (attrResName.packageName.equals("android")) ? ANDROID_NS : AUTO_NS;
            attrName = attrResName.name;
          }

          String value = entry.getValue();
          DataType type;
          int valueInt;

          if (value == null || AttributeResource.isNull(value)) {
            type = DataType.NULL;
            valueInt = TypedValue.DATA_NULL_EMPTY;
          } else if (AttributeResource.isResourceReference(value)) {
            ResName resRef = AttributeResource.getResourceReference(value, packageName, null);
            Integer valueResId = resourceResolver.getIdentifier(resRef.name, resRef.type, resRef.packageName);
            if (valueResId == 0) {
              throw new IllegalArgumentException("no such resource " + value
                  + " while resolving value for "
                  + (attrResName == null ? attrName : attrResName.getFullyQualifiedName()));
            }
            type = DataType.REFERENCE;
            if (attrResName != null) {
              value = "@" +  valueResId;
            }
            valueInt = valueResId;
          } else if (AttributeResource.isStyleReference(value)) {
            ResName resRef = AttributeResource.getStyleReference(value, packageName, "attr");
            Integer valueResId = resourceResolver.getIdentifier(resRef.name, resRef.type, resRef.packageName);
            if (valueResId == 0) {
              throw new IllegalArgumentException("no such attr " + value
                  + " while resolving value for "
                  + (attrResName == null ? attrName : attrResName.getFullyQualifiedName()));
            }
            type = DataType.ATTRIBUTE;
            valueInt = valueResId;
          } else if (attrResName == null) { // class, id, or style
            type = DataType.STRING;
            valueInt = resStringPoolWriter.string(value);
          } else {
            TypedValue outValue = parse(attrId, attrResName, value, packageName);
            type = DataType.fromCode(outValue.type);
            value = (String) outValue.string;
            if (type == DataType.STRING && outValue.data == 0) {
              valueInt = resStringPoolWriter.string(value);
            } else {
              valueInt = outValue.data;
            }
          }

          Res_value resValue = new Res_value(type.code(), valueInt);

          int attrNameIndex = resStringPoolWriter.uniqueString(attrName);
          attr(resStringPoolWriter.string(attrNs), attrNameIndex,
              resStringPoolWriter.string(value), resValue, attrNs + ":" + attrName);
          if (attrId != null) {
            resIds.put(attrNameIndex, attrId);
          }
          maxAttrNameIndex[0] = Math.max(maxAttrNameIndex[0], attrNameIndex);
        }
      }
    };

    ResXMLTree_endElementExt.Writer dummyEnd =
        new ResXMLTree_endElementExt.Writer(buf, resStringPoolWriter, null, "dummy");

    int finalMaxAttrNameIndex = maxAttrNameIndex[0];
    ResXMLTree_header.write(buf, resStringPoolWriter, () -> {
      if (finalMaxAttrNameIndex > 0) {
        ResChunk_header.write(buf, (short) RES_XML_RESOURCE_MAP_TYPE, () -> {}, () -> {
          // not particularly compact, but no big deal for our purposes...
          for (int i = 0; i <= finalMaxAttrNameIndex; i++) {
            Integer value = resIds.get(i);
            buf.putInt(value == null ? 0 : value);
          }
        });
      }

      ResXMLTree_node.write(buf, RES_XML_START_ELEMENT_TYPE, dummyStart::write);
      ResXMLTree_node.write(buf, RES_XML_END_ELEMENT_TYPE, dummyEnd::write);
    });

    int size = buf.position();
    byte[] bytes = new byte[size];
    buf.position(0);
    buf.get(bytes, 0, size);

    Object xmlBlockInstance = ReflectionHelpers
        .callConstructor(xmlBlockClass, ClassParameter.from(byte[].class, bytes));

    AttributeSet parser = ReflectionHelpers.callInstanceMethod(xmlBlockClass, xmlBlockInstance,
        "newParser");
    ReflectionHelpers.callInstanceMethod(parser, "next");
    ReflectionHelpers.callInstanceMethod(parser, "next");

    return parser;
  }

  private TypedValue parse(Integer attrId, ResName attrResName, String value,
      String packageName) {
    AttributeResource attribute =
        new AttributeResource(attrResName, value, packageName);
    TypedValue outValue = new TypedValue();

    if (attribute.isResourceReference()) {
      ResName resourceReference = attribute.getResourceReference();
      int id = resourceResolver.getIdentifier(resourceReference.name, resourceReference.type,
          resourceReference.packageName);
      if (id == 0) {
        throw new IllegalArgumentException("couldn't resolve " + attribute);
      }

      outValue.type = Res_value.TYPE_REFERENCE;
      outValue.data = id;
      outValue.resourceId = id;
      outValue.string = "@" + id;
    } else {
      resourceResolver.parseValue(attrId, attrResName, attribute, outValue);
    }
    return outValue;
  }
}
