package org.robolectric.shadows;

import android.content.res.ApkAssets;
import android.content.res.AssetManager;
import android.util.ArraySet;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Ordering;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ResourcesMode;
import org.robolectric.annotation.ResourcesMode.Mode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.res.android.AssetPath;
import org.robolectric.res.android.CppAssetManager;
import org.robolectric.res.android.ResTable;
import org.robolectric.res.android.String8;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

public abstract class ShadowAssetManager {

  public static final Ordering<String> ATTRIBUTE_TYPE_PRECIDENCE =
      Ordering.explicit(
          "reference",
          "color",
          "boolean",
          "integer",
          "fraction",
          "dimension",
          "float",
          "enum",
          "flag",
          "flags",
          "string");

  private static final Object resourcesModeLock = new Object();
  private static Mode cachedResourcesMode;

  public static class Picker extends ResourceModeShadowPicker<ShadowAssetManager> {

    public Picker() {
      super(
          ShadowArscAssetManager.class,
          ShadowArscAssetManager9.class,
          ShadowArscAssetManager10.class,
          ShadowArscAssetManager14.class,
          ShadowNativeAssetManager.class);
    }
  }

  static ResourcesMode.Mode resourcesMode() {
    synchronized (resourcesModeLock) {
      if (cachedResourcesMode == null) {
        cachedResourcesMode = ConfigurationRegistry.get(ResourcesMode.Mode.class);
      }
    }
    return cachedResourcesMode;
  }

  abstract Collection<Path> getAllAssetDirs();

  @VisibleForTesting
  abstract long getNativePtr();

  public abstract static class ArscBase extends ShadowAssetManager {
    private ResTable compileTimeResTable;

    /**
     * @deprecated Avoid use.
     */
    @Deprecated
    public synchronized ResTable getCompileTimeResTable() {
      if (compileTimeResTable == null) {
        CppAssetManager compileTimeCppAssetManager = new CppAssetManager();
        for (AssetPath assetPath : getAssetPaths()) {
          if (assetPath.isSystem) {
            compileTimeCppAssetManager.addDefaultAssets(
                RuntimeEnvironment.compileTimeSystemResourcesFile);
          } else {
            compileTimeCppAssetManager.addAssetPath(new String8(assetPath.file), null, false);
          }
        }
        compileTimeResTable = compileTimeCppAssetManager.getResources();
      }

      return compileTimeResTable;
    }

    abstract List<AssetPath> getAssetPaths();
  }

  /** Accessor interface for {@link AssetManager}'s internals. */
  @ForType(AssetManager.class)
  interface _AssetManager_ {
    @Direct
    @Static
    AssetManager getSystem();

    @Static
    @Accessor("sSystem")
    void setSystem(AssetManager o);

    @Accessor("mObject")
    long getNativePtr();
  }

  /** Accessor interface for {@link AssetManager}'s internals added in API level 28. */
  @ForType(AssetManager.class)
  interface _AssetManager28_ extends _AssetManager_ {

    @Static
    @Accessor("sSystemApkAssets")
    ApkAssets[] getSystemApkAssets();

    @Static
    @Accessor("sSystemApkAssets")
    void setSystemApkAssets(ApkAssets[] apkAssets);

    @Static
    @Accessor("sSystemApkAssetsSet")
    ArraySet<ApkAssets> getSystemApkAssetsSet();

    @Static
    @Accessor("sSystemApkAssetsSet")
    void setSystemApkAssetsSet(ArraySet<ApkAssets> assetsSet);

    ApkAssets[] getApkAssets();
  }
}
