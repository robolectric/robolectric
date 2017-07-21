/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robolectric.res.arsc;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Shorts;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.robolectric.res.arsc.Chunk.PackageChunk.TypeChunk;
import org.robolectric.res.arsc.Chunk.PackageChunk.TypeSpecChunk;

/** Represents a generic chunk. */
public class Chunk {

  private static final int UINT16_SIZE = 2;
  private static final int UINT32_SIZE = 4;

  private final ByteBuffer buffer;
  private final int offset;
  private final short headerLength;
  private final int chunkLength;
  private Type type;

  private static final int OFFSET_HEADER_SIZE = 2;
  private static final int OFFSET_CHUNK_LENGTH = 4;
  private static final int OFFSET_FIRST_HEADER = 8;
  private final short typeCode;

  public Chunk(ByteBuffer buffer, int offset, Type type) {
    this.buffer = buffer;
    this.offset = offset;
    this.type = type;
    buffer.position(offset);
    typeCode = buffer.getShort();
    headerLength = buffer.getShort();
    chunkLength = buffer.getInt();
    Preconditions.checkArgument(
        typeCode == type.code(), "Invalid chunk type, expected: " + type + " but got " + typeCode);
  }

  public Type getType() {
    return type;
  }

  public short getHeaderLength() {
    return headerLength;
  }

  public int getChunkLength() {
    return chunkLength;
  }

  protected int getChunkStart() {
    return offset;
  }

  protected int getChunkEnd() {
    return offset + getChunkLength();
  }

  protected int getPayloadStart() {
    return getChunkStart() + getHeaderLength();
  }

  /** Types of chunks that can exist. */
  public enum Type {
    NULL(0x0000),
    STRING_POOL(0x0001),
    TABLE(0x0002),
    XML(0x0003),
    XML_START_NAMESPACE(0x0100),
    XML_END_NAMESPACE(0x0101),
    XML_START_ELEMENT(0x0102),
    XML_END_ELEMENT(0x0103),
    XML_CDATA(0x0104),
    XML_RESOURCE_MAP(0x0180),
    TABLE_PACKAGE(0x0200),
    TABLE_TYPE(0x0201),
    TABLE_TYPE_SPEC(0x0202),
    TABLE_LIBRARY(0x0203);

    private final short code;

    private static final Map<Short, Type> FROM_SHORT;

    static {
      Builder<Short, Type> builder = ImmutableMap.builder();
      for (Type type : values()) {
        builder.put(type.code(), type);
      }
      FROM_SHORT = builder.build();
    }

    Type(int code) {
      this.code = Shorts.checkedCast(code);
    }

    public short code() {
      return code;
    }

    public static Type fromCode(short code) {
      return Preconditions.checkNotNull(FROM_SHORT.get(code), "Unknown chunk type: %s", code);
    }
  }

  public static TableChunk newInstance(ByteBuffer buffer) {
    return readChunk(buffer, 0);
  }

  protected static <T extends Chunk> T readChunk(ByteBuffer buffer, int chunkStartPosition) {
    Type type;
      short aShort = buffer.getShort(chunkStartPosition);
      if (aShort == -1) {
        return null;
      }

      type = Type.fromCode(aShort);
      Chunk chunk = null;
      if (Type.TABLE.equals(type)) {
        chunk = new TableChunk(buffer, chunkStartPosition, type);
      } else if (Type.STRING_POOL.equals(type)) {
        chunk = new StringPoolChunk(buffer, chunkStartPosition, type);
      } else if (Type.TABLE_PACKAGE.equals(type)) {
        chunk = new PackageChunk(buffer, chunkStartPosition, type);
      } else if (Type.TABLE_TYPE.equals(type)) {
        chunk = new TypeChunk(buffer, chunkStartPosition, type);
      } else if (Type.TABLE_TYPE_SPEC.equals(type)) {
        chunk = new TypeSpecChunk(buffer, chunkStartPosition, type);
      }
      return (T) chunk;
  }

  private void dump() {
    System.out.println("Chunk Type:  " + getType());
    System.out.println("Chunk Start:  " + offset);
    System.out.println("Header Length: " + getHeaderLength());
    System.out.println("Chunk Length:  " + getChunkLength());
    byte[] header = new byte[getHeaderLength()];
    buffer.position(offset);
    buffer.get(header);
    System.out.println("Header: " + BaseEncoding.base16().lowerCase().encode(header));

    buffer.position(offset);
    byte[] chunk = new byte[getChunkLength()];
    buffer.get(chunk);
    System.out.println("Chunk:  " + BaseEncoding.base16().lowerCase().encode(chunk));
  }

