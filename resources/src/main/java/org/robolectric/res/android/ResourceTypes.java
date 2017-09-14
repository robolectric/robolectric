package org.robolectric.res.android;

import static org.robolectric.res.android.Errors.BAD_TYPE;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.ResXMLParser.SIZEOF_RESXMLTREE_ATTR_EXT;
import static org.robolectric.res.android.Util.ALOGW;
import static org.robolectric.res.android.Util.dtohl;
import static org.robolectric.res.android.Util.dtohs;

import java.nio.ByteBuffer;

public class ResourceTypes {
  static int validate_chunk(ResChunk_header chunk,
      int minSize,
      int dataLen,
      String name)
  {
    final short headerSize = dtohs(chunk.headerSize);
    final int size = dtohl(chunk.size);

    if (headerSize >= minSize) {
      if (headerSize <= size) {
        if (((headerSize|size)&0x3) == 0) {
          if (size <= (dataLen)) {
            return NO_ERROR;
          }
          ALOGW("%s data size 0x%x extends beyond resource end.",
              name, size /*, (dataEnd-((const uint8_t*)chunk))*/);
          return BAD_TYPE;
        }
        ALOGW("%s size 0x%x or headerSize 0x%x is not on an integer boundary.",
            name, (int)size, (int)headerSize);
        return BAD_TYPE;
      }
      ALOGW("%s size 0x%x is smaller than header size 0x%x.",
          name, size, headerSize);
      return BAD_TYPE;
    }
    ALOGW("%s header size 0x%04x is too small.",
        name, headerSize);
    return BAD_TYPE;
  }

  static class WithOffset {
    private int offset;

    WithOffset(int offset) {
      this.offset = offset;
    }

    public int myOffset() {
      return offset;
    }
  }

  /** ********************************************************************
   *  Base Types
   *
   *  These are standard types that are shared between multiple specific
   *  resource types.
   *
   *********************************************************************** */

  /**
   * Header that appears at the front of every data chunk in a resource.
   */
  static class ResChunk_header extends WithOffset
  {
    static int SIZEOF = 8;

    // Type identifier for this chunk.  The meaning of this value depends
    // on the containing chunk.
    final short type;

    // Size of the chunk header (in bytes).  Adding this value to
    // the address of the chunk allows you to find its associated data
    // (if any).
    final short headerSize;

    // Total size of this chunk (in bytes).  This is the chunkSize plus
    // the size of any data associated with the chunk.  Adding this value
    // to the chunk allows you to completely skip its contents (including
    // any child chunks).  If this value is the same as chunkSize, there is
    // no data associated with the chunk.
    final int size;

    public ResChunk_header(ByteBuffer buf, int offset) {
      super(offset);
      this.type = buf.getShort(offset);
      this.headerSize = buf.getShort(offset + 2);
      this.size = buf.getInt(offset + 4);
    }
  };

  public static final int RES_NULL_TYPE               = 0x0000;
  public static final int RES_STRING_POOL_TYPE        = 0x0001;
  public static final int RES_TABLE_TYPE              = 0x0002;
  public static final int RES_XML_TYPE                = 0x0003;

  // Chunk types in RES_XML_TYPE
  public static final int RES_XML_FIRST_CHUNK_TYPE    = 0x0100;
  public static final int RES_XML_START_NAMESPACE_TYPE= 0x0100;
  public static final int RES_XML_END_NAMESPACE_TYPE  = 0x0101;
  public static final int RES_XML_START_ELEMENT_TYPE  = 0x0102;
  public static final int RES_XML_END_ELEMENT_TYPE    = 0x0103;
  public static final int RES_XML_CDATA_TYPE          = 0x0104;
  public static final int RES_XML_LAST_CHUNK_TYPE     = 0x017f;
  // This contains a uint32_t array mapping strings in the string
  // pool back to resource identifiers.  It is optional.
  public static final int RES_XML_RESOURCE_MAP_TYPE   = 0x0180;

  // Chunk types in RES_TABLE_TYPE
  public static final int RES_TABLE_PACKAGE_TYPE      = 0x0200;
  public static final int RES_TABLE_TYPE_TYPE         = 0x0201;
  public static final int RES_TABLE_TYPE_SPEC_TYPE    = 0x0202;
  public static final int RES_TABLE_LIBRARY_TYPE      = 0x0203;

