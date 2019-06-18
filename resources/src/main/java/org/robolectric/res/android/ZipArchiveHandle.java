package org.robolectric.res.android;

import com.google.common.collect.ImmutableMap;
import java.util.zip.ZipFile;

public class ZipArchiveHandle {
  final ZipFile zipFile;
  final ImmutableMap<String, Long> dataOffsets;

  public ZipArchiveHandle(ZipFile zipFile, ImmutableMap<String, Long> dataOffsets) {
    this.zipFile = zipFile;
    this.dataOffsets = dataOffsets;
  }
}
