package org.robolectric.res.android;

import java.lang.ref.WeakReference;
import org.robolectric.res.android.CppAssetManager2.Theme;

public class Registries {

  public static final NativeObjRegistry<Asset> NATIVE_ASSET_REGISTRY =
      new NativeObjRegistry<>(Asset.class);
  public static final NativeObjRegistry<CppAssetManager2> NATIVE_ASSET_MANAGER_REGISTRY =
      new NativeObjRegistry<>(CppAssetManager2.class);
  public static final NativeObjRegistry<CppApkAssets> NATIVE_APK_ASSETS_REGISTRY =
      new NativeObjRegistry<>(CppApkAssets.class);
  public static final NativeObjRegistry<ResTableTheme> NATIVE_THEME_REGISTRY =
      new NativeObjRegistry<>(ResTableTheme.class);
  public static final NativeObjRegistry<ResXMLTree> NATIVE_RES_XML_TREES =
      new NativeObjRegistry<>(ResXMLTree.class);
  public static final NativeObjRegistry<ResXMLParser> NATIVE_RES_XML_PARSERS =
          new NativeObjRegistry<>(ResXMLParser.class);
  static final NativeObjRegistry<WeakReference<ResStringPool>> NATIVE_STRING_POOLS =
      new NativeObjRegistry<>("ResStringPool");
  public static final NativeObjRegistry<Theme> NATIVE_THEME9_REGISTRY =
      new NativeObjRegistry<>(Theme.class);
}