  /**
   * Definition for a pool of strings.  The data of this chunk is an
   * array of uint32_t providing indices into the pool, relative to
   * stringsStart.  At stringsStart are all of the UTF-16 strings
   * concatenated together; each starts with a uint16_t of the string's
   * length and each ends with a 0x0000 terminator.  If a string is >
   * 32767 characters, the high bit of the length is set meaning to take
   * those 15 bits as a high word and it will be followed by another
   * uint16_t containing the low word.
   *
   * If styleCount is not zero, then immediately following the array of
   * uint32_t indices into the string table is another array of indices
   * into a style table starting at stylesStart.  Each entry in the
   * style table is an array of ResStringPool_span structures.
   */
  static class ResStringPool_header
  {
    final ResChunk_header header;

    // Number of strings in this pool (number of uint32_t indices that follow
    // in the data).
    final int stringCount;

    // Number of style span arrays in the pool (number of uint32_t indices
    // follow the string indices).
    final int styleCount;

    // Flags.
//    enum {
    // If set, the string index is sorted by the string values (based
    // on strcmp16()).
    public static final int SORTED_FLAG = 1<<0;

        // String pool is encoded in UTF-8
        public static final int UTF8_FLAG = 1<<8;
//  };
    final int flags;

    // Index from header of the string data.
    final int stringsStart;

    // Index from header of the style data.
    final int stylesStart;

    public ResStringPool_header(ByteBuffer buf, int offset) {
      this.header = new ResChunk_header(buf, offset);
      this.stringCount = buf.getInt(offset + ResChunk_header.SIZEOF);
      this.styleCount = buf.getInt(offset + ResChunk_header.SIZEOF + 4);
      this.flags = buf.getInt(offset + ResChunk_header.SIZEOF + 8);
      this.stringsStart = buf.getInt(offset + ResChunk_header.SIZEOF + 12);
      this.stylesStart = buf.getInt(offset + ResChunk_header.SIZEOF + 16);
    }
  };

  /**
   * This structure defines a span of style information associated with
   * a string in the pool.
   */
  interface ResStringPool_span
  {
//    enum {
    public static final int END = 0xFFFFFFFF;
//  };

    // This is the name of the span -- that is, the name of the XML
    // tag that defined it.  The special value END (0xFFFFFFFF) indicates
    // the end of an array of spans.
    ResStringPool_ref name();

    // The range of characters in the string that this span applies to.
    int firstChar();
    int lastChar();
  };


  /** ********************************************************************
   *  XML Tree
   *
   *  Binary representation of an XML document.  This is designed to
   *  express everything in an XML document, in a form that is much
   *  easier to parse on the device.
   *
   *********************************************************************** */

  /**
   * XML tree header.  This appears at the front of an XML tree,
   * describing its content.  It is followed by a flat array of
   * ResXMLTree_node structures; the hierarchy of the XML document
   * is described by the occurrance of RES_XML_START_ELEMENT_TYPE
   * and corresponding RES_XML_END_ELEMENT_TYPE nodes in the array.
   */
  static class ResXMLTree_header extends WithOffset
  {
    final ResChunk_header header;

    ResXMLTree_header(ByteBuffer buf, int offset) {
      super(offset);
      header = new ResChunk_header(buf, offset);
    }
  };

  /**
   * Basic XML tree node.  A single item in the XML document.  Extended info
   * about the node can be found after header.headerSize.
   */
  static class ResXMLTree_node extends WithOffset
  {
    final ResChunk_header header;

    // Line number in original source file at which this element appeared.
    final int lineNumber;

    // Optional XML comment that was associated with this element; -1 if none.
    final ResStringPool_ref comment;

    ResXMLTree_node(ByteBuffer buf, int offset) {
      super(offset);

      this.header = new ResChunk_header(buf, offset);
      this.lineNumber = buf.getInt(offset + ResChunk_header.SIZEOF);
      this.comment = new ResStringPool_ref(buf, offset + 12);
    }
  };

  /**
   * Extended XML tree node for CDATA tags -- includes the CDATA string.
   * Appears header.headerSize bytes after a ResXMLTree_node.
   */
  static class ResXMLTree_cdataExt
  {
    // The raw CDATA character data.
    final ResStringPool_ref data;

    // The typed value of the character data if this is a CDATA node.
    final ResValue typedData;

    public ResXMLTree_cdataExt(ByteBuffer buf, int offset) {
      this.data = new ResStringPool_ref(buf, offset);

      int dataType = buf.getInt(offset + 4);
      int data = buf.getInt(offset + 8);
      this.typedData = new ResValue(dataType, data);
    }
  };