  public static class TableChunk extends Chunk {

    StringPoolChunk stringPoolChunk;
    List<PackageChunk> packageChunks = new ArrayList<>();

    public TableChunk(ByteBuffer buffer, int chunkStartPosition, Type type) {
      super(buffer, chunkStartPosition, type);
      stringPoolChunk = readChunk(buffer, getHeaderLength());

      int packageChunkOffset = getHeaderLength() + stringPoolChunk.getChunkLength();
      for (int i = 0; i < getPackageCount(); i++) {
        PackageChunk packageChunk = readChunk(buffer, packageChunkOffset);
        packageChunks.add(packageChunk);
        packageChunkOffset = packageChunk.getChunkLength();
      }
    }

    public int getPackageCount() {
      return super.buffer.getInt(OFFSET_FIRST_HEADER);
    }

    public StringPoolChunk getStringPoolChunk() {
      return stringPoolChunk;
    }

    public void dump() {
      super.dump();
      System.out.println("Package count: " + getPackageCount());
      getStringPoolChunk().dump();
      for (PackageChunk packageChunk : packageChunks) {
        packageChunk.dump();
      }
    }
  }

  public static class StringPoolChunk extends Chunk {
    // These are the defined flags for the "flags" field of ResourceStringPoolHeader
    private static final int SORTED_FLAG = 1 << 0;
    private static final int UTF8_FLAG   = 1 << 8;

    private static final int OFFSET_STYLE_COUNT = OFFSET_FIRST_HEADER + 4;
    private static final int OFFSET_FLAGS = OFFSET_STYLE_COUNT + 4;
    private static final int OFFSET_STRING_START = OFFSET_FLAGS + 4;
    private static final int OFFSET_STYLE_START = OFFSET_STRING_START + 4;
    private static final int OFFSET_STRING_INDICIES = OFFSET_STYLE_START + 4;

    public StringPoolChunk(ByteBuffer buffer, int chunkStartPosition, Type type) {
      super(buffer, chunkStartPosition, type);
    }

    public int getStringCount() {
      return super.buffer.getInt(getChunkStart() + OFFSET_FIRST_HEADER);
    }

    public int getStyleCount() {
      return super.buffer.getInt(getChunkStart() + OFFSET_STYLE_COUNT);
    }

    public int getFlags() {
      return super.buffer.getInt(getChunkStart() + OFFSET_FLAGS);
    }

    public boolean isUTF8() {
      return (getFlags() & UTF8_FLAG) != 0;
    }

    /**
     * True if this string pool contains already-sorted strings.
     *
     * @return true if @{code strings} are sorted.
     */
    public boolean isSorted() {
      return (getFlags() & SORTED_FLAG) != 0;
    }

    public int getStringStart() {
      return super.buffer.getInt(getChunkStart() + OFFSET_STRING_START);
    }

    public int getStyleStart() {
      return super.buffer.getInt(getChunkStart() + OFFSET_STYLE_START);
    }

    public int[] getStringIndicies() {
      int[] result = new int[getStringCount()];
      int start = getChunkStart() + OFFSET_STRING_INDICIES;
      for (int i = 0; i < result.length; i++) {
        result[i] = super.buffer.getInt(start);
        start += UINT32_SIZE;
      }
      return result;
    }

    public int[] getStyleIndicies() {
      int[] result = new int[getStyleCount()];
      int start = getChunkStart() + OFFSET_STRING_INDICIES + (getStringCount() * UINT32_SIZE);
      for (int i = 0; i < result.length; i++) {
        result[i] = super.buffer.getInt(start);
        start += UINT32_SIZE;
      }
      return result;
    }

    public List<String> getStrings() {
      List<String> result = new LinkedList<>();
      for (int i : getStringIndicies()) {
        int stringStartIdx = getChunkStart() + getStringStart() + i;
        result.add(ResourceString.decodeString(super.buffer, stringStartIdx, getStringType()));
      }
      return result;
    }

    private List<StringPoolStyle> getStyles() {
      List<StringPoolStyle> result = new ArrayList<>();
      // After the array of offsets for the strings in the pool, we have an offset for the styles
      // in this pool.
      for (int i : getStyleIndicies()) {
        int styleOffset = getChunkStart() + getStringStart() + i;
        result.add(new StringPoolStyle(super.buffer, styleOffset));
      }
      return result;
    }

    /** Returns the type of strings in this pool. */
    public ResourceString.Type getStringType() {
      return isUTF8() ? ResourceString.Type.UTF8 : ResourceString.Type.UTF16;
    }

    public void dump() {
      super.dump();
      System.out.println("String count: " + getStringCount());
      System.out.println("Style count: " + getStyleCount());
      System.out.println("Flags: " + getFlags());
      System.out.println("String start: " + getStringStart());
      System.out.println("Style start: " + getStyleStart());
      System.out.println("String indicies: " + Arrays.toString(getStringIndicies()));
      System.out.println("Style indicies: " + Arrays.toString(getStyleIndicies()));
      System.out.println("String: " + getStrings());
      System.out.println("Styles:");
      for (StringPoolStyle stringPoolStyle : getStyles()) {
        stringPoolStyle.dump();
      }
    }

    public class StringPoolStyle {

      // Styles are a list of integers with 0xFFFFFFFF serving as a sentinel value.
      static final int RES_STRING_POOL_SPAN_END = 0xFFFFFFFF;
      private final ByteBuffer buffer;
      private final int offset;

      public StringPoolStyle(ByteBuffer buffer, int offset) {
        this.buffer = buffer;
        this.offset = offset;
      }

      public List<StringPoolSpan> getSpans() {
        List<StringPoolSpan> result = new LinkedList<>();
        int idx = offset;
        int nameIndex = buffer.getInt(idx);
        while (nameIndex != RES_STRING_POOL_SPAN_END) {
          result.add(new StringPoolSpan(buffer, idx));
          idx += StringPoolSpan.SPAN_LENGTH;
          nameIndex = buffer.getInt(idx);
        }
        return result;
      }

      public void dump() {
        System.out.println("Style:");
        for (StringPoolSpan stringPoolSpan : getSpans()) {
          stringPoolSpan.dump();
        }
      }
    }

    public class StringPoolSpan {
      static final int SPAN_LENGTH = 12;
      private final ByteBuffer buffer;
      private final int offset;

      public StringPoolSpan(ByteBuffer buffer, int offset) {

        this.buffer = buffer;
        this.offset = offset;

        int nameIndex = buffer.getInt(offset);
        int start = buffer.getInt(offset + 4);
        int stop = buffer.getInt(offset + 8);
      }

      public int getNameIndex() {
        return buffer.getInt(offset);
      }

      public int getStart() {
        return buffer.getInt(offset + 4);
      }

      public int getEnd() {
        return buffer.getInt(offset + 8);
      }

      public void dump() {
        System.out.println(String.format("StringPoolSpan{%s, start=%d, stop=%d}",
            getNameIndex(), getStart(), getEnd()));
      }
    }
  }

  public static class PackageChunk extends Chunk {

    public static final int PACKAGE_NAME_SIZE = 128 * 2;
    private static final int OFFSET_NAME = OFFSET_FIRST_HEADER + 4;
    private static final int OFFSET_TYPE_STRINGS = OFFSET_NAME + 2 * 128;
    private static final int OFFSET_LAST_PUBLIC_TYPE = OFFSET_TYPE_STRINGS + 4;
    private static final int OFFSET_KEY_STRINGS = OFFSET_LAST_PUBLIC_TYPE + 4;
    private static final int OFFSET_LAST_PUBLIC_KEY = OFFSET_KEY_STRINGS + 4;
    private static final int OFFSET_TYPE_ID_OFFSET = OFFSET_LAST_PUBLIC_KEY + 4;
    private final int id;
    private final String name;
    private final int typeStrings;
    private final int lastPublicType;
    private final int keyStrings;
    private final int lastPublicKey;
    private final int typeIdOffset;
    Map<Integer, Chunk> chunkMap = new HashMap<>();

    public PackageChunk(ByteBuffer buffer, int offset, Type type) {
      super(buffer, offset, type);
      id = buffer.getInt();
      byte[] nameBytes = new byte[PACKAGE_NAME_SIZE];
      buffer.get(nameBytes);
      name = new String(nameBytes, Charset.forName("UTF-16LE"));
      typeStrings = buffer.getInt();
      lastPublicType = buffer.getInt();
      keyStrings = buffer.getInt();
      lastPublicKey = buffer.getInt();
      typeIdOffset = buffer.getInt();
    }

    public int getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    /**
     * Offset from the beginning of this Chunk to where the Type StringPool begins.
     */
    public int getTypeStrings() {
      return typeStrings;
    }

    public int getLastPublicType() {
      return lastPublicType;
    }

