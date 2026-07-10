package org.robolectric.res.android;

import static org.robolectric.res.android.Errors.BAD_TYPE;
import static org.robolectric.res.android.Errors.NAME_NOT_FOUND;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.ResTable.kDebugStringPoolNoisy;
import static org.robolectric.res.android.ResTable.kDebugXMLNoisy;
import static org.robolectric.res.android.ResXMLParser.event_code_t.BAD_DOCUMENT;
import static org.robolectric.res.android.ResXMLParser.event_code_t.END_DOCUMENT;
import static org.robolectric.res.android.ResXMLParser.event_code_t.END_NAMESPACE;
import static org.robolectric.res.android.ResXMLParser.event_code_t.END_TAG;
import static org.robolectric.res.android.ResXMLParser.event_code_t.FIRST_CHUNK_CODE;
import static org.robolectric.res.android.ResXMLParser.event_code_t.START_DOCUMENT;
import static org.robolectric.res.android.ResXMLParser.event_code_t.START_NAMESPACE;
import static org.robolectric.res.android.ResXMLParser.event_code_t.START_TAG;
import static org.robolectric.res.android.ResXMLParser.event_code_t.TEXT;
import static org.robolectric.res.android.ResourceTypes.RES_XML_CDATA_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_END_ELEMENT_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_END_NAMESPACE_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_FIRST_CHUNK_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_START_ELEMENT_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_START_NAMESPACE_TYPE;
import static org.robolectric.res.android.Util.ALOGI;
import static org.robolectric.res.android.Util.ALOGW;
import static org.robolectric.res.android.Util.dtohl;
import static org.robolectric.res.android.Util.dtohs;
import static org.robolectric.res.android.Util.isTruthy;

import java.nio.ByteBuffer;
import org.robolectric.res.android.ResourceTypes.ResChunk_header;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_attrExt;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_endElementExt;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_node;
import org.robolectric.res.android.ResourceTypes.Res_value;

public class ResXMLParser {

  static final int SIZEOF_RESXMLTREE_NAMESPACE_EXT = 4;
  static final int SIZEOF_RESXMLTREE_NODE = ResChunk_header.SIZEOF + 8;
  static final int SIZEOF_RESXMLTREE_ATTR_EXT = 20;
  static final int SIZEOF_RESXMLTREE_CDATA_EXT = 4 + ResourceTypes.Res_value.SIZEOF;
  static final int SIZEOF_CHAR = 2;

  // Byte offsets within a ResXMLTree_attrExt struct. This makes it possible to access the
  // underlying data without having to allocate ResXMLTree_attrExt and ResXMLTree_attribute
  // objects.
  private static final int ATTR_EXT_ATTRIBUTE_START = 8; // short
  private static final int ATTR_EXT_ATTRIBUTE_SIZE = 10; // short
  private static final int ATTR_EXT_ATTRIBUTE_COUNT = 12; // short

  // Byte offsets within a ResXMLTree_attribute struct.
  private static final int ATTRIBUTE_NAME = 4; // ResStringPool_ref (int)
  private static final int ATTRIBUTE_RAW_VALUE = 8; // ResStringPool_ref (int)
  private static final int ATTRIBUTE_TYPED_VALUE = 12; // Res_value

  // Byte offsets within a Res_value struct.
  private static final int RES_VALUE_DATATYPE = 3; // byte (after size:2, res0:1)
  private static final int RES_VALUE_DATA = 4; // int

  public static class event_code_t {
    public static final int BAD_DOCUMENT = -1;
    public static final int START_DOCUMENT = 0;
    public static final int END_DOCUMENT = 1;

    public static final int FIRST_CHUNK_CODE = RES_XML_FIRST_CHUNK_TYPE;

    public static final int START_NAMESPACE = RES_XML_START_NAMESPACE_TYPE;
    public static final int END_NAMESPACE = RES_XML_END_NAMESPACE_TYPE;
    public static final int START_TAG = RES_XML_START_ELEMENT_TYPE;
    public static final int END_TAG = RES_XML_END_ELEMENT_TYPE;
    public static final int TEXT = RES_XML_CDATA_TYPE;
  }

  ResXMLTree mTree;
  int mEventCode;
  ResXMLTree_node mCurNode;
  int mCurExt;
  int mSourceResourceId;

  public ResXMLParser(ResXMLTree tree) {
    this.mTree = tree;
    this.mEventCode = BAD_DOCUMENT;
  }

  public void restart() {
    mCurNode = null;
    mEventCode = mTree.mError == NO_ERROR ? START_DOCUMENT : BAD_DOCUMENT;
  }

  public ResStringPool getStrings() {
    return mTree.mStrings;
  }

  int getEventType() {
    return mEventCode;
  }

  public int next() {
    if (mEventCode == START_DOCUMENT) {
      mCurNode = mTree.mRootNode;
      mCurExt = mTree.mRootExt;
      return (mEventCode = mTree.mRootCode);
    } else if (mEventCode >= FIRST_CHUNK_CODE) {
      return nextNode();
    }
    return mEventCode;
  }

  int getCommentID() {
    return mCurNode != null ? dtohl(mCurNode.comment.index) : -1;
  }

  final String getComment(Ref<Integer> outLen) {
    int id = getCommentID();
    return id >= 0 ? mTree.mStrings.stringAt(id, outLen) : null;
  }

  public int getLineNumber() {
    return mCurNode != null ? dtohl(mCurNode.lineNumber) : -1;
  }

  public int getTextID() {
    if (mEventCode == TEXT) {
      return dtohl(new ResourceTypes.ResXMLTree_cdataExt(mTree.mBuffer.buf, mCurExt).data.index);
    }
    return -1;
  }

  final String getText(Ref<Integer> outLen) {
    int id = getTextID();
    return id >= 0 ? mTree.mStrings.stringAt(id, outLen) : null;
  }

  int getTextValue(Res_value outValue) {
    if (mEventCode == TEXT) {
      // outValue.copyFrom_dtoh(new ResourceTypes.ResXMLTree_cdataExt(mTree.mBuffer.buf,
      // mCurExt).typedData);
      return ResourceTypes.Res_value.SIZEOF /* sizeof(Res_value) */;
    }
    return BAD_TYPE;
  }

  int getNamespacePrefixID() {
    if (mEventCode == START_NAMESPACE || mEventCode == END_NAMESPACE) {
      return dtohl(
          new ResourceTypes.ResXMLTree_namespaceExt(mTree.mBuffer.buf, mCurExt).prefix.index);
    }
    return -1;
  }

  final String getNamespacePrefix(Ref<Integer> outLen) {
    int id = getNamespacePrefixID();
    // printf("prefix=%d  event=%s\n", id, mEventCode);
    return id >= 0 ? mTree.mStrings.stringAt(id, outLen) : null;
  }

  int getNamespaceUriID() {
    if (mEventCode == START_NAMESPACE || mEventCode == END_NAMESPACE) {
      return dtohl(new ResourceTypes.ResXMLTree_namespaceExt(mTree.mBuffer.buf, mCurExt).uri.index);
    }
    return -1;
  }

  final String getNamespaceUri(Ref<Integer> outLen) {
    int id = getNamespaceUriID();
    // printf("uri=%d  event=%s\n", id, mEventCode);
    return id >= 0 ? mTree.mStrings.stringAt(id, outLen) : null;
  }

  public int getElementNamespaceID() {
    if (mEventCode == START_TAG) {
      return dtohl(new ResXMLTree_attrExt(mTree.mBuffer.buf, mCurExt).ns.index);
    }
    if (mEventCode == END_TAG) {
      return dtohl(new ResXMLTree_endElementExt(mTree.mBuffer.buf, mCurExt).ns.index);
    }
    return -1;
  }

  final String getElementNamespace(Ref<Integer> outLen) {
    int id = getElementNamespaceID();
    return id >= 0 ? mTree.mStrings.stringAt(id, outLen) : null;
  }

  public int getElementNameID() {
    if (mEventCode == START_TAG) {
      return dtohl(new ResXMLTree_attrExt(mTree.mBuffer.buf, mCurExt).name.index);
    }
    if (mEventCode == END_TAG) {
      return dtohl(new ResXMLTree_endElementExt(mTree.mBuffer.buf, mCurExt).name.index);
    }
    return -1;
  }

  final String getElementName(Ref<Integer> outLen) {
    int id = getElementNameID();
    return id >= 0 ? mTree.mStrings.stringAt(id, outLen) : null;
  }

  public int getAttributeCount() {
    if (mEventCode == START_TAG) {
      return dtohs(mTree.mBuffer.buf.getShort(mCurExt + ATTR_EXT_ATTRIBUTE_COUNT));
    }
    return 0;
  }

  /**
   * Returns the absolute buffer offset of attribute {@code idx} of the current START_TAG, or -1 if
   * the current event is not a START_TAG or {@code idx} is out of range. This avoids allocating
   * ResXMLTree_attrExt/ResXMLTree_attribute objects in the hot attribute accessors.
   */
  private int attributeOffset(int idx) {
    if (mEventCode != START_TAG) {
      return -1;
    }
    ByteBuffer buf = mTree.mBuffer.buf;
    final int attributeCount = dtohs(buf.getShort(mCurExt + ATTR_EXT_ATTRIBUTE_COUNT));
    if (idx < 0 || idx >= attributeCount) {
      return -1;
    }
    final int attributeStart = dtohs(buf.getShort(mCurExt + ATTR_EXT_ATTRIBUTE_START));
    final int attributeSize = dtohs(buf.getShort(mCurExt + ATTR_EXT_ATTRIBUTE_SIZE));
    return mCurExt + attributeStart + attributeSize * idx;
  }

  public int getAttributeNamespaceID(int idx) {
    final int attrOff = attributeOffset(idx);
    if (attrOff < 0) {
      return -2;
    }
    return dtohl(mTree.mBuffer.buf.getInt(attrOff));
  }

  final String getAttributeNamespace(int idx, Ref<Integer> outLen) {
    int id = getAttributeNamespaceID(idx);
    // printf("attribute namespace=%d  idx=%d  event=%s\n", id, idx, mEventCode);
    if (kDebugXMLNoisy) {
      System.out.printf("getAttributeNamespace 0x%x=0x%x\n%n", idx, id);
    }
    return id >= 0 ? mTree.mStrings.stringAt(id, outLen) : null;
  }

  final String getAttributeNamespace8(int idx, Ref<Integer> outLen) {
    int id = getAttributeNamespaceID(idx);
    // printf("attribute namespace=%d  idx=%d  event=%s\n", id, idx, mEventCode);
    if (kDebugXMLNoisy) {
      System.out.printf("getAttributeNamespace 0x%x=0x%x\n%n", idx, id);
    }
    return id >= 0 ? mTree.mStrings.string8At(id, outLen) : null;
  }

  public int getAttributeNameID(int idx) {
    final int attrOff = attributeOffset(idx);
    if (attrOff < 0) {
      return -1;
    }
    return dtohl(mTree.mBuffer.buf.getInt(attrOff + ATTRIBUTE_NAME));
  }

  final String getAttributeName(int idx, Ref<Integer> outLen) {
    int id = getAttributeNameID(idx);
    // printf("attribute name=%d  idx=%d  event=%s\n", id, idx, mEventCode);
    if (kDebugXMLNoisy) {
      System.out.printf("getAttributeName 0x%x=0x%x\n%n", idx, id);
    }
    return id >= 0 ? mTree.mStrings.stringAt(id, outLen) : null;
  }

  final String getAttributeName8(int idx, Ref<Integer> outLen) {
    int id = getAttributeNameID(idx);
    // printf("attribute name=%d  idx=%d  event=%s\n", id, idx, mEventCode);
    if (kDebugXMLNoisy) {
      System.out.printf("getAttributeName 0x%x=0x%x\n%n", idx, id);
    }
    return id >= 0 ? mTree.mStrings.string8At(id, outLen) : null;
  }

