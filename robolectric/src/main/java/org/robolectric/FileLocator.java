package org.robolectric;

import org.robolectric.res.FsFile;

public interface FileLocator {
  FsFile locateManifest(String type, String flavor, String abiSplit);
  FsFile locateResDir(String type, String flavor);
  FsFile locateAssetsDir(String type, String flavor);
}
