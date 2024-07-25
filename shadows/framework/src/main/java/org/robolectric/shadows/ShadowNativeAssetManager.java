package org.robolectric.shadows;

import android.content.res.AssetManager;
import java.nio.file.Path;
import java.util.Collection;
import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.V;

@Implements(
    value = AssetManager.class,
    minSdk = V.SDK_INT,
    callNativeMethodsByDefault = true,
    shadowPicker = ShadowAssetManager.Picker.class)
public class ShadowNativeAssetManager extends ShadowAssetManager {

  @Override
  Collection<Path> getAllAssetDirs() {
    throw new UnsupportedOperationException();
  }

  @Override
  long getNativePtr() {
    throw new UnsupportedOperationException();
  }
}
