package org.robolectric.res.android;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.res.android.Errors.BAD_TYPE;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Util.ALOGW;
import static org.robolectric.res.android.Util.SIZEOF_INT;
import static org.robolectric.res.android.Util.SIZEOF_SHORT;
import static org.robolectric.res.android.Util.dtohl;
import static org.robolectric.res.android.Util.dtohs;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.res.android.ResourceTypes.ResStringPool_header.Writer;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/ResourceTypes.cpp
//   and https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/include/androidfw/ResourceTypes.h
public class ResourceTypes {
  public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
  public static final String AUTO_NS = "http://schemas.android.com/apk/res-auto";

  static final int kIdmapMagic = 0x504D4449;
  static final int kIdmapCurrentVersion = 0x00000001;

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
    private final ByteBuffer buf;
    private final int offset;

    WithOffset(ByteBuffer buf, int offset) {
      this.buf = buf;
      this.offset = offset;
    }

    public final ByteBuffer myBuf() {
      return buf;
    }

    public final int myOffset() {
      return offset;
    }

    @Override
    public String toString() {
      return "{buf+" + offset + '}';
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
  public static class ResChunk_header extends WithOffset
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
      super(buf, offset);
      this.type = buf.getShort(offset);
      this.headerSize = buf.getShort(offset + 2);
      this.size = buf.getInt(offset + 4);
    }

    public static void write(ByteBuffer buf, short type, Runnable header, Runnable contents) {
      int startPos = buf.position();
      buf.putShort(type);
      ShortWriter headerSize = new ShortWriter(buf);
      IntWriter size = new IntWriter(buf);

      header.run();
      headerSize.write((short) (buf.position() - startPos));

      contents.run();

      // pad to next int boundary
      int len = buf.position() - startPos;
      while ((len & 0x3) != 0) {
        buf.put((byte) 0);
        len++;
      }
      size.write(len);
    }
  }

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
   * Macros for building/splitting resource identifiers.
   */
//#define Res_VALIDID(resid) (resid != 0)
//#define Res_CHECKID(resid) ((resid&0xFFFF0000) != 0)
//#define Res_MAKEID(package, type, entry) \
//(((package+1)<<24) | (((type+1)&0xFF)<<16) | (entry&0xFFFF))
//#define Res_GETPACKAGE(id) ((id>>24)-1)
//#define Res_GETTYPE(id) (((id>>16)&0xFF)-1)
//#define Res_GETENTRY(id) (id&0xFFFF)

//#define Res_INTERNALID(resid) ((resid&0xFFFF0000) != 0 && (resid&0xFF0000) == 0)
  private static int Res_MAKEINTERNAL(int entry) {
    return (0x01000000 | (entry & 0xFFFF));
  }
//#define Res_MAKEARRAY(entry) (0x02000000 | (entry&0xFFFF))

