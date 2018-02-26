package org.robolectric.shadows;

import java.util.Collection;
import org.robolectric.res.FsFile;

abstract class ShadowAssetManagerCommon {

  abstract Collection<FsFile> getAllAssetDirs();
}
