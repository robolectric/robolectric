package org.robolectric.shadows;

import android.content.res.AssetManager;
import java.util.Collection;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.res.FsFile;
import org.robolectric.res.android.AssetPath;
import org.robolectric.res.android.CppAssetManager;
import org.robolectric.res.android.ResTable;
import org.robolectric.res.android.String8;
import org.robolectric.shadow.api.Shadow;

abstract public class ShadowAssetManager {
  static final int STYLE_NUM_ENTRIES = 6;
  static final int STYLE_TYPE = 0;
  static final int STYLE_DATA = 1;
  static final int STYLE_ASSET_COOKIE = 2;
  static final int STYLE_RESOURCE_ID = 3;
  static final int STYLE_CHANGING_CONFIGURATIONS = 4;
  static final int STYLE_DENSITY = 5;

  public static class Picker extends ResourceModeShadowPicker<ShadowAssetManager> {

    public Picker() {
      super(ShadowLegacyAssetManager.class, ShadowArscAssetManager.class,
          ShadowArscAssetManager9.class);
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

  public abstract static class ArscBase extends ShadowAssetManager {
    private ResTable compileTimeResTable;

    /**
     * @deprecated Avoid use.
     */
    @Deprecated
    synchronized public ResTable getCompileTimeResTable() {
      if (compileTimeResTable == null) {
        CppAssetManager compileTimeCppAssetManager = new CppAssetManager();
        for (AssetPath assetPath : getAssetPaths()) {
          if (assetPath.isSystem) {
            compileTimeCppAssetManager.addDefaultAssets(
                RuntimeEnvironment.compileTimeSystemResourcesFile.getPath());
          } else {
            compileTimeCppAssetManager.addAssetPath(new String8(assetPath.file.getPath()), null, false);
          }
        }
        compileTimeResTable = compileTimeCppAssetManager.getResources();
      }

      return compileTimeResTable;
    }

    abstract List<AssetPath> getAssetPaths();
  }
}
