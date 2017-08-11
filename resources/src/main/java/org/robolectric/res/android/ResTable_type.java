package org.robolectric.res.android;

import java.util.List;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/libs/androidfw/ResourceTypes.cpp
public class ResTable_type {

  final ResTableHeader header;
  final ResTablePackage _package_;
  final int entryCount;
  final ResTableTypeSpec typeSpec;
  final int typeSpecFlags;
  public IdmapEntries                    idmapEntries;
  List<ResTable_type> configs;

  ResTable_type(final ResTableHeader _header, final ResTablePackage _package, int count)
//        : header(_header), package(_package), entryCount(count),
//  typeSpec(NULL), typeSpecFlags(NULL) { }
  {
    this.header = _header;
    _package_ = _package;
    this.entryCount = count;
    this.typeSpec = null;
    this.typeSpecFlags = 0;
    this.configs = configs;
  }
}