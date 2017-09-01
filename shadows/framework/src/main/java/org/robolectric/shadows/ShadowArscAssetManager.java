package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.res.android.Errors.BAD_INDEX;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.SparseArray;
import android.util.TypedValue;
import com.google.common.base.Strings;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.android.CppAssetManager;
import org.robolectric.res.android.Ref;
import org.robolectric.res.android.ResTable;
import org.robolectric.res.android.ResTableConfig;
import org.robolectric.res.android.ResValue;
import org.robolectric.res.android.String8;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

// native method impls transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/core/jni/android_util_AssetManager.cpp
@Implements(AssetManager.class)
public class ShadowArscAssetManager {

  private static final boolean USE_LEGACY = false;

  @RealObject
  private AssetManager realObject;
  private ResTable resTable;
  private CppAssetManager cppAssetManager;

  @Implementation
  public void __constructor__() {
    if (USE_LEGACY) {
      ShadowAssetManager shadowAssetManager = new ShadowAssetManager();
      replaceShadow(shadowAssetManager);
      shadowAssetManager.__constructor__();
    } else {
      invokeConstructor(AssetManager.class, realObject);
      resTable = loadAppResTable();
      cppAssetManager = new CppAssetManager();
    }
  }

  @Implementation
  public void __constructor__(boolean isSystem) {
    if (USE_LEGACY) {
      ShadowAssetManager shadowAssetManager = new ShadowAssetManager();
      replaceShadow(shadowAssetManager);
      shadowAssetManager.__constructor__(isSystem);
    } else {
      invokeConstructor(AssetManager.class, realObject,
          ClassParameter.from(boolean.class, isSystem));
      resTable = isSystem ? loadSystemResTable() : loadAppResTable();
      cppAssetManager = new CppAssetManager();
    }
  }

  private ResTable loadAppResTable() {
    ResTable resTable = new ResTable();
    return resTable;
  }

  private ResTable loadSystemResTable() {
    ResTable resTable = new ResTable();
    return resTable;
  }

  @Resetter
  public static void reset() {
    ShadowAssetManager.reset();
  }

  @HiddenApi
  @Implementation
  public CharSequence getResourceText(int ident) {
    return directlyOn(realObject, AssetManager.class, "getResourceText",
        ClassParameter.from(int.class, ident));
  }

  @HiddenApi
  @Implementation
  public CharSequence getResourceBagText(int ident, int bagEntryId) {
    return directlyOn(realObject, AssetManager.class, "getResourceBagText",
        ClassParameter.from(int.class, ident),
        ClassParameter.from(int.class, bagEntryId));
  }

  @HiddenApi
  @Implementation
  public String[] getResourceStringArray(final int id) {
    return directlyOn(realObject, AssetManager.class, "getResourceStringArray",
        ClassParameter.from(int.class, id));
  }

//  @HiddenApi
//  @Implementation
//  public int getResourceIdentifier(String name, String defType, String defPackage) {
//    return directlyOn(realObject, AssetManager.class, "getResourceIdentifier",
//        ClassParameter.from(String.class, name),
//        ClassParameter.from(String.class, defType),
//        ClassParameter.from(String.class, defPackage));
//  }

  @HiddenApi
  @Implementation
  public boolean getResourceValue(int ident, int density, TypedValue outValue,
      boolean resolveRefs) {
    return directlyOn(realObject, AssetManager.class, "getResourceValue",
        ClassParameter.from(int.class, ident),
        ClassParameter.from(int.class, density),
        ClassParameter.from(TypedValue.class, outValue),
        ClassParameter.from(boolean.class, resolveRefs));
  }

  @HiddenApi
  @Implementation
  public CharSequence[] getResourceTextArray(int resId) {
    return directlyOn(realObject, AssetManager.class, "getResourceTextArray",
        ClassParameter.from(int.class, resId));
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public boolean getThemeValue(int themePtr, int ident, TypedValue outValue, boolean resolveRefs) {
    return directlyOn(realObject, AssetManager.class, "getThemeValue",
        ClassParameter.from(int.class, themePtr),
        ClassParameter.from(int.class, ident),
        ClassParameter.from(TypedValue.class, outValue),
        ClassParameter.from(boolean.class, resolveRefs));
  }

  @HiddenApi
  @Implementation(minSdk = LOLLIPOP)
  public boolean getThemeValue(long themePtr, int ident, TypedValue outValue, boolean resolveRefs) {
    return directlyOn(realObject, AssetManager.class, "getThemeValue",
        ClassParameter.from(long.class, themePtr),
        ClassParameter.from(int.class, ident),
        ClassParameter.from(TypedValue.class, outValue),
        ClassParameter.from(boolean.class, resolveRefs));
  }

  @HiddenApi
  @Implementation
  public void ensureStringBlocks() {
    directlyOn(realObject, AssetManager.class, "ensureStringBlocks");
  }

  @Implementation
  public final InputStream open(String fileName) throws IOException {
    return directlyOn(realObject, AssetManager.class, "open",
        ClassParameter.from(String.class, fileName));
  }

  @Implementation
  public final InputStream open(String fileName, int accessMode) throws IOException {
    return directlyOn(realObject, AssetManager.class, "open",
        ClassParameter.from(String.class, fileName),
        ClassParameter.from(int.class, accessMode));
  }

  @Implementation
  public final AssetFileDescriptor openFd(String fileName) throws IOException {
    return directlyOn(realObject, AssetManager.class, "openFd",
        ClassParameter.from(String.class, fileName));
  }

  @Implementation
  public final String[] list(String path) throws IOException {
    return directlyOn(realObject, AssetManager.class, "list",
        ClassParameter.from(String.class, path));
  }

  @HiddenApi
  @Implementation
  public final InputStream openNonAsset(int cookie, String fileName, int accessMode)
      throws IOException {
    return directlyOn(realObject, AssetManager.class, "openNonAsset",
        ClassParameter.from(int.class, cookie),
        ClassParameter.from(String.class, fileName),
        ClassParameter.from(int.class, accessMode));
  }

  @HiddenApi
  @Implementation
  public final AssetFileDescriptor openNonAssetFd(int cookie, String fileName) throws IOException {
    return directlyOn(realObject, AssetManager.class, "openNonAssetFd",
        ClassParameter.from(int.class, cookie),
        ClassParameter.from(String.class, fileName));
  }

  @Implementation
  public final XmlResourceParser openXmlResourceParser(int cookie, String fileName)
      throws IOException {
    return directlyOn(realObject, AssetManager.class, "openXmlResourceParser",
        ClassParameter.from(int.class, cookie),
        ClassParameter.from(String.class, fileName));
  }

  public XmlResourceParser loadXmlResourceParser(int resId, String type)
      throws Resources.NotFoundException {
    return directlyOn(realObject, AssetManager.class, "loadXmlResourceParser",
        ClassParameter.from(int.class, resId),
        ClassParameter.from(String.class, type));
  }

  @HiddenApi
  @Implementation
  public int addAssetPath(String path) {
    return directlyOn(realObject, AssetManager.class, "addAssetPath", ClassParameter.from(String.class, path));
  }

  @HiddenApi
  @Implementation
  public boolean isUpToDate() {
    return directlyOn(realObject, AssetManager.class, "isUpToDate");
  }

  @HiddenApi
  @Implementation
  public void setLocale(String locale) {
    directlyOn(realObject, AssetManager.class, "setLocale",
        ClassParameter.from(String.class, locale));
  }

  @Implementation
  public String[] getLocales() {
    return directlyOn(realObject, AssetManager.class, "getLocales");
  }

  @HiddenApi
  @Implementation
  public void setConfiguration(int mcc, int mnc, String locale,
      int orientation, int touchscreen, int density, int keyboard,
      int keyboardHidden, int navigation, int screenWidth, int screenHeight,
      int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
      int screenLayout, int uiMode, int majorVersion) {
    directlyOn(realObject, AssetManager.class, "setConfiguration",
        ClassParameter.from(int.class, mcc),
        ClassParameter.from(int.class, mnc),
        ClassParameter.from(String.class, locale),
        ClassParameter.from(int.class, orientation),
        ClassParameter.from(int.class, touchscreen),
        ClassParameter.from(int.class, density),
        ClassParameter.from(int.class, keyboard),
        ClassParameter.from(int.class, keyboardHidden),
        ClassParameter.from(int.class, navigation),
        ClassParameter.from(int.class, screenWidth),
        ClassParameter.from(int.class, screenHeight),
        ClassParameter.from(int.class, smallestScreenWidthDp),
        ClassParameter.from(int.class, screenWidthDp),
        ClassParameter.from(int.class, screenHeightDp),
        ClassParameter.from(int.class, screenLayout),
        ClassParameter.from(int.class, uiMode),
        ClassParameter.from(int.class, majorVersion));
  }

  @HiddenApi
  @Implementation
  public int[] getArrayIntResource(int resId) {
    return directlyOn(realObject, AssetManager.class, "getArrayIntResource",
        ClassParameter.from(int.class, resId));
  }

  @HiddenApi
  @Implementation
  public Number createTheme() {
    return directlyOn(realObject, AssetManager.class, "createTheme");
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public void releaseTheme(int themePtr) {
    directlyOn(realObject, AssetManager.class, "releaseTheme",
        ClassParameter.from(int.class, themePtr));
  }

  @HiddenApi
  @Implementation(minSdk = LOLLIPOP)
  public void releaseTheme(long themePtr) {
    directlyOn(realObject, AssetManager.class, "releaseTheme",
        ClassParameter.from(long.class, themePtr));
  }

  private static boolean shouldDelegateToLegacyShadow(long themePtr) {
    return true;
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void applyThemeStyle(int themePtr, int styleRes, boolean force) {
    if (shouldDelegateToLegacyShadow(themePtr)) {
      ShadowAssetManager.applyThemeStyle(themePtr, styleRes, force);
    } else {
      directlyOn(AssetManager.class, "applyThemeStyle",
          ClassParameter.from(int.class, themePtr),
          ClassParameter.from(int.class, styleRes),
          ClassParameter.from(boolean.class, force));
    }
  }

  @HiddenApi
  @Implementation(minSdk = LOLLIPOP)
  public static void applyThemeStyle(long themePtr, int styleRes, boolean force) {
    if (shouldDelegateToLegacyShadow(themePtr)) {
      ShadowAssetManager.applyThemeStyle(themePtr, styleRes, force);
    } else {
      directlyOn(AssetManager.class, "applyThemeStyle",
          ClassParameter.from(long.class, themePtr),
          ClassParameter.from(int.class, styleRes),
          ClassParameter.from(boolean.class, force));
    }
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void copyTheme(int destPtr, int sourcePtr) {
    if (shouldDelegateToLegacyShadow(destPtr)) {
      ShadowAssetManager.copyTheme(destPtr, sourcePtr);
    } else {
      directlyOn(AssetManager.class, "copyTheme",
          ClassParameter.from(int.class, destPtr),
          ClassParameter.from(int.class, sourcePtr));
    }
  }

  @HiddenApi
  @Implementation(minSdk = LOLLIPOP)
  public static void copyTheme(long destPtr, long sourcePtr) {
    if (shouldDelegateToLegacyShadow(destPtr)) {
      ShadowAssetManager.copyTheme(destPtr, sourcePtr);
    } else {
      directlyOn(AssetManager.class, "copyTheme",
          ClassParameter.from(long.class, destPtr),
          ClassParameter.from(long.class, sourcePtr));
    }
  }

  @Implementation
  public String getResourceName(int resid) {
    return directlyOn(realObject, AssetManager.class, "getResourceName",
        ClassParameter.from(int.class, resid));
  }

  @Implementation
  public String getResourcePackageName(int resid) {
    return directlyOn(realObject, AssetManager.class, "getResourcePackageName",
        ClassParameter.from(int.class, resid));
  }

  @Implementation
  public String getResourceTypeName(int resid) {
    return directlyOn(realObject, AssetManager.class, "getResourceTypeName",
        ClassParameter.from(int.class, resid));
  }

  @Implementation
  public String getResourceEntryName(int resid) {
    return directlyOn(realObject, AssetManager.class, "getResourceEntryName",
        ClassParameter.from(int.class, resid));
  }

  @Implementation
  public final SparseArray<String> getAssignedPackageIdentifiers() {
    return directlyOn(realObject, AssetManager.class, "getAssignedPackageIdentifiers");
  }

  private void replaceShadow(ShadowAssetManager shadowAssetManager) {
    shadowAssetManager.realObject = realObject;
    ReflectionHelpers.setField(realObject, "__robo_data__", shadowAssetManager);
  }

  //////////// native method implementations

  private static final boolean kThrowOnBadId = false;
  private static final boolean kDebugStyles = false;

//  public native final String[] list(String path)
//      throws IOException;
//private native final int addAssetPathNative(String path, boolean appAsLib);
//  public native final int addOverlayPathNative(String idmapPath);
//  public native final boolean isUpToDate();
//  public native final String[] getLocales();
//  public native final String[] getNonSystemLocales();
//  public native final Configuration[] getSizeConfigurations();
//  public native final void setConfiguration(int mcc, int mnc, String locale,
//      int orientation, int touchscreen, int density, int keyboard,
//      int keyboardHidden, int navigation, int screenWidth, int screenHeight,
//      int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
//      int screenLayout, int uiMode, int majorVersion);
//

  @HiddenApi
  @Implementation
  public int getResourceIdentifier(String name, String defType, String defPackage) {
    if (Strings.isNullOrEmpty(name)) {
      return 0;
    }
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0;
    }

    int ident = am.getResources().identifierForName(name, defType, defPackage);

    return ident;
  }

//
//  /*package*/ native final String getResourceName(int resid);
//  /*package*/ native final String getResourcePackageName(int resid);
//  /*package*/ native final String getResourceTypeName(int resid);
//  /*package*/ native final String getResourceEntryName(int resid);
//
//  private native final long openAsset(String fileName, int accessMode);
//  private final native ParcelFileDescriptor openAssetFd(String fileName,
//      long[] outOffsets) throws IOException;
//  private native final long openNonAssetNative(int cookie, String fileName,
//      int accessMode);
//  private native ParcelFileDescriptor openNonAssetFdNative(int cookie,
//      String fileName, long[] outOffsets) throws IOException;
//  private native final void destroyAsset(long asset);
//  private native final int readAssetChar(long asset);
//  private native final int readAsset(long asset, byte[] b, int off, int len);
//  private native final long seekAsset(long asset, long offset, int whence);
//  private native final long getAssetLength(long asset);
//  private native final long getAssetRemainingLength(long asset);

  @HiddenApi
  @Implementation
  public int loadResourceValue(int ident, short density, TypedValue outValue, boolean resolve) {
    if (outValue == null) {
      throw new NullPointerException("outValue");
      //return 0;
    }
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0;
    }
    final ResTable res = am.getResources();

    Ref<ResValue> value = new Ref<>(null);
    Ref<ResTableConfig> config = new Ref<>(null);
    Ref<Integer> typeSpecFlags = new Ref<>(null);
    int block = res.getResource(ident, value, false, density, typeSpecFlags, config);
    if (kThrowOnBadId) {
        if (block == BAD_INDEX) {
            throw new IllegalStateException("Bad resource!");
            //return 0;
        }
    }
    Ref<Integer> ref = new Ref<>(ident);
    if (resolve) {
        block = res.resolveReference(value.get(), block, ref, typeSpecFlags, config);
        if (kThrowOnBadId) {
            if (block == BAD_INDEX) {
              throw new IllegalStateException("Bad resource!");
                //return 0;
            }
        }
    }
    if (block >= 0) {
        //return copyValue(env, outValue, &res, value, ref, block, typeSpecFlags, &config);
      return copyValue(outValue, res, value.get(), ref.get(), block, typeSpecFlags.get(),
          config.get());

    }
    return block;
}

  int copyValue(TypedValue outValue, ResTable table,  ResValue value, int ref, int block,
      int typeSpecFlags, ResTableConfig config) {
    outValue.type = value.dataType;
    outValue.assetCookie = table.getTableCookie(block);
    outValue.data = value.data;
    outValue.string = null;
    outValue.resourceId = ref;
    outValue.changingConfigurations = typeSpecFlags;

    if (config != null) {
      outValue.density = config.density;
    }
    return block;
  }

//  /** Returns true if the resource was found, filling in mRetStringBlock and
//   *  mRetData. */
//  private native final int loadResourceBagValue(int ident, int bagEntryId, TypedValue outValue,
//      boolean resolve);
//  /*package*/ static final int STYLE_NUM_ENTRIES = 6;
//  /*package*/ static final int STYLE_TYPE = 0;
//  /*package*/ static final int STYLE_DATA = 1;
//  /*package*/ static final int STYLE_ASSET_COOKIE = 2;
//  /*package*/ static final int STYLE_RESOURCE_ID = 3;
//
//  /* Offset within typed data array for native changingConfigurations. */
//  static final int STYLE_CHANGING_CONFIGURATIONS = 4;
//
//  /*package*/ static final int STYLE_DENSITY = 5;
//  /*package*/ native static final boolean applyStyle(long theme,
//      int defStyleAttr, int defStyleRes, long xmlParser,
//      int[] inAttrs, int[] outValues, int[] outIndices);
//  /*package*/ native static final boolean resolveAttrs(long theme,
//      int defStyleAttr, int defStyleRes, int[] inValues,
//      int[] inAttrs, int[] outValues, int[] outIndices);
//  /*package*/ native final boolean retrieveAttributes(
//      long xmlParser, int[] inAttrs, int[] outValues, int[] outIndices);
//  /*package*/ native final int getArraySize(int resource);
//  /*package*/ native final int retrieveArray(int resource, int[] outValues);
//  private native final int getStringBlockCount();
//  private native final long getNativeStringBlock(int block);
//
//  /**
//   * {@hide}
//   */
//  public native final String getCookieName(int cookie);
//
//  /**
//   * {@hide}
//   */
//  public native final SparseArray<String> getAssignedPackageIdentifiers();
//
//  /**
//   * {@hide}
//   */
//  public native static final int getGlobalAssetCount();
//
//  /**
//   * {@hide}
//   */
//  public native static final String getAssetAllocations();
//
//  /**
//   * {@hide}
//   */
//  public native static final int getGlobalAssetManagerCount();
//
//  private native final long newTheme();
//  private native final void deleteTheme(long theme);
//  /*package*/ native static final void applyThemeStyle(long theme, int styleRes, boolean force);
//  /*package*/ native static final void copyTheme(long dest, long source);
//  /*package*/ native static final void clearTheme(long theme);
//  /*package*/ native static final int loadThemeAttributeValue(long theme, int ident,
//      TypedValue outValue,
//      boolean resolve);
//  /*package*/ native static final void dumpTheme(long theme, int priority, String tag, String prefix);
//  /*package*/ native static final @NativeConfig
//  int getThemeChangingConfigurations(long theme);
//
//  private native final long openXmlAssetNative(int cookie, String fileName);
//
//  private native final String[] getArrayStringResource(int arrayRes);
//  private native final int[] getArrayStringInfo(int arrayRes);
//  /*package*/ native final int[] getArrayIntResource(int arrayRes);
//  /*package*/ native final int[] getStyleAttributes(int themeRes);
//
//  private native final void init(boolean isSystem);
//  private native final void destroy();

  @HiddenApi
  @Implementation
  public int addAssetPathNative(String path, boolean appAsLib) {
    if (Strings.isNullOrEmpty(path)) {
      return 0;
    }

    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0;
    }
    Ref<Integer> cookie = new Ref<>(null);
    boolean res = am.addAssetPath(new String8(path), cookie, appAsLib);
    return (res) ? cookie.get() : 0;
  }

//  @HiddenApi
//  @Implementation
//  public int addOverlayPathNative(String idmapPath) {
//    if (Strings.isNullOrEmpty(idmapPath)) {
//      return 0;
//    }
//
//    CppAssetManager am = assetManagerForJavaObject();
//    if (am == null) {
//      return 0;
//    }
//    Ref<Integer> cookie = new Ref<>(null);
//    boolean res = am.addOverlayPath(new String8(idmapPath), cookie);
//    return (res) ? cookie.get() : 0;
//  }



  private CppAssetManager assetManagerForJavaObject() {
    return cppAssetManager;
  }

}
