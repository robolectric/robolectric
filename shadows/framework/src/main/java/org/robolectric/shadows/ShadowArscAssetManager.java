package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.res.android.Asset.SEEK_CUR;
import static org.robolectric.res.android.Asset.SEEK_SET;
import static org.robolectric.res.android.AttributeResolution.kThrowOnBadId;
import static org.robolectric.res.android.Errors.BAD_INDEX;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Util.ALOGV;
import static org.robolectric.res.android.Util.isTruthy;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.AssetManager;
import android.os.Build.VERSION_CODES;
import android.os.ParcelFileDescriptor;
import android.util.SparseArray;
import android.util.TypedValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import dalvik.system.VMRuntime;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.Fs;
import org.robolectric.res.android.Asset;
import org.robolectric.res.android.Asset.AccessMode;
import org.robolectric.res.android.AssetDir;
import org.robolectric.res.android.AssetPath;
import org.robolectric.res.android.AttributeResolution;
import org.robolectric.res.android.CppAssetManager;
import org.robolectric.res.android.DataType;
import org.robolectric.res.android.DynamicRefTable;
import org.robolectric.res.android.Ref;
import org.robolectric.res.android.Registries;
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
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowAssetManager.Picker;

// native method impls transliterated from
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/core/jni/android_util_AssetManager.cpp
@Implements(value = AssetManager.class, maxSdk = VERSION_CODES.O_MR1, shadowPicker = Picker.class)
@SuppressWarnings("NewApi")
public class ShadowArscAssetManager extends ShadowAssetManager.ArscBase {

  @RealObject
  protected AssetManager realObject;

  private CppAssetManager cppAssetManager;

  @Resetter
  public static void reset() {
    // todo: ShadowPicker doesn't discriminate properly between concrete shadow classes for resetters...
    if (!useLegacy() && RuntimeEnvironment.getApiLevel() < P) {
      reflector(_AssetManager_.class).setSystem(null);
      // NATIVE_THEME_REGISTRY.clear();
      // nativeXMLParserRegistry.clear(); // todo: shouldn't these be freed explicitly? [yes! xw]
      // NATIVE_ASSET_REGISTRY.clear();
    }
  }

  @Implementation
  protected final String[] list(String path) throws IOException {
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

  // @HiddenApi @Implementation(minSdk = VERSION_CODES.P)
  // public void setApkAssets(Object apkAssetsObjects, Object invalidateCaches) {
  //   throw new UnsupportedOperationException("implement me");
  // }

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

  @HiddenApi @Implementation(minSdk = O)
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

  @HiddenApi @Implementation
  protected static void dumpTheme(long theme, int priority, String tag, String prefix) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Implementation
  protected String getResourceName(int resid) {
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

  @Implementation
  protected String getResourcePackageName(int resid) {
    CppAssetManager cppAssetManager = assetManagerForJavaObject();

    ResourceName name = new ResourceName();
    if (!cppAssetManager.getResources().getResourceName(resid, true, name)) {
      return null;
    }

    return name.packageName.trim();
  }

  @Implementation
  protected String getResourceTypeName(int resid) {
    CppAssetManager cppAssetManager = assetManagerForJavaObject();

    ResourceName name = new ResourceName();
    if (!cppAssetManager.getResources().getResourceName(resid, true, name)) {
      return null;
    }

    return name.type;
  }

  @Implementation
  protected String getResourceEntryName(int resid) {
    CppAssetManager cppAssetManager = assetManagerForJavaObject();

    ResourceName name = new ResourceName();
    if (!cppAssetManager.getResources().getResourceName(resid, true, name)) {
      return null;
    }

    return name.name;
  }

  //////////// native method implementations

//  public native final String[] list(String path)
//      throws IOException;

//  @HiddenApi @Implementation(minSdk = VERSION_CODES.P)
//  public void setApkAssets(Object apkAssetsObjects, Object invalidateCaches) {
//    throw new UnsupportedOperationException("implement me");
//  }
//

  @HiddenApi @Implementation(maxSdk = VERSION_CODES.JELLY_BEAN_MR1)
  public int addAssetPath(String path) {
    return addAssetPathNative(path);
  }

  @HiddenApi @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = M)
  final protected int addAssetPathNative(String path) {
    return addAssetPathNative(path, false);
  }

  @HiddenApi @Implementation(minSdk = VERSION_CODES.N)
  protected int addAssetPathNative(String path, boolean appAsLib) {
    if (Strings.isNullOrEmpty(path)) {
      return 0;
    }

    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0;
    }
    final Ref<Integer> cookie = new Ref<>(null);
    boolean res = am.addAssetPath(new String8(path), cookie, appAsLib);
    return (res) ? cookie.get() : 0;
  }

