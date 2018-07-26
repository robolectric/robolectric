package org.robolectric.shadows;

import android.content.res.AssetManager;
import android.os.Build;
import java.util.Collection;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.res.FsFile;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadow.api.ShadowPicker;

abstract public class ShadowAssetManager {
  static final int STYLE_NUM_ENTRIES = 6;
  static final int STYLE_TYPE = 0;
  static final int STYLE_DATA = 1;
  static final int STYLE_ASSET_COOKIE = 2;
  static final int STYLE_RESOURCE_ID = 3;
  static final int STYLE_CHANGING_CONFIGURATIONS = 4;
  static final int STYLE_DENSITY = 5;

  public static class Picker implements ShadowPicker<ShadowAssetManager> {

    @Override
    public Class<? extends ShadowAssetManager> pickShadowClass() {
      if (useLegacy()) {
        return ShadowLegacyAssetManager.class;
      } else {
        if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.P) {
          return ShadowArscAssetManager9.class;
        } else {
          return ShadowArscAssetManager.class;
        }
      }
    }
  }

  /**
   * @deprecated Avoid use.
   */
  @Deprecated
  public static boolean useLegacy() {
    return RuntimeEnvironment.useLegacyResources();
  }

  /**
   * @deprecated Avoid use.
   */
  @Deprecated
  static ShadowLegacyAssetManager legacyShadowOf(AssetManager assetManager) {
    return (ShadowLegacyAssetManager) Shadow.extract(assetManager);
  }

  abstract Collection<FsFile> getAllAssetDirs();

}
