package org.robolectric.res.android;

import java.util.Map;
import java.util.zip.ZipFile;

public class ZipArchiveHandle {
  final ZipFile zipFile;
  final Map<String, Long> dataOffsets;

  public ZipArchiveHandle(ZipFile zipFile, Map<String, Long> dataOffsets) {
    this.zipFile = zipFile;
    this.dataOffsets = dataOffsets;
  }
}