//  static const size_t Res_MAXPACKAGE = 255;
//  static const size_t Res_MAXTYPE = 255;

  /**
   * Representation of a value in a resource, supplying type
   * information.
   */
  public static class Res_value
  {
    static final int SIZEOF = 8;

    // Number of bytes in this structure.
    final short size;

    // Always set to 0.
//    byte res0;

    // Type of the data value.
//    enum {
    // The 'data' is either 0 or 1, specifying this resource is either
    // undefined or empty, respectively.
    public static final int TYPE_NULL = 0x00;
    // The 'data' holds a ResTable_ref, a reference to another resource
    // table entry.
    public static final int TYPE_REFERENCE = 0x01;
    // The 'data' holds an attribute resource identifier.
    public static final int TYPE_ATTRIBUTE = 0x02;
    // The 'data' holds an index into the containing resource table's
    // global value string pool.
    public static final int TYPE_STRING = 0x03;
    // The 'data' holds a single-precision floating point number.
    public static final int TYPE_FLOAT = 0x04;
    // The 'data' holds a complex number encoding a dimension value,
    // such as "100in".
    public static final int TYPE_DIMENSION = 0x05;
    // The 'data' holds a complex number encoding a fraction of a
    // container.
    public static final int TYPE_FRACTION = 0x06;
    // The 'data' holds a dynamic ResTable_ref, which needs to be
    // resolved before it can be used like a TYPE_REFERENCE.
    public static final int TYPE_DYNAMIC_REFERENCE = 0x07;
    // The 'data' holds an attribute resource identifier, which needs to be resolved
    // before it can be used like a TYPE_ATTRIBUTE.
    public static final int TYPE_DYNAMIC_ATTRIBUTE = 0x08;

    // Beginning of integer flavors...
    public static final int TYPE_FIRST_INT = 0x10;

    // The 'data' is a raw integer value of the form n..n.
    public static final int TYPE_INT_DEC = 0x10;
    // The 'data' is a raw integer value of the form 0xn..n.
    public static final int TYPE_INT_HEX = 0x11;
    // The 'data' is either 0 or 1, for input "false" or "true" respectively.
    public static final int TYPE_INT_BOOLEAN = 0x12;

    // Beginning of color integer flavors...
    public static final int TYPE_FIRST_COLOR_INT = 0x1c;

    // The 'data' is a raw integer value of the form #aarrggbb.
    public static final int TYPE_INT_COLOR_ARGB8 = 0x1c;
    // The 'data' is a raw integer value of the form #rrggbb.
    public static final int TYPE_INT_COLOR_RGB8 = 0x1d;
    // The 'data' is a raw integer value of the form #argb.
    public static final int TYPE_INT_COLOR_ARGB4 = 0x1e;
    // The 'data' is a raw integer value of the form #rgb.
    public static final int TYPE_INT_COLOR_RGB4 = 0x1f;

    // ...end of integer flavors.
    public static final int TYPE_LAST_COLOR_INT = 0x1f;

    // ...end of integer flavors.
    public static final int TYPE_LAST_INT = 0x1f;
//  };

    public final byte dataType;

    // Structure of complex data values (TYPE_UNIT and TYPE_FRACTION)
//    enum {
    // Where the unit type information is.  This gives us 16 possible
    // types, as defined below.
    public static final int COMPLEX_UNIT_SHIFT = 0;
    public static final int COMPLEX_UNIT_MASK = 0xf;

    // TYPE_DIMENSION: Value is raw pixels.
    public static final int COMPLEX_UNIT_PX = 0;
    // TYPE_DIMENSION: Value is Device Independent Pixels.
    public static final int COMPLEX_UNIT_DIP = 1;
    // TYPE_DIMENSION: Value is a Scaled device independent Pixels.
    public static final int COMPLEX_UNIT_SP = 2;
    // TYPE_DIMENSION: Value is in points.
    public static final int COMPLEX_UNIT_PT = 3;
    // TYPE_DIMENSION: Value is in inches.
    public static final int COMPLEX_UNIT_IN = 4;
    // TYPE_DIMENSION: Value is in millimeters.
    public static final int COMPLEX_UNIT_MM = 5;

    // TYPE_FRACTION: A basic fraction of the overall size.
    public static final int COMPLEX_UNIT_FRACTION = 0;
    // TYPE_FRACTION: A fraction of the parent size.
    public static final int COMPLEX_UNIT_FRACTION_PARENT = 1;

    // Where the radix information is, telling where the decimal place
    // appears in the mantissa.  This give us 4 possible fixed point
    // representations as defined below.
    public static final int COMPLEX_RADIX_SHIFT = 4;
    public static final int COMPLEX_RADIX_MASK = 0x3;

    // The mantissa is an integral number -- i.e., 0xnnnnnn.0
    public static final int COMPLEX_RADIX_23p0 = 0;
    // The mantissa magnitude is 16 bits -- i.e, 0xnnnn.nn
    public static final int COMPLEX_RADIX_16p7 = 1;
    // The mantissa magnitude is 8 bits -- i.e, 0xnn.nnnn
    public static final int COMPLEX_RADIX_8p15 = 2;
    // The mantissa magnitude is 0 bits -- i.e, 0x0.nnnnnn
    public static final int COMPLEX_RADIX_0p23 = 3;

    // Where the actual value is.  This gives us 23 bits of
    // precision.  The top bit is the sign.
    public static final int COMPLEX_MANTISSA_SHIFT = 8;
    public static final int COMPLEX_MANTISSA_MASK = 0xffffff;
//  };

    // Possible data values for TYPE_NULL.
//    enum {
    // The value is not defined.
    public static final int DATA_NULL_UNDEFINED = 0;
    // The value is explicitly defined as empty.
    public static final int DATA_NULL_EMPTY = 1;
//  };

    public static final Res_value NULL_VALUE = new Res_value((byte) TYPE_NULL, DATA_NULL_UNDEFINED);

    // The data for this item, as interpreted according to dataType.
//    typedef uint32_t data_type;
    public final int data;

    public Res_value() {
      this.size = 0;
//      this.res0 = 0;
      this.dataType = 0;
      this.data = 0;
    }

    public Res_value(ByteBuffer buf, int offset) {
      this.size = buf.getShort(offset);
      byte res0 = buf.get(offset + 2);
      this.dataType = buf.get(offset + 3);
      this.data = buf.getInt(offset + 4);

      if (res0 != 0) {
        throw new IllegalStateException("res0 != 0 (" + res0 + ")");
      }
    }

    public Res_value(Res_value other) {
      this.size = other.size;
//      this.res0 = other.res0;
      this.dataType = other.dataType;
      this.data = other.data;
    }

    public Res_value(byte dataType, int data) {
      this.size = 0;
//      this.res0 = 0;
      this.dataType = dataType;
      this.data = data;
    }

    public static void write(ByteBuffer buf, int dataType, int data) {
      buf.putShort((short) SIZEOF); // size
      buf.put((byte) 0); // res0
      buf.put((byte) dataType); // dataType
      buf.putInt(data); // data
    }

    public Res_value withType(byte dataType) {
      return new Res_value(dataType, data);
    }

    public Res_value withData(int data) {
      return new Res_value(dataType, data);
    }

//    public void copyFrom_dtoh(Res_value other) {
//      this.size = other.size;
// //      this.res0 = other.res0;
//      this.dataType = other.dataType;
//      this.data = other.data;
//    }

    public Res_value copy() {
      return new Res_value(this);
    }

    @Override
    public String toString() {
      return "Res_value{dataType=" + dataType + ", data=" + data + '}';
    }
  }

/**
 *  This is a reference to a unique entry (a ResTable_entry structure)
 *  in a resource table.  The value is structured as: 0xpptteeee,
 *  where pp is the package index, tt is the type index in that
 *  package, and eeee is the entry index in that type.  The package
 *  and type values start at 1 for the first item, to help catch cases
 *  where they have not been supplied.
 */
public static class ResTable_ref
    {
      public static final int SIZEOF = 4;

      public int ident;

      public ResTable_ref(ByteBuffer buf, int offset) {
        ident = buf.getInt(offset);
      }

      public ResTable_ref() {
        ident = 0;
      }

      @Override
      public String toString() {
        return "ResTable_ref{ident=" + ident + '}';
      }
    };

  /**
   * Reference to a string in a string pool.
   */
  public static class ResStringPool_ref
  {
    public static final int SIZEOF = 4;

    // Index into the string pool table (uint32_t-offset from the indices
    // immediately after ResStringPool_header) at which to find the location
    // of the string data in the pool.
    public final int index;

    public ResStringPool_ref(ByteBuffer buf, int offset) {
      this.index = buf.getInt(offset);
    }

    public static void write(ByteBuffer buf, int value) {
      buf.putInt(value);
    }

    @Override
    public String toString() {
      return "ResStringPool_ref{index=" + index + '}';
    }
  }

