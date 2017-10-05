package org.robolectric.manifest;

import org.robolectric.res.FsFile;

public interface AndroidManifestParser {
  void parse(FsFile androidManifestFile, AndroidManifest androidManifest);
}
