package org.robolectric.res.android;

import java.util.zip.ZipFile;

public class ZipArchiveHandle {
  final ZipFile zipFile;

  public ZipArchiveHandle(ZipFile zipFile) {
    this.zipFile = zipFile;
  }
}