/** ********************************************************************
 *  String Pool
 *
 *  A set of strings that can be references by others through a
 *  ResStringPool_ref.
 *
 *********************************************************************** */


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
  public static class ResStringPool_header extends WithOffset
  {
    public static final int SIZEOF = ResChunk_header.SIZEOF + 20;

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
      super(buf, offset);

      this.header = new ResChunk_header(buf, offset);
      this.stringCount = buf.getInt(offset + ResChunk_header.SIZEOF);
      this.styleCount = buf.getInt(offset + ResChunk_header.SIZEOF + 4);
      this.flags = buf.getInt(offset + ResChunk_header.SIZEOF + 8);
      this.stringsStart = buf.getInt(offset + ResChunk_header.SIZEOF + 12);
      this.stylesStart = buf.getInt(offset + ResChunk_header.SIZEOF + 16);
    }

    public int getByte(int i) {
      return myBuf().get(myOffset() + i);
    }

    public int getShort(int i) {
      return myBuf().getShort(myOffset() + i);
    }

    public static class Writer {

      private final List<String> strings = new ArrayList<>();
      private final List<byte[]> stringsAsBytes = new ArrayList<>();
      private final Map<String, Integer> stringIds = new HashMap<>();

      private boolean frozen;

      public int string(String s) {
        if (frozen) {
          throw new IllegalStateException("string pool is frozen!");
        }

        if (s == null) {
          return -1;
        }

        Integer id = stringIds.get(s);
        if (id == null) {
          id = strings.size();
          strings.add(s);
          stringsAsBytes.add(s.getBytes(UTF_8));
          stringIds.put(s, id);
        }
        return id;
      }

      public int uniqueString(String s) {
        if (frozen) {
          throw new IllegalStateException("string pool is frozen!");
        }

        if (s == null) {
          return -1;
        }

        int id = strings.size();
        strings.add(s);
        stringsAsBytes.add(s.getBytes(UTF_8));
        return id;
      }

      public void write(ByteBuffer buf) {
        freeze();

        ResChunk_header.write(buf, (short) RES_STRING_POOL_TYPE, () -> {
          // header
          int startPos = buf.position();
          int stringCount = strings.size();

          // begin string pool...
          buf.putInt(stringCount); // stringCount
          buf.putInt(0); // styleCount
          buf.putInt(UTF8_FLAG); // flags
          IntWriter stringStart = new IntWriter(buf);
          buf.putInt(0); // stylesStart

          stringStart.write(buf.position() - startPos);
        }, () -> {
          // contents
          int stringOffset = /*buf.position() + */8 + 4 * stringsAsBytes.size();
          for (int i = 0; i < stringsAsBytes.size(); i++) {
            String string = strings.get(i);
            byte[] bytes = stringsAsBytes.get(i);
            buf.putInt(stringOffset);
            stringOffset += lenLen(string.length()) + lenLen(bytes.length) + bytes.length + 1;
          }

          for (int i = 0; i < stringsAsBytes.size(); i++) {
            // number of chars
            writeLen(buf, strings.get(i).length());

            // number of bytes
            writeLen(buf, stringsAsBytes.get(i).length);

            // bytes
            buf.put(stringsAsBytes.get(i));
            // null terminator
            buf.put((byte) '\0');
          }
        });
      }

      private int lenLen(int length) {
        return length > 0x7f ? 2 : 1;
      }

      private void writeLen(ByteBuffer buf, int length) {
        if (length <= 0x7f) {
          buf.put((byte) length);
        } else {
          buf.put((byte) ((length >> 8) | 0x80));
          buf.put((byte) (length & 0x7f));
        }
      }

      public void freeze() {
        frozen = true;
      }
    }
  }

  /**
   * This structure defines a span of style information associated with
   * a string in the pool.
   */
  public static class ResStringPool_span extends WithOffset
  {
    public static final int SIZEOF = ResStringPool_ref.SIZEOF + 8;

    //    enum {
    public static final int END = 0xFFFFFFFF;
//  };

    // This is the name of the span -- that is, the name of the XML
    // tag that defined it.  The special value END (0xFFFFFFFF) indicates
    // the end of an array of spans.
    public final ResStringPool_ref name;

    // The range of characters in the string that this span applies to.
    final int firstChar;
    final int lastChar;

    public ResStringPool_span(ByteBuffer buf, int offset) {
      super(buf, offset);

      name = new ResStringPool_ref(buf, offset);
      firstChar = buf.getInt(offset + ResStringPool_ref.SIZEOF);
      lastChar = buf.getInt(offset + ResStringPool_ref.SIZEOF + 4);
    }

    public boolean isEnd() {
      return name.index == END && firstChar == END && lastChar == END;
    }
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
  public static class ResXMLTree_header extends WithOffset
  {
    public final ResChunk_header header;

    ResXMLTree_header(ByteBuffer buf, int offset) {
      super(buf, offset);
      header = new ResChunk_header(buf, offset);
    }

    public static void write(ByteBuffer buf, Writer resStringPoolWriter, Runnable contents) {
      ResChunk_header.write(buf, (short) RES_XML_TYPE, ()-> {}, () -> {
        resStringPoolWriter.write(buf);
        contents.run();
      });
    }
  }

  /**
   * Basic XML tree node.  A single item in the XML document.  Extended info
   * about the node can be found after header.headerSize.
   */
  public static class ResXMLTree_node extends WithOffset
  {
    final ResChunk_header header;

    // Line number in original source file at which this element appeared.
    final int lineNumber;

    // Optional XML comment that was associated with this element; -1 if none.
    final ResStringPool_ref comment;

    ResXMLTree_node(ByteBuffer buf, int offset) {
      super(buf, offset);

      this.header = new ResChunk_header(buf, offset);
      this.lineNumber = buf.getInt(offset + ResChunk_header.SIZEOF);
      this.comment = new ResStringPool_ref(buf, offset + 12);
    }

    ResXMLTree_node(ByteBuffer buf, ResChunk_header header) {
      super(buf, header.myOffset());

      this.header = header;
      this.lineNumber = buf.getInt(myOffset() + ResChunk_header.SIZEOF);
      this.comment = new ResStringPool_ref(buf, myOffset() + ResChunk_header.SIZEOF + 4);
    }

    public static void write(ByteBuffer buf, int type, Runnable contents) {
      ResChunk_header.write(buf, (short) type, () -> {
        buf.putInt(-1); // lineNumber
        ResStringPool_ref.write(buf, -1); // comment
      }, contents);
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
    final Res_value typedData;

    public ResXMLTree_cdataExt(ByteBuffer buf, int offset) {
      this.data = new ResStringPool_ref(buf, offset);

      int dataType = buf.getInt(offset + 4);
      int data = buf.getInt(offset + 8);
      this.typedData = new Res_value((byte) dataType, data);
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
  public static class ResXMLTree_endElementExt
  {
    static final int SIZEOF = 8;

    // String of the full namespace of this element.
    final ResStringPool_ref ns;

    // String name of this node if it is an ELEMENT; the raw
    // character data if this is a CDATA node.
    final ResStringPool_ref name;

    public ResXMLTree_endElementExt(ByteBuffer buf, int offset) {
      this.ns = new ResStringPool_ref(buf, offset);
      this.name = new ResStringPool_ref(buf, offset + ResStringPool_ref.SIZEOF);
    }

    public static class Writer {
      private final ByteBuffer buf;
      private final ResStringPool_header.Writer resStringPoolWriter;
      private final int ns;
      private final int name;

      public Writer(ByteBuffer buf, ResStringPool_header.Writer resStringPoolWriter,
          String ns, String name) {
        this.buf = buf;
        this.resStringPoolWriter = resStringPoolWriter;
        this.ns = resStringPoolWriter.string(ns);
        this.name = resStringPoolWriter.string(name);
      }

      public void write() {
        ResStringPool_ref.write(buf, ns);
        ResStringPool_ref.write(buf, name);
      }
    }
  };

  /**
   * Extended XML tree node for start tags -- includes attribute
   * information.
   * Appears header.headerSize bytes after a ResXMLTree_node.
   */
  public static class ResXMLTree_attrExt extends WithOffset
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
      super(buf, offset);
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
          myOffset() + dtohs(attributeStart) + dtohs(attributeSize) * idx);
    }

    public static class Writer {
      private final ByteBuffer buf;
      private final ResStringPool_header.Writer resStringPoolWriter;
      private final int ns;
      private final int name;

      private short idIndex;
      private short classIndex;
      private short styleIndex;

      private final List<Attr> attrs = new ArrayList<>();

      public Writer(ByteBuffer buf, ResStringPool_header.Writer resStringPoolWriter,
          String ns, String name) {
        this.buf = buf;
        this.resStringPoolWriter = resStringPoolWriter;
        this.ns = resStringPoolWriter.string(ns);
        this.name = resStringPoolWriter.string(name);
      }

      public void attr(int ns, int name, int value, Res_value resValue, String fullName) {
        attrs.add(new Attr(ns, name, value, resValue, fullName));
      }

      public void write() {
        int startPos = buf.position();
        int attributeCount = attrs.size();

        ResStringPool_ref.write(buf, ns);
        ResStringPool_ref.write(buf, name);
        ShortWriter attributeStartWriter = new ShortWriter(buf);
        buf.putShort((short) ResXMLTree_attribute.SIZEOF); // attributeSize
        buf.putShort((short) attributeCount); // attributeCount
        ShortWriter idIndexWriter = new ShortWriter(buf);
        ShortWriter classIndexWriter = new ShortWriter(buf);
        ShortWriter styleIndexWriter = new ShortWriter(buf);

        attributeStartWriter.write((short) (buf.position() - startPos));
        for (int i = 0; i < attributeCount; i++) {
          Attr attr = attrs.get(i);

          switch (attr.fullName) {
            case ":id":
              idIndex = (short) (i + 1);
              break;
            case ":style":
              styleIndex = (short) (i + 1);
              break;
            case ":class":
              classIndex = (short) (i + 1);
              break;
          }

          attr.write(buf);
        }

        idIndexWriter.write(idIndex);
        classIndexWriter.write(classIndex);
        styleIndexWriter.write(styleIndex);
      }

      private static class Attr {
        final int ns;
        final int name;
        final int value;
        final int resValueDataType;
        final int resValueData;
        final String fullName;

        public Attr(int ns, int name, int value, Res_value resValue, String fullName) {
          this.ns = ns;
          this.name = name;
          this.value = value;
          this.resValueDataType = resValue.dataType;
          this.resValueData = resValue.data;
          this.fullName = fullName;
        }

        public void write(ByteBuffer buf) {
          ResXMLTree_attribute.write(buf, ns, name, value, resValueDataType, resValueData);
        }
      }
    }
  };

  static class ResXMLTree_attribute
  {
    public static final int SIZEOF = 12+ ResourceTypes.Res_value.SIZEOF;

    // Namespace of this attribute.
    final ResStringPool_ref ns;

    // Name of this attribute.
    final ResStringPool_ref name;

    // The original raw string value of this attribute.
    final ResStringPool_ref rawValue;

    // Processesd typed value of this attribute.
    final Res_value typedValue;

    public ResXMLTree_attribute(ByteBuffer buf, int offset) {
      this.ns = new ResStringPool_ref(buf, offset);
      this.name = new ResStringPool_ref(buf, offset + 4);
      this.rawValue = new ResStringPool_ref(buf, offset + 8);
      this.typedValue = new Res_value(buf, offset + 12);
    }

    public static void write(ByteBuffer buf, int ns, int name, int value, int resValueDataType,
        int resValueData) {
      ResStringPool_ref.write(buf, ns);
      ResStringPool_ref.write(buf, name);
      ResStringPool_ref.write(buf, value);
      ResourceTypes.Res_value.write(buf, resValueDataType, resValueData);
    }
  };

/** ********************************************************************
 *  RESOURCE TABLE
 *
 *********************************************************************** */

  /**
   * Header for a resource table.  Its data contains a series of
   * additional chunks:
   *   * A ResStringPool_header containing all table values.  This string pool
   *     contains all of the string values in the entire resource table (not
   *     the names of entries or type identifiers however).
   *   * One or more ResTable_package chunks.
   *
   * Specific entries within a resource table can be uniquely identified
   * with a single integer as defined by the ResTable_ref structure.
   */
  static class ResTable_header extends WithOffset
  {
    public static final int SIZEOF = ResChunk_header.SIZEOF + 4;

    final ResChunk_header header;

    // The number of ResTable_package structures.
    final int packageCount;

    public ResTable_header(ByteBuffer buf, int offset) {
      super(buf, offset);
      this.header = new ResChunk_header(buf, offset);
      this.packageCount = buf.getInt(offset + ResChunk_header.SIZEOF);
    }
  }

  /**
   * A collection of resource data types within a package.  Followed by
   * one or more ResTable_type and ResTable_typeSpec structures containing the
   * entry values for each resource type.
   */
  static class ResTable_package extends WithOffset
  {
    public static final int SIZEOF = ResChunk_header.SIZEOF + 4 + 128 + 20;

    final ResChunk_header header;

    // If this is a base package, its ID.  Package IDs start
    // at 1 (corresponding to the value of the package bits in a
    // resource identifier).  0 means this is not a base package.
    public final int id;

    // Actual name of this package, \0-terminated.
    public final char[] name = new char[128];

    // Offset to a ResStringPool_header defining the resource
    // type symbol table.  If zero, this package is inheriting from
    // another base package (overriding specific values in it).
    public final int typeStrings;

    // Last index into typeStrings that is for public use by others.
    public final int lastPublicType;

    // Offset to a ResStringPool_header defining the resource
    // key symbol table.  If zero, this package is inheriting from
    // another base package (overriding specific values in it).
    public final int keyStrings;

    // Last index into keyStrings that is for public use by others.
    public final int lastPublicKey;

    public final int typeIdOffset;

    public ResTable_package(ByteBuffer buf, int offset) {
      super(buf, offset);
      header = new ResChunk_header(buf, offset);
      id = buf.getInt(offset + ResChunk_header.SIZEOF);
      for (int i = 0; i < name.length; i++) {
        name[i] = buf.getChar(offset + ResChunk_header.SIZEOF + 4 + i * 2);
      }
      typeStrings = buf.getInt(offset + ResChunk_header.SIZEOF + 4 + 256);
      lastPublicType = buf.getInt(offset + ResChunk_header.SIZEOF + 4 + 256 + 4);
      keyStrings = buf.getInt(offset + ResChunk_header.SIZEOF + 4 + 256 + 8);
      lastPublicKey = buf.getInt(offset + ResChunk_header.SIZEOF + 4 + 256 + 12);
      typeIdOffset = buf.getInt(offset + ResChunk_header.SIZEOF + 4 + 256 + 16);
    }
  };

  // The most specific locale can consist of:
  //
  // - a 3 char language code
  // - a 3 char region code prefixed by a 'r'
  // - a 4 char script code prefixed by a 's'
  // - a 8 char variant code prefixed by a 'v'
  //
// each separated by a single char separator, which sums up to a total of 24
// chars, (25 include the string terminator). Numbering system specificator,
// if present, can add up to 14 bytes (-u-nu-xxxxxxxx), giving 39 bytes,
// or 40 bytes to make it 4 bytes aligned.
  public static final int RESTABLE_MAX_LOCALE_LEN = 40;

  /**
   * A specification of the resources defined by a particular type.
   *
   * There should be one of these chunks for each resource type.
   *
   * This structure is followed by an array of integers providing the set of
   * configuration change flags (ResTable_config::CONFIG_*) that have multiple
   * resources for that configuration.  In addition, the high bit is set if that
   * resource has been made public.
   */
  static class ResTable_typeSpec extends WithOffset
  {
    public static final int SIZEOF = ResChunk_header.SIZEOF + 8;

    final ResChunk_header header;

    // The type identifier this chunk is holding.  Type IDs start
    // at 1 (corresponding to the value of the type bits in a
    // resource identifier).  0 is invalid.
    final byte id;

    // Must be 0.
    final byte res0;
    // Must be 0.
    final short res1;

    // Number of uint32_t entry configuration masks that follow.
    final int entryCount;

    //enum : uint32_t {
    // Additional flag indicating an entry is public.
    static final int SPEC_PUBLIC = 0x40000000;

    // Additional flag indicating an entry is overlayable at runtime.
    // Added in Android-P.
    static final int SPEC_OVERLAYABLE = 0x80000000;
//    };

    public ResTable_typeSpec(ByteBuffer buf, int offset) {
      super(buf, offset);

      header = new ResChunk_header(buf, offset);
      id = buf.get(offset + ResChunk_header.SIZEOF);
      res0 = buf.get(offset + ResChunk_header.SIZEOF + 1);
      res1 = buf.getShort(offset + ResChunk_header.SIZEOF + 2);
      entryCount = buf.getInt(offset + ResChunk_header.SIZEOF + 4);
    }

    public int[] getSpecFlags() {
      int[] ints = new int[(header.size - header.headerSize) / 4];
      for (int i = 0; i < ints.length; i++) {
        ints[i] = myBuf().getInt(myOffset() + header.headerSize + i * 4);

      }
      return ints;
    }
  };

  /**
   * A collection of resource entries for a particular resource data
   * type.
   *
   * If the flag FLAG_SPARSE is not set in `flags`, then this struct is
   * followed by an array of uint32_t defining the resource
   * values, corresponding to the array of type strings in the
   * ResTable_package::typeStrings string block. Each of these hold an
   * index from entriesStart; a value of NO_ENTRY means that entry is
   * not defined.
   *
   * If the flag FLAG_SPARSE is set in `flags`, then this struct is followed
   * by an array of ResTable_sparseTypeEntry defining only the entries that
   * have values for this type. Each entry is sorted by their entry ID such
   * that a binary search can be performed over the entries. The ID and offset
   * are encoded in a uint32_t. See ResTabe_sparseTypeEntry.
   *
   * There may be multiple of these chunks for a particular resource type,
   * supply different configuration variations for the resource values of
   * that type.
   *
   * It would be nice to have an additional ordered index of entries, so
   * we can do a binary search if trying to find a resource by string name.
   */
  static class ResTable_type extends WithOffset
  {
    //      public static final int SIZEOF = ResChunk_header.SIZEOF + 12 + ResTable_config.SIZ;
    public static final int SIZEOF_WITHOUT_CONFIG = ResChunk_header.SIZEOF + 12;

    final ResChunk_header header;

    //enum {
    public static final int NO_ENTRY = 0xFFFFFFFF;
//    };

    // The type identifier this chunk is holding.  Type IDs start
    // at 1 (corresponding to the value of the type bits in a
    // resource identifier).  0 is invalid.
    final byte id;

    //      enum {
    // If set, the entry is sparse, and encodes both the entry ID and offset into each entry,
    // and a binary search is used to find the key. Only available on platforms >= O.
    // Mark any types that use this with a v26 qualifier to prevent runtime issues on older
    // platforms.
    public static final int FLAG_SPARSE = 0x01;
    //    };
    final byte flags;

    // Must be 0.
    final short reserved;

    // Number of uint32_t entry indices that follow.
    final int entryCount;

    // Offset from header where ResTable_entry data starts.
    final int entriesStart;

    // Configuration this collection of entries is designed for. This must always be last.
    final ResTable_config config;

    ResTable_type(ByteBuffer buf, int offset) {
      super(buf, offset);

      header = new ResChunk_header(buf, offset);
      id = buf.get(offset + ResChunk_header.SIZEOF);
      flags = buf.get(offset + ResChunk_header.SIZEOF + 1);
      reserved = buf.getShort(offset + ResChunk_header.SIZEOF + 2);
      entryCount = buf.getInt(offset + ResChunk_header.SIZEOF + 4);
      entriesStart = buf.getInt(offset + ResChunk_header.SIZEOF + 8);

      buf.position(offset + ResChunk_header.SIZEOF + 12);
      config = ResTable_config.createConfig(buf);
    }

    public int findEntryByResName(int stringId) {
      for (int i = 0; i < entryCount; i++) {
        if (entryNameIndex(i) == stringId) {
          return i;
        }
      }
      return -1;
    }

    int entryOffset(int entryIndex) {
      ByteBuffer byteBuffer = myBuf();
      int offset = myOffset();

      // from ResTable cpp:
//            const uint32_t* const eindex = reinterpret_cast<const uint32_t*>(
//            reinterpret_cast<const uint8_t*>(thisType) + dtohs(thisType->header.headerSize));
//
//        uint32_t thisOffset = dtohl(eindex[realEntryIndex]);
      return byteBuffer.getInt(offset + header.headerSize + entryIndex * 4);
    }

    private int entryNameIndex(int entryIndex) {
      ByteBuffer byteBuffer = myBuf();
      int offset = myOffset();

      // from ResTable cpp:
//            const uint32_t* const eindex = reinterpret_cast<const uint32_t*>(
//            reinterpret_cast<const uint8_t*>(thisType) + dtohs(thisType->header.headerSize));
//
//        uint32_t thisOffset = dtohl(eindex[realEntryIndex]);
      int entryOffset = byteBuffer.getInt(offset + header.headerSize + entryIndex * 4);
      if (entryOffset == -1) {
        return -1;
      }

      int STRING_POOL_REF_OFFSET = 4;
      return dtohl(byteBuffer.getInt(offset + entriesStart + entryOffset + STRING_POOL_REF_OFFSET));
    }
  };

  // The minimum size required to read any version of ResTable_type.
//   constexpr size_t kResTableTypeMinSize =
//   sizeof(ResTable_type) - sizeof(ResTable_config) + sizeof(ResTable_config::size);
  static final int kResTableTypeMinSize =
      ResTable_type.SIZEOF_WITHOUT_CONFIG - ResTable_config.SIZEOF + SIZEOF_INT /*sizeof(ResTable_config::size)*/;

  /**
   * An entry in a ResTable_type with the flag `FLAG_SPARSE` set.
   */
  static class ResTable_sparseTypeEntry extends WithOffset {
    public static final int SIZEOF = 6;

    // Holds the raw uint32_t encoded value. Do not read this.
    int entry;

    short idxOrOffset;
//    struct {
      // The index of the entry.
//      uint16_t idx;

      // The offset from ResTable_type::entriesStart, divided by 4.
//      uint16_t offset;
//    };

    public ResTable_sparseTypeEntry(ByteBuffer buf, int offset) {
      super(buf, offset);

      entry = buf.getInt(offset);
      idxOrOffset = buf.getShort(offset + 4);
    }
  };

  /**
   * This is the beginning of information about an entry in the resource
   * table.  It holds the reference to the name of this entry, and is
   * immediately followed by one of:
   *   * A Res_value structure, if FLAG_COMPLEX is -not- set.
   *   * An array of ResTable_map structures, if FLAG_COMPLEX is set.
   *     These supply a set of name/value mappings of data.
   */
  static class ResTable_entry extends WithOffset
  {
    public static final int SIZEOF = 4 + ResStringPool_ref.SIZEOF;

    // Number of bytes in this structure.
    final short size;

    //enum {
    // If set, this is a complex entry, holding a set of name/value
    // mappings.  It is followed by an array of ResTable_map structures.
    public static final int FLAG_COMPLEX = 0x0001;
    // If set, this resource has been declared public, so libraries
    // are allowed to reference it.
    public static final int FLAG_PUBLIC = 0x0002;
    // If set, this is a weak resource and may be overriden by strong
    // resources of the same name/type. This is only useful during
    // linking with other resource tables.
    public static final int FLAG_WEAK = 0x0004;
    //    };
    final short flags;

    // Reference into ResTable_package::keyStrings identifying this entry.
    final ResStringPool_ref key;

    ResTable_entry(ByteBuffer buf, int offset) {
      super(buf, offset);

      size = buf.getShort(offset);
      flags = buf.getShort(offset + 2);
      key = new ResStringPool_ref(buf, offset + 4);
    }

    public Res_value getResValue() {
      // something like:

      // final Res_value device_value = reinterpret_cast<final Res_value>(
      //     reinterpret_cast<final byte*>(entry) + dtohs(entry.size));

      return new Res_value(myBuf(), myOffset() + dtohs(size));
    }
  }

  /**
   * Extended form of a ResTable_entry for map entries, defining a parent map
   * resource from which to inherit values.
   */
  static class ResTable_map_entry extends ResTable_entry
  {

    /**
     * Indeterminate size, calculate using {@link #size} instead.
     */
    public static final Void SIZEOF = null;

    public static final int BASE_SIZEOF = ResTable_entry.SIZEOF + 8;

    // Resource identifier of the parent mapping, or 0 if there is none.
    // This is always treated as a TYPE_DYNAMIC_REFERENCE.
    ResTable_ref parent;
    // Number of name/value pairs that follow for FLAG_COMPLEX.
    int count;

    ResTable_map_entry(ByteBuffer buf, int offset) {
      super(buf, offset);

      parent = new ResTable_ref(buf, offset + ResTable_entry.SIZEOF);
      count = buf.getInt(offset + ResTable_entry.SIZEOF + ResTable_ref.SIZEOF);
    }
  };

  /**
   * A single name/value mapping that is part of a complex resource
   * entry.
   */
  public static class ResTable_map extends WithOffset
  {
    public static final int SIZEOF = ResTable_ref.SIZEOF + ResourceTypes.Res_value.SIZEOF;

    // The resource identifier defining this mapping's name.  For attribute
    // resources, 'name' can be one of the following special resource types
    // to supply meta-data about the attribute; for all other resource types
    // it must be an attribute resource.
    public final ResTable_ref name;

    // Special values for 'name' when defining attribute resources.
//enum {
    // This entry holds the attribute's type code.
    public static final int ATTR_TYPE = Res_MAKEINTERNAL(0);

    // For integral attributes, this is the minimum value it can hold.
    public static final int ATTR_MIN = Res_MAKEINTERNAL(1);

    // For integral attributes, this is the maximum value it can hold.
    public static final int ATTR_MAX = Res_MAKEINTERNAL(2);

    // Localization of this resource is can be encouraged or required with
    // an aapt flag if this is set
    public static final int ATTR_L10N = Res_MAKEINTERNAL(3);

    // for plural support, see android.content.res.PluralRules#attrForQuantity(int)
    public static final int ATTR_OTHER = Res_MAKEINTERNAL(4);
    public static final int ATTR_ZERO = Res_MAKEINTERNAL(5);
    public static final int ATTR_ONE = Res_MAKEINTERNAL(6);
    public static final int ATTR_TWO = Res_MAKEINTERNAL(7);
    public static final int ATTR_FEW = Res_MAKEINTERNAL(8);
    public static final int ATTR_MANY = Res_MAKEINTERNAL(9);

//    };

    // Bit mask of allowed types, for use with ATTR_TYPE.
//enum {
    // No type has been defined for this attribute, use generic
    // type handling.  The low 16 bits are for types that can be
    // handled generically; the upper 16 require additional information
    // in the bag so can not be handled generically for TYPE_ANY.
    public static final int TYPE_ANY = 0x0000FFFF;

    // Attribute holds a references to another resource.
    public static final int TYPE_REFERENCE = 1<<0;

    // Attribute holds a generic string.
    public static final int TYPE_STRING = 1<<1;

    // Attribute holds an integer value.  ATTR_MIN and ATTR_MIN can
    // optionally specify a constrained range of possible integer values.
    public static final int TYPE_INTEGER = 1<<2;

    // Attribute holds a boolean integer.
    public static final int TYPE_BOOLEAN = 1<<3;

    // Attribute holds a color value.
    public static final int TYPE_COLOR = 1<<4;

    // Attribute holds a floating point value.
    public static final int TYPE_FLOAT = 1<<5;

    // Attribute holds a dimension value, such as "20px".
    public static final int TYPE_DIMENSION = 1<<6;

    // Attribute holds a fraction value, such as "20%".
    public static final int TYPE_FRACTION = 1<<7;

    // Attribute holds an enumeration.  The enumeration values are
    // supplied as additional entries in the map.
    public static final int TYPE_ENUM = 1<<16;

    // Attribute holds a bitmaks of flags.  The flag bit values are
    // supplied as additional entries in the map.
    public static final int TYPE_FLAGS = 1<<17;
//    };

    // Enum of localization modes, for use with ATTR_L10N.
//enum {
    public static final int L10N_NOT_REQUIRED = 0;
    public static final int L10N_SUGGESTED    = 1;
//    };

    // This mapping's value.
    public Res_value value;

    public ResTable_map(ByteBuffer buf, int offset) {
      super(buf, offset);

      name = new ResTable_ref(buf, offset);
      value = new Res_value(buf, offset + ResTable_ref.SIZEOF);
    }

    public ResTable_map() {
      super(null, 0);
      this.name = new ResTable_ref();
      this.value = new Res_value();
    }

    @Override
    public String toString() {
      return "ResTable_map{" + "name=" + name + ", value=" + value + '}';
    }
  };

  /**
   * A package-id to package name mapping for any shared libraries used
   * in this resource table. The package-id's encoded in this resource
   * table may be different than the id's assigned at runtime. We must
   * be able to translate the package-id's based on the package name.
   */
  static class ResTable_lib_header extends WithOffset
  {
    static final int SIZEOF = ResChunk_header.SIZEOF + 4;

    ResChunk_header header;

    // The number of shared libraries linked in this resource table.
    int count;

    ResTable_lib_header(ByteBuffer buf, int offset) {
      super(buf, offset);

      header = new ResChunk_header(buf, offset);
      count = buf.getInt(offset + ResChunk_header.SIZEOF);
    }
  };

  /**
   * A shared library package-id to package name entry.
   */
  static class ResTable_lib_entry extends WithOffset
  {
    public static final int SIZEOF = 4 + 128 * SIZEOF_SHORT;

    // The package-id this shared library was assigned at build time.
    // We use a uint32 to keep the structure aligned on a uint32 boundary.
    int packageId;

    // The package name of the shared library. \0 terminated.
    char[] packageName = new char[128];

    ResTable_lib_entry(ByteBuffer buf, int offset) {
      super(buf, offset);

      packageId = buf.getInt(offset);

      for (int i = 0; i < packageName.length; i++) {
        packageName[i] = buf.getChar(offset + 4 + i * SIZEOF_SHORT);
      }
    }
  };

  // struct alignas(uint32_t) Idmap_header {
  static class Idmap_header extends WithOffset {
    // Always 0x504D4449 ('IDMP')
    int magic;

    int version;

    int target_crc32;
    int overlay_crc32;

    final byte[] target_path = new byte[256];
    final byte[] overlay_path = new byte[256];

    short target_package_id;
    short type_count;

    Idmap_header(ByteBuffer buf, int offset) {
      super(buf, offset);

      magic = buf.getInt(offset);
      version = buf.getInt(offset + 4);
      target_crc32 = buf.getInt(offset + 8);
      overlay_crc32 = buf.getInt(offset + 12);

      buf.get(target_path, offset + 16, 256);
      buf.get(overlay_path, offset + 16 + 256, 256);

      target_package_id = buf.getShort(offset + 16 + 256 + 256);
      type_count = buf.getShort(offset + 16 + 256 + 256 + 2);
    }
  } // __attribute__((packed));

  // struct alignas(uint32_t) IdmapEntry_header {
  static class IdmapEntry_header extends WithOffset {
    static final int SIZEOF = 2 * 4;

    short target_type_id;
    short overlay_type_id;
    short entry_count;
    short entry_id_offset;
    int entries[];

    IdmapEntry_header(ByteBuffer buf, int offset) {
      super(buf, offset);

      target_type_id = buf.getShort(offset);
      overlay_type_id = buf.getShort(offset + 2);
      entry_count = buf.getShort(offset + 4);
      entry_id_offset = buf.getShort(offset + 6);
      entries = new int[entry_count];
      for (int i = 0; i < entries.length; i++) {
        entries[i] = buf.getInt(offset + 8 + i * SIZEOF_INT);
      }
    }
  } // __attribute__((packed));


  abstract private static class FutureWriter<T> {
    protected final ByteBuffer buf;
    private final int position;

    public FutureWriter(ByteBuffer buf, int size) {
      this.buf = buf;
      this.position = buf.position();
      buf.position(position + size);
    }

    abstract protected void put(int position, T value);

    public void write(T value) {
      put(position, value);
    }
  }

  private static class IntWriter extends FutureWriter<Integer> {
    public IntWriter(ByteBuffer buf) {
      super(buf, 4);
    }

    @Override
    protected void put(int position, Integer value) {
      buf.putInt(position, value);
    }
  }

  private static class ShortWriter extends FutureWriter<Short> {
    public ShortWriter(ByteBuffer buf) {
      super(buf, 2);
    }

    @Override
    protected void put(int position, Short value) {
      buf.putShort(position, value);
    }
  }

  private static final Runnable NO_OP = () -> {};
}