  public int getAttributeNameResID(int idx) {
    int id = getAttributeNameID(idx);
    if (id >= 0 && id < mTree.mNumResIds) {
      int resId = dtohl(mTree.mResIds[id]);
      if (mTree.mDynamicRefTable != null) {
        final Ref<Integer> resIdRef = new Ref<>(resId);
        mTree.mDynamicRefTable.lookupResourceId(resIdRef);
        resId = resIdRef.get();
      }
      return resId;
    }
    return 0;
  }

  public int getAttributeValueStringID(int idx) {
    final int attrOff = attributeOffset(idx);
    if (attrOff < 0) {
      return -1;
    }
    return dtohl(mTree.mBuffer.buf.getInt(attrOff + ATTRIBUTE_RAW_VALUE));
  }

  final String getAttributeStringValue(int idx, Ref<Integer> outLen) {
    int id = getAttributeValueStringID(idx);
    if (kDebugXMLNoisy) {
      System.out.printf("getAttributeValue 0x%x=0x%x\n%n", idx, id);
    }
    return id >= 0 ? mTree.mStrings.stringAt(id, outLen) : null;
  }

  public int getAttributeDataType(int idx) {
    final int attrOff = attributeOffset(idx);
    if (attrOff < 0) {
      return DataType.NULL.code();
    }
    int type = mTree.mBuffer.buf.get(attrOff + ATTRIBUTE_TYPED_VALUE + RES_VALUE_DATATYPE);
    if (type != DataType.DYNAMIC_REFERENCE.code()) {
      return type;
    }

    // This is a dynamic reference. We adjust those references
    // to regular references at this level, so lie to the caller.
    return DataType.REFERENCE.code();
  }

  public int getAttributeData(int idx) {
    final int attrOff = attributeOffset(idx);
    if (attrOff < 0) {
      return 0;
    }
    final ByteBuffer buf = mTree.mBuffer.buf;
    final int dataType = buf.get(attrOff + ATTRIBUTE_TYPED_VALUE + RES_VALUE_DATATYPE);
    final int dataValue = dtohl(buf.getInt(attrOff + ATTRIBUTE_TYPED_VALUE + RES_VALUE_DATA));
    if (dataType != DataType.DYNAMIC_REFERENCE.code() || mTree.mDynamicRefTable == null) {
      return dataValue;
    }

    final Ref<Integer> data = new Ref<>(dataValue);
    if (mTree.mDynamicRefTable.lookupResourceId(data) == NO_ERROR) {
      return data.get();
    }
    return 0;
  }

  public int getAttributeValue(int idx, Ref<Res_value> outValue) {
    final int attrOff = attributeOffset(idx);
    if (attrOff < 0) {
      return BAD_TYPE;
    }
    outValue.set(new Res_value(mTree.mBuffer.buf, attrOff + ATTRIBUTE_TYPED_VALUE));
    if (mTree.mDynamicRefTable != null
        && mTree.mDynamicRefTable.lookupResourceValue(outValue) != NO_ERROR) {
      return BAD_TYPE;
    }

    return ResourceTypes.Res_value.SIZEOF /* sizeof(Res_value) */;
  }

  int indexOfAttribute(final String ns, final String attr) {
    String nsStr = ns != null ? ns : "";
    return indexOfAttribute(
        isTruthy(ns) ? nsStr : null, isTruthy(ns) ? nsStr.length() : 0, attr, attr.length());
  }

  public int indexOfAttribute(final String ns, int nsLen, final String attr, int attrLen) {
    if (mEventCode == START_TAG) {
      if (attr == null) {
        return NAME_NOT_FOUND;
      }
      final int N = getAttributeCount();
      if (mTree.mStrings.isUTF8()) {
        String8 ns8 = null, attr8;
        if (ns != null) {
          ns8 = new String8(ns, nsLen);
        }
        attr8 = new String8(attr, attrLen);
        if (kDebugStringPoolNoisy) {
          ALOGI(
              "indexOfAttribute UTF8 %s (0x%x) / %s (0x%x)",
              ns8.string(), nsLen, attr8.string(), attrLen);
        }
        for (int i = 0; i < N; i++) {
          final Ref<Integer> curNsLen = new Ref<>(0), curAttrLen = new Ref<>(0);
          final String curNs = getAttributeNamespace8(i, curNsLen);
          final String curAttr = getAttributeName8(i, curAttrLen);
          if (kDebugStringPoolNoisy) {
            ALOGI(
                "  curNs=%s (0x%x), curAttr=%s (0x%x)",
                curNs, curNsLen.get(), curAttr, curAttrLen.get());
          }
          if (curAttr != null
              && curNsLen.get() == nsLen
              && curAttrLen.get() == attrLen
              && memcmp(attr8.string(), curAttr, attrLen) == 0) {
            if (ns == null) {
              if (curNs == null) {
                if (kDebugStringPoolNoisy) {
                  ALOGI("  FOUND!");
                }
                return i;
              }
            } else if (curNs != null) {
              // printf(" -. ns=%s, curNs=%s\n",
              //       String8(ns).string(), String8(curNs).string());
              if (memcmp(ns8.string(), curNs, nsLen) == 0) {
                if (kDebugStringPoolNoisy) {
                  ALOGI("  FOUND!");
                }
                return i;
              }
            }
          }
        }
      } else {
        if (kDebugStringPoolNoisy) {
          ALOGI(
              "indexOfAttribute UTF16 %s (0x%x) / %s (0x%x)",
              ns /*String8(ns, nsLen).string()*/,
              nsLen,
              attr /*String8(attr, attrLen).string()*/,
              attrLen);
        }
        for (int i = 0; i < N; i++) {
          final Ref<Integer> curNsLen = new Ref<>(0), curAttrLen = new Ref<>(0);
          final String curNs = getAttributeNamespace(i, curNsLen);
          final String curAttr = getAttributeName(i, curAttrLen);
          if (kDebugStringPoolNoisy) {
            ALOGI(
                "  curNs=%s (0x%x), curAttr=%s (0x%x)",
                curNs /*String8(curNs, curNsLen).string()*/,
                curNsLen.get(),
                curAttr /*String8(curAttr, curAttrLen).string()*/,
                curAttrLen.get());
          }
          if (curAttr != null
              && curNsLen.get() == nsLen
              && curAttrLen.get() == attrLen
              && (memcmp(attr, curAttr, attrLen * SIZEOF_CHAR /*sizeof(char16_t)*/) == 0)) {
            if (ns == null) {
              if (curNs == null) {
                if (kDebugStringPoolNoisy) {
                  ALOGI("  FOUND!");
                }
                return i;
              }
            } else if (curNs != null) {
              // printf(" -. ns=%s, curNs=%s\n",
              //       String8(ns).string(), String8(curNs).string());
              if (memcmp(ns, curNs, nsLen * SIZEOF_CHAR /*sizeof(char16_t)*/) == 0) {
                if (kDebugStringPoolNoisy) {
                  ALOGI("  FOUND!");
                }
                return i;
              }
            }
          }
        }
      }
    }

    return NAME_NOT_FOUND;
  }

  private int memcmp(String s1, String s2, int len) {
    for (int i = 0; i < len; i++) {
      int d = s1.charAt(i) - s2.charAt(i);
      if (d != 0) {
        return d;
      }
    }
    return 0;
  }

  public int indexOfID() {
    if (mEventCode == START_TAG) {
      final int idx = dtohs(new ResXMLTree_attrExt(mTree.mBuffer.buf, mCurExt).idIndex);
      if (idx > 0) return (idx - 1);
    }
    return NAME_NOT_FOUND;
  }

  public int indexOfClass() {
    if (mEventCode == START_TAG) {
      final int idx = dtohs(new ResXMLTree_attrExt(mTree.mBuffer.buf, mCurExt).classIndex);
      if (idx > 0) return (idx - 1);
    }
    return NAME_NOT_FOUND;
  }