  /**
   * Extended XML tree node for namespace start/end nodes.
   * Appears header.headerSize bytes after a ResXMLTree_node.
   */
  static class ResXMLTree_namespaceExt
  {
    // The prefix of the namespace.
    final ResStringPool_ref prefix;

    // The URI of the namespace.
    final ResStringPool_ref uri;

    public ResXMLTree_namespaceExt(ByteBuffer buf, int offset) {
      this.prefix = new ResStringPool_ref(buf, offset);
      this.uri = new ResStringPool_ref(buf, offset + 4);
    }
  };

  /**
   * Extended XML tree node for element start/end nodes.
   * Appears header.headerSize bytes after a ResXMLTree_node.
   */
  static class ResXMLTree_endElementExt
  {
    // String of the full namespace of this element.
    final ResStringPool_ref ns;

    // String name of this node if it is an ELEMENT; the raw
    // character data if this is a CDATA node.
    final ResStringPool_ref name;

    public ResXMLTree_endElementExt(ByteBuffer buf, int offset) {
      throw new UnsupportedOperationException();
//      this.ns = throw new UnsupportedOperationException();
//      this.name = name;
    }
  };

  /**
   * Extended XML tree node for start tags -- includes attribute
   * information.
   * Appears header.headerSize bytes after a ResXMLTree_node.
   */
  static class ResXMLTree_attrExt extends WithOffset
  {
    private final ByteBuffer buf;

    // String of the full namespace of this element.
    final ResStringPool_ref ns;

    // String name of this node if it is an ELEMENT; the raw
    // character data if this is a CDATA node.
    final ResStringPool_ref name;

    // Byte offset from the start of this structure where the attributes start.
    final short attributeStart;

    // Size of the ResXMLTree_attribute structures that follow.
    final short attributeSize;

    // Number of attributes associated with an ELEMENT.  These are
    // available as an array of ResXMLTree_attribute structures
    // immediately following this node.
    final short attributeCount;

    // Index (1-based) of the "id" attribute. 0 if none.
    final short idIndex;

    // Index (1-based) of the "class" attribute. 0 if none.
    final short classIndex;

    // Index (1-based) of the "style" attribute. 0 if none.
    final short styleIndex;

    public ResXMLTree_attrExt(ByteBuffer buf, int offset) {
      super(offset);
      this.buf = buf;

      this.ns = new ResStringPool_ref(buf, offset);
      this.name = new ResStringPool_ref(buf, offset + 4);
      this.attributeStart = buf.getShort(offset + 8);
      this.attributeSize = buf.getShort(offset + 10);
      this.attributeCount = buf.getShort(offset + 12);
      this.idIndex = buf.getShort(offset + 14);
      this.classIndex = buf.getShort(offset + 16);
      this.styleIndex = buf.getShort(offset + 18);
    }

    ResXMLTree_attribute attributeAt(int idx) {
      return new ResXMLTree_attribute(buf,
          myOffset() + SIZEOF_RESXMLTREE_ATTR_EXT + SIZEOF_RESXMLTREE_ATTR_EXT * idx);
    }
  };

  static class ResXMLTree_attribute
  {
    // Namespace of this attribute.
    final ResStringPool_ref ns;

    // Name of this attribute.
    final ResStringPool_ref name;

    // The original raw string value of this attribute.
    final ResStringPool_ref rawValue;

    // Processesd typed value of this attribute.
    final ResValue typedValue;

    public ResXMLTree_attribute(ByteBuffer buf, int offset) {
      this.ns = new ResStringPool_ref(buf, offset);
      this.name = new ResStringPool_ref(buf, offset + 4);
      this.rawValue = new ResStringPool_ref(buf, offset + 4);

      int dataType = buf.getInt(offset + 12);
      int data = buf.getInt(offset + 16);
      this.typedValue = new ResValue(dataType, data);
    }
  };

  /**
   *  This is a reference to a unique entry (a ResTable_entry structure)
   *  in a resource table.  The value is structured as: 0xpptteeee,
   *  where pp is the package index, tt is the type index in that
   *  package, and eeee is the entry index in that type.  The package
   *  and type values start at 1 for the first item, to help catch cases
   *  where they have not been supplied.
   */
  interface ResTable_ref
  {
    int ident();
  };

  /**
   * Reference to a string in a string pool.
   */
  static class ResStringPool_ref
  {
    // Index into the string pool table (uint32_t-offset from the indices
    // immediately after ResStringPool_header) at which to find the location
    // of the string data in the pool.
    final int index;
    
    public ResStringPool_ref(ByteBuffer buf, int offset) {
      this.index = buf.getInt(offset);
    }
  };

}
