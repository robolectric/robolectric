package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static org.robolectric.res.android.Asset.SEEK_CUR;
import static org.robolectric.res.android.Asset.SEEK_SET;
import static org.robolectric.res.android.Errors.BAD_INDEX;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Util.ALOGI;
import static org.robolectric.res.android.Util.ALOGV;
import static org.robolectric.res.android.Util.isTruthy;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.os.Build.VERSION_CODES;
import android.os.ParcelFileDescriptor;
import android.util.SparseArray;
import android.util.TypedValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.android.Asset;
import org.robolectric.res.android.Asset.AccessMode;
import org.robolectric.res.android.AssetDir;
import org.robolectric.res.android.AssetPath;
import org.robolectric.res.android.BagAttributeFinder;
import org.robolectric.res.android.CppAssetManager;
import org.robolectric.res.android.DataType;
import org.robolectric.res.android.DynamicRefTable;
import org.robolectric.res.android.Ref;
import org.robolectric.res.android.ResStringPool;
import org.robolectric.res.android.ResTable;
import org.robolectric.res.android.ResTable.ResourceName;
import org.robolectric.res.android.ResTable.bag_entry;
import org.robolectric.res.android.ResTableTheme;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.res.android.ResXMLParser;
import org.robolectric.res.android.ResXMLTree;
import org.robolectric.res.android.ResourceTypes.Res_value;
import org.robolectric.res.android.String8;
import org.robolectric.res.android.XmlAttributeFinder;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

// native method impls transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/core/jni/android_util_AssetManager.cpp
public class ShadowArscAssetManager extends ShadowAssetManager {

  private static final NativeObjRegistry<ResTableTheme> nativeThemeRegistry = new NativeObjRegistry<>();
  private static final NativeObjRegistry<Asset> nativeAssetRegistry = new NativeObjRegistry<>();

  private CppAssetManager cppAssetManager;
  private ResTable compileTimeResTable;

  @Override @Implementation
  protected void __constructor__() {
    invokeConstructor(AssetManager.class, realObject);
  }

  @Override @Implementation
  protected void __constructor__(boolean isSystem) {
    invokeConstructor(AssetManager.class, realObject,
        ClassParameter.from(boolean.class, isSystem));
  }

  public static void reset() {
    ReflectionHelpers.setStaticField(AssetManager.class, "sSystem", null);
    // nativeThemeRegistry.clear();
    // nativeXMLParserRegistry.clear(); // todo: shouldn't these be freed explicitly? [yes! xw]
    // nativeAssetRegistry.clear();
  }

  @Override @HiddenApi @Implementation
  public CharSequence getResourceText(int ident) {
    return directlyOn(realObject, AssetManager.class, "getResourceText",
        ClassParameter.from(int.class, ident));
  }

  @Override @HiddenApi @Implementation
  public CharSequence getResourceBagText(int ident, int bagEntryId) {
    return directlyOn(realObject, AssetManager.class, "getResourceBagText",
        ClassParameter.from(int.class, ident),
        ClassParameter.from(int.class, bagEntryId));
  }

  @Override @HiddenApi @Implementation
  public String[] getResourceStringArray(final int id) {
    return directlyOn(realObject, AssetManager.class, "getResourceStringArray",
        ClassParameter.from(int.class, id));
  }

  @Override @HiddenApi @Implementation
  public boolean getResourceValue(int ident, int density, TypedValue outValue,
      boolean resolveRefs) {
    return directlyOn(realObject, AssetManager.class, "getResourceValue",
        ClassParameter.from(int.class, ident),
        ClassParameter.from(int.class, density),
        ClassParameter.from(TypedValue.class, outValue),
        ClassParameter.from(boolean.class, resolveRefs));
  }

  @Override @HiddenApi @Implementation
  public CharSequence[] getResourceTextArray(int resId) {
    return directlyOn(realObject, AssetManager.class, "getResourceTextArray",
        ClassParameter.from(int.class, resId));
  }

  @Override @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public boolean getThemeValue(int themePtr, int ident, TypedValue outValue, boolean resolveRefs) {
    return directlyOn(realObject, AssetManager.class, "getThemeValue",
        ClassParameter.from(int.class, themePtr),
        ClassParameter.from(int.class, ident),
        ClassParameter.from(TypedValue.class, outValue),
        ClassParameter.from(boolean.class, resolveRefs));
  }

  @Override @HiddenApi @Implementation(minSdk = LOLLIPOP)
  public boolean getThemeValue(long themePtr, int ident, TypedValue outValue, boolean resolveRefs) {
    return directlyOn(realObject, AssetManager.class, "getThemeValue",
        ClassParameter.from(long.class, themePtr),
        ClassParameter.from(int.class, ident),
        ClassParameter.from(TypedValue.class, outValue),
        ClassParameter.from(boolean.class, resolveRefs));
  }

  @Override @HiddenApi @Implementation
  protected Object ensureStringBlocks() {
    return directlyOn(realObject, AssetManager.class, "ensureStringBlocks");
  }

  @Override @Implementation
  public final InputStream open(String fileName) throws IOException {
    return directlyOn(realObject, AssetManager.class).open(fileName);
  }

  @Override @Implementation
  public final InputStream open(String fileName, int accessMode) throws IOException {
    return directlyOn(realObject, AssetManager.class).open(fileName, accessMode);
  }

  @Override @Implementation
  public final AssetFileDescriptor openFd(String fileName) throws IOException {
    try {
      return directlyOn(realObject, AssetManager.class).openFd(fileName);
    } catch (RuntimeException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      } else {
        throw e;
      }
    }
  }

  @Override @Implementation
  public final String[] list(String path) throws IOException {
    CppAssetManager am = assetManagerForJavaObject();

    String fileName8 = path;
    if (fileName8 == null) {
      return null;
    }

    AssetDir dir = am.openDir(fileName8);

    if (dir == null) {
      throw new FileNotFoundException(fileName8);
    }


    int N = dir.getFileCount();

    String[] array = new String[dir.getFileCount()];

    for (int i=0; i<N; i++) {
        String8 name = dir.getFileName(i);
        array[i] = name.string();
    }

    return array;
  }


  @Override @HiddenApi @Implementation
  public final InputStream openNonAsset(int cookie, String fileName, int accessMode)
      throws IOException {
    return directlyOn(realObject, AssetManager.class, "openNonAsset",
        ClassParameter.from(int.class, cookie),
        ClassParameter.from(String.class, fileName),
        ClassParameter.from(int.class, accessMode));
  }

  @Override @HiddenApi @Implementation
  public final AssetFileDescriptor openNonAssetFd(int cookie, String fileName) throws IOException {
    return directlyOn(realObject, AssetManager.class, "openNonAssetFd",
        ClassParameter.from(int.class, cookie),
        ClassParameter.from(String.class, fileName));
  }

  @Override @Implementation
  public final XmlResourceParser openXmlResourceParser(int cookie, String fileName)
      throws IOException {
    return directlyOn(realObject, AssetManager.class).openXmlResourceParser(cookie, fileName);
  }

  
  @Override @HiddenApi @Implementation(minSdk = VERSION_CODES.P)
  public void setApkAssets(Object apkAssetsObjects, Object invalidateCaches) {
    throw new UnsupportedOperationException("implement me");
  }
  

  @Override @HiddenApi @Implementation
  public int addAssetPath(String path) {
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.JELLY_BEAN_MR1) {
      return addAssetPathNative(path);
    } else {
      return directlyOn(realObject, AssetManager.class, "addAssetPath",
          ClassParameter.from(String.class, path));
    }
  }

  @Override @HiddenApi @Implementation
  public boolean isUpToDate() {
    return directlyOn(realObject, AssetManager.class, "isUpToDate");
  }

  @Override @HiddenApi @Implementation
  public void setLocale(String locale) {
    directlyOn(realObject, AssetManager.class, "setLocale",
        ClassParameter.from(String.class, locale));
  }

  @Override @Implementation
  public String[] getLocales() {
    return directlyOn(realObject, AssetManager.class).getLocales();
  }

  @Override @HiddenApi @Implementation(minSdk = O)
  public void setConfiguration(int mcc, int mnc, String locale,
      int orientation, int touchscreen, int density, int keyboard,
      int keyboardHidden, int navigation, int screenWidth, int screenHeight,
      int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
      int screenLayout, int uiMode, int colorMode, int sdkVersion) {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return;
    }

    ResTable_config config = new ResTable_config();
