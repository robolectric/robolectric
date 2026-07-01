package org.robolectric.res.android;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

public class ZipArchiveHandle {
  final ZipFile zipFile;
  private final File file;
  // name -> local file header offset (from the central directory).
  private final Map<String, Long> localHeaderOffsets;
  // name -> exact data offset.
  private final Map<String, Long> dataOffsetCache = new HashMap<>();
  private RandomAccessFile randomAccessFile;

  public ZipArchiveHandle(ZipFile zipFile, File file, Map<String, Long> localHeaderOffsets) {
    this.zipFile = zipFile;
    this.file = file;
    this.localHeaderOffsets = localHeaderOffsets;
  }

  /**
   * Returns the exact offset of the entry's data within the file, or {@code null} if there is no
   * such entry. The local file header is read (and the result cached) on first access.
   */
  synchronized Long dataOffset(String entryName) {
    Long cached = dataOffsetCache.get(entryName);
    if (cached != null) {
      return cached;
    }
    Long localHeaderOffset = localHeaderOffsets.get(entryName);
    if (localHeaderOffset == null) {
      return null;
    }
    try {
      if (randomAccessFile == null) {
        randomAccessFile = new RandomAccessFile(file, "r");
      }
      long exact = FileMap.dataOffsetForLocalHeader(randomAccessFile, localHeaderOffset);
      dataOffsetCache.put(entryName, exact);
      return exact;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