  @HiddenApi @Implementation
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

  @HiddenApi @Implementation
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

    return RuntimeEnvironment.castNativePtr(Registries.NATIVE_ASSET_REGISTRY.register(a));
  }

  @HiddenApi @Implementation
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

  @HiddenApi @Implementation
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
    long assetId = Registries.NATIVE_ASSET_REGISTRY.register(a);
    // todo: something better than this [xw]
    a.onClose = () -> destroyAsset(assetId);
    //printf("Created Asset Stream: %p\n", a);
    return RuntimeEnvironment.castNativePtr(assetId);
  }

  @HiddenApi @Implementation
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

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final void destroyAsset(int asset) {
    destroyAsset((long) asset);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final void destroyAsset(long asset) {
    Registries.NATIVE_ASSET_REGISTRY.unregister(asset);
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final int readAssetChar(int asset) {
    return readAssetChar((long) asset);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final int readAssetChar(long asset) {
    Asset a = getAsset(asset);
    byte[] b = new byte[1];
    int res = a.read(b, 1);
    return res == 1 ? b[0] & 0xff : -1;
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final int readAsset(int asset, byte[] b, int off, int len) throws IOException {
    return readAsset((long) asset, b, off, len);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
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

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final long seekAsset(int asset, long offset, int whence) {
    return seekAsset((long) asset, offset, whence);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final long seekAsset(long asset, long offset, int whence) {
    Asset a = getAsset(asset);
    return a.seek(offset, whence < 0 ? SEEK_SET : SEEK_CUR);
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final long getAssetLength(int asset) {
    return getAssetLength((long) asset);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final long getAssetLength(long asset) {
    Asset a = getAsset(asset);
    return a.getLength();
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final long getAssetRemainingLength(int asset) {
    return getAssetRemainingLength((long) asset);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final long getAssetRemainingLength(long assetHandle) {
    Asset a = getAsset(assetHandle);

    if (a == null) {
      throw new NullPointerException("asset");
    }

    return a.getRemainingLength();
  }

  private Asset getAsset(long asset) {
    return Registries.NATIVE_ASSET_REGISTRY.getNativeObject(asset);
  }

  @HiddenApi @Implementation
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

    final Ref<Res_value> value = new Ref<>(null);
    final Ref<ResTable_config> config = new Ref<>(null);
    final Ref<Integer> typeSpecFlags = new Ref<>(null);
    int block = res.getResource(ident, value, false, density, typeSpecFlags, config);
    if (kThrowOnBadId) {
        if (block == BAD_INDEX) {
            throw new IllegalStateException("Bad resource!");
            //return 0;
        }
    }
    final Ref<Integer> ref = new Ref<>(ident);
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
      final Ref<bag_entry[]> entryRef = new Ref<>(null);
      final Ref<Integer> typeSpecFlags = new Ref<>(0);
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
  @Implementation @HiddenApi
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
    final Ref<Res_value> valueRef = new Ref<>(null);
    final Ref<bag_entry[]> entryRef = new Ref<>(null);
    final Ref<Integer> typeSpecFlags = new Ref<>(0);
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

    final Ref<Integer> ref = new Ref<>(ident);
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

  // /*package*/ static final int STYLE_NUM_ENTRIES = 6;
  // /*package*/ static final int STYLE_TYPE = 0;
  // /*package*/ static final int STYLE_DATA = 1;
  // /*package*/ static final int STYLE_ASSET_COOKIE = 2;
  // /*package*/ static final int STYLE_RESOURCE_ID = 3;
  //
  // /* Offset within typed data array for native changingConfigurations. */
  // static final int STYLE_CHANGING_CONFIGURATIONS = 4;

  // /*package*/ static final int STYLE_DENSITY = 5;

/* lowercase hexadecimal notation.  */
//# define PRIx8		"x"
//      # define PRIx16		"x"
//      # define PRIx32		"x"

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected static void applyStyle(int themeToken, int defStyleAttr, int defStyleRes,
      int xmlParserToken, int[] attrs, int[] outValues, int[] outIndices) {
    applyStyle((long)themeToken, defStyleAttr, defStyleRes, (long)xmlParserToken, attrs,
        outValues, outIndices);
  }

  @HiddenApi @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void applyStyle(long themeToken, int defStyleAttr, int defStyleRes,
      long xmlParserToken, int[] inAttrs, int length, long outValuesAddress,
      long outIndicesAddress) {
    ShadowVMRuntime shadowVMRuntime = Shadow.extract(VMRuntime.getRuntime());
    int[] outValues = (int[])shadowVMRuntime.getObjectForAddress(outValuesAddress);
    int[] outIndices = (int[])shadowVMRuntime.getObjectForAddress(outIndicesAddress);
    applyStyle(themeToken, defStyleAttr, defStyleRes, xmlParserToken, inAttrs,
        outValues, outIndices);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = N_MR1)
  protected static void applyStyle(long themeToken, int defStyleAttr, int defStyleRes,
      long xmlParserToken, int[] attrs, int[] outValues, int[] outIndices) {
    ResTableTheme theme = Registries.NATIVE_THEME_REGISTRY.getNativeObject(themeToken);
    ResXMLParser xmlParser = xmlParserToken == 0
        ? null
        : Registries.NATIVE_RES_XML_PARSERS.getNativeObject(xmlParserToken);
    AttributeResolution.ApplyStyle(theme, xmlParser, defStyleAttr, defStyleRes,
        attrs, attrs.length, outValues, outIndices);
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
    int destOffset = 0;
    if (baseDest == null) {
      return false;
    }

    int[] indices = null;
    if (outIndices != null) {
      if (outIndices.length > NI) {
        indices = outIndices;
      }
    }

    ResTableTheme theme = Registries.NATIVE_THEME_REGISTRY.getNativeObject(themeToken);

    boolean result = AttributeResolution.ResolveAttrs(theme, defStyleAttr, defStyleRes,
        srcValues, NSV,
        src, NI,
        baseDest,
        indices);

    if (indices != null) {
//      env.ReleasePrimitiveArrayCritical(outIndices, indices, 0);
    }
//    env.ReleasePrimitiveArrayCritical(outValues, baseDest, 0);
//    env.ReleasePrimitiveArrayCritical(inValues, srcValues, 0);
//    env.ReleasePrimitiveArrayCritical(attrs, src, 0);

    return result;
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final boolean retrieveAttributes(
      int xmlParserToken, int[] attrs, int[] outValues, int[] outIndices) {
    return retrieveAttributes((long)xmlParserToken, attrs, outValues, outIndices);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
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
    ResXMLParser xmlParser = Registries.NATIVE_RES_XML_PARSERS.getNativeObject(xmlParserToken);

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
    if (baseDest == null) {
//      env.ReleasePrimitiveArrayCritical(attrs, src, 0);
//      return JNI_FALSE;
      return false;
    }

    int[] indices = null;
    if (outIndices != null) {
      if (outIndices.length > NI) {
//        indices = (int[])env.GetPrimitiveArrayCritical(outIndices, 0);
        indices = outIndices;
      }
    }
    boolean result = AttributeResolution.RetrieveAttributes(res, xmlParser, src, NI, baseDest, indices);

    if (indices != null) {
//      indices[0] = indicesIdx;
//      env.ReleasePrimitiveArrayCritical(outIndices, indices, 0);
    }

//    env.ReleasePrimitiveArrayCritical(outValues, baseDest, 0);
//    env.ReleasePrimitiveArrayCritical(attrs, src, 0);

    return result;
  }

  @HiddenApi @Implementation
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

  @Implementation @HiddenApi
  protected int retrieveArray(int id, int[] outValues) {
    if (outValues == null) {
      throw new NullPointerException("out values");
    }

    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0 /*JNI_FALSE */;
    }
    ResTable res = am.getResources();
    final Ref<ResTable_config> config = new Ref<>(new ResTable_config());
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

    final Ref<bag_entry[]> arrayEnt = new Ref<>(null);
    final Ref<Integer> arrayTypeSetFlags = new Ref<>(0);
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
        final Ref<Res_value> resValueRef = new Ref<>(value);
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

  @HiddenApi @Implementation
  protected Number getNativeStringBlock(int block) {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return RuntimeEnvironment.castNativePtr(0);
    }

    return RuntimeEnvironment.castNativePtr(
        am.getResources().getTableStringBlock(block).getNativePtr());
  }

  @Implementation
  protected final SparseArray<String> getAssignedPackageIdentifiers() {
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

  @HiddenApi @Implementation
  protected final Number newTheme() {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return RuntimeEnvironment.castNativePtr(0);
    }
    ResTableTheme theme = new ResTableTheme(am.getResources());
    return RuntimeEnvironment.castNativePtr(Registries.NATIVE_THEME_REGISTRY.register(theme));
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final void deleteTheme(int theme) {
    deleteTheme((long) theme);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected final void deleteTheme(long theme) {
    Registries.NATIVE_THEME_REGISTRY.unregister(theme);
  }

  @HiddenApi
  @Implementation(maxSdk = KITKAT_WATCH)
  public static void applyThemeStyle(int themePtr, int styleRes, boolean force) {
    applyThemeStyle((long)themePtr, styleRes, force);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  public static void applyThemeStyle(long themePtr, int styleRes, boolean force) {
    Registries.NATIVE_THEME_REGISTRY.getNativeObject(themePtr).applyStyle(styleRes, force);
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public static void copyTheme(int destPtr, int sourcePtr) {
    copyTheme((long) destPtr, (long) sourcePtr);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  public static void copyTheme(long destPtr, long sourcePtr) {
    ResTableTheme dest = Registries.NATIVE_THEME_REGISTRY.getNativeObject(destPtr);
    ResTableTheme src = Registries.NATIVE_THEME_REGISTRY.getNativeObject(sourcePtr);
    dest.setTo(src);
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected static int loadThemeAttributeValue(int themeHandle, int ident,
      TypedValue outValue, boolean resolve) {
    return loadThemeAttributeValue((long) themeHandle, ident, outValue, resolve);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  protected static int loadThemeAttributeValue(long themeHandle, int ident,
      TypedValue outValue, boolean resolve) {
    ResTableTheme theme = Preconditions.checkNotNull(Registries.NATIVE_THEME_REGISTRY.getNativeObject(themeHandle));
    ResTable res = theme.getResTable();

    final Ref<Res_value> value = new Ref<>(null);
    // XXX value could be different in different configs!
    final Ref<Integer> typeSpecFlags = new Ref<>(0);
    int block = theme.GetAttribute(ident, value, typeSpecFlags);
    final Ref<Integer> ref = new Ref<>(0);
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

  @HiddenApi @Implementation
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
      final Ref<Integer> assetCookieRef = new Ref<>(assetCookie);
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
        Registries.NATIVE_RES_XML_TREES.register(block));
  }

  @HiddenApi @Implementation
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

    final Ref<Res_value> valueRef = new Ref<>(null);
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

  @HiddenApi @Implementation
  protected final int[] getArrayStringInfo(int arrayResId) {
    CppAssetManager am = assetManagerForJavaObject();
    ResTable res = am.getResources();

    final Ref<bag_entry[]> startOfBag = new Ref<>(null);
    final int N = res.lockBag(arrayResId, startOfBag);
    if (N < 0) {
      return null;
    }

    int[] array = new int[N * 2];

    final Ref<Res_value> value = new Ref<>(null);
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

  @HiddenApi @Implementation
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

    final Ref<Res_value> valueRef = new Ref<>(null);
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

  @HiddenApi @Implementation(maxSdk = VERSION_CODES.KITKAT)
  protected void init() {
  //  if (isSystem) {
  //    verifySystemIdmaps();
  //  }
    init(false);
  }

  private static CppAssetManager systemCppAssetManager;

  @HiddenApi @Implementation(minSdk = VERSION_CODES.KITKAT_WATCH)
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
    final Ref<ResTable_config> config = new Ref<>(new ResTable_config());
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
//    final Ref<Integer> cookie = new Ref<>(null);
//    boolean res = am.addOverlayPath(new String8(idmapPath), cookie);
//    return (res) ? cookie.get() : 0;
//  }

  @HiddenApi @Implementation
  protected int getStringBlockCount() {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0;
    }
    return am.getResources().getTableCount();
  }

  
  synchronized private CppAssetManager assetManagerForJavaObject() {
    if (cppAssetManager == null) {
      throw new NullPointerException();
    }
    return cppAssetManager;
  }

  static ParcelFileDescriptor returnParcelFileDescriptor(Asset a, long[] outOffsets)
      throws FileNotFoundException {
    final Ref<Long> startOffset = new Ref<Long>(-1L);
    final Ref<Long> length = new Ref<Long>(-1L);;
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

    // TODO: consider doing this
    // return new ParcelFileDescriptor(fileDesc);
    return ParcelFileDescriptor.open(a.getFile(), ParcelFileDescriptor.MODE_READ_ONLY);
  }

  @Override
  Collection<Path> getAllAssetDirs() {
    ArrayList<Path> paths = new ArrayList<>();
    for (AssetPath assetPath : cppAssetManager.getAssetPaths()) {
      if (Files.isRegularFile(assetPath.file)) {
        paths.add(Fs.forJar(assetPath.file).getPath("assets"));
      } else {
        paths.add(assetPath.file);
      }
    }
    return paths;
  }

  @Override
  List<AssetPath> getAssetPaths() {
    return assetManagerForJavaObject().getAssetPaths();
  }

}
