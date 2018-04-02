package org.robolectric.res.android;

import org.robolectric.res.FsFile;

public class AssetPath {
  public final FsFile file;
  public final boolean isSystem;

  public AssetPath(FsFile file, boolean isSystem) {
    this.file = file;
    this.isSystem = isSystem;
  }
}
