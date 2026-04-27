package org.robolectric.shadows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds minimal valid {@code resources.arsc} binary files for testing split APK resource loading.
 *
 * <p>The generated resource tables follow the Android binary resource format and can be parsed by
 * Robolectric's {@code LoadedArsc.Load()} method. Currently supports string resources only.
 *
 * <p>This class is intended for testing purposes and generates the simplest possible valid resource
 * table. For production resource tables, use AAPT2.
 */
final class ArscResourceTableBuilder {

  // Chunk type constants from ResourceTypes.h
  private static final short RES_STRING_POOL_TYPE = 0x0001;
  private static final short RES_TABLE_TYPE = 0x0002;
  private static final short RES_TABLE_PACKAGE_TYPE = 0x0200;
  private static final short RES_TABLE_TYPE_SPEC_TYPE = 0x0202;
  private static final short RES_TABLE_TYPE_TYPE = 0x0201;

  // Res_value data types
  private static final byte TYPE_STRING = 0x03;

  // String pool flags
  private static final int UTF8_FLAG = 0x00000100;

  private ArscResourceTableBuilder() {}

  /**
   * Builds a minimal valid {@code resources.arsc} containing string resources.
   *
   * <p>The generated resource table has one package with one type ("string") containing entries for
   * each provided string resource. Resource IDs follow the pattern {@code packageId:01:NNNN} where
   * NNNN is the 0-based index of the entry.
   *
   * @param packageName the package name (e.g., "com.example.feature")
   * @param packageId the package ID (0x7f for app resources, 0x02-0x7e for libraries)
   * @param stringResources ordered map from entry name to string value
   * @return the raw bytes of the resources.arsc file
   */
  static byte[] buildStringResourceTable(
      String packageName, int packageId, Map<String, String> stringResources) {
    try {
      List<String> entryNames = new ArrayList<>(stringResources.keySet());
      List<String> entryValues = new ArrayList<>(stringResources.values());

      // Build global string pool (contains the actual string values)
      byte[] globalStringPool = buildUtf8StringPool(entryValues);

      // Build type string pool (contains type names: just "string")
      List<String> typeNames = new ArrayList<>();
      typeNames.add("string");
      byte[] typeStringPool = buildUtf8StringPool(typeNames);

      // Build key string pool (contains entry names)
      byte[] keyStringPool = buildUtf8StringPool(entryNames);

      // Build typeSpec chunk
      byte[] typeSpec = buildTypeSpec(entryNames.size());

      // Build type chunk (default config, references global string pool)
      byte[] typeChunk = buildTypeChunk(entryNames.size());

      // Build package chunk
      byte[] packageChunk =
          buildPackageChunk(
              packageId, packageName, typeStringPool, keyStringPool, typeSpec, typeChunk);

      // Build the complete resource table
      return buildResourceTable(globalStringPool, packageChunk);
    } catch (IOException e) {
      throw new RuntimeException("Failed to build resource table", e);
    }
  }

  private static byte[] buildResourceTable(byte[] globalStringPool, byte[] packageChunk)
      throws IOException {
    int totalSize = 12 + globalStringPool.length + packageChunk.length; // 12 = header
    ByteBuffer buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN);

    // ResTable_header
    buf.putShort(RES_TABLE_TYPE); // type
    buf.putShort((short) 12); // headerSize
    buf.putInt(totalSize); // size
    buf.putInt(1); // packageCount

    buf.put(globalStringPool);
    buf.put(packageChunk);

