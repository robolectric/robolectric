package org.robolectric.res.android;

import static org.robolectric.res.android.Errors.BAD_INDEX;
import static org.robolectric.res.android.Util.ALOGI;

import org.robolectric.res.android.ResourceTypes.Res_value;
import org.robolectric.util.Logger;

public class AttributeResolution {
  public static final boolean kThrowOnBadId = false;
  private static final boolean kDebugStyles = false;

  public static final int STYLE_NUM_ENTRIES = 6;
  public static final int STYLE_TYPE = 0;
  public static final int STYLE_DATA = 1;
  public static final int STYLE_ASSET_COOKIE = 2;
  public static final int STYLE_RESOURCE_ID = 3;
  public static final int STYLE_CHANGING_CONFIGURATIONS = 4;
  public static final int STYLE_DENSITY = 5;

  public static class BagAttributeFinder {

    private final ResTable.bag_entry[] bag_entries;
    private final int bagEndIndex;

    public BagAttributeFinder(ResTable.bag_entry[] bag_entries, int bagEndIndex) {
      this.bag_entries = bag_entries;
      this.bagEndIndex = bagEndIndex;
    }

    public ResTable.bag_entry find(int curIdent) {
      for (int curIndex = bagEndIndex - 1; curIndex >= 0; curIndex--) {
        if (bag_entries[curIndex].map.name.ident == curIdent) {
          return bag_entries[curIndex];
        }
      }
      return null;
    }
  }

  public static class XmlAttributeFinder {

    private ResXMLParser xmlParser;

    public XmlAttributeFinder(ResXMLParser xmlParser) {
      this.xmlParser = xmlParser;
    }

    public int find(int curIdent) {
      if (xmlParser == null) {
        return 0;
      }

      int attributeCount = xmlParser.getAttributeCount();
      for (int i = 0; i < attributeCount; i++) {
        if (xmlParser.getAttributeNameResID(i) == curIdent) {
          return i;
        }
      }
      return attributeCount;
    }
  }

