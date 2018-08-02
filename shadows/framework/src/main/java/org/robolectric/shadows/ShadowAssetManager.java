package org.robolectric.shadows;

import android.content.res.AssetManager;
import java.util.Collection;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.res.FsFile;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadow.api.ShadowPicker;

public abstract class ShadowAssetManager {
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
      return useLegacy()
          ? ShadowLegacyAssetManager.class
          : ShadowArscAssetManager.class;
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