    public int getKeyStrings() {
      return keyStrings;
    }

    public int getLastPublicKey() {
      return lastPublicKey;
    }

    public int getTypeIdOffset() {
      return typeIdOffset;
    }

    public StringPoolChunk getTypeStringPool() {
      return new StringPoolChunk(super.buffer, getChunkStart() + getTypeStrings(), Type.STRING_POOL);
    }

    public StringPoolChunk getKeyStringPool() {
      return new StringPoolChunk(super.buffer, getChunkStart() + getKeyStrings(), Type.STRING_POOL);
    }

    public void init() {
      int start = super.offset + super.getHeaderLength();
      int offset = start;
      int end = super.getChunkEnd();
      int position = super.buffer.position();
      super.buffer.position(getPayloadStart());

      while (offset < end) {
        Chunk chunk = Chunk.readChunk(super.buffer, offset);
        chunkMap.put(offset, chunk);
        offset += chunk.getChunkLength();
      }

      super.buffer.position(position);
    }

    public TypeSpecChunk getTypeSpec() {
      return new TypeSpecChunk(super.buffer, getKeyStringPool().getChunkEnd(), Type.TABLE_TYPE_SPEC);
    }

    public TypeChunk getTypeChunk() {
      return new TypeChunk(super.buffer, getTypeSpec().getChunkEnd(), Type.TABLE_TYPE);
    }

    public void dump() {
      super.dump();
      System.out.println("ID: " + getId());
      System.out.println("Name: " + getName());
      System.out.println("Type Strings (String pool start idx): " + getTypeStrings());
      System.out.println("Last public type index: " + getLastPublicType());
      System.out.println("Key Strings (String pool start idx): " + getKeyStrings());
      System.out.println("Last public key index: " + getLastPublicKey());
      System.out.println("TypeId Offset: " + getTypeIdOffset());
//      System.out.println("TypeStrings: ");
//      getTypeStringPool().dump();
//      System.out.println("TypeKeys: ");
      init();

      for (Integer chunkOffset : chunkMap.keySet()) {
        System.out.println("chunkOffset = " + chunkOffset);
        chunkMap.get(chunkOffset).dump();
      }
//      getKeyStringPool().dump();
//      getTypeSpec().dump();
//      getTypeChunk().dump();
    }

    public static class TypeSpecChunk extends Chunk {

      private final byte id;
      private final byte res0;
      private final short res1;
      private final int entryCount;
      private int[] payload;

      public TypeSpecChunk(ByteBuffer buffer, int offset, Type type) {
        super(buffer, offset, type);
        buffer.position(16);
        id = buffer.get();
        res0 = buffer.get();
        res1 = buffer.getShort();
        entryCount = buffer.getInt();
      }

      public byte getId() {
        return id;
      }

      public byte getRes0() {
        return res0;
      }

      public short getRes1() {
        return res1;
      }

      public int[] getPayload() {
        return payload;
      }

      public void dump() {
        super.dump();
        System.out.println("id = " + id);
        System.out.println("res0 = " + res0);
        System.out.println("res1 = " + res1);
        System.out.println("entryCount = " + entryCount);
        System.out.println("payload = " + Arrays.toString(payload));
      }
    }

    public static class TypeChunk extends Chunk {

      private final byte id;
      private final byte res0;
      private final short res1;
      private final int entryCount;
      private final int entriesStart;
      private final byte[] config = new byte[44];
      private int[] payload;

      public TypeChunk(ByteBuffer buffer, int offset, Type type) {
        super(buffer, offset, type);
        buffer.position(16);
        id = buffer.get();
        res0 = buffer.get();
        res1 = buffer.getShort();
        entryCount = buffer.getInt();
        entriesStart = buffer.getInt();
        buffer.get(config);
        payload = new int[entryCount];
        for (int i = 0; i < entryCount; i++) {
          payload[i] = buffer.getInt();
        }
      }

      public byte getId() {
        return id;
      }

      public byte getRes0() {
        return res0;
      }

      public short getRes1() {
        return res1;
      }

      public int getEntriesStart() {
        return entriesStart;
      }

      public int[] getPayload() {
        return payload;
      }

      public void dump() {
        super.dump();
        System.out.println("id = " + id);
        System.out.println("res0 = " + res0);
        System.out.println("res1 = " + res1);
        System.out.println("entryCount = " + entryCount);
        System.out.println("entriesStart = " + entriesStart);
        System.out.println("payload = " + Arrays.toString(payload));
      }
    }
  }



}