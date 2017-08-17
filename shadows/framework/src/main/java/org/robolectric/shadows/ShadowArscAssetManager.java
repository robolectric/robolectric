package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.SparseArray;
import android.util.TypedValue;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.TypedResource;
import org.robolectric.res.builder.XmlBlock;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(AssetManager.class)
public class ShadowArscAssetManager {

  @RealObject
  private AssetManager realObject;

  @Implementation
  public void __constructor__() {
    ShadowAssetManager shadowAssetManager = new ShadowAssetManager();
    replaceShadow(shadowAssetManager);
    shadowAssetManager.__constructor__();
  }

  @Implementation
  public void __constructor__(boolean isSystem) {
    ShadowAssetManager shadowAssetManager = new ShadowAssetManager();
    replaceShadow(shadowAssetManager);
    shadowAssetManager.__constructor__(isSystem);
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

  @HiddenApi
  @Implementation
  public int getResourceIdentifier(String name, String defType, String defPackage) {
    return directlyOn(realObject, AssetManager.class, "getResourceIdentifier",
        ClassParameter.from(String.class, name),
        ClassParameter.from(String.class, defType),
        ClassParameter.from(String.class, defPackage));
  }

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
    return directlyOn(realObject, AssetManager.class, "addAssetPath",
        ClassParameter.from(String.class, path));
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
}
