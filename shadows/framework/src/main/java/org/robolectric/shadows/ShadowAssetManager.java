package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N_MR1;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.ParcelFileDescriptor;
import android.util.SparseArray;
import android.util.TypedValue;
import dalvik.system.VMRuntime;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.FsFile;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadow.api.ShadowFactory;
import org.robolectric.shadows.ShadowAssetManager.Factory;

@Implements(value = AssetManager.class, looseSignatures = true, factory = Factory.class)
public abstract class ShadowAssetManager {
  static final int STYLE_NUM_ENTRIES = 6;
  static final int STYLE_TYPE = 0;
  static final int STYLE_DATA = 1;
  static final int STYLE_ASSET_COOKIE = 2;
  static final int STYLE_RESOURCE_ID = 3;
  static final int STYLE_CHANGING_CONFIGURATIONS = 4;
  static final int STYLE_DENSITY = 5;

  public static class Factory implements ShadowFactory<ShadowAssetManager> {
    @Override
    public ShadowAssetManager newInstance() {
      return useLegacy()
          ? new ShadowLegacyAssetManager()
          : new ShadowArscAssetManager();
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

  @RealObject
  protected AssetManager realObject;

  @Implementation
  abstract protected void __constructor__();

  @Implementation
  abstract protected void __constructor__(boolean isSystem);

  
  @Implementation(minSdk = VERSION_CODES.P)
  protected static long nativeCreate() {
    // Return a fake pointer, must not be 0.
    return 1;
  }
  

  @HiddenApi @Implementation(maxSdk = VERSION_CODES.KITKAT)
  abstract protected void init();

  @HiddenApi @Implementation(minSdk = VERSION_CODES.KITKAT_WATCH)
  abstract protected void init(boolean isSystem);

  @HiddenApi @Implementation
  abstract public CharSequence getResourceText(int ident);

  @HiddenApi @Implementation
  abstract public CharSequence getResourceBagText(int ident, int bagEntryId);

  @HiddenApi @Implementation
  abstract protected int getStringBlockCount();

  @HiddenApi @Implementation
  abstract public String[] getResourceStringArray(final int id);

  @HiddenApi @Implementation
  abstract public int getResourceIdentifier(String name, String defType, String defPackage);

  @HiddenApi @Implementation
  abstract public boolean getResourceValue(int ident, int density, TypedValue outValue, boolean resolveRefs);

  @HiddenApi @Implementation
  abstract public CharSequence[] getResourceTextArray(int resId);

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  abstract public boolean getThemeValue(int themePtr, int ident, TypedValue outValue, boolean resolveRefs);

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  abstract public boolean getThemeValue(long themePtr, int ident, TypedValue outValue, boolean resolveRefs);

  @HiddenApi @Implementation
  abstract protected Object ensureStringBlocks();

  @Implementation
  abstract public InputStream open(String fileName) throws IOException;

  @Implementation
  abstract public InputStream open(String fileName, int accessMode) throws IOException;

  @Implementation
  abstract public AssetFileDescriptor openFd(String fileName) throws IOException;

  @Implementation
  abstract public String[] list(String path) throws IOException;

  @HiddenApi @Implementation
  abstract protected Number openAsset(String fileName, int mode) throws FileNotFoundException;

  @HiddenApi @Implementation
  abstract protected ParcelFileDescriptor openAssetFd(String fileName, long[] outOffsets)
      throws IOException;

  @HiddenApi @Implementation
  abstract public InputStream openNonAsset(int cookie, String fileName, int accessMode)
      throws IOException;

  @HiddenApi @Implementation
  abstract protected Number openNonAssetNative(int cookie, String fileName, int accessMode)
      throws FileNotFoundException;

  @HiddenApi @Implementation
  abstract public AssetFileDescriptor openNonAssetFd(int cookie, String fileName)
      throws IOException;

  @HiddenApi @Implementation
  abstract protected ParcelFileDescriptor openNonAssetFdNative(int cookie,
      String fileName, long[] outOffsets) throws IOException;

  @HiddenApi @Implementation
  abstract protected Number openXmlAssetNative(int cookie, String fileName)
      throws FileNotFoundException;

  @Implementation
  abstract public XmlResourceParser openXmlResourceParser(int cookie, String fileName)
      throws IOException;

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final int readAssetChar(int asset) {
    return readAssetChar((long) asset);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  abstract protected int readAssetChar(long asset);

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final int readAsset(int asset, byte[] b, int off, int len) throws IOException {
    return readAsset((long) asset, b, off, len);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  abstract protected int readAsset(long asset, byte[] bArray, int off, int len) throws IOException;

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final long seekAsset(int asset, long offset, int whence) {
    return seekAsset((long) asset, offset, whence);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  abstract protected long seekAsset(long asset, long offset, int whence);

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final long getAssetLength(int asset) {
    return getAssetLength((long) asset);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  abstract protected long getAssetLength(long asset);

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final long getAssetRemainingLength(int asset) {
    return getAssetRemainingLength((long) asset);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  abstract protected long getAssetRemainingLength(long assetHandle);

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final void destroyAsset(int asset) {
    destroyAsset((long) asset);
  }

  
  @HiddenApi @Implementation(minSdk = VERSION_CODES.P)
  abstract public void setApkAssets(Object apkAssetsObjects, Object invalidateCaches);
  

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  abstract protected void destroyAsset(long asset);

  @HiddenApi @Implementation
  abstract public int addAssetPath(String path);

  @HiddenApi @Implementation(maxSdk = VERSION_CODES.M)
  final protected int addAssetPathNative(String path) {
    return addAssetPathNative(path, false);
  }

  @HiddenApi @Implementation(minSdk = VERSION_CODES.N)
  protected abstract int addAssetPathNative(String path, boolean appAsLib);

  @HiddenApi @Implementation
  abstract public boolean isUpToDate();

  @HiddenApi @Implementation
  abstract public void setLocale(String locale);

  @Implementation
  abstract public String[] getLocales();

  @HiddenApi @Implementation(maxSdk = N_MR1)
  final public void setConfiguration(int mcc, int mnc, String locale,
      int orientation, int touchscreen, int density, int keyboard,
      int keyboardHidden, int navigation, int screenWidth, int screenHeight,
      int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
      int screenLayout, int uiMode, int sdkVersion) {
    setConfiguration(mcc, mnc, locale,
        orientation, touchscreen, density, keyboard,
        keyboardHidden, navigation, screenWidth, screenHeight,
        smallestScreenWidthDp, screenWidthDp, screenHeightDp,
        screenLayout, uiMode, 0, sdkVersion);
  }

  @HiddenApi @Implementation(minSdk = VERSION_CODES.O)
  abstract public void setConfiguration(int mcc, int mnc, String locale,
      int orientation, int touchscreen, int density, int keyboard,
      int keyboardHidden, int navigation, int screenWidth, int screenHeight,
      int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
      int screenLayout, int uiMode, int colorMode, int majorVersion);

  @HiddenApi @Implementation
  abstract public int[] getArrayIntResource(int resId);

  @HiddenApi @Implementation
  abstract protected String[] getArrayStringResource(int arrayResId);

  
  @HiddenApi @Implementation(minSdk = Build.VERSION_CODES.P)
  protected int[] getResourceIntArray(int resId) {
    return getArrayIntResource(resId);
  }
  

  @HiddenApi @Implementation
  abstract protected int[] getArrayStringInfo(int arrayResId);

  @HiddenApi @Implementation
  protected final int[] getStyleAttributes(int themeRes) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi @Implementation
  abstract protected Number newTheme();

  @HiddenApi @Implementation
  abstract public Number createTheme();

  @HiddenApi @Implementation
  protected static void dumpTheme(long theme, int priority, String tag, String prefix) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  abstract public void releaseTheme(int themePtr);

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  abstract public void releaseTheme(long themePtr);

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final void deleteTheme(int theme) {
    deleteTheme((long) theme);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  abstract protected void deleteTheme(long theme);

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public static void applyThemeStyle(int themePtr, int styleRes, boolean force) {
    applyThemeStyle((long) themePtr, styleRes, force);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  public static void applyThemeStyle(long themePtr, int styleRes, boolean force) {
    if (useLegacy()) {
      ShadowLegacyAssetManager.applyThemeStyle(themePtr, styleRes, force);
    } else {
      ShadowArscAssetManager.applyThemeStyle(themePtr, styleRes, force);
    }
  }

  
  @HiddenApi @Implementation(minSdk = VERSION_CODES.P)
  protected void applyStyleToTheme(long themePtr, int resId, boolean force) {
    applyThemeStyle(themePtr, resId, force);
  }
  

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public static void copyTheme(int destPtr, int sourcePtr) {
    copyTheme((long) destPtr, (long) sourcePtr);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  public static void copyTheme(long destPtr, long sourcePtr) {
    if (useLegacy()) {
      ShadowLegacyAssetManager.copyTheme(destPtr, sourcePtr);
    } else {
      ShadowArscAssetManager.copyTheme(destPtr, sourcePtr);
    }
  }

  
  @HiddenApi @Implementation(minSdk = VERSION_CODES.P)
  protected static void nativeThemeCopy(long destPtr, long sourcePtr) {
    copyTheme(destPtr, sourcePtr);
  }
  

  @HiddenApi @Implementation(maxSdk = VERSION_CODES.LOLLIPOP)
  protected static boolean applyStyle(int themeToken, int defStyleAttr, int defStyleRes,
      int xmlParserToken, int[] attrs, int[] outValues, int[] outIndices) {
    return applyStyle((long)themeToken, defStyleAttr, defStyleRes, (long)xmlParserToken, attrs,
        outValues, outIndices);
  }

  @HiddenApi @Implementation(maxSdk = N_MR1)
  protected static boolean applyStyle(long themeToken, int defStyleAttr, int defStyleRes,
      long xmlParserToken, int[] attrs, int[] outValues, int[] outIndices) {
    if (useLegacy()) {
      return ShadowLegacyAssetManager
          .applyStyle(themeToken, defStyleAttr, defStyleRes, xmlParserToken, attrs, outValues,
              outIndices);
    } else {
      return ShadowArscAssetManager
          .applyStyle(themeToken, defStyleAttr, defStyleRes, xmlParserToken, attrs, outValues,
              outIndices);
    }
  }

  @HiddenApi @Implementation(minSdk = VERSION_CODES.O)
  protected static boolean applyStyle(long themeToken, int defStyleAttr, int defStyleRes,
      long xmlParserToken, int[] inAttrs, int length, long outValuesAddress,
      long outIndicesAddress) {
    ShadowVMRuntime shadowVMRuntime = Shadow.extract(VMRuntime.getRuntime());
    int[] outValues = (int[])shadowVMRuntime.getObjectForAddress(outValuesAddress);
    int[] outIndices = (int[])shadowVMRuntime.getObjectForAddress(outIndicesAddress);
    return applyStyle(themeToken, defStyleAttr, defStyleRes, xmlParserToken, inAttrs,
        outValues, outIndices);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected static boolean resolveAttrs(long themeToken,
      int defStyleAttr, int defStyleRes, int[] inValues,
      int[] attrs, int[] outValues, int[] outIndices) {
    if (useLegacy()) {
      return ShadowLegacyAssetManager
          .resolveAttrs(themeToken, defStyleAttr, defStyleRes, inValues, attrs, outValues,
              outIndices);
    } else {
      return ShadowArscAssetManager
          .resolveAttrs(themeToken, defStyleAttr, defStyleRes, inValues, attrs, outValues,
              outIndices);
    }
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final boolean retrieveAttributes(
      int xmlParserToken, int[] attrs, int[] outValues, int[] outIndices) {
    return retrieveAttributes((long)xmlParserToken, attrs, outValues, outIndices);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  abstract protected boolean retrieveAttributes(
      long xmlParserToken, int[] attrs, int[] outValues, int[] outIndices);

  @Implementation
  abstract public String getResourceName(int resid);

  @Implementation
  abstract public String getResourcePackageName(int resid);

  @Implementation
  abstract public String getResourceTypeName(int resid);

  @Implementation
  abstract public String getResourceEntryName(int resid);

  @HiddenApi @Implementation
  abstract protected int getArraySize(int id);

  @Implementation @HiddenApi
  abstract protected int retrieveArray(int id, int[] outValues);

  @HiddenApi @Implementation
  abstract protected Number getNativeStringBlock(int block);

  @Implementation
  abstract public SparseArray<String> getAssignedPackageIdentifiers();

  @HiddenApi @Implementation
  abstract protected int loadResourceValue(int ident, short density, TypedValue outValue,
      boolean resolve);

  @Implementation @HiddenApi
  abstract protected int loadResourceBagValue(int ident, int bagEntryId, TypedValue outValue,
      boolean resolve);

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected static int loadThemeAttributeValue(int themeHandle, int ident,
      TypedValue outValue, boolean resolve) {
    return loadThemeAttributeValue((long) themeHandle, ident, outValue, resolve);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected static int loadThemeAttributeValue(long themeHandle, int ident,
      TypedValue outValue, boolean resolve) {
    if (useLegacy()) {
      return ShadowLegacyAssetManager
          .loadThemeAttributeValue(themeHandle, ident, outValue, resolve);
    } else {
      return ShadowArscAssetManager
          .loadThemeAttributeValue(themeHandle, ident, outValue, resolve);
    }
  }

  abstract Collection<FsFile> getAllAssetDirs();

  @Resetter
  public static void reset() {
    if (useLegacy()) {
      ShadowLegacyAssetManager.reset();
    } else {
      ShadowArscAssetManager.reset();
    }
  }
}