  public int indexOfStyle() {
    if (mEventCode == START_TAG) {
      final int idx = dtohs(new ResXMLTree_attrExt(mTree.mBuffer.buf, mCurExt).styleIndex);
      if (idx > 0) return (idx - 1);
    }
    return NAME_NOT_FOUND;
  }

  int nextNode() {
    if (mEventCode < 0) {
      return mEventCode;
    }

    do {
      int nextOffset = mCurNode.myOffset() + dtohl(mCurNode.header.size);
      if (nextOffset >= mTree.mDataLen) {
        mCurNode = null;
        return (mEventCode = END_DOCUMENT);
      }

      //        final ResXMLTree_node next = (ResXMLTree_node)
      //      (((final int8_t*)mCurNode) + dtohl(mCurNode.header.size));
      ResXMLTree_node next = new ResXMLTree_node(mTree.mBuffer.buf, nextOffset);
      if (kDebugXMLNoisy) {
        ALOGI("Next node: prev=%s, next=%s\n", mCurNode, next);
      }

      if (next.myOffset() >= mTree.mDataLen) {
        mCurNode = null;
        return (mEventCode = END_DOCUMENT);
      }

      if (mTree.validateNode(next) != NO_ERROR) {
        mCurNode = null;
        return (mEventCode = BAD_DOCUMENT);
      }

      mCurNode = next;
      final int headerSize = dtohs(next.header.headerSize);
      final int totalSize = dtohl(next.header.size);
      mCurExt = next.myOffset() + headerSize;
      int minExtSize;
      int eventCode = dtohs(next.header.type);
      switch ((mEventCode = eventCode)) {
        case RES_XML_START_NAMESPACE_TYPE:
        case RES_XML_END_NAMESPACE_TYPE:
          minExtSize = SIZEOF_RESXMLTREE_NAMESPACE_EXT /*sizeof(ResXMLTree_namespaceExt)*/;
          break;
        case RES_XML_START_ELEMENT_TYPE:
          minExtSize = SIZEOF_RESXMLTREE_ATTR_EXT /*sizeof(ResXMLTree_attrExt)*/;
          break;
        case RES_XML_END_ELEMENT_TYPE:
          minExtSize = ResXMLTree_endElementExt.SIZEOF /*sizeof(ResXMLTree_endElementExt)*/;
          break;
        case RES_XML_CDATA_TYPE:
          minExtSize = SIZEOF_RESXMLTREE_CDATA_EXT /*sizeof(ResXMLTree_cdataExt)*/;
          break;
        default:
          ALOGW(
              "Unknown XML block: header type %d in node at %d\n",
              (int) dtohs(next.header.type), (next.myOffset() - mTree.mHeader.myOffset()));
          continue;
      }

      if ((totalSize - headerSize) < minExtSize) {
        ALOGW(
            "Bad XML block: header type 0x%x in node at 0x%x has size %d, need %d\n",
            (int) dtohs(next.header.type),
            (next.myOffset() - mTree.mHeader.myOffset()),
            (totalSize - headerSize),
            minExtSize);
        return (mEventCode = BAD_DOCUMENT);
      }

      // printf("CurNode=%s, CurExt=%s, headerSize=%d, minExtSize=%d\n",
      //       mCurNode, mCurExt, headerSize, minExtSize);

      return eventCode;
    } while (true);
  }

  void getPosition(ResXMLPosition pos) {
    pos.eventCode = mEventCode;
    pos.curNode = mCurNode;
    pos.curExt = mCurExt;
  }

  void setPosition(final ResXMLPosition pos) {
    mEventCode = pos.eventCode;
    mCurNode = pos.curNode;
    mCurExt = pos.curExt;
  }

  public void setSourceResourceId(int resId) {
    mSourceResourceId = resId;
  }

  public int getSourceResourceId() {
    return mSourceResourceId;
  }

  static class ResXMLPosition {
    int eventCode;
    ResXMLTree_node curNode;
    int curExt;
  }
}