    return buf.array();
  }

  private static byte[] buildPackageChunk(
      int packageId,
      String packageName,
      byte[] typeStringPool,
      byte[] keyStringPool,
      byte[] typeSpec,
      byte[] typeChunk)
      throws IOException {
    // Package header size: 8 (chunk header) + 4 (id) + 256 (name) + 4*4 (offsets) + 4
    //   = 8 + 4 + 256 + 16 + 4 = 288
    int headerSize = 288;
    int totalSize =
        headerSize
            + typeStringPool.length
            + keyStringPool.length
            + typeSpec.length
            + typeChunk.length;

    ByteBuffer buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN);

    // ResChunk_header
    buf.putShort(RES_TABLE_PACKAGE_TYPE); // type
    buf.putShort((short) headerSize); // headerSize
    buf.putInt(totalSize); // size

    // Package ID
    buf.putInt(packageId);

    // Package name (128 chars = 256 bytes, UTF-16LE, null-terminated)
    byte[] nameBytes = new byte[256];
    byte[] nameUtf16 = packageName.getBytes(StandardCharsets.UTF_16LE);
    System.arraycopy(nameUtf16, 0, nameBytes, 0, Math.min(nameUtf16.length, 254));
    buf.put(nameBytes);

    // typeStrings offset (relative to package header start)
    buf.putInt(headerSize);
    // lastPublicType
    buf.putInt(1);
    // keyStrings offset (relative to package header start)
    buf.putInt(headerSize + typeStringPool.length);
    // lastPublicKey
    buf.putInt(0);
    // typeIdOffset (API 21+)
    buf.putInt(0);

    // Append type string pool, key string pool, type spec, and type chunk
    buf.put(typeStringPool);
    buf.put(keyStringPool);
    buf.put(typeSpec);
    buf.put(typeChunk);

    return buf.array();
  }

  private static byte[] buildTypeSpec(int entryCount) {
    int size = 16 + entryCount * 4; // header + flags array
    // Align to 4 bytes
    size = (size + 3) & ~3;
    ByteBuffer buf = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);

    // ResChunk_header
    buf.putShort(RES_TABLE_TYPE_SPEC_TYPE); // type
    buf.putShort((short) 16); // headerSize
    buf.putInt(size); // size

    // id (1-based type ID; "string" = 1)
    buf.put((byte) 1);
    // res0, res1 (reserved)
    buf.put((byte) 0);
    buf.putShort((short) 0);
    // entryCount
    buf.putInt(entryCount);

    // Flags (one per entry, 0 = no special config)
    for (int i = 0; i < entryCount; i++) {
      buf.putInt(0);
    }

    return buf.array();
  }

  private static byte[] buildTypeChunk(int entryCount) {
    // Each entry: 4 bytes offset + 8 bytes ResTable_entry + 8 bytes Res_value = 20 bytes per entry
    // But offsets are in the offset array, entries start after config
    int entrySize = 8 + 8; // ResTable_entry + Res_value
    int offsetArraySize = entryCount * 4;
    // ResTable_config (minimum 28 bytes for default config)
    int configSize = 28;
    // Header: 8 (chunk) + 1 (id) + 1 (flags) + 2 (reserved) + 4 (entryCount) + 4 (entriesStart)
    int headerSize = 8 + 1 + 1 + 2 + 4 + 4 + configSize;
    int entriesStart = headerSize + offsetArraySize;
    int totalSize = entriesStart + entryCount * entrySize;
    // Align to 4 bytes
    totalSize = (totalSize + 3) & ~3;

    ByteBuffer buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN);

    // ResChunk_header
    buf.putShort(RES_TABLE_TYPE_TYPE); // type
    buf.putShort((short) headerSize); // headerSize
    buf.putInt(totalSize); // size

    // id (1-based type ID)
    buf.put((byte) 1);
    // flags (0 = dense entries)
    buf.put((byte) 0);
    // reserved
    buf.putShort((short) 0);
    // entryCount
    buf.putInt(entryCount);
    // entriesStart (offset from beginning of this chunk to entry data)
    buf.putInt(entriesStart);

    // ResTable_config (default config: all zeros except size)
    buf.putInt(configSize); // config.size
    for (int i = 4; i < configSize; i++) {
      buf.put((byte) 0);
    }

    // Offset array
    for (int i = 0; i < entryCount; i++) {
      buf.putInt(i * entrySize);
    }

    // Entry data
    for (int i = 0; i < entryCount; i++) {
      // ResTable_entry
      buf.putShort((short) 8); // size of ResTable_entry
      buf.putShort((short) 0); // flags
      buf.putInt(i); // key (index into key string pool)

      // Res_value
      buf.putShort((short) 8); // size of Res_value
      buf.put((byte) 0); // res0
      buf.put(TYPE_STRING); // dataType
      buf.putInt(i); // data (index into global string pool)
    }

    return buf.array();
  }

  /**
   * Builds a UTF-8 string pool chunk. The strings are stored as UTF-8 with length-prefixed entries.
   */
  static byte[] buildUtf8StringPool(List<String> strings) throws IOException {
    int stringCount = strings.size();

    // Calculate string data
    ByteArrayOutputStream stringData = new ByteArrayOutputStream();
    int[] offsets = new int[stringCount];
    for (int i = 0; i < stringCount; i++) {
      offsets[i] = stringData.size();
      byte[] utf8 = strings.get(i).getBytes(StandardCharsets.UTF_8);
      int charLen = strings.get(i).length();
      // UTF-8 string pool format: charLen (1-2 bytes) + byteLen (1-2 bytes) + data + \0
      if (charLen > 127) {
        stringData.write(((charLen >> 8) & 0x7F) | 0x80);
        stringData.write(charLen & 0xFF);
      } else {
        stringData.write(charLen & 0x7F);
      }
      if (utf8.length > 127) {
        stringData.write(((utf8.length >> 8) & 0x7F) | 0x80);
        stringData.write(utf8.length & 0xFF);
      } else {
        stringData.write(utf8.length & 0x7F);
      }
      stringData.write(utf8);
      stringData.write(0); // null terminator
    }

    int headerSize = 28;
    int offsetArraySize = stringCount * 4;
    int stringsStart = headerSize + offsetArraySize;
    int dataSize = stringData.size();
    // Align total to 4 bytes
    int totalSize = stringsStart + dataSize;
    totalSize = (totalSize + 3) & ~3;

    ByteBuffer buf = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN);

    // ResStringPool_header
    buf.putShort(RES_STRING_POOL_TYPE); // type
    buf.putShort((short) headerSize); // headerSize
    buf.putInt(totalSize); // size
    buf.putInt(stringCount); // stringCount
    buf.putInt(0); // styleCount
    buf.putInt(UTF8_FLAG); // flags (UTF-8)
    buf.putInt(stringsStart); // stringsStart
    buf.putInt(0); // stylesStart

    // Offset array
    for (int offset : offsets) {
      buf.putInt(offset);
    }

    // String data
    buf.put(stringData.toByteArray());

    return buf.array();
  }
}
