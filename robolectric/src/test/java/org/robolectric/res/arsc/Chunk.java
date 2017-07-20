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
import com.google.common.primitives.Shorts;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** Represents a generic chunk. */
public class Chunk {

  private final ByteBuffer buffer;
  private final int offset;
  private Type type;

  private static final int OFFSET_HEADER_SIZE = 2;
  private static final int OFFSET_CHUNK_LENGTH = 4;
  private static final int OFFSET_FIRST_HEADER = 8;

  public Chunk(ByteBuffer buffer, int offset, Type type) {
    this.buffer = buffer;
    this.offset = offset;
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  public short getHeaderLength() {
    return buffer.getShort(offset + OFFSET_HEADER_SIZE);
  }

  public int getChunkLength() {
    return buffer.getInt(offset + OFFSET_CHUNK_LENGTH);
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
      }
      return (T) chunk;
  }

  private void dump() {
    System.out.println("Chunk Type:  " + getType());
    System.out.println("Chunk Start:  " + offset);
    System.out.println("Header Length: " + getHeaderLength());
    System.out.println("Chunk Length: " + getChunkLength());
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
    private static final int OFFSET_STYLE_COUNT = OFFSET_FIRST_HEADER + 4;
    private static final int OFFSET_FLAGS = OFFSET_STYLE_COUNT + 4;
    private static final int OFFSET_STRING_START = OFFSET_FLAGS + 4;
    private static final int OFFSET_STYLE_START = OFFSET_STRING_START + 4;

    public StringPoolChunk(ByteBuffer buffer, int chunkStartPosition, Type type) {
      super(buffer, chunkStartPosition, type);
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

    public int getStringStart() {
      return super.buffer.getInt(super.offset + OFFSET_STRING_START);
    }

    public int getStyleStart() {
      return super.buffer.getInt(super.offset + OFFSET_STYLE_START);
    }

    public void dump() {
      super.dump();
      System.out.println("String count: " + getStringCount());
      System.out.println("Style count: " + getStyleCount());
      System.out.println("Flags: " + getFlags());
      System.out.println("String start: " + getStringStart());
      System.out.println("Style start: " + getStyleStart());
    }
  }

  public static class PackageChunk extends Chunk {

    private static final int OFFSET_NAME = OFFSET_FIRST_HEADER + 4;
    private static final int OFFSET_TYPE_STRINGS = OFFSET_NAME + 2 * 128;
    private static final int OFFSET_LAST_PUBLIC_TYPE = OFFSET_TYPE_STRINGS + 4;
    private static final int OFFSET_KEY_STRINGS = OFFSET_LAST_PUBLIC_TYPE + 4;
    private static final int OFFSET_LAST_PUBLIC_KEY = OFFSET_KEY_STRINGS + 4;
    private static final int OFFSET_TYPE_ID_OFFSET = OFFSET_LAST_PUBLIC_KEY + 4;

    public PackageChunk(ByteBuffer buffer, int offset, Type type) {
      super(buffer, offset, type);
    }

    public int getId() {
      return super.buffer.getInt(super.offset + OFFSET_FIRST_HEADER);
    }

    public char[] getName() {
      char[] name = new char[128];
      super.buffer.position(super.offset + OFFSET_NAME);
      for (int i = 0; i < name.length; i++) {
        name[i] = super.buffer.getChar();
      }
      return name;
    }

    public int getTypeStrings() {
      return super.buffer.getInt(super.offset + OFFSET_TYPE_STRINGS);
    }

    public int getLastPublicType() {
      return super.buffer.getInt(super.offset + OFFSET_LAST_PUBLIC_TYPE);
    }

    public int getKeyStrings() {
      return super.buffer.getInt(super.offset + OFFSET_KEY_STRINGS);
    }

    public int getLastPublicKey() {
      return super.buffer.getInt(super.offset + OFFSET_LAST_PUBLIC_KEY);
    }

    public int getTypeIdOffset() {
      return super.buffer.getInt(super.offset + OFFSET_TYPE_ID_OFFSET);
    }

    public StringPoolChunk getTypeStringPool() {
      return new StringPoolChunk(super.buffer, getTypeStrings(), Type.STRING_POOL);
    }

    public StringPoolChunk getKeyStringPool() {
      return new StringPoolChunk(super.buffer, getKeyStrings(), Type.STRING_POOL);
    }

    public void dump() {
      super.dump();
      System.out.println("ID: " + getId());
      System.out.println("Name: " + new String(getName()));
      System.out.println("Type Strings (String pool start idx): " + getTypeStrings());
      System.out.println("Last public type index: " + getLastPublicType());
      System.out.println("Key Strings (String pool start idx): " + getKeyStrings());
      System.out.println("Last public key index: " + getLastPublicKey());
      System.out.println("TypeId Offset: " + getTypeIdOffset());
      System.out.println("TypeStrings: ");
      getTypeStringPool().dump();
      System.out.println("TypeKeys: ");
      getKeyStringPool().dump();
    }

    public static class TypeSpecChunk extends Chunk {

      public TypeSpecChunk(ByteBuffer buffer, int offset, Type type) {
        super(buffer, offset, type);
      }
    }
  }



}