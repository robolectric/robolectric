package org.robolectric.res.android;

import java.nio.file.Path;

public class AssetPath {
  public final Path file;
  public final boolean isSystem;

  public AssetPath(Path file, boolean isSystem) {
    this.file = file;
    this.isSystem = isSystem;
  }
}