//    memset(&config, 0, sizeof(config));

//    const char* locale8 = locale != NULL ? env->GetStringUTFChars(locale, NULL) : NULL;

    // Constants duplicated from Java class android.content.res.Configuration.
    int kScreenLayoutRoundMask = 0x300;
    int kScreenLayoutRoundShift = 8;

    config.mcc = mcc;
    config.mnc = mnc;
    config.orientation = orientation;
    config.touchscreen = touchscreen;
    config.density = density;
    config.keyboard = keyboard;
    config.inputFlags = keyboardHidden;
    config.navigation = navigation;
    config.screenWidth = screenWidth;
    config.screenHeight = screenHeight;
    config.smallestScreenWidthDp = smallestScreenWidthDp;
    config.screenWidthDp = screenWidthDp;
    config.screenHeightDp = screenHeightDp;
    config.screenLayout = screenLayout;
    config.uiMode = uiMode;
    config.colorMode = (byte) colorMode;
    config.sdkVersion = sdkVersion;
    config.minorVersion = 0;

    // In Java, we use a 32bit integer for screenLayout, while we only use an 8bit integer
    // in C++. We must extract the round qualifier out of the Java screenLayout and put it
    // into screenLayout2.
    config.screenLayout2 =
        (byte) ((screenLayout & kScreenLayoutRoundMask) >> kScreenLayoutRoundShift);

    am.setConfiguration(config, locale);

//    if (locale != null) env->ReleaseStringUTFChars(locale, locale8);
  }

  @Override @HiddenApi @Implementation
  public Number createTheme() {
    return directlyOn(realObject, AssetManager.class, "createTheme");
  }

  @Override @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public void releaseTheme(int themePtr) {
    directlyOn(realObject, AssetManager.class, "releaseTheme",
        ClassParameter.from(int.class, themePtr));
  }

  @Override @HiddenApi @Implementation(minSdk = LOLLIPOP)
  public void releaseTheme(long themePtr) {
    directlyOn(realObject, AssetManager.class, "releaseTheme",
        ClassParameter.from(long.class, themePtr));
  }

  @Override @Implementation
  public String getResourceName(int resid) {
    CppAssetManager am = assetManagerForJavaObject();

    ResourceName name = new ResourceName();
    if (!am.getResources().getResourceName(resid, true, name)) {
      return null;
    }

    StringBuilder str = new StringBuilder();
    if (name.packageName != null) {
      str.append(name.packageName.trim());
    }
    if (name.type != null) {
      if (str.length() > 0) {
        char div = ':';
        str.append(div);
      }
      str.append(name.type);
    }
    if (name.name != null) {
      if (str.length() > 0) {
        char div = '/';
        str.append(div);
      }
      str.append(name.name);
    }
    return str.toString();
  }

  @Override @Implementation
  public String getResourcePackageName(int resid) {
    CppAssetManager cppAssetManager = assetManagerForJavaObject();

    ResourceName name = new ResourceName();
    if (!cppAssetManager.getResources().getResourceName(resid, true, name)) {
      return null;
    }

    return name.packageName.trim();
  }

  @Override @Implementation
  public String getResourceTypeName(int resid) {
    CppAssetManager cppAssetManager = assetManagerForJavaObject();

    ResourceName name = new ResourceName();
    if (!cppAssetManager.getResources().getResourceName(resid, true, name)) {
      return null;
    }

    return name.type;
  }

  @Override @Implementation
  public String getResourceEntryName(int resid) {
    CppAssetManager cppAssetManager = assetManagerForJavaObject();

    ResourceName name = new ResourceName();
    if (!cppAssetManager.getResources().getResourceName(resid, true, name)) {
      return null;
    }

    return name.name;
  }

  //////////// native method implementations

  private static final boolean kThrowOnBadId = false;
  private static final boolean kDebugStyles = false;

//  public native final String[] list(String path)
//      throws IOException;


  @Override @HiddenApi @Implementation(minSdk = VERSION_CODES.N)
  protected int addAssetPathNative(String path, boolean appAsLib) {
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

  @Override @HiddenApi @Implementation
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

  @Override @HiddenApi @Implementation
  protected final Number openAsset(String fileName, int mode) throws FileNotFoundException {
    CppAssetManager am = assetManagerForJavaObject();

    ALOGV("openAsset in %s", am);

    String fileName8 = fileName;
    if (fileName8 == null) {
      throw new IllegalArgumentException("Empty file name");
    }

    if (mode != AccessMode.ACCESS_UNKNOWN.mode() && mode != AccessMode.ACCESS_RANDOM.mode()
        && mode != AccessMode.ACCESS_STREAMING.mode() && mode != AccessMode.ACCESS_BUFFER.mode()) {
      throw new IllegalArgumentException("Bad access mode");
    }

    Asset a = am.open(fileName8, AccessMode.fromInt(mode));

    if (a == null) {
      throw new FileNotFoundException(fileName8);
    }

    //printf("Created Asset Stream: %p\n", a);

    return RuntimeEnvironment.castNativePtr(nativeAssetRegistry.getNativeObjectId(a));
  }

  @Override @HiddenApi @Implementation
  protected ParcelFileDescriptor openAssetFd(String fileName, long[] outOffsets) throws IOException {
    CppAssetManager am = assetManagerForJavaObject();

    ALOGV("openAssetFd in %s", am);

    String fileName8 = fileName;
    if (fileName8 == null) {
      return null;
    }

    Asset a = am.open(fileName8, Asset.AccessMode.ACCESS_RANDOM);

    if (a == null) {
      throw new FileNotFoundException(fileName8);
    }

    return returnParcelFileDescriptor(a, outOffsets);
  }

  @Override @HiddenApi @Implementation
  protected final Number openNonAssetNative(int cookie, String fileName,
      int accessMode) throws FileNotFoundException {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return RuntimeEnvironment.castNativePtr(0);
    }
    ALOGV("openNonAssetNative in %s (Java object %s)\n", am, AssetManager.class);
    String fileName8 = fileName;
    if (fileName8 == null) {
      return RuntimeEnvironment.castNativePtr(-1);
    }
    AccessMode mode = AccessMode.fromInt(accessMode);
    if (mode != Asset.AccessMode.ACCESS_UNKNOWN && mode != Asset.AccessMode.ACCESS_RANDOM
        && mode != Asset.AccessMode.ACCESS_STREAMING && mode != Asset.AccessMode.ACCESS_BUFFER) {
      throw new IllegalArgumentException("Bad access mode");
    }
    Asset a = isTruthy(cookie)
        ? am.openNonAsset(cookie, fileName8, mode)
        : am.openNonAsset(fileName8, mode, null);
    if (a == null) {
      throw new FileNotFoundException(fileName8);
    }
    long assetId = nativeAssetRegistry.getNativeObjectId(a);
    // todo: something better than this [xw]
    a.onClose = () -> destroyAsset(assetId);
    //printf("Created Asset Stream: %p\n", a);
    return RuntimeEnvironment.castNativePtr(assetId);
  }

  @Override @HiddenApi @Implementation
  protected ParcelFileDescriptor openNonAssetFdNative(int cookie,
      String fileName, long[] outOffsets) throws IOException {
    CppAssetManager am = assetManagerForJavaObject();

    ALOGV("openNonAssetFd in %s (Java object %s)", am, this);

    if (fileName == null) {
      return null;
    }

    Asset a = isTruthy(cookie)
        ? am.openNonAsset(cookie, fileName, Asset.AccessMode.ACCESS_RANDOM)
        : am.openNonAsset(fileName, Asset.AccessMode.ACCESS_RANDOM, null);

    if (a == null) {
      throw new FileNotFoundException(fileName);
    }

    //printf("Created Asset Stream: %p\n", a);

    return returnParcelFileDescriptor(a, outOffsets);
  }

  @Override @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final void destroyAsset(long asset) {
    nativeAssetRegistry.unregister(asset);
  }

  @Override @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final int readAssetChar(long asset) {
    Asset a = getAsset(asset);
    byte[] b = new byte[1];
    int res = a.read(b, 1);
    return res == 1 ? b[0] & 0xff : -1;
  }

  @Override @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final int readAsset(long asset, byte[] bArray, int off, int len) throws IOException {
    Asset a = getAsset(asset);

    if (a == null || bArray == null) {
      throw new NullPointerException("asset");
    }

    if (len == 0) {
      return 0;
    }

    int bLen = bArray.length;
    if (off < 0 || off >= bLen || len < 0 || len > bLen || (off+len) > bLen) {
      throw new IndexOutOfBoundsException();
    }

    byte[] b = bArray;
    int res = a.read(b, off, len);

    if (res > 0) return res;

    if (res < 0) {
      throw new IOException();
    }
    return -1;
  }

  @Override @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final long seekAsset(long asset, long offset, int whence) {
    Asset a = getAsset(asset);
    return a.seek(offset, whence < 0 ? SEEK_SET : SEEK_CUR);
  }

  @Override @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final long getAssetLength(long asset) {
    Asset a = getAsset(asset);
    return a.getLength();
  }

  @Override @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final long getAssetRemainingLength(long assetHandle) {
    Asset a = getAsset(assetHandle);

    if (a == null) {
      throw new NullPointerException("asset");
    }

    return a.getRemainingLength();
  }

  private Asset getAsset(long asset) {
    return nativeAssetRegistry.getNativeObject(asset);
  }

  @Override @HiddenApi @Implementation
  protected int loadResourceValue(int ident, short density, TypedValue outValue, boolean resolve) {
    if (outValue == null) {
      throw new NullPointerException("outValue");
      //return 0;
    }
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0;
    }
    final ResTable res = am.getResources();

    Ref<Res_value> value = new Ref<>(null);
    Ref<ResTable_config> config = new Ref<>(null);
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
        block = res.resolveReference(value, block, ref, typeSpecFlags, config);
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

  private static int copyValue(TypedValue outValue, ResTable table,  Res_value value, int ref, int block,
      int typeSpecFlags) {
    return copyValue(outValue, table, value, ref, block, typeSpecFlags, null);
  }

  private static int copyValue(TypedValue outValue, ResTable table,  Res_value value, int ref, int block,
      int typeSpecFlags, ResTable_config config) {
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

  public static Map<String, Integer> getResourceBagValues(int ident, ResTable res) {
    // Now lock down the resource object and start pulling stuff from it.
    res.lock();

    HashMap<String, Integer> map;
    try {
      Ref<bag_entry[]> entryRef = new Ref<>(null);
      Ref<Integer> typeSpecFlags = new Ref<>(0);
      int entryCount = res.getBagLocked(ident, entryRef, typeSpecFlags);

      map = new HashMap<>();
      bag_entry[] bag_entries = entryRef.get();
      for (int i=0; i < entryCount; i++) {
        bag_entry entry = bag_entries[i];
        ResourceName resourceName = new ResourceName();
        if (res.getResourceName(entry.map.name.ident, true, resourceName)) {
          map.put(resourceName.name, entry.map.value.data);
        }
      }
    } finally {
      res.unlock();
    }

    return map;
  }

  /**
   * Returns true if the resource was found, filling in mRetStringBlock and
   * mRetData.
   */
  @Override @Implementation @HiddenApi
  protected final int loadResourceBagValue(int ident, int bagEntryId, TypedValue outValue,
      boolean resolve) {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0;
    }
    final ResTable res = am.getResources();
    return loadResourceBagValueInternal(ident, bagEntryId, outValue, resolve, res);
  }

  public static String getResourceBagValue(int ident, int bagEntryId, ResTable resTable) {
    TypedValue outValue = new TypedValue();
    int blockId = ShadowArscAssetManager
        .loadResourceBagValueInternal(ident, bagEntryId, outValue, true, resTable);
    if (outValue.type == TypedValue.TYPE_STRING) {
      return resTable.getTableStringBlock(blockId).stringAt(outValue.data);
    } else {
      return outValue.coerceToString().toString();
    }
  }

  private static int loadResourceBagValueInternal(int ident, int bagEntryId, TypedValue outValue,
      boolean resolve, ResTable res) {
    // Now lock down the resource object and start pulling stuff from it.
    res.lock();

    int block = -1;
    Ref<Res_value> valueRef = new Ref<>(null);
    Ref<bag_entry[]> entryRef = new Ref<>(null);
    Ref<Integer> typeSpecFlags = new Ref<>(0);
    int entryCount = res.getBagLocked(ident, entryRef, typeSpecFlags);

    bag_entry[] bag_entries = entryRef.get();
    for (int i=0; i < entryCount; i++) {
      bag_entry entry = bag_entries[i];
      if (bagEntryId == entry.map.name.ident) {
        block = entry.stringBlock;
        valueRef.set(entry.map.value);
      }
    }

    res.unlock();

    if (block < 0) {
      return block;
    }

    Ref<Integer> ref = new Ref<Integer>(ident);
    if (resolve) {
      block = res.resolveReference(valueRef, block, ref, typeSpecFlags);
      if (kThrowOnBadId) {
        if (block == BAD_INDEX) {
          throw new IllegalStateException("Bad resource!");
        }
      }
    }
    if (block >= 0) {
      return copyValue(outValue, res, valueRef.get(), ref.get(), block, typeSpecFlags.get());
    }

    return block;
  }

  /*package*/ static final int STYLE_NUM_ENTRIES = 6;
  /*package*/ static final int STYLE_TYPE = 0;
  /*package*/ static final int STYLE_DATA = 1;
  /*package*/ static final int STYLE_ASSET_COOKIE = 2;
  /*package*/ static final int STYLE_RESOURCE_ID = 3;

  /* Offset within typed data array for native changingConfigurations. */
  static final int STYLE_CHANGING_CONFIGURATIONS = 4;

  /*package*/ static final int STYLE_DENSITY = 5;

/* lowercase hexadecimal notation.  */
//# define PRIx8		"x"
//      # define PRIx16		"x"
//      # define PRIx32		"x"
      private static final String PRIx64 =  "x";

  @HiddenApi @Implementation(maxSdk = N_MR1)
  protected static boolean applyStyle(long themeToken, int defStyleAttr, int defStyleRes,
      long xmlParserToken, int[] attrs, int[] outValues, int[] outIndices) {
    if (themeToken == 0) {
      throw new NullPointerException("theme token");
    }
    if (attrs == null) {
      throw new NullPointerException("attrs");
    }
    if (outValues == null) {
      throw new NullPointerException("outValues");
    }

    if (kDebugStyles) {
      ALOGI("APPLY STYLE: theme=0x%"+ PRIx64 + " defStyleAttr=0x%x defStyleRes=0x%x "+
              "xml=0x%" + PRIx64, themeToken, defStyleAttr, defStyleRes,
          xmlParserToken);
    }

    ResTableTheme theme = nativeThemeRegistry.getNativeObject(themeToken);
    final ResTable res = theme.getResTable();
    ResXMLParser xmlParser = xmlParserToken == 0
        ? null
        : ShadowXmlBlock.NATIVE_RES_XML_PARSERS.getNativeObject(xmlParserToken);
    Ref<ResTable_config> config = new Ref<>(new ResTable_config());
    Ref<Res_value> value = new Ref<>(new Res_value());

    final int NI = attrs.length;
    final int NV = outValues.length;
    if (NV < (NI*STYLE_NUM_ENTRIES)) {
      throw new IndexOutOfBoundsException("out values too small");
    }

    int[] src = attrs;
//    if (src == null) {
//      return false;
//    }

    int[] baseDest = outValues;
    int[] dest = baseDest;
//    if (dest == null) {
//      return false;
//    }

    int[] indices = null;
    int indicesIdx = 0;
    if (outIndices != null) {
      if (outIndices.length > NI) {
        indices = outIndices;
      }
    }

    // Load default style from attribute, if specified...
    Ref<Integer> defStyleBagTypeSetFlags = new Ref<>(0);
    if (defStyleAttr != 0) {
      if (theme.getAttribute(defStyleAttr, value, defStyleBagTypeSetFlags) >= 0) {
        if (value.get().dataType == DataType.REFERENCE.code()) {
          defStyleRes = value.get().data;
        }
      }
    }

    // Retrieve the style class associated with the current XML tag.
    int style = 0;
    Ref<Integer> styleBagTypeSetFlags = new Ref<>(0);
    if (xmlParser != null) {
      int idx = xmlParser.indexOfStyle();
      if (idx >= 0 && xmlParser.getAttributeValue(idx, value) >= 0) {
        if (value.get().dataType == DataType.ATTRIBUTE.code()) {
          if (theme.getAttribute(value.get().data, value, styleBagTypeSetFlags) < 0) {
            value.set(value.get().withType(DataType.NULL.code()));
          }
        }
        if (value.get().dataType == DataType.REFERENCE.code()) {
          style = value.get().data;
        }
      }
    }

    // Now lock down the resource object and start pulling stuff from it.
    res.lock();

    // Retrieve the default style bag, if requested.
    final Ref<ResTable.bag_entry[]> defStyleAttrStart = new Ref<>(null);
    Ref<Integer> defStyleTypeSetFlags = new Ref<>(0);
    int bagOff = defStyleRes != 0
        ? res.getBagLocked(defStyleRes, defStyleAttrStart, defStyleTypeSetFlags) : -1;
    defStyleTypeSetFlags.set(defStyleTypeSetFlags.get() | defStyleBagTypeSetFlags.get());

    // TODO: Figure our how to deal with C++ iterators..........................................................................
    //final ResTable::bag_entry* defStyleAttrEnd = defStyleAttrStart + (bagOff >= 0 ? bagOff : 0);
    final ResTable.bag_entry defStyleAttrEnd = null;
    //BagAttributeFinder defStyleAttrFinder = new BagAttributeFinder(defStyleAttrStart, defStyleAttrEnd);
    BagAttributeFinder defStyleAttrFinder = new BagAttributeFinder(defStyleAttrStart.get(), bagOff);

    // Retrieve the style class bag, if requested.
    final Ref<ResTable.bag_entry[]> styleAttrStart = new Ref<>(null);
    Ref<Integer> styleTypeSetFlags = new Ref<>(0);
    bagOff = style != 0 ? res.getBagLocked(style, styleAttrStart, styleTypeSetFlags) : -1;
    styleTypeSetFlags.set(styleTypeSetFlags.get() | styleBagTypeSetFlags.get());

    // TODO: Figure our how to deal with C++ iterators..........................................................................
    //final ResTable::bag_entry* final styleAttrEnd = styleAttrStart + (bagOff >= 0 ? bagOff : 0);
    final ResTable.bag_entry styleAttrEnd = null;
    //BagAttributeFinder styleAttrFinder = new BagAttributeFinder(styleAttrStart, styleAttrEnd);
    BagAttributeFinder styleAttrFinder = new BagAttributeFinder(styleAttrStart.get(), bagOff);

    // Retrieve the XML attributes, if requested.
    final int kXmlBlock = 0x10000000;
    XmlAttributeFinder xmlAttrFinder = new XmlAttributeFinder(xmlParser);
    final int xmlAttrEnd = xmlParser != null ? xmlParser.getAttributeCount() : 0;

    // Now iterate through all of the attributes that the client has requested,
    // filling in each with whatever data we can find.
    int block = 0;
    Ref<Integer> typeSetFlags;
    for (int ii = 0; ii < NI; ii++) {
      final int curIdent = (int)src[ii];

      if (kDebugStyles) {
        ALOGI("RETRIEVING ATTR 0x%08x...", curIdent);
      }

      // Try to find a value for this attribute...  we prioritize values
      // coming from, first XML attributes, then XML style, then default
      // style, and finally the theme.
      value.set(Res_value.NULL_VALUE);
      typeSetFlags = new Ref<>(0);
      config.get().density = 0;

      // Walk through the xml attributes looking for the requested attribute.
      final int xmlAttrIdx = xmlAttrFinder.find(curIdent);
      if (xmlAttrIdx != xmlAttrEnd) {
        // We found the attribute we were looking for.
        block = kXmlBlock;
        xmlParser.getAttributeValue(xmlAttrIdx, value);
        if (kDebugStyles) {
          ALOGI(". From XML: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
        }
      }

      if (value.get().dataType == DataType.NULL.code()) {
        // Walk through the style class values looking for the requested attribute.
        final ResTable.bag_entry styleAttrEntry = styleAttrFinder.find(curIdent);
        if (styleAttrEntry != styleAttrEnd) {
          // We found the attribute we were looking for.
          block = styleAttrEntry.stringBlock;
          typeSetFlags.set(styleTypeSetFlags.get());
          value.set(styleAttrEntry.map.value);
          if (kDebugStyles) {
            ALOGI(". From style: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
          }
        }
      }

      if (value.get().dataType == DataType.NULL.code()) {
        // Walk through the default style values looking for the requested attribute.
        final ResTable.bag_entry defStyleAttrEntry = defStyleAttrFinder.find(curIdent);
        if (defStyleAttrEntry != defStyleAttrEnd) {
          // We found the attribute we were looking for.
          block = defStyleAttrEntry.stringBlock;
          typeSetFlags.set(styleTypeSetFlags.get());
          value.set(defStyleAttrEntry.map.value);
          if (kDebugStyles) {
            ALOGI(". From def style: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
          }
        }
      }

      Ref<Integer> resid = new Ref<>(0);
      if (value.get().dataType != DataType.NULL.code()) {
        // Take care of resolving the found resource to its final value.
        int newBlock = theme.resolveAttributeReference(value, block,
            resid, typeSetFlags, config);
        if (newBlock >= 0) {
          block = newBlock;
        }

        if (kDebugStyles) {
          ALOGI(". Resolved attr: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
        }
      } else {
        // If we still don't have a value for this attribute, try to find
        // it in the theme!
        int newBlock = theme.getAttribute(curIdent, value, typeSetFlags);
        if (newBlock >= 0) {
          if (kDebugStyles) {
            ALOGI(". From theme: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
          }
          // TODO: platform code passes in 'block' here, which seems incorrect as it can be not set yet
          // ... how does this work in AOSP?
          newBlock = res.resolveReference(value, newBlock, resid,
              typeSetFlags, config);
          if (kThrowOnBadId) {
            if (newBlock == BAD_INDEX) {
              throw new IllegalStateException("Bad resource!");
            }
          }

          if (newBlock >= 0) {
            block = newBlock;
          }

          if (kDebugStyles) {
            ALOGI(". Resolved theme: type=0x%x, data=0x%08x", value.get().dataType, value.get().data);
          }
        }
      }

      // Deal with the special @null value -- it turns back to TYPE_NULL.
      if (value.get().dataType == DataType.REFERENCE.code() && value.get().data == 0) {
        if (kDebugStyles) {
          ALOGI(". Setting to @null!");
        }
        value.set(Res_value.NULL_VALUE);
        block = kXmlBlock;
      }

      if (kDebugStyles) {
        ALOGI("Attribute 0x%08x: type=0x%x, data=0x%08x", curIdent, value.get().dataType, value.get().data);
      }

      // Write the final value back to Java.
      int destIndex = ii * STYLE_NUM_ENTRIES;
      Res_value res_value = value.get();
      dest[destIndex + STYLE_TYPE] = res_value.dataType;
      dest[destIndex + STYLE_DATA] = res_value.data;
      dest[destIndex + STYLE_ASSET_COOKIE] = block != kXmlBlock ?
          res.getTableCookie(block) : -1;
      dest[destIndex + STYLE_RESOURCE_ID] = resid.get();
      dest[destIndex + STYLE_CHANGING_CONFIGURATIONS] = typeSetFlags.get();
      dest[destIndex + STYLE_DENSITY] = config.get().density;

      if (indices != null && res_value.dataType != DataType.NULL.code()) {
        indicesIdx++;
        indices[indicesIdx] = ii;
      }

      if (res_value.dataType == DataType.ATTRIBUTE.code()) {
        ResourceName attrName = new ResourceName();
        ResourceName attrRefName = new ResourceName();
        boolean gotName = res.getResourceName(curIdent, true, attrName);
        boolean gotRefName = res.getResourceName(res_value.data, true, attrRefName);
        Logger.warn(
            "Failed to resolve attribute lookup: %s=\"?%s\"; theme: %s",
            gotName ? attrName : "unknown", gotRefName ? attrRefName : "unknown",
            theme);
      }

      //dest += STYLE_NUM_ENTRIES;
    }

    res.unlock();

    if (indices != null) {
      indices[0] = indicesIdx;
//      env.ReleasePrimitiveArrayCritical(outIndices, indices, 0);
    }
//    env.ReleasePrimitiveArrayCritical(outValues, baseDest, 0);
//    env.ReleasePrimitiveArrayCritical(attrs, src, 0);

    return true;

  }

  @Implementation @HiddenApi
  protected static boolean resolveAttrs(long themeToken,
      int defStyleAttr, int defStyleRes, int[] inValues,
      int[] attrs, int[] outValues, int[] outIndices) {
    if (themeToken == 0) {
      throw new NullPointerException("theme token");
    }
    if (attrs == null) {
      throw new NullPointerException("attrs");
    }
    if (outValues == null) {
      throw new NullPointerException("out values");
    }

    if (kDebugStyles) {
      ALOGI("APPLY STYLE: theme=0x%" + PRIx64 + " defStyleAttr=0x%x " +
          "defStyleRes=0x%x", themeToken, defStyleAttr, defStyleRes);
    }

    ResTableTheme theme = nativeThemeRegistry.getNativeObject(themeToken);
    final ResTable res = theme.getResTable();
    ResTable_config config = new ResTable_config();
    Res_value value;

    final int NI = attrs.length;
    final int NV = outValues.length;
    if (NV < (NI*STYLE_NUM_ENTRIES)) {
      throw new IndexOutOfBoundsException("out values too small");
    }

    int[] src = attrs;
//    if (src == null) {
//      return JNI_FALSE;
//    }

    int[] srcValues = inValues;
    final int NSV = srcValues == null ? 0 : inValues.length;

    int[] baseDest = outValues;
    int[] dest = baseDest;
    int destOffset = 0;
    if (dest == null) {
      return false;
    }

    int[] indices = null;
    int indicesIdx = 0;
    if (outIndices != null) {
      if (outIndices.length > NI) {
        indices = outIndices;
      }
    }

    // Load default style from attribute, if specified...
    Ref<Integer> defStyleBagTypeSetFlags = new Ref<>(0);
    if (defStyleAttr != 0) {
      Ref<Res_value> valueRef = new Ref<>(null);
      if (theme.getAttribute(defStyleAttr, valueRef, defStyleBagTypeSetFlags) >= 0) {
        value = valueRef.get();
        if (value.dataType == Res_value.TYPE_REFERENCE) {
          defStyleRes = value.data;
        }
      }
    }

    // Now lock down the resource object and start pulling stuff from it.
    res.lock();

    // Retrieve the default style bag, if requested.
    final Ref<bag_entry[]> defStyleStart = new Ref<>(null);
    Ref<Integer> defStyleTypeSetFlags = new Ref<>(0);
    int bagOff = defStyleRes != 0
        ? res.getBagLocked(defStyleRes, defStyleStart, defStyleTypeSetFlags) : -1;
    defStyleTypeSetFlags.set(defStyleTypeSetFlags.get() | defStyleBagTypeSetFlags.get());
//    const ResTable::bag_entry* const defStyleEnd = defStyleStart + (bagOff >= 0 ? bagOff : 0);
    final int defStyleEnd = (bagOff >= 0 ? bagOff : 0);
    BagAttributeFinder defStyleAttrFinder = new BagAttributeFinder(defStyleStart.get(), defStyleEnd);

    // Now iterate through all of the attributes that the client has requested,
    // filling in each with whatever data we can find.
    int block = 0;
    int typeSetFlags;
    for (int ii=0; ii<NI; ii++) {
        final int curIdent = (int)src[ii];

      if (kDebugStyles) {
        ALOGI("RETRIEVING ATTR 0x%08x...", curIdent);
      }

      // Try to find a value for this attribute...  we prioritize values
      // coming from, first XML attributes, then XML style, then default
      // style, and finally the theme.
      value = Res_value.NULL_VALUE;
      typeSetFlags = 0;
      config.density = 0;

      // Retrieve the current input value if available.
      if (NSV > 0 && srcValues[ii] != 0) {
        block = -1;
        value = new Res_value((byte) Res_value.TYPE_ATTRIBUTE, srcValues[ii]);
        if (kDebugStyles) {
          ALOGI(". From values: type=0x%x, data=0x%08x", value.dataType, value.data);
        }
      }

      if (value.dataType == Res_value.TYPE_NULL) {
            final bag_entry defStyleEntry = defStyleAttrFinder.find(curIdent);
        if (defStyleEntry != null) {
          block = defStyleEntry.stringBlock;
          typeSetFlags = defStyleTypeSetFlags.get();
          value = defStyleEntry.map.value;
          if (kDebugStyles) {
            ALOGI(". From def style: type=0x%x, data=0x%08x", value.dataType, value.data);
          }
        }
      }

      int resid = 0;
      Ref<Res_value> valueRef = new Ref<>(value);
      Ref<Integer> residRef = new Ref<>(resid);
      Ref<Integer> typeSetFlagsRef = new Ref<>(typeSetFlags);
      Ref<ResTable_config> configRef = new Ref<>(config);
      if (value.dataType != Res_value.TYPE_NULL) {
        // Take care of resolving the found resource to its final value.
        int newBlock = theme.resolveAttributeReference(valueRef, block,
                    residRef, typeSetFlagsRef, configRef);
        value = valueRef.get();
        resid = residRef.get();
        typeSetFlags = typeSetFlagsRef.get();
        config = configRef.get();
        if (newBlock >= 0) block = newBlock;
        if (kDebugStyles) {
          ALOGI(". Resolved attr: type=0x%x, data=0x%08x", value.dataType, value.data);
        }
      } else {
        // If we still don't have a value for this attribute, try to find
        // it in the theme!
        int newBlock = theme.getAttribute(curIdent, valueRef, typeSetFlagsRef);
        value = valueRef.get();
        typeSetFlags = typeSetFlagsRef.get();

        if (newBlock >= 0) {
          if (kDebugStyles) {
            ALOGI(". From theme: type=0x%x, data=0x%08x", value.dataType, value.data);
          }
          newBlock = res.resolveReference(valueRef, block, residRef,
                        typeSetFlagsRef, configRef);
          value = valueRef.get();
          resid = residRef.get();
          typeSetFlags = typeSetFlagsRef.get();
          config = configRef.get();
          if (kThrowOnBadId) {
            if (newBlock == BAD_INDEX) {
              throw new IllegalStateException("Bad resource!");
            }
          }
          if (newBlock >= 0) block = newBlock;
          if (kDebugStyles) {
            ALOGI(". Resolved theme: type=0x%x, data=0x%08x", value.dataType, value.data);
          }
        }
      }

      // Deal with the special @null value -- it turns back to TYPE_NULL.
      if (value.dataType == Res_value.TYPE_REFERENCE && value.data == 0) {
        if (kDebugStyles) {
          ALOGI(". Setting to @null!");
        }
        value = Res_value.NULL_VALUE;
        block = -1;
      }

      if (kDebugStyles) {
        ALOGI("Attribute 0x%08x: type=0x%x, data=0x%08x", curIdent, value.dataType,
            value.data);
      }

      // Write the final value back to Java.
      dest[destOffset + STYLE_TYPE] = value.dataType;
      dest[destOffset + STYLE_DATA] = value.data;
      dest[destOffset + STYLE_ASSET_COOKIE] =
          block != -1 ? res.getTableCookie(block) : -1;
      dest[destOffset + STYLE_RESOURCE_ID] = resid;
      dest[destOffset + STYLE_CHANGING_CONFIGURATIONS] = typeSetFlags;
      dest[destOffset + STYLE_DENSITY] = config.density;

      if (indices != null && value.dataType != Res_value.TYPE_NULL) {
        indicesIdx++;
        indices[indicesIdx] = ii;
      }

      destOffset += STYLE_NUM_ENTRIES;
    }

    res.unlock();

    if (indices != null) {
      indices[0] = indicesIdx;
//      env.ReleasePrimitiveArrayCritical(outIndices, indices, 0);
    }
//    env.ReleasePrimitiveArrayCritical(outValues, baseDest, 0);
//    env.ReleasePrimitiveArrayCritical(inValues, srcValues, 0);
//    env.ReleasePrimitiveArrayCritical(attrs, src, 0);

    return true;
  }

  @Override @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final boolean retrieveAttributes(
      long xmlParserToken, int[] attrs, int[] outValues, int[] outIndices) {
    if (xmlParserToken == 0) {
      throw new NullPointerException("xmlParserToken");
//      return JNI_FALSE;
    }
    if (attrs == null) {
      throw new NullPointerException("attrs");
//      return JNI_FALSE;
    }
    if (outValues == null) {
      throw new NullPointerException("out values");
//      return JNI_FALSE;
    }

    CppAssetManager am = assetManagerForJavaObject();
//    if (am == null) {
//      return JNI_FALSE;
//    }
    ResTable res = am.getResources();
//    ResXMLParser xmlParser = (ResXMLParser*)xmlParserToken;
    ResXMLParser xmlParser = ShadowXmlBlock.NATIVE_RES_XML_PARSERS.getNativeObject(xmlParserToken);

    Ref<ResTable_config> config = new Ref<>(new ResTable_config());
    Ref<Res_value> value = new Ref<>(new Res_value());

//    const int NI = env.GetArrayLength(attrs);
//    const int NV = env.GetArrayLength(outValues);
    final int NI = attrs.length;
    final int NV = outValues.length;
    if (NV < (NI*STYLE_NUM_ENTRIES)) {
      throw new IndexOutOfBoundsException("out values too small");
//      return JNI_FALSE;
    }

//    int[] src = (int[])env.GetPrimitiveArrayCritical(attrs, 0);
//    if (src == null) {
//      return JNI_FALSE;
//    }
    int[] src = attrs;

//    int[] baseDest = (int[])env.GetPrimitiveArrayCritical(outValues, 0);
    int[] baseDest = outValues;
    int[] dest = baseDest;
    int destOffset = 0;
    if (dest == null) {
//      env.ReleasePrimitiveArrayCritical(attrs, src, 0);
//      return JNI_FALSE;
      return false;
    }

    int[] indices = null;
    int indicesIdx = 0;
    if (outIndices != null) {
      if (outIndices.length > NI) {
//        indices = (int[])env.GetPrimitiveArrayCritical(outIndices, 0);
        indices = outIndices;
      }
    }

    // Now lock down the resource object and start pulling stuff from it.
    res.lock();

    // Retrieve the XML attributes, if requested.
    final int NX = xmlParser.getAttributeCount();
    int ix=0;
    int curXmlAttr = xmlParser.getAttributeNameResID(ix);

    final int kXmlBlock = 0x10000000;

    // Now iterate through all of the attributes that the client has requested,
    // filling in each with whatever data we can find.
    int block = 0;
    Ref<Integer> typeSetFlags = new Ref<>(0);
    for (int ii=0; ii<NI; ii++) {
      final int curIdent = (int)src[ii];

      // Try to find a value for this attribute...
      value.set(Res_value.NULL_VALUE);
      typeSetFlags.set(0);
      config.get().density = 0;

      // Skip through XML attributes until the end or the next possible match.
      while (ix < NX && curIdent > curXmlAttr) {
        ix++;
        curXmlAttr = xmlParser.getAttributeNameResID(ix);
      }
      // Retrieve the current XML attribute if it matches, and step to next.
      if (ix < NX && curIdent == curXmlAttr) {
        block = kXmlBlock;
        xmlParser.getAttributeValue(ix, value);
        ix++;
        curXmlAttr = xmlParser.getAttributeNameResID(ix);
      }

      //printf("Attribute 0x%08x: type=0x%x, data=0x%08x\n", curIdent, value.dataType, value.data);
      Ref<Integer> resid = new Ref<>(0);
      if (value.get().dataType != Res_value.TYPE_NULL) {
        // Take care of resolving the found resource to its final value.
        //printf("Resolving attribute reference\n");
        int newBlock = res.resolveReference(value, block, resid,
                    typeSetFlags, config);
        if (kThrowOnBadId) {
          if (newBlock == BAD_INDEX) {
            throw new IllegalStateException("Bad resource!");
//            return false;
          }
        }
        if (newBlock >= 0) block = newBlock;
      }

      // Deal with the special @null value -- it turns back to TYPE_NULL.
      if (value.get().dataType == Res_value.TYPE_REFERENCE && value.get().data == 0) {
        value.set(Res_value.NULL_VALUE);
      }

      //printf("Attribute 0x%08x: final type=0x%x, data=0x%08x\n", curIdent, value.dataType, value.data);

      // Write the final value back to Java.
      dest[destOffset + STYLE_TYPE] = value.get().dataType;
      dest[destOffset + STYLE_DATA] = value.get().data;
      dest[destOffset + STYLE_ASSET_COOKIE] =
          block != kXmlBlock ? res.getTableCookie(block) : -1;
      dest[destOffset + STYLE_RESOURCE_ID] = resid.get();
      dest[destOffset + STYLE_CHANGING_CONFIGURATIONS] = typeSetFlags.get();
      dest[destOffset + STYLE_DENSITY] = config.get().density;

      if (indices != null && value.get().dataType != Res_value.TYPE_NULL) {
        indicesIdx++;
        indices[indicesIdx] = ii;
      }

//      dest += STYLE_NUM_ENTRIES;
      destOffset += STYLE_NUM_ENTRIES;
    }

    res.unlock();

    if (indices != null) {
      indices[0] = indicesIdx;
//      env.ReleasePrimitiveArrayCritical(outIndices, indices, 0);
    }

//    env.ReleasePrimitiveArrayCritical(outValues, baseDest, 0);
//    env.ReleasePrimitiveArrayCritical(attrs, src, 0);

    return true;
  }

  @Override @HiddenApi @Implementation
  protected int getArraySize(int id) {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0;
    }
    final ResTable res = am.getResources();

    res.lock();
    final Ref<bag_entry[]> defStyleEnt = new Ref<>(null);
    int bagOff = res.getBagLocked(id, defStyleEnt, null);
    res.unlock();

    return bagOff;

  }

  @Override @Implementation @HiddenApi
  protected int retrieveArray(int id, int[] outValues) {
    if (outValues == null) {
      throw new NullPointerException("out values");
    }

    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0 /*JNI_FALSE */;
    }
    ResTable res = am.getResources();
    Ref<ResTable_config> config = new Ref<>(new ResTable_config());
    Res_value value;
    int block;

    int NV = outValues.length;

//    int[] baseDest = (int[])env->GetPrimitiveArrayCritical(outValues, 0);
    int[] baseDest = outValues;
    int[] dest = baseDest;
//    if (dest == null) {
//      throw new NullPointerException(env, "java/lang/OutOfMemoryError", "");
//      return JNI_FALSE;
//    }

    // Now lock down the resource object and start pulling stuff from it.
    res.lock();

    Ref<bag_entry[]> arrayEnt = new Ref<>(null);
    Ref<Integer> arrayTypeSetFlags = new Ref<>(0);
    int bagOff = res.getBagLocked(id, arrayEnt, arrayTypeSetFlags);
//    final ResTable::bag_entry* endArrayEnt = arrayEnt +
//        (bagOff >= 0 ? bagOff : 0);

    int destOffset = 0;
    final Ref<Integer> typeSetFlags = new Ref<>(0);
    while (destOffset < NV && destOffset < bagOff * STYLE_NUM_ENTRIES /*&& arrayEnt < endArrayEnt*/) {
      bag_entry curArrayEnt = arrayEnt.get()[destOffset / STYLE_NUM_ENTRIES];

      block = curArrayEnt.stringBlock;
      typeSetFlags.set(arrayTypeSetFlags.get());
      config.get().density = 0;
      value = curArrayEnt.map.value;

      final Ref<Integer> resid = new Ref<>(0);
      if (value.dataType != DataType.NULL.code()) {
        // Take care of resolving the found resource to its final value.
        //printf("Resolving attribute reference\n");
        Ref<Res_value> resValueRef = new Ref<>(value);
        int newBlock = res.resolveReference(resValueRef, block, resid,
                    typeSetFlags, config);
        value = resValueRef.get();
        if (kThrowOnBadId) {
          if (newBlock == BAD_INDEX) {
            throw new IllegalStateException("Bad resource!");
          }
        }
        if (newBlock >= 0) block = newBlock;
      }

      // Deal with the special @null value -- it turns back to TYPE_NULL.
      if (value.dataType == DataType.REFERENCE.code() && value.data == 0) {
        value = Res_value.NULL_VALUE;
      }

      //printf("Attribute 0x%08x: final type=0x%x, data=0x%08x\n", curIdent, value.dataType, value.data);

      // Write the final value back to Java.
      dest[destOffset + STYLE_TYPE] = value.dataType;
      dest[destOffset + STYLE_DATA] = value.data;
      dest[destOffset + STYLE_ASSET_COOKIE] = res.getTableCookie(block);
      dest[destOffset + STYLE_RESOURCE_ID] = resid.get();
      dest[destOffset + STYLE_CHANGING_CONFIGURATIONS] = typeSetFlags.get();
      dest[destOffset + STYLE_DENSITY] = config.get().density;
//      dest += STYLE_NUM_ENTRIES;
      destOffset+= STYLE_NUM_ENTRIES;
//      arrayEnt++;
    }

    destOffset /= STYLE_NUM_ENTRIES;

    res.unlock();

//    env->ReleasePrimitiveArrayCritical(outValues, baseDest, 0);

    return destOffset;

  }

  @Override @HiddenApi @Implementation
  protected Number getNativeStringBlock(int block) {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return RuntimeEnvironment.castNativePtr(0);
    }

    return RuntimeEnvironment.castNativePtr(
        ShadowStringBlock.getNativePointer(am.getResources().getTableStringBlock(block)));
  }

  @Override @Implementation
  public final SparseArray<String> getAssignedPackageIdentifiers() {
    CppAssetManager am = assetManagerForJavaObject();
    final ResTable res = am.getResources();

    SparseArray<String> sparseArray = new SparseArray<>();
    final int N = res.getBasePackageCount();
    for (int i = 0; i < N; i++) {
      final String name = res.getBasePackageName(i);
      sparseArray.put(res.getBasePackageId(i), name);
    }
    return sparseArray;
  }

  @Override @HiddenApi @Implementation
  protected final Number newTheme() {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return RuntimeEnvironment.castNativePtr(0);
    }
    ResTableTheme theme = new ResTableTheme(am.getResources());
    return RuntimeEnvironment.castNativePtr(nativeThemeRegistry.getNativeObjectId(theme));
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final void deleteTheme(long theme) {
    nativeThemeRegistry.unregister(theme);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void applyThemeStyle(int themePtr, int styleRes, boolean force) {
    applyThemeStyle((long)themePtr, styleRes, force);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  public static void applyThemeStyle(long themePtr, int styleRes, boolean force) {
    nativeThemeRegistry.getNativeObject(themePtr).applyStyle(styleRes, force);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  public static void copyTheme(long destPtr, long sourcePtr) {
    ResTableTheme dest = nativeThemeRegistry.getNativeObject(destPtr);
    ResTableTheme src = nativeThemeRegistry.getNativeObject(sourcePtr);
    dest.setTo(src);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected static int loadThemeAttributeValue(long themeHandle, int ident,
      TypedValue outValue, boolean resolve) {
    ResTableTheme theme = Preconditions.checkNotNull(nativeThemeRegistry.getNativeObject(themeHandle));
    ResTable res = theme.getResTable();

    Ref<Res_value> value = new Ref<>(null);
    // XXX value could be different in different configs!
    Ref<Integer> typeSpecFlags = new Ref<>(0);
    int block = theme.getAttribute(ident, value, typeSpecFlags);
    Ref<Integer> ref = new Ref<>(0);
    if (resolve) {
      block = res.resolveReference(value, block, ref, typeSpecFlags);
      if (kThrowOnBadId) {
        if (block == BAD_INDEX) {
          throw new IllegalStateException("Bad resource!");
        }
      }
    }
    return block >= 0 ? copyValue(outValue, res, value.get(), ref.get(), block, typeSpecFlags.get(), null) : block;
  }

//  /*package*/@HiddenApi @Implementation public static final @NativeConfig
//  int getThemeChangingConfigurations(long theme);

  @Override @HiddenApi @Implementation
  protected final Number openXmlAssetNative(int cookie, String fileName) throws FileNotFoundException {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return RuntimeEnvironment.castNativePtr(0);
    }

    ALOGV("openXmlAsset in %s (Java object %s)\n", am, ShadowArscAssetManager.class);

    String fileName8 = fileName;
    if (fileName8 == null) {
      return RuntimeEnvironment.castNativePtr(0);
    }

    int assetCookie = cookie;
    Asset a;
    if (isTruthy(assetCookie)) {
      a = am.openNonAsset(assetCookie, fileName8, AccessMode.ACCESS_BUFFER);
    } else {
      Ref<Integer> assetCookieRef = new Ref<>(assetCookie);
      a = am.openNonAsset(fileName8, AccessMode.ACCESS_BUFFER, assetCookieRef);
      assetCookie = assetCookieRef.get();
    }

    if (a == null) {
      throw new FileNotFoundException(fileName8);
    }

    final DynamicRefTable dynamicRefTable =
        am.getResources().getDynamicRefTableForCookie(assetCookie);
    ResXMLTree block = new ResXMLTree(dynamicRefTable);
    int err = block.setTo(a.getBuffer(true), (int) a.getLength(), true);
    a.close();
//    delete a;

    if (err != NO_ERROR) {
      throw new FileNotFoundException("Corrupt XML binary file");
    }

    return RuntimeEnvironment.castNativePtr(
        ShadowXmlBlock.NATIVE_RES_XML_TREES.getNativeObjectId(block));
  }

  @Override @HiddenApi @Implementation
  protected final String[] getArrayStringResource(int arrayResId) {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return null;
    }
    final ResTable res = am.getResources();

    final Ref<bag_entry[]> startOfBag = new Ref<>(null);
    final int N = res.lockBag(arrayResId, startOfBag);
    if (N < 0) {
      return null;
    }

    String[] array = new String[N];

    Ref<Res_value> valueRef = new Ref<>(null);
    final bag_entry[] bag = startOfBag.get();
    int strLen = 0;
    for (int i=0; ((int)i)<N; i++) {
      valueRef.set(bag[i].map.value);
      String str = null;

      // Take care of resolving the found resource to its final value.
      int block = res.resolveReference(valueRef, bag[i].stringBlock, null);
      if (kThrowOnBadId) {
        if (block == BAD_INDEX) {
          throw new IllegalStateException("Bad resource!");
        }
      }
      if (valueRef.get().dataType == DataType.STRING.code()) {
            final ResStringPool pool = res.getTableStringBlock(block);
            str = pool.stringAt(valueRef.get().data);

            // assume we can skip utf8 vs utf 16 handling

//            final char* str8 = pool.string8At(value.data, &strLen);
//        if (str8 != NULL) {
//          str = env.NewStringUTF(str8);
//        } else {
//                final char16_t* str16 = pool.stringAt(value.data, &strLen);
//          str = env.NewString(reinterpret_cast<final jchar*>(str16),
//              strLen);
//        }
//
//        // If one of our NewString{UTF} calls failed due to memory, an
//        // exception will be pending.
//        if (env.ExceptionCheck()) {
//          res.unlockBag(startOfBag);
//          return NULL;
//        }
        if (str == null) {
          res.unlockBag(startOfBag);
          return null;
        }

        array[i] = str;

        // str is not NULL at that point, otherwise ExceptionCheck would have been true.
        // If we have a large amount of strings in our array, we might
        // overflow the local reference table of the VM.
        // env.DeleteLocalRef(str);
      }
    }
    res.unlockBag(startOfBag);
    return array;
  }

  @Override @HiddenApi @Implementation
  protected final int[] getArrayStringInfo(int arrayResId) {
    CppAssetManager am = assetManagerForJavaObject();
    ResTable res = am.getResources();

    Ref<bag_entry[]> startOfBag = new Ref<>(null);
    final int N = res.lockBag(arrayResId, startOfBag);
    if (N < 0) {
      return null;
    }

    int[] array = new int[N * 2];

    Ref<Res_value> value = new Ref<>(null);
    bag_entry[] bag = startOfBag.get();
    for (int i = 0, j = 0; i<N; i++) {
      int stringIndex = -1;
      int stringBlock = 0;
      value.set(bag[i].map.value);

      // Take care of resolving the found resource to its final value.
      stringBlock = res.resolveReference(value, bag[i].stringBlock, null);
      if (value.get().dataType == DataType.STRING.code()) {
        stringIndex = value.get().data;
      }

      if (kThrowOnBadId) {
        if (stringBlock == BAD_INDEX) {
          throw new IllegalStateException("Bad resource!");
        }
      }

      //todo: It might be faster to allocate a C array to contain
      //      the blocknums and indices, put them in there and then
      //      do just one SetIntArrayRegion()
      //env->SetIntArrayRegion(array, j, 1, &stringBlock);
      array[j] = stringBlock;
      //env->SetIntArrayRegion(array, j + 1, 1, &stringIndex);
      array[j+1] = stringIndex;
      j += 2;
    }
    res.unlockBag(startOfBag);
    return array;
  }

  @Override @HiddenApi @Implementation
  public int[] getArrayIntResource(int arrayResId) {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return null;
    }
    final ResTable res = am.getResources();

//    final ResTable::bag_entry* startOfBag;
    final Ref<bag_entry[]> startOfBag = new Ref<>(null);
    final int N = res.lockBag(arrayResId, startOfBag);
    if (N < 0) {
      return null;
    }

    int[] array = new int[N];
    if (array == null) {
      res.unlockBag(startOfBag);
      return null;
    }

    Ref<Res_value> valueRef = new Ref<>(null);
    bag_entry[] bag = startOfBag.get();
    for (int i=0; i<N; i++) {
      valueRef.set(bag[i].map.value);

      // Take care of resolving the found resource to its final value.
      int block = res.resolveReference(valueRef, bag[i].stringBlock, null, null, null);
      if (kThrowOnBadId) {
        if (block == BAD_INDEX) {
          res.unlockBag(startOfBag); // seems like this is missing from android_util_AssetManager.cpp?
          throw new IllegalStateException("Bad resource!");
//          return array;
        }
      }
      Res_value value = valueRef.get();
      if (value.dataType >= DataType.TYPE_FIRST_INT
          && value.dataType <= DataType.TYPE_LAST_INT) {
        int intVal = value.data;
//        env->SetIntArrayRegion(array, i, 1, &intVal);
        array[i] = intVal;
      }
    }
    res.unlockBag(startOfBag);
    return array;
  }

  @Override @HiddenApi @Implementation(maxSdk = VERSION_CODES.KITKAT)
  protected void init() {
  //  if (isSystem) {
  //    verifySystemIdmaps();
  //  }
    init(false);
  }

  private static CppAssetManager systemCppAssetManager;

  @Override @HiddenApi @Implementation(minSdk = VERSION_CODES.KITKAT_WATCH)
  protected void init(boolean isSystem) {
    //  if (isSystem) {
    //    verifySystemIdmaps();
    //  }

    String androidFrameworkJarPath = RuntimeEnvironment.getAndroidFrameworkJarPath();
    Preconditions.checkNotNull(androidFrameworkJarPath);

    if (isSystem) {
      synchronized (ShadowArscAssetManager.class) {
        if (systemCppAssetManager == null) {
          systemCppAssetManager = new CppAssetManager();
          systemCppAssetManager.addDefaultAssets(androidFrameworkJarPath);
        }
      }
      this.cppAssetManager = systemCppAssetManager;
    } else {
      this.cppAssetManager = new CppAssetManager();
      cppAssetManager.addDefaultAssets(androidFrameworkJarPath);
    }

    ALOGV("Created AssetManager %s for Java object %s\n", cppAssetManager,
        ShadowArscAssetManager.class);
  }

  @VisibleForTesting
  ResTable_config getConfiguration() {
    Ref<ResTable_config> config = new Ref<>(new ResTable_config());
    assetManagerForJavaObject().getConfiguration(config);
    return config.get();
  }

//  private native final void destroy();

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

  @Override @HiddenApi @Implementation
  protected int getStringBlockCount() {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0;
    }
    return am.getResources().getTableCount();
  }

  
  @Implementation(minSdk = VERSION_CODES.P)
  public static long nativeCreate() {
    return directlyOn(AssetManager.class, "nativeCreate");
  }
  

  synchronized private CppAssetManager assetManagerForJavaObject() {
    if (cppAssetManager == null) {
      throw new NullPointerException();
    }
    return cppAssetManager;
  }

  static ParcelFileDescriptor returnParcelFileDescriptor(Asset a, long[] outOffsets)
      throws FileNotFoundException {
    Ref<Long> startOffset = new Ref<Long>(-1L);
    Ref<Long> length = new Ref<Long>(-1L);;
    FileDescriptor fd = a.openFileDescriptor(startOffset, length);

    if (fd == null) {
      throw new FileNotFoundException(
          "This file can not be opened as a file descriptor; it is probably compressed");
    }

    long[] offsets = outOffsets;
    if (offsets == null) {
      // fd.close();
      return null;
    }

    offsets[0] = startOffset.get();
    offsets[1] = length.get();

    // FileDescriptor fileDesc = jniCreateFileDescriptor(fd);
    // if (fileDesc == null) {
    // close(fd);
    // return null;
    // }

    // return newParcelFileDescriptor(fileDesc);
    return new ParcelFileDescriptor(fd);
  }

  /**
   * @deprecated Avoid use.
   */
  @Deprecated
  synchronized public ResTable getCompileTimeResTable() {
    if (compileTimeResTable == null) {
      CppAssetManager compileTimeCppAssetManager = new CppAssetManager();
      for (AssetPath assetPath : assetManagerForJavaObject().getAssetPaths()) {
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

  @Override
  Collection<FsFile> getAllAssetDirs() {
    ArrayList<FsFile> fsFiles = new ArrayList<>();
    for (AssetPath assetPath : cppAssetManager.getAssetPaths()) {
      if (assetPath.file.isFile()) {
        fsFiles.add(Fs.newJarFile(new File(assetPath.file.getPath())).join("assets"));
      } else {
        fsFiles.add(assetPath.file);
      }
    }
    return fsFiles;
  }
}
