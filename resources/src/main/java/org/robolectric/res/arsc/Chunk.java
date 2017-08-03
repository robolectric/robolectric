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

import com.google.common.primitives.UnsignedBytes;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.robolectric.res.android.ResChunkHeader;
import org.robolectric.res.android.ResTableEntry;
import org.robolectric.res.android.ResTableHeader;
import org.robolectric.res.android.ResTableMap;
import org.robolectric.res.android.ResTableMapEntry;
import org.robolectric.res.android.ResTablePackage;
import org.robolectric.res.android.ResTableType;
import org.robolectric.res.android.ResTableTypeSpec;
import org.robolectric.res.android.ResValue;
import org.robolectric.res.android.ResTableConfig;
import org.robolectric.res.arsc.Chunk.PackageChunk.TypeChunk;
import org.robolectric.res.arsc.Chunk.PackageChunk.TypeSpecChunk;

/** Represents a generic chunk. */
abstract public class Chunk {

  private static final int UINT32_SIZE = 4;

  private final ByteBuffer buffer;
  private final int offset;
  ResChunkHeader header;

  private static final int OFFSET_FIRST_HEADER = 8;

  public Chunk(ByteBuffer buffer, int offset, ResChunkHeader header) {
    this.buffer = buffer;
    this.offset = offset;

    this.header = header;
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
    return readChunk(buffer, 0, getResChunkHeader(buffer, 0));
  }

  protected static <T extends Chunk> T readChunk(ByteBuffer buffer, int chunkStartPosition, ResChunkHeader header) {
    Type type;
    if (header == null) return null;

    type = Type.fromCode(header.type);
    Chunk chunk;
    if (Type.TABLE.equals(type)) {
      chunk = new TableChunk(buffer, chunkStartPosition, header);
    } else if (Type.STRING_POOL.equals(type)) {
      chunk = new StringPoolChunk(buffer, chunkStartPosition, header);
    } else if (Type.TABLE_PACKAGE.equals(type)) {
      chunk = new PackageChunk(buffer, chunkStartPosition, header);
    } else if (Type.TABLE_TYPE.equals(type)) {
      chunk = new TypeChunk(buffer, chunkStartPosition, header);
    } else if (Type.TABLE_TYPE_SPEC.equals(type)) {
      chunk = new TypeSpecChunk(buffer, chunkStartPosition, header);
    } else {
      throw new IllegalArgumentException("unknown table type " + header.type);
    }
    return (T) chunk;
  }

  private static ResChunkHeader getResChunkHeader(ByteBuffer buffer, int chunkStartPosition) {
    buffer.position(chunkStartPosition);
    ResChunkHeader header = new ResChunkHeader();
    header.type = buffer.getShort();
    if (header.type == -1) {
      return null;
    }
    header.headerSize = buffer.getShort();
    header.size = buffer.getInt();
    return header;
  }

  public static class TableChunk extends Chunk {

    private final ResTableHeader tableHeader;
    private final StringPoolChunk valuesStringPool;
    private final Map<Integer, PackageChunk> packageChunks = new HashMap<>();

    public TableChunk(ByteBuffer buffer, int chunkStartPosition, ResChunkHeader header) {
      super(buffer, chunkStartPosition, header);
      tableHeader = new ResTableHeader();
      tableHeader.header = header;
      tableHeader.packageCount = buffer.getInt();
      valuesStringPool = readChunk(buffer, header.headerSize, getResChunkHeader(buffer, header.headerSize));

      int packageChunkOffset = header.headerSize + valuesStringPool.header.size;
      for (int i = 0; i < tableHeader.packageCount; i++) {
        PackageChunk packageChunk = readChunk(buffer, packageChunkOffset, getResChunkHeader(buffer, packageChunkOffset));
        packageChunks.put(packageChunk.tablePackage.id, packageChunk);
        packageChunkOffset = packageChunk.header.size;
      }
    }