  public static boolean ResolveAttrs(ResTableTheme theme, int defStyleAttr,
                                     int defStyleRes, int[] srcValues,
                                     int srcValuesLength, int[] attrs,
                                     int attrsLength, int[] outValues, int[] outIndices) {
    if (kDebugStyles) {
      ALOGI("APPLY STYLE: theme=0x%p defStyleAttr=0x%x defStyleRes=0x%x", theme,
          defStyleAttr, defStyleRes);
    }

    final ResTable res = theme.getResTable();
    ResTable_config config = new ResTable_config();
    Res_value value;

    int indicesIdx = 0;

    // Load default style from attribute, if specified...
    Ref<Integer> defStyleBagTypeSetFlags = new Ref<>(0);
    if (defStyleAttr != 0) {
      Ref<Res_value> valueRef = new Ref<>(null);
      if (theme.GetAttribute(defStyleAttr, valueRef, defStyleBagTypeSetFlags) >= 0) {
        value = valueRef.get();
        if (value.dataType == Res_value.TYPE_REFERENCE) {
          defStyleRes = value.data;
        }
      }
    }

    // Now lock down the resource object and start pulling stuff from it.
    res.lock();

    // Retrieve the default style bag, if requested.
    final Ref<ResTable.bag_entry[]> defStyleStart = new Ref<>(null);
    Ref<Integer> defStyleTypeSetFlags = new Ref<>(0);
    int bagOff = defStyleRes != 0
        ? res.getBagLocked(defStyleRes, defStyleStart, defStyleTypeSetFlags) : -1;
    defStyleTypeSetFlags.set(defStyleTypeSetFlags.get() | defStyleBagTypeSetFlags.get());
//    const ResTable::bag_entry* const defStyleEnd = defStyleStart + (bagOff >= 0 ? bagOff : 0);
    final int defStyleEnd = (bagOff >= 0 ? bagOff : 0);
    BagAttributeFinder defStyleAttrFinder = new BagAttributeFinder(defStyleStart.get(), defStyleEnd);

    // Now iterate through all of the attributes that the client has requested,
    // filling in each with whatever data we can find.
    int destOffset = 0;
    for (int ii=0; ii<attrsLength; ii++) {
      final int curIdent = attrs[ii];

      if (kDebugStyles) {
        ALOGI("RETRIEVING ATTR 0x%08x...", curIdent);
      }

      int block = -1;
      int typeSetFlags = 0;

      value = Res_value.NULL_VALUE;
      config.density = 0;

      // Try to find a value for this attribute...  we prioritize values
      // coming from, first XML attributes, then XML style, then default
      // style, and finally the theme.

      // Retrieve the current input value if available.
      if (srcValuesLength > 0 && srcValues[ii] != 0) {
        value = new Res_value((byte) Res_value.TYPE_ATTRIBUTE, srcValues[ii]);
        if (kDebugStyles) {
          ALOGI("-> From values: type=0x%x, data=0x%08x", value.dataType, value.data);
        }
      } else {
        final ResTable.bag_entry defStyleEntry = defStyleAttrFinder.find(curIdent);
        if (defStyleEntry != null) {
          block = defStyleEntry.stringBlock;
          typeSetFlags = defStyleTypeSetFlags.get();
          value = defStyleEntry.map.value;
          if (kDebugStyles) {
            ALOGI("-> From def style: type=0x%x, data=0x%08x", value.dataType, value.data);
          }
        }
      }

      int resid = 0;
      Ref<Res_value> valueRef = new Ref<>(value);
      Ref<Integer> residRef = new Ref<>(resid);
      Ref<Integer> typeSetFlagsRef = new Ref<>(typeSetFlags);
      Ref<ResTable_config> configRef = new Ref<>(config);
      if (value.dataType != Res_value.TYPE_NULL) {
        // Take care of resolving the found resource to its final value.
        int newBlock = theme.resolveAttributeReference(valueRef, block,
            residRef, typeSetFlagsRef, configRef);
        value = valueRef.get();
        resid = residRef.get();
        typeSetFlags = typeSetFlagsRef.get();
        config = configRef.get();
        if (newBlock >= 0) block = newBlock;
        if (kDebugStyles) {
          ALOGI("-> Resolved attr: type=0x%x, data=0x%08x", value.dataType, value.data);
        }
      } else {
        // If we still don't have a value for this attribute, try to find
        // it in the theme!
        int newBlock = theme.GetAttribute(curIdent, valueRef, typeSetFlagsRef);
        value = valueRef.get();
        typeSetFlags = typeSetFlagsRef.get();

        if (newBlock >= 0) {
          if (kDebugStyles) {
            ALOGI("-> From theme: type=0x%x, data=0x%08x", value.dataType, value.data);
          }
          newBlock = res.resolveReference(valueRef, newBlock, residRef, typeSetFlagsRef, configRef);
          value = valueRef.get();
          resid = residRef.get();
          typeSetFlags = typeSetFlagsRef.get();
          config = configRef.get();
          if (kThrowOnBadId) {
            if (newBlock == BAD_INDEX) {
              throw new IllegalStateException("Bad resource!");
            }
          }
          if (newBlock >= 0) block = newBlock;
          if (kDebugStyles) {
            ALOGI("-> Resolved theme: type=0x%x, data=0x%08x", value.dataType, value.data);
          }
        }
      }

      // Deal with the special @null value -- it turns back to TYPE_NULL.
      if (value.dataType == Res_value.TYPE_REFERENCE && value.data == 0) {
        if (kDebugStyles) {
          ALOGI("-> Setting to @null!");
        }
        value = Res_value.NULL_VALUE;
        block = -1;
      }

      if (kDebugStyles) {
        ALOGI("Attribute 0x%08x: type=0x%x, data=0x%08x", curIdent, value.dataType,
            value.data);
      }

      // Write the final value back to Java.
      outValues[destOffset + STYLE_TYPE] = value.dataType;
      outValues[destOffset + STYLE_DATA] = value.data;
      outValues[destOffset + STYLE_ASSET_COOKIE] =
          block != -1 ? res.getTableCookie(block) : -1;
      outValues[destOffset + STYLE_RESOURCE_ID] = resid;
      outValues[destOffset + STYLE_CHANGING_CONFIGURATIONS] = typeSetFlags;
      outValues[destOffset + STYLE_DENSITY] = config.density;

      if (outIndices != null && value.dataType != Res_value.TYPE_NULL) {
        indicesIdx++;
        outIndices[indicesIdx] = ii;
      }

      destOffset += STYLE_NUM_ENTRIES;
    }

    res.unlock();

    if (outIndices != null) {
      outIndices[0] = indicesIdx;
    }
    return true;
  }

  public static void ApplyStyle(ResTableTheme theme, ResXMLParser xmlParser, int defStyleAttr, int defStyleRes,
                                int[] attrs, int attrsLength, int[] outValues, int[] outIndices) {
    if (kDebugStyles) {
      ALOGI("APPLY STYLE: theme=%s defStyleAttr=0x%x defStyleRes=0x%x xml=%s",
          theme, defStyleAttr, defStyleRes, xmlParser);
    }

    final ResTable res = theme.getResTable();
    Ref<ResTable_config> config = new Ref<>(new ResTable_config());
    Ref<Res_value> value = new Ref<>(new Res_value());

    int indices_idx = 0;

    // Load default style from attribute, if specified...
    Ref<Integer> defStyleBagTypeSetFlags = new Ref<>(0);
    if (defStyleAttr != 0) {
      if (theme.GetAttribute(defStyleAttr, value, defStyleBagTypeSetFlags) >= 0) {
        if (value.get().dataType == DataType.REFERENCE.code()) {
          defStyleRes = value.get().data;
        }
      }
    }

    // Retrieve the style class associated with the current XML tag.
    int style = 0;
    Ref<Integer> styleBagTypeSetFlags = new Ref<>(0);
    if (xmlParser != null) {
      int idx = xmlParser.indexOfStyle();
      if (idx >= 0 && xmlParser.getAttributeValue(idx, value) >= 0) {
        if (value.get().dataType == DataType.ATTRIBUTE.code()) {
          if (theme.GetAttribute(value.get().data, value, styleBagTypeSetFlags) < 0) {
            value.set(value.get().withType(DataType.NULL.code()));
          }
        }
        if (value.get().dataType == DataType.REFERENCE.code()) {
          style = value.get().data;
        }
      }
    }

    // Now lock down the resource object and start pulling stuff from it.
    res.lock();

    // Retrieve the default style bag, if requested.
    final Ref<ResTable.bag_entry[]> defStyleAttrStart = new Ref<>(null);
    Ref<Integer> defStyleTypeSetFlags = new Ref<>(0);
    int bagOff = defStyleRes != 0
        ? res.getBagLocked(defStyleRes, defStyleAttrStart, defStyleTypeSetFlags)
        : -1;
    defStyleTypeSetFlags.set(defStyleTypeSetFlags.get() | defStyleBagTypeSetFlags.get());
    // const ResTable::bag_entry* defStyleAttrEnd = defStyleAttrStart + (bagOff >= 0 ? bagOff : 0);
    final ResTable.bag_entry defStyleAttrEnd = null;
    // BagAttributeFinder defStyleAttrFinder = new BagAttributeFinder(defStyleAttrStart, defStyleAttrEnd);
    BagAttributeFinder defStyleAttrFinder = new BagAttributeFinder(defStyleAttrStart.get(), bagOff);

    // Retrieve the style class bag, if requested.
    final Ref<ResTable.bag_entry[]> styleAttrStart = new Ref<>(null);
    Ref<Integer> styleTypeSetFlags = new Ref<>(0);
    bagOff = style != 0
        ? res.getBagLocked(style, styleAttrStart, styleTypeSetFlags)
        : -1;
    styleTypeSetFlags.set(styleTypeSetFlags.get() | styleBagTypeSetFlags.get());
    // final ResTable::bag_entry* final styleAttrEnd = styleAttrStart + (bagOff >= 0 ? bagOff : 0);
    final ResTable.bag_entry styleAttrEnd = null;
    //BagAttributeFinder styleAttrFinder = new BagAttributeFinder(styleAttrStart, styleAttrEnd);
    BagAttributeFinder styleAttrFinder = new BagAttributeFinder(styleAttrStart.get(), bagOff);

    // Retrieve the XML attributes, if requested.
    final int kXmlBlock = 0x10000000;
    XmlAttributeFinder xmlAttrFinder = new XmlAttributeFinder(xmlParser);
    final int xmlAttrEnd = xmlParser != null ? xmlParser.getAttributeCount() : 0;

    // Now iterate through all of the attributes that the client has requested,
    // filling in each with whatever data we can find.
    for (int ii = 0; ii < attrsLength; ii++) {
      final int curIdent = attrs[ii];

      if (kDebugStyles) {
        ALOGI("RETRIEVING ATTR 0x%08x...", curIdent);
      }

      int block = kXmlBlock;
      Ref<Integer> typeSetFlags = new Ref<>(0);

      value.set(Res_value.NULL_VALUE);
      config.get().density = 0;

      // Try to find a value for this attribute...  we prioritize values
      // coming from, first XML attributes, then XML style, then default
      // style, and finally the theme.

      // Walk through the xml attributes looking for the requested attribute.
      final int xmlAttrIdx = xmlAttrFinder.find(curIdent);
      if (xmlAttrIdx != xmlAttrEnd) {
        // We found the attribute we were looking for.
        xmlParser.getAttributeValue(xmlAttrIdx, value);
        if (kDebugStyles) {
          ALOGI("-> From XML: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
        }
      }

      if (value.get().dataType == DataType.NULL.code() && value.get().data != Res_value.DATA_NULL_EMPTY) {
        // Walk through the style class values looking for the requested attribute.
        final ResTable.bag_entry styleAttrEntry = styleAttrFinder.find(curIdent);
        if (styleAttrEntry != styleAttrEnd) {
          // We found the attribute we were looking for.
          block = styleAttrEntry.stringBlock;
          typeSetFlags.set(styleTypeSetFlags.get());
          value.set(styleAttrEntry.map.value);
          if (kDebugStyles) {
            ALOGI("-> From style: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
          }
        }
      }

      if (value.get().dataType == DataType.NULL.code() && value.get().data != Res_value.DATA_NULL_EMPTY) {
        // Walk through the default style values looking for the requested attribute.
        final ResTable.bag_entry defStyleAttrEntry = defStyleAttrFinder.find(curIdent);
        if (defStyleAttrEntry != defStyleAttrEnd) {
          // We found the attribute we were looking for.
          block = defStyleAttrEntry.stringBlock;
          typeSetFlags.set(styleTypeSetFlags.get());
          value.set(defStyleAttrEntry.map.value);
          if (kDebugStyles) {
            ALOGI("-> From def style: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
          }
        }
      }

      Ref<Integer> resid = new Ref<>(0);
      if (value.get().dataType != DataType.NULL.code()) {
        // Take care of resolving the found resource to its final value.
        int newBlock = theme.resolveAttributeReference(value, block,
            resid, typeSetFlags, config);
        if (newBlock >= 0) {
          block = newBlock;
        }

        if (kDebugStyles) {
          ALOGI("-> Resolved attr: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
        }
      } else if (value.get().data != Res_value.DATA_NULL_EMPTY) {
        // If we still don't have a value for this attribute, try to find it in the theme!
        int newBlock = theme.GetAttribute(curIdent, value, typeSetFlags);
        if (newBlock >= 0) {
          if (kDebugStyles) {
            ALOGI("-> From theme: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
          }
          newBlock = res.resolveReference(value, newBlock, resid, typeSetFlags, config);
          if (newBlock >= 0) {
            block = newBlock;
          }

          if (kDebugStyles) {
            ALOGI("-> Resolved theme: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
          }
        }
      }

      // Deal with the special @null value -- it turns back to TYPE_NULL.
      if (value.get().dataType == DataType.REFERENCE.code() && value.get().data == 0) {
        if (kDebugStyles) {
          ALOGI(". Setting to @null!");
        }
        value.set(Res_value.NULL_VALUE);
        block = kXmlBlock;
      }

      if (kDebugStyles) {
        ALOGI("Attribute 0x%08x: type=0x%x, data=0x%08x", curIdent, value.get().dataType, value.get().data);
      }

      // Write the final value back to Java.
      int destIndex = ii * STYLE_NUM_ENTRIES;
      Res_value res_value = value.get();
      outValues[destIndex + STYLE_TYPE] = res_value.dataType;
      outValues[destIndex + STYLE_DATA] = res_value.data;
      outValues[destIndex + STYLE_ASSET_COOKIE] =
          block != kXmlBlock ? res.getTableCookie(block) : -1;
      outValues[destIndex + STYLE_RESOURCE_ID] = resid.get();
      outValues[destIndex + STYLE_CHANGING_CONFIGURATIONS] = typeSetFlags.get();
      outValues[destIndex + STYLE_DENSITY] = config.get().density;

      if (res_value.dataType != DataType.NULL.code() || res_value.data == Res_value.DATA_NULL_EMPTY) {
        indices_idx++;

        // out_indices must NOT be nullptr.
        outIndices[indices_idx] = ii;
      }

      if (res_value.dataType == DataType.ATTRIBUTE.code()) {
        ResTable.ResourceName attrName = new ResTable.ResourceName();
        ResTable.ResourceName attrRefName = new ResTable.ResourceName();
        boolean gotName = res.getResourceName(curIdent, true, attrName);
        boolean gotRefName = res.getResourceName(res_value.data, true, attrRefName);
        Logger.warn(
            "Failed to resolve attribute lookup: %s=\"?%s\"; theme: %s",
            gotName ? attrName : "unknown", gotRefName ? attrRefName : "unknown",
            theme);
      }

//      out_values += STYLE_NUM_ENTRIES;
    }

    res.unlock();

    // out_indices must NOT be nullptr.
    outIndices[0] = indices_idx;
  }

  public static boolean RetrieveAttributes(ResTable res, ResXMLParser xmlParser, int[] attrs, int attrsLength, int[] outValues, int[] outIndices) {
    Ref<ResTable_config> config = new Ref<>(new ResTable_config());
    Ref<Res_value> value = new Ref<>(null);

    int indices_idx = 0;

    // Now lock down the resource object and start pulling stuff from it.
    res.lock();

    // Retrieve the XML attributes, if requested.
    final int xmlAttrCount = xmlParser.getAttributeCount();
    int ix=0;
    int curXmlAttr = xmlParser.getAttributeNameResID(ix);

    final int kXmlBlock = 0x10000000;

    // Now iterate through all of the attributes that the client has requested,
    // filling in each with whatever data we can find.
    int baseDest = 0;
    for (int ii=0; ii<attrsLength; ii++) {
      final int curIdent = attrs[ii];
      int block = 0;
      Ref<Integer> typeSetFlags = new Ref<>(0);

      value.set(Res_value.NULL_VALUE);
      config.get().density = 0;

      // Try to find a value for this attribute...
      // Skip through XML attributes until the end or the next possible match.
      while (ix < xmlAttrCount && curIdent > curXmlAttr) {
        ix++;
        curXmlAttr = xmlParser.getAttributeNameResID(ix);
      }
      // Retrieve the current XML attribute if it matches, and step to next.
      if (ix < xmlAttrCount && curIdent == curXmlAttr) {
        block = kXmlBlock;
        xmlParser.getAttributeValue(ix, value);
        ix++;
        curXmlAttr = xmlParser.getAttributeNameResID(ix);
      }

      //printf("Attribute 0x%08x: type=0x%x, data=0x%08x\n", curIdent, value.dataType, value.data);
      Ref<Integer> resid = new Ref<>(0);
      if (value.get().dataType != Res_value.TYPE_NULL) {
        // Take care of resolving the found resource to its final value.
        //printf("Resolving attribute reference\n");
        int newBlock = res.resolveReference(value, block, resid,
            typeSetFlags, config);
        if (newBlock >= 0) block = newBlock;
      }

      // Deal with the special @null value -- it turns back to TYPE_NULL.
      if (value.get().dataType == Res_value.TYPE_REFERENCE && value.get().data == 0) {
        value.set(Res_value.NULL_VALUE);
        block = kXmlBlock;
      }

      //printf("Attribute 0x%08x: final type=0x%x, data=0x%08x\n", curIdent, value.dataType, value.data);

      // Write the final value back to Java.
      outValues[baseDest + STYLE_TYPE] = value.get().dataType;
      outValues[baseDest + STYLE_DATA] = value.get().data;
      outValues[baseDest + STYLE_ASSET_COOKIE] =
          block != kXmlBlock ? res.getTableCookie(block) : -1;
      outValues[baseDest + STYLE_RESOURCE_ID] = resid.get();
      outValues[baseDest + STYLE_CHANGING_CONFIGURATIONS] = typeSetFlags.get();
      outValues[baseDest + STYLE_DENSITY] = config.get().density;

      if (outIndices != null &&
          (value.get().dataType != Res_value.TYPE_NULL || value.get().data == Res_value.DATA_NULL_EMPTY)) {
        indices_idx++;
        outIndices[indices_idx] = ii;
      }

//      dest += STYLE_NUM_ENTRIES;
      baseDest += STYLE_NUM_ENTRIES;
    }

    res.unlock();

    if (outIndices != null) {
      outIndices[0] = indices_idx;
    }

    return true;
  }
}
