package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.res.android.Errors.BAD_INDEX;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build.VERSION_CODES;
import android.os.ParcelFileDescriptor;
import android.util.SparseArray;
import android.util.TypedValue;
import com.google.common.base.Strings;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.android.CppAssetManager;
import org.robolectric.res.android.DataType;
import org.robolectric.res.android.Ref;
import org.robolectric.res.android.ResStringPool;
import org.robolectric.res.android.ResTable;
import org.robolectric.res.android.ResTable.bag_entry;
import org.robolectric.res.android.ResTableConfig;
import org.robolectric.res.android.ResValue;
import org.robolectric.res.android.String8;
import org.robolectric.shadows.ShadowActivity.IntentForResult;
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
//      cppAssetManager = new CppAssetManager();
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
//      cppAssetManager = new CppAssetManager();
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
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.JELLY_BEAN_MR1) {
      return addAssetPathNative(path);
    } else {
      return directlyOn(realObject, AssetManager.class, "addAssetPath",
          ClassParameter.from(String.class, path));
    }
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

  private void replaceShadow(ShadowAssetManager shadowAssetManager) {
    shadowAssetManager.realObject = realObject;
    ReflectionHelpers.setField(realObject, "__robo_data__", shadowAssetManager);
  }

  //////////// native method implementations

  private static final boolean kThrowOnBadId = false;
  private static final boolean kDebugStyles = false;

//  public native final String[] list(String path)
//      throws IOException;

  @HiddenApi
  @Implementation(maxSdk = VERSION_CODES.M)
  public int addAssetPathNative(String path) {
  return addAssetPathNative(path, false);
}

  @HiddenApi
  @Implementation(minSdk = VERSION_CODES.N)
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
  
  @HiddenApi @Implementation public final int addOverlayPathNative(String idmapPath) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi @Implementation public final String[] getNonSystemLocales() {
    throw new UnsupportedOperationException("not yet implemented");
  }
  @HiddenApi @Implementation public final Configuration[] getSizeConfigurations() {
    throw new UnsupportedOperationException("not yet implemented");
  }

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

  @HiddenApi
  @Implementation
  public final long openAsset(String fileName, int accessMode) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi
  @Implementation
  public ParcelFileDescriptor openAssetFd(String fileName,
      long[] outOffsets) throws IOException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi
  @Implementation
  public final long openNonAssetNative(int cookie, String fileName,
      int accessMode) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi
  @Implementation
  public ParcelFileDescriptor openNonAssetFdNative(int cookie,
      String fileName, long[] outOffsets) throws IOException {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi
  @Implementation
  public final void destroyAsset(long asset) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi
  @Implementation
  public final int readAssetChar(long asset) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi
  @Implementation
  public final int readAsset(long asset, byte[] b, int off, int len) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi
  @Implementation
  public final long seekAsset(long asset, long offset, int whence) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi
  @Implementation
  public final long getAssetLength(long asset) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi
  @Implementation
  public final long getAssetRemainingLength(long asset) {
    throw new UnsupportedOperationException("not yet implemented");
  }

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

  /**
   * Returns true if the resource was found, filling in mRetStringBlock and
   * mRetData.
   */
  @Implementation
  @HiddenApi
  public final int loadResourceBagValue(int ident, int bagEntryId, TypedValue outValue,
      boolean resolve) {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0;
    }
    final ResTable res = am.getResources();

    // Now lock down the resource object and start pulling stuff from it.
    res.lock();

    int block = -1;
    Ref<ResValue> valueRef = new Ref<>(null);
    Ref<bag_entry[]> entryRef = new Ref<>(null);
    Ref<Integer> typeSpecFlags = new Ref<>(0);
    int entryCount = res.getBagLocked(ident, entryRef, typeSpecFlags);

    for (int i=0; i < entryCount; i++) {
      bag_entry entry = entryRef.get()[i];
      if (bagEntryId == entry.map.nameIdent) {
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
      return copyValue(outValue, res, valueRef.get(), ref.get(), block, typeSpecFlags.get(), null);
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

  @Implementation
  @HiddenApi
  public static final boolean applyStyle(long theme,
      int defStyleAttr, int defStyleRes, long xmlParser,
      int[] inAttrs, int[] outValues, int[] outIndices) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Implementation
  @HiddenApi
  public static final boolean resolveAttrs(long theme,
      int defStyleAttr, int defStyleRes, int[] inValues,
      int[] inAttrs, int[] outValues, int[] outIndices) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Implementation
  @HiddenApi
  public final boolean retrieveAttributes(
      long xmlParser, int[] inAttrs, int[] outValues, int[] outIndices) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Implementation
  @HiddenApi
  public int getArraySize(int id) {
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

  @Implementation
  @HiddenApi
  public int retrieveArray(int id, int[] outValues) {
    if (outValues == null) {
      throw new NullPointerException("out values");
    }

    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0 /*JNI_FALSE */;
    }
    ResTable res = am.getResources();
    Ref<ResTableConfig> config = new Ref<>(new ResTableConfig());
    ResValue value;
    int block;

    int NV = outValues.length;

//    jint* baseDest = (jint*)env->GetPrimitiveArrayCritical(outValues, 0);
    int[] baseDest = outValues;
    int[] dest = baseDest;
//    if (dest == null) {
//      jniThrowException(env, "java/lang/OutOfMemoryError", "");
//      return JNI_FALSE;
//    }

    // Now lock down the resource object and start pulling stuff from it.
    res.lock();

    Ref<bag_entry[]> arrayEnt = new Ref<>(null);
    Ref<Integer> arrayTypeSetFlags = new Ref<>(0);
    int bagOff = res.getBagLocked(id, arrayEnt, arrayTypeSetFlags);
//    const ResTable::bag_entry* endArrayEnt = arrayEnt +
//        (bagOff >= 0 ? bagOff : 0);

    int i = 0;
    final Ref<Integer> typeSetFlags = new Ref<>(0);
    while (i < NV /*&& arrayEnt < endArrayEnt*/) {
      bag_entry curArrayEnt = arrayEnt.get()[i / STYLE_NUM_ENTRIES];

      block = curArrayEnt.stringBlock;
      typeSetFlags.set(arrayTypeSetFlags.get());
      config.get().density = 0;
      value = curArrayEnt.map.value;

      final Ref<Integer> resid = new Ref<>(0);
      if (value.dataType != DataType.NULL.code()) {
        // Take care of resolving the found resource to its final value.
        //printf("Resolving attribute reference\n");
        Ref<ResValue> resValueRef = new Ref<>(value);
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
        value.dataType = DataType.NULL.code();
        value.data = TypedValue.DATA_NULL_UNDEFINED;
      }

      //printf("Attribute 0x%08x: final type=0x%x, data=0x%08x\n", curIdent, value.dataType, value.data);

      // Write the final value back to Java.
      dest[i + STYLE_TYPE] = value.dataType;
      dest[i + STYLE_DATA] = value.data;
      dest[i + STYLE_ASSET_COOKIE] = res.getTableCookie(block);
      dest[i + STYLE_RESOURCE_ID] = resid.get();
      dest[i + STYLE_CHANGING_CONFIGURATIONS] = typeSetFlags.get();
      dest[i + STYLE_DENSITY] = config.get().density;
//      dest += STYLE_NUM_ENTRIES;
      i+= STYLE_NUM_ENTRIES;
//      arrayEnt++;
    }

    i /= STYLE_NUM_ENTRIES;

    res.unlock();

//    env->ReleasePrimitiveArrayCritical(outValues, baseDest, 0);

    return i;

  }

  @HiddenApi
  @Implementation
  public long getNativeStringBlock(int block) {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0;
    }

    return ShadowStringBlock.getNativePointer(am.getResources().getTableStringBlock(block));
  }

  @HiddenApi @Implementation public final String getCookieName(int cookie) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @Implementation
  public final SparseArray<String> getAssignedPackageIdentifiers() {
    return directlyOn(realObject, AssetManager.class, "getAssignedPackageIdentifiers");
  }

  @HiddenApi @Implementation public static final int getGlobalAssetCount(){
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi @Implementation public static final String getAssetAllocations(){
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi @Implementation public static final int getGlobalAssetManagerCount(){
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi @Implementation public final long newTheme(){
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi @Implementation public final void deleteTheme(long theme){
    throw new UnsupportedOperationException("not yet implemented");
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

  /*package*/@HiddenApi @Implementation public static final void clearTheme(long theme){
    throw new UnsupportedOperationException("not yet implemented");
  }
  /*package*/@HiddenApi @Implementation public static final int loadThemeAttributeValue(long theme, int ident,
      TypedValue outValue,
      boolean resolve){
    throw new UnsupportedOperationException("not yet implemented");
  }
  /*package*/@HiddenApi @Implementation public static final void dumpTheme(long theme, int priority, String tag, String prefix){
    throw new UnsupportedOperationException("not yet implemented");
  }
//  /*package*/@HiddenApi @Implementation public static final @NativeConfig
//  int getThemeChangingConfigurations(long theme);

  @HiddenApi @Implementation public final long openXmlAssetNative(int cookie, String fileName){
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi @Implementation public final String[] getArrayStringResource(int arrayResId){
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

    Ref<ResValue> valueRef = new Ref<>(null);
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

  @HiddenApi @Implementation public final int[] getArrayStringInfo(int arrayRes){
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi
  @Implementation
  public int[] getArrayIntResource(int arrayResId) {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return null;
    }
    final ResTable res = am.getResources();

//    const ResTable::bag_entry* startOfBag;
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

    Ref<ResValue> valueRef = new Ref<>(null);
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
      ResValue value = valueRef.get();
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

  /*package*/@HiddenApi @Implementation public final int[] getStyleAttributes(int themeRes){
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi
  @Implementation(maxSdk = VERSION_CODES.KITKAT)
  public void init() {
  //  if (isSystem) {
  //    verifySystemIdmaps();
  //  }
    init(false);
  }

    @HiddenApi
    @Implementation(minSdk = VERSION_CODES.KITKAT_WATCH)
    public void init(boolean isSystem) {
  //  if (isSystem) {
  //    verifySystemIdmaps();
  //  }
      CppAssetManager am = assetManagerForJavaObject();

      am.addDefaultAssets();

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

  @HiddenApi
  @Implementation
  public int getStringBlockCount()
  {
    CppAssetManager am = assetManagerForJavaObject();
    if (am == null) {
      return 0;
    }
    return am.getResources().getTableCount();
  }


  private CppAssetManager assetManagerForJavaObject() {
    if (cppAssetManager == null) {
      cppAssetManager = new CppAssetManager();
    }
    return cppAssetManager;
  }

}