    public StringPoolChunk getValuesStringPool() {
      return valuesStringPool;
    }

    public PackageChunk getPackageChunk(int packageId) {
      return packageChunks.get(packageId);
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
    private final int stringsStart;

    public StringPoolChunk(ByteBuffer buffer, int offset, ResChunkHeader header) {
      super(buffer, offset, header);
      this.header = header;
      stringsStart = super.buffer.getInt(offset + OFFSET_STRING_START);
    }

    public int getStringCount() {
      return super.buffer.getInt(super.offset + OFFSET_FIRST_HEADER);
    }

    public int getStyleCount() {
      return super.buffer.getInt(super.offset + OFFSET_STYLE_COUNT);
    }

    public int getFlags() {
      return super.buffer.getInt(super.offset + OFFSET_FLAGS);
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

    /**
     * Return the offset to the string data, from the beginning of this Chunk.
     */
    private int getStringsStart() {
      return stringsStart;
    }

    public int[] getStyleIndicies() {
      int[] result = new int[getStyleCount()];
      int start = super.offset + OFFSET_STRING_INDICIES + (getStringCount() * UINT32_SIZE);
      for (int i = 0; i < result.length; i++) {
        result[i] = super.buffer.getInt(start);
        start += UINT32_SIZE;
      }
      return result;
    }

    public String getString(int i) {
      int chunkStart = super.offset;
      int start = chunkStart + OFFSET_STRING_INDICIES;
      int valueIndex = super.buffer.getInt(start + i * UINT32_SIZE);
      int stringStartIdx = chunkStart + getStringsStart() + valueIndex;
      return ResourceString.decodeString(super.buffer, stringStartIdx, getStringType());
    }

    /** Returns the type of strings in this pool. */
    public ResourceString.Type getStringType() {
      return isUTF8() ? ResourceString.Type.UTF8 : ResourceString.Type.UTF16;
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

    public static class StringPoolSpan {
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

    private final Map<Integer, Chunk> chunksByOffset = new HashMap<>();
    private final Map<Integer, TypeSpecChunk> typeSpecsByTypeId = new HashMap<>();
    private final Map<Integer, List<TypeChunk>> typesByTypeId = new HashMap<>();
    private StringPoolChunk typeStringPool;
    private StringPoolChunk keyStringPool;

    private ResTablePackage tablePackage;

    public PackageChunk(ByteBuffer buffer, int offset, ResChunkHeader header) {
      super(buffer, offset, header);
      tablePackage = new ResTablePackage();
      tablePackage.id = buffer.getInt();
      for (int i = 0; i < tablePackage.name.length; i++) {
        tablePackage.name[i] = buffer.getChar();
      }
      tablePackage.header = header;
      tablePackage.typeStrings = buffer.getInt();
      tablePackage.lastPublicType = buffer.getInt();
      tablePackage.keyStrings = buffer.getInt();
      tablePackage.lastPublicKey = buffer.getInt();
      tablePackage.typeIdOffset = buffer.getInt();

      int payloadStart = super.offset + header.headerSize;
      int end = offset + header.size;
      int position = buffer.position();

      // read type string pool
      typeStringPool = Chunk.readChunk(buffer, payloadStart, getResChunkHeader(buffer, payloadStart));
      payloadStart += typeStringPool.header.size;

      // read key string pool
      keyStringPool = Chunk.readChunk(buffer, payloadStart, getResChunkHeader(buffer, payloadStart));
      payloadStart += keyStringPool.header.size;

      while (payloadStart < end) {
        Chunk chunk = Chunk.readChunk(buffer, payloadStart, getResChunkHeader(buffer, payloadStart));
        chunksByOffset.put(payloadStart, chunk);
        switch (Type.fromCode(chunk.header.type)) {
          case TABLE_TYPE_SPEC:
            typeSpecsByTypeId.put((int) ((TypeSpecChunk) chunk).typeSpec.id, (TypeSpecChunk) chunk);
            break;
          case TABLE_TYPE:
            List<TypeChunk> typeChunks = typesByTypeId
                .computeIfAbsent((int) ((TypeChunk) chunk).type.id, integer -> new ArrayList<>());
            typeChunks.add((TypeChunk) chunk);
            break;
          default:
            // no op
        }
        payloadStart += chunk.header.size;
      }

      buffer.position(position);
    }

    public String getName() {
      return new String(tablePackage.name);
    }

    public StringPoolChunk getTypeStringPool() {
      return typeStringPool;
    }

    public StringPoolChunk getKeyStringPool() {
      return keyStringPool;
    }

    public TypeSpecChunk getTypeSpec(int typeId) {
      return typeSpecsByTypeId.get(typeId);
    }

    public List<TypeChunk> getTypes(int typeId) {
      return typesByTypeId.get(typeId);
    }

    public static class TypeSpecChunk extends Chunk {

      public final ResTableTypeSpec typeSpec;

      public TypeSpecChunk(ByteBuffer buffer, int offset, ResChunkHeader header) {
        super(buffer, offset, header);
        typeSpec = new ResTableTypeSpec();
        typeSpec.header = header;
        typeSpec.id = buffer.get();
        typeSpec.res0 = buffer.get();
        typeSpec.res1 = buffer.getShort();
        typeSpec.entryCount = buffer.getInt();
      }
    }

    public static class TypeChunk extends Chunk {

      /** The minimum size in bytes that this configuration must be to contain screen config info. */
      private static final int SCREEN_CONFIG_MIN_SIZE = 32;

      /** The minimum size in bytes that this configuration must be to contain screen dp info. */
      private static final int SCREEN_DP_MIN_SIZE = 36;

      /** The minimum size in bytes that this configuration must be to contain locale info. */
      private static final int LOCALE_MIN_SIZE = 48;

      /** The minimum size in bytes that this config must be to contain the screenConfig extension. */
      private static final int SCREEN_CONFIG_EXTENSION_MIN_SIZE = 52;

      private final ResTableType type;
      private List<ResTableEntry> entries = new LinkedList<>();

      public TypeChunk(ByteBuffer buffer, int offset, ResChunkHeader header) {
        super(buffer, offset, header);
        type = new ResTableType();
        type.header = header;
        type.id = buffer.get();
        type.flags = buffer.get();
        Preconditions.checkArgument(type.flags == 0); // Res0 Unused - must be 0
        type.reserved = buffer.getShort();
        Preconditions.checkArgument(type.reserved == 0); // Res1 Unused - must be 0
        type.entryCount = buffer.getInt();
        type.entriesStart = buffer.getInt();
        type.config = createConfig(buffer);
        int[] payload = new int[type.entryCount];
        for (int i = 0; i < type.entryCount; i++) {
          payload[i] = buffer.getInt();
        }

        for (int i = 0; i < type.entryCount; i++) {
          int entryOffset = payload[i];
          if (entryOffset == -1) {
            entries.add(null);
          } else {
            entries.add(createEntry(buffer, offset + type.entriesStart + entryOffset));
          }
        }
      }

      public static ResValue createValue(ByteBuffer buffer) {
        short size = buffer.getShort();
        byte res0 = buffer.get();
        byte dataType = buffer.get();
        int data = buffer.getInt();
        return new ResValue(dataType, data);
      }

      public static ResTableEntry createEntry(ByteBuffer buffer, int entryOffset) {
        System.out.println("entryOffset = " + entryOffset);
        buffer.position(entryOffset);
        short headerLength = buffer.getShort();
        short flags = buffer.getShort();
        System.out.println("flags = " + flags);
        ResTableEntry entry;
        int key = buffer.getInt();
        if ((flags & ResTableEntry.FLAG_COMPLEX) == 0) {
          System.out.println("SimpleEntry at " + Integer.toHexString(entryOffset));
          ResValue value = createValue(buffer);

          entry = new ResTableEntry(value);
        } else {
          ArrayList<ResTableMap> mapEntries = new ArrayList<>();

          int parent = buffer.getInt();
          int count = buffer.getInt();
          for (int i = 0; i < count; i++) {
            int name = buffer.getInt();
            ResValue value = createValue(buffer);

            mapEntries.add(new ResTableMap(name, value));
          }

          entry = new ResTableMapEntry(mapEntries, parent);
        }

        entry.flags = flags;

        int oldPosition = buffer.position();
        byte[] chunk = new byte[buffer.position() - entryOffset];
        buffer.position(entryOffset);
        buffer.get(chunk);
        System.out.println(BaseEncoding.base16().lowerCase().withSeparator(" ", 8).encode(chunk));
        buffer.position(oldPosition);

        return entry;
      }

      private static ResTableConfig createConfig(ByteBuffer buffer) {
        int startPosition = buffer.position();  // The starting buffer position to calculate bytes read.
        int size = buffer.getInt();
        int mcc = buffer.getShort() & 0xFFFF;
        int mnc = buffer.getShort() & 0xFFFF;
        byte[] language = new byte[2];
        buffer.get(language);
        byte[] region = new byte[2];
        buffer.get(region);
        int orientation = UnsignedBytes.toInt(buffer.get());
        int touchscreen = UnsignedBytes.toInt(buffer.get());
        int density = buffer.getShort() & 0xFFFF;
        int keyboard = UnsignedBytes.toInt(buffer.get());
        int navigation = UnsignedBytes.toInt(buffer.get());
        int inputFlags = UnsignedBytes.toInt(buffer.get());
        buffer.get();  // 1 byte of padding
        int screenWidth = buffer.getShort() & 0xFFFF;
        int screenHeight = buffer.getShort() & 0xFFFF;
        int sdkVersion = buffer.getShort() & 0xFFFF;
        int minorVersion = buffer.getShort() & 0xFFFF;

        // At this point, the configuration's size needs to be taken into account as not all
        // configurations have all values.
        int screenLayout = 0;
        int uiMode = 0;
        int smallestScreenWidthDp = 0;
        int screenWidthDp = 0;
        int screenHeightDp = 0;
        byte[] localeScript = new byte[4];
        byte[] localeVariant = new byte[8];
        int screenLayout2 = 0;

        if (size >= SCREEN_CONFIG_MIN_SIZE) {
          screenLayout = UnsignedBytes.toInt(buffer.get());
          uiMode = UnsignedBytes.toInt(buffer.get());
          smallestScreenWidthDp = buffer.getShort() & 0xFFFF;
        }

        if (size >= SCREEN_DP_MIN_SIZE) {
          screenWidthDp = buffer.getShort() & 0xFFFF;
          screenHeightDp = buffer.getShort() & 0xFFFF;
        }

        if (size >= LOCALE_MIN_SIZE) {
          buffer.get(localeScript);
          buffer.get(localeVariant);
        }

        if (size >= SCREEN_CONFIG_EXTENSION_MIN_SIZE) {
          screenLayout2 = UnsignedBytes.toInt(buffer.get());
          buffer.get();  // Reserved padding
          buffer.getShort();  // More reserved padding
        }

        // After parsing everything that's known, account for anything that's unknown.
        int bytesRead = buffer.position() - startPosition;
        byte[] unknown = new byte[size - bytesRead];
        buffer.get(unknown);

        return new ResTableConfig(size, mcc, mnc, language, region, orientation,
            touchscreen, density, keyboard, navigation, inputFlags, screenWidth, screenHeight,
            sdkVersion, minorVersion, screenLayout, uiMode, smallestScreenWidthDp, screenWidthDp,
            screenHeightDp, localeScript, localeVariant, screenLayout2, unknown);
      }

      public List<ResTableEntry> getEntries() {
        return entries;
      }
    }
  }
}