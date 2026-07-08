package org.robolectric.res.android;

import static org.robolectric.res.android.ApkAssetsCookie.K_INVALID_COOKIE;
import static org.robolectric.res.android.ApkAssetsCookie.kInvalidCookie;
import static org.robolectric.res.android.Util.ALOGI;

import org.robolectric.res.android.CppAssetManager2.ResolvedBag;
import org.robolectric.res.android.CppAssetManager2.ResolvedBag.Entry;
import org.robolectric.res.android.CppAssetManager2.Theme;
import org.robolectric.res.android.ResourceTypes.Res_value;

// transliterated from
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/AttributeResolution.cpp and
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/include/androidfw/AttributeResolution.h

public class AttributeResolution9 {
  public static final boolean kThrowOnBadId = false;
  private static final boolean kDebugStyles = false;

  // Offsets into the outValues array populated by the methods below. outValues is a uint32_t
  // array, but each logical element takes up 6 uint32_t-sized physical elements.
  public static final int STYLE_NUM_ENTRIES = 6;
  public static final int STYLE_TYPE = 0;
  public static final int STYLE_DATA = 1;
  public static final int STYLE_ASSET_COOKIE = 2;
  public static final int STYLE_RESOURCE_ID = 3;
  public static final int STYLE_CHANGING_CONFIGURATIONS = 4;
  public static final int STYLE_DENSITY = 5;

  // Java asset cookies have 0 as an invalid cookie, but TypedArray expects < 0.
  private static int ApkAssetsCookieToJavaCookie(ApkAssetsCookie cookie) {
    return cookie.intValue() != kInvalidCookie ? (cookie.intValue() + 1) : -1;
  }

  public static class XmlAttributeFinder {
    private final int[] attrResIds;

    XmlAttributeFinder(ResXMLParser xmlParser) {
      if (xmlParser == null) {
        this.attrResIds = null;
      } else {
        // Resolve each XML attribute's resource id exactly once. Caching the
        // ids turns O(styleable * xmlAttrs) resolutions into O(xmlAttrs).
        int attributeCount = xmlParser.getAttributeCount();
        int[] ids = new int[attributeCount];
        for (int i = 0; i < attributeCount; i++) {
          ids[i] = xmlParser.getAttributeNameResID(i);
        }
        this.attrResIds = ids;
      }
    }

    public int Find(int curIdent) {
      if (attrResIds == null) {
        return -1;
      }

      final int[] ids = attrResIds;
      for (int i = 0; i < ids.length; i++) {
        if (ids[i] == curIdent) {
          return i;
        }
      }
      return -1;
    }
  }

  public static class BagAttributeFinder {
    public static Entry find(ResolvedBag bag, int ident) {
      if (bag == null || bag.entries == null) {
        return null;
      }
      Entry[] bagEntries = bag.entries;
      int low = 0;
      int high = bagEntries.length - 1;

      // Do a manual binary search for the attribute id in the bag entries. This is faster than
      // Arrays.binarySearch() because it avoids a `needle` allocation and it avoids boxing the
      // int keys.
      while (low <= high) {
        int mid = (low + high) >>> 1;
        Entry midVal = bagEntries[mid];
        int cmp = midVal.key - ident;

        if (cmp < 0) {
          low = mid + 1;
        } else if (cmp > 0) {
          high = mid - 1;
        } else {
          return midVal; // key found
        }
      }
      return null; // key not found
    }
  }

  // These are all variations of the same method. They each perform the exact same operation,
  // but on various data sources. I *think* they are re-written to avoid an extra branch
  // in the inner loop, but after one branch miss (some pointer != null), the branch predictor
  // should
  // predict the rest of the iterations' branch correctly.
  // TODO(adamlesinski): Run performance tests against these methods and a new, single method
  // that uses all the sources and branches to the right ones within the inner loop.

  // `out_values` must NOT be nullptr.
  // `out_indices` may be nullptr.
  public static boolean ResolveAttrs(
      Theme theme,
      int def_style_attr,
      int def_style_res,
      int[] src_values,
      int src_values_length,
      int[] attrs,
      int attrs_length,
      int[] out_values,
      int[] out_indices) {
    if (kDebugStyles) {
      ALOGI(
          "APPLY STYLE: theme=0x%s defStyleAttr=0x%x defStyleRes=0x%x",
          theme, def_style_attr, def_style_res);
    }

    CppAssetManager2 assetManager = theme.GetAssetManager();
    ResTable_config config = new ResTable_config();
    Res_value value;

    int indicesIdx = 0;

    // Load default style from attribute, if specified...
    final Ref<Integer> def_style_flags = new Ref<>(0);
    if (def_style_attr != 0) {
      final Ref<Res_value> valueRef = new Ref<>(null);
      if (theme.GetAttribute(def_style_attr, valueRef, def_style_flags).intValue()
          != kInvalidCookie) {
        value = valueRef.get();
        if (value.dataType == Res_value.TYPE_REFERENCE) {
          def_style_res = value.data;
        }
      }
    }

    // Retrieve the default style bag, if requested.
    ResolvedBag default_style_bag = null;
    if (def_style_res != 0) {
      default_style_bag = assetManager.GetBag(def_style_res);
      if (default_style_bag != null) {
        def_style_flags.set(def_style_flags.get() | default_style_bag.type_spec_flags);
      }
    }

    final Ref<Res_value> valueRef = new Ref<>(null);
    final Ref<Integer> residRef = new Ref<>(0);
    final Ref<Integer> typeSetFlagsRef = new Ref<>(0);
    final Ref<ResTable_config> configRef = new Ref<>(null);
    // Now iterate through all of the attributes that the client has requested,
    // filling in each with whatever data we can find.
    int destOffset = 0;
    for (int ii = 0; ii < attrs_length; ii++) {
      final int cur_ident = attrs[ii];

      if (kDebugStyles) {
        ALOGI("RETRIEVING ATTR 0x%08x...", cur_ident);
      }

      ApkAssetsCookie cookie = K_INVALID_COOKIE;
      int type_set_flags = 0;

      value = Res_value.NULL_VALUE;
      config.density = 0;

      // Try to find a value for this attribute...  we prioritize values
      // coming from, first XML attributes, then XML style, then default
      // style, and finally the theme.

      // Retrieve the current input value if available.
      if (src_values_length > 0 && src_values[ii] != 0) {
        value = new Res_value((byte) Res_value.TYPE_ATTRIBUTE, src_values[ii]);
        if (kDebugStyles) {
          ALOGI("-> From values: type=0x%x, data=0x%08x", value.dataType, value.data);
        }
      } else {
        final Entry entry = BagAttributeFinder.find(default_style_bag, cur_ident);
        if (entry != null) {
          cookie = entry.cookie;
          type_set_flags = def_style_flags.get();
          value = entry.value;
          if (kDebugStyles) {
            ALOGI("-> From def style: type=0x%x, data=0x%08x", value.dataType, value.data);
          }
        }
      }

      int resId = 0;
      valueRef.set(value);
      residRef.set(resId);
      typeSetFlagsRef.set(type_set_flags);
      configRef.set(config);
      if (value.dataType != Res_value.TYPE_NULL) {
        // Take care of resolving the found resource to its final value.
        ApkAssetsCookie newCookie =
            theme.ResolveAttributeReference(cookie, valueRef, configRef, typeSetFlagsRef, residRef);
        if (newCookie.intValue() != kInvalidCookie) {
          cookie = newCookie;
        }
        if (kDebugStyles) {
          ALOGI("-> Resolved attr: type=0x%x, data=0x%08x", value.dataType, value.data);
        }
      } else if (value.data != Res_value.DATA_NULL_EMPTY) {
        // If we still don't have a value for this attribute, try to find it in the theme!
        ApkAssetsCookie newCookie = theme.GetAttribute(cur_ident, valueRef, typeSetFlagsRef);
        if (newCookie.intValue() != kInvalidCookie) {
          if (kDebugStyles) {
            ALOGI("-> From theme: type=0x%x, data=0x%08x", value.dataType, value.data);
          }
          newCookie =
              assetManager.ResolveReference(
                  newCookie, valueRef, configRef, typeSetFlagsRef, residRef);
          if (newCookie.intValue() != kInvalidCookie) {
            cookie = newCookie;
          }
          if (kDebugStyles) {
            ALOGI("-> Resolved theme: type=0x%x, data=0x%08x", value.dataType, value.data);
          }
        }
      }
      value = valueRef.get();
      resId = residRef.get();
      type_set_flags = typeSetFlagsRef.get();
      config = configRef.get();

      // Deal with the special @null value -- it turns back to TYPE_NULL.
      if (value.dataType == Res_value.TYPE_REFERENCE && value.data == 0) {
        if (kDebugStyles) {
          ALOGI("-> Setting to @null!");
        }
        value = Res_value.NULL_VALUE;
        cookie = K_INVALID_COOKIE;
      }

      if (kDebugStyles) {
        ALOGI("Attribute 0x%08x: type=0x%x, data=0x%08x", cur_ident, value.dataType, value.data);
      }

      // Write the final value back to Java.
      out_values[destOffset + STYLE_TYPE] = value.dataType;
      out_values[destOffset + STYLE_DATA] = value.data;
      out_values[destOffset + STYLE_ASSET_COOKIE] = ApkAssetsCookieToJavaCookie(cookie);
      out_values[destOffset + STYLE_RESOURCE_ID] = resId;
      out_values[destOffset + STYLE_CHANGING_CONFIGURATIONS] = type_set_flags;
      out_values[destOffset + STYLE_DENSITY] = config.density;

      if (out_indices != null && value.dataType != Res_value.TYPE_NULL) {
        indicesIdx++;
        out_indices[indicesIdx] = ii;
      }

      destOffset += STYLE_NUM_ENTRIES;
    }

    if (out_indices != null) {
      out_indices[0] = indicesIdx;
    }
    return true;
  }

  public static void ApplyStyle(
      Theme theme,
      ResXMLParser xml_parser,
      int def_style_attr,
      int def_style_res_id,
      int[] attrs,
      int attrs_length,
      int[] out_values,
      int[] out_indices) {
    if (kDebugStyles) {
      ALOGI(
          "APPLY STYLE: theme=%s defStyleAttr=0x%x defStyleRes=0x%x xml=%s",
          theme, def_style_attr, def_style_res_id, xml_parser);
    }

    CppAssetManager2 assetManager = theme.GetAssetManager();
    final Ref<ResTable_config> config = new Ref<>(new ResTable_config());
    final Ref<Res_value> value = new Ref<>(new Res_value());

    int indices_idx = 0;

    // Load default style from attribute, if specified...
    final Ref<Integer> def_style_flags = new Ref<>(0);
    if (def_style_attr != 0) {
      if (theme.GetAttribute(def_style_attr, value, def_style_flags).intValue() != kInvalidCookie) {
        if (value.get().dataType == DataType.REFERENCE.code()) {
          def_style_res_id = value.get().data;
        }
      }
    }

    // Retrieve the style resource ID associated with the current XML tag's style attribute.
    int style_res_id = 0;
    final Ref<Integer> style_flags = new Ref<>(0);
    if (xml_parser != null) {
      int idx = xml_parser.indexOfStyle();
      if (idx >= 0 && xml_parser.getAttributeValue(idx, value) >= 0) {
        if (value.get().dataType == DataType.ATTRIBUTE.code()) {
          // Resolve the attribute with out theme.
          if (theme.GetAttribute(value.get().data, value, style_flags).intValue()
              == kInvalidCookie) {
            value.set(value.get().withType(DataType.NULL.code()));
          }
        }

        if (value.get().dataType == DataType.REFERENCE.code()) {
          style_res_id = value.get().data;
        }
      }
    }

    // Retrieve the default style bag, if requested.
    ResolvedBag default_style_bag = null;
    if (def_style_res_id != 0) {
      default_style_bag = assetManager.GetBag(def_style_res_id);
      if (default_style_bag != null) {
        def_style_flags.set(def_style_flags.get() | default_style_bag.type_spec_flags);
      }
    }

    // Retrieve the style class bag, if requested.
    ResolvedBag xml_style_bag = null;
    if (style_res_id != 0) {
      xml_style_bag = assetManager.GetBag(style_res_id);
      if (xml_style_bag != null) {
        style_flags.set(style_flags.get() | xml_style_bag.type_spec_flags);
      }
    }

    // Retrieve the XML attributes, if requested.
    XmlAttributeFinder xml_attr_finder = new XmlAttributeFinder(xml_parser);

    final Ref<Integer> type_set_flags = new Ref<>(0);
    final Ref<Integer> resId = new Ref<>(0);

    // Now iterate through all of the attributes that the client has requested,
    // filling in each with whatever data we can find.
    for (int ii = 0; ii < attrs_length; ii++) {
      final int cur_ident = attrs[ii];

      if (kDebugStyles) {
        ALOGI("RETRIEVING ATTR 0x%08x...", cur_ident);
      }

      ApkAssetsCookie cookie = K_INVALID_COOKIE;
      type_set_flags.set(0);

      value.set(Res_value.NULL_VALUE);
      config.get().density = 0;

      // Try to find a value for this attribute...  we prioritize values
      // coming from, first XML attributes, then XML style, then default
      // style, and finally the theme.

      // Walk through the xml attributes looking for the requested attribute.
      int xml_attr_idx = xml_attr_finder.Find(cur_ident);
      if (xml_attr_idx != -1) {
        // We found the attribute we were looking for.
        xml_parser.getAttributeValue(xml_attr_idx, value);
        type_set_flags.set(style_flags.get());
        if (kDebugStyles) {
          ALOGI("-> From XML: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
        }
      }

      if (value.get().dataType == DataType.NULL.code()
          && value.get().data != Res_value.DATA_NULL_EMPTY) {
        // Walk through the style class values looking for the requested attribute.
        Entry entry = BagAttributeFinder.find(xml_style_bag, cur_ident);
        if (entry != null) {
          // We found the attribute we were looking for.
          cookie = entry.cookie;
          type_set_flags.set(style_flags.get());
          value.set(entry.value);
          if (kDebugStyles) {
            ALOGI("-> From style: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
          }
        }
      }

      if (value.get().dataType == DataType.NULL.code()
          && value.get().data != Res_value.DATA_NULL_EMPTY) {
        // Walk through the default style values looking for the requested attribute.
        Entry entry = BagAttributeFinder.find(default_style_bag, cur_ident);
        if (entry != null) {
          // We found the attribute we were looking for.
          cookie = entry.cookie;
          type_set_flags.set(def_style_flags.get());

          value.set(entry.value);
          if (kDebugStyles) {
            ALOGI(
                "-> From def style: type=0x%x, data=0x%08x",
                value.get().dataType, value.get().data);
          }
        }
      }

      resId.set(0);
      if (value.get().dataType != DataType.NULL.code()) {
        // Take care of resolving the found resource to its final value.
        ApkAssetsCookie new_cookie =
            theme.ResolveAttributeReference(cookie, value, config, type_set_flags, resId);
        if (new_cookie.intValue() != kInvalidCookie) {
          cookie = new_cookie;
        }

        if (kDebugStyles) {
          ALOGI("-> Resolved attr: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
        }
      } else if (value.get().data != Res_value.DATA_NULL_EMPTY) {
        // If we still don't have a value for this attribute, try to find it in the theme!
        ApkAssetsCookie new_cookie = theme.GetAttribute(cur_ident, value, type_set_flags);
        if (new_cookie.intValue() != kInvalidCookie) {
          if (kDebugStyles) {
            ALOGI("-> From theme: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
          }
          new_cookie =
              assetManager.ResolveReference(new_cookie, value, config, type_set_flags, resId);
          if (new_cookie.intValue() != kInvalidCookie) {
            cookie = new_cookie;
          }

          if (kDebugStyles) {
            ALOGI(
                "-> Resolved theme: type=0x%x, data=0x%08x",
                value.get().dataType, value.get().data);
          }
        }
      }

      // Deal with the special @null value -- it turns back to TYPE_NULL.
      if (value.get().dataType == DataType.REFERENCE.code() && value.get().data == 0) {
        if (kDebugStyles) {
          ALOGI(". Setting to @null!");
        }
        value.set(Res_value.NULL_VALUE);
        cookie = K_INVALID_COOKIE;
      }

      if (kDebugStyles) {
        ALOGI(
            "Attribute 0x%08x: type=0x%x, data=0x%08x",
            cur_ident, value.get().dataType, value.get().data);
      }

      // Write the final value back to Java.
      int destIndex = ii * STYLE_NUM_ENTRIES;
      Res_value res_value = value.get();
      out_values[destIndex + STYLE_TYPE] = res_value.dataType;
      out_values[destIndex + STYLE_DATA] = res_value.data;
      out_values[destIndex + STYLE_ASSET_COOKIE] = ApkAssetsCookieToJavaCookie(cookie);
      out_values[destIndex + STYLE_RESOURCE_ID] = resId.get();
      out_values[destIndex + STYLE_CHANGING_CONFIGURATIONS] = type_set_flags.get();
      out_values[destIndex + STYLE_DENSITY] = config.get().density;

      if (res_value.dataType != DataType.NULL.code()
          || res_value.data == Res_value.DATA_NULL_EMPTY) {
        indices_idx++;

        // out_indices must NOT be nullptr.
        out_indices[indices_idx] = ii;
      }

      // Robolectric-custom:
      // if (false && res_value.dataType == DataType.ATTRIBUTE.code()) {
      //   final Ref<ResourceName> attrName = new Ref<>(null);
      //   final Ref<ResourceName> attrRefName = new Ref<>(null);
      //   boolean gotName = assetManager.GetResourceName(cur_ident, attrName);
      //   boolean gotRefName = assetManager.GetResourceName(res_value.data, attrRefName);
      //   Logger.warn(
      //       "Failed to resolve attribute lookup: %s=\"?%s\"; theme: %s",
      //       gotName ? attrName.get() : "unknown", gotRefName ? attrRefName.get() : "unknown",
      //       theme);
      // }

      //      out_values += STYLE_NUM_ENTRIES;
    }

    // out_indices must NOT be nullptr.
    out_indices[0] = indices_idx;
  }

  public static boolean RetrieveAttributes(
      CppAssetManager2 assetManager,
      ResXMLParser xml_parser,
      int[] attrs,
      int attrs_length,
      int[] out_values,
      int[] out_indices) {
    final Ref<ResTable_config> config = new Ref<>(new ResTable_config());
    final Ref<Res_value> value = new Ref<>(null);

    int indices_idx = 0;

    // Retrieve the XML attributes, if requested.
    final int xml_attr_count = xml_parser.getAttributeCount();
    int ix = 0;
    int cur_xml_attr = xml_parser.getAttributeNameResID(ix);

    // Now iterate through all of the attributes that the client has requested,
    // filling in each with whatever data we can find.
    int baseDest = 0;
    for (int ii = 0; ii < attrs_length; ii++) {
      final int cur_ident = attrs[ii];
      ApkAssetsCookie cookie = K_INVALID_COOKIE;
      final Ref<Integer> type_set_flags = new Ref<>(0);

      value.set(Res_value.NULL_VALUE);
      config.get().density = 0;

      // Try to find a value for this attribute...
      // Skip through XML attributes until the end or the next possible match.
      while (ix < xml_attr_count && cur_ident > cur_xml_attr) {
        ix++;
        cur_xml_attr = xml_parser.getAttributeNameResID(ix);
      }
      // Retrieve the current XML attribute if it matches, and step to next.
      if (ix < xml_attr_count && cur_ident == cur_xml_attr) {
        xml_parser.getAttributeValue(ix, value);
        ix++;
        cur_xml_attr = xml_parser.getAttributeNameResID(ix);
      }

      final Ref<Integer> resId = new Ref<>(0);
      if (value.get().dataType != Res_value.TYPE_NULL) {
        // Take care of resolving the found resource to its final value.
        ApkAssetsCookie new_cookie =
            assetManager.ResolveReference(cookie, value, config, type_set_flags, resId);
        if (new_cookie.intValue() != kInvalidCookie) {
          cookie = new_cookie;
        }
      }

      // Deal with the special @null value -- it turns back to TYPE_NULL.
      if (value.get().dataType == Res_value.TYPE_REFERENCE && value.get().data == 0) {
        value.set(Res_value.NULL_VALUE);
        cookie = K_INVALID_COOKIE;
      }

      // Write the final value back to Java.
      out_values[baseDest + STYLE_TYPE] = value.get().dataType;
      out_values[baseDest + STYLE_DATA] = value.get().data;
      out_values[baseDest + STYLE_ASSET_COOKIE] = ApkAssetsCookieToJavaCookie(cookie);
      out_values[baseDest + STYLE_RESOURCE_ID] = resId.get();
      out_values[baseDest + STYLE_CHANGING_CONFIGURATIONS] = type_set_flags.get();
      out_values[baseDest + STYLE_DENSITY] = config.get().density;

      if (out_indices != null
          && (value.get().dataType != Res_value.TYPE_NULL
              || value.get().data == Res_value.DATA_NULL_EMPTY)) {
        indices_idx++;
        out_indices[indices_idx] = ii;
      }

      //      out_values += STYLE_NUM_ENTRIES;
      baseDest += STYLE_NUM_ENTRIES;
    }

    if (out_indices != null) {
      out_indices[0] = indices_idx;
    }

    return true;
  }
}
