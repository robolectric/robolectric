package org.robolectric.shadows;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/core/jni/android_util_AssetManager.cpp

import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.res.android.ApkAssetsCookie.K_INVALID_COOKIE;
import static org.robolectric.res.android.ApkAssetsCookie.kInvalidCookie;
import static org.robolectric.res.android.Asset.SEEK_CUR;
import static org.robolectric.res.android.Asset.SEEK_END;
import static org.robolectric.res.android.Asset.SEEK_SET;
import static org.robolectric.res.android.AttributeResolution9.ApplyStyle;
import static org.robolectric.res.android.AttributeResolution9.ResolveAttrs;
import static org.robolectric.res.android.AttributeResolution9.RetrieveAttributes;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Registries.NATIVE_RES_XML_PARSERS;
import static org.robolectric.res.android.Registries.NATIVE_RES_XML_TREES;
import static org.robolectric.res.android.Util.ATRACE_NAME;
import static org.robolectric.res.android.Util.CHECK;
import static org.robolectric.res.android.Util.JNI_FALSE;
import static org.robolectric.res.android.Util.JNI_TRUE;
import static org.robolectric.res.android.Util.isTruthy;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.AnyRes;
import android.annotation.ArrayRes;
import android.annotation.AttrRes;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.StyleRes;
import android.content.res.ApkAssets;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Configuration.NativeConfig;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.ArraySet;
import android.util.SparseArray;
import android.util.TypedValue;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.Fs;
import org.robolectric.res.android.ApkAssetsCookie;
import org.robolectric.res.android.Asset;
import org.robolectric.res.android.AssetDir;
import org.robolectric.res.android.AssetPath;
import org.robolectric.res.android.CppApkAssets;
import org.robolectric.res.android.CppAssetManager;
import org.robolectric.res.android.CppAssetManager2;
import org.robolectric.res.android.CppAssetManager2.ResolvedBag;
import org.robolectric.res.android.CppAssetManager2.ResourceName;
import org.robolectric.res.android.CppAssetManager2.Theme;
import org.robolectric.res.android.DynamicRefTable;
import org.robolectric.res.android.Ref;
import org.robolectric.res.android.Registries;
import org.robolectric.res.android.ResStringPool;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.res.android.ResXMLParser;
import org.robolectric.res.android.ResXMLTree;
import org.robolectric.res.android.ResourceTypes.Res_value;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(
    value = AssetManager.class,
    minSdk = Build.VERSION_CODES.P,
    shadowPicker = ShadowAssetManager.Picker.class)
@SuppressWarnings("NewApi")
public class ShadowArscAssetManager9 extends ShadowAssetManager.ArscBase {

  private static CppAssetManager2 systemCppAssetManager2;
  private static long systemCppAssetManager2Ref;
  private static boolean inNonSystemConstructor;
  private static ApkAssets[] cachedSystemApkAssets;
  private static ArraySet<ApkAssets> cachedSystemApkAssetsSet;

  @RealObject AssetManager realAssetManager;

//  @RealObject
//  protected AssetManager realObject;

  // #define ATRACE_TAG ATRACE_TAG_RESOURCES
// #define LOG_TAG "asset"
//
// #include <inttypes.h>
// #include <linux/capability.h>
// #include <stdio.h>
// #include <sys/stat.h>
// #include <sys/system_properties.h>
// #include <sys/types.h>
// #include <sys/wait.h>
//
// #include <private/android_filesystem_config.h> // for AID_SYSTEM
//
// #include "android-base/logging.h"
// #include "android-base/properties.h"
// #include "android-base/stringprintf.h"
// #include "android_runtime/android_util_AssetManager.h"
// #include "android_runtime/AndroidRuntime.h"
// #include "android_util_Binder.h"
// #include "androidfw/Asset.h"
// #include "androidfw/AssetManager.h"
// #include "androidfw/AssetManager2.h"
// #include "androidfw/AttributeResolution.h"
// #include "androidfw/MutexGuard.h"
// #include "androidfw/ResourceTypes.h"
// #include "core_jni_helpers.h"
// #include "jni.h"
// #include "nativehelper/JNIHelp.h"
// #include "nativehelper/ScopedPrimitiveArray.h"
// #include "nativehelper/ScopedStringChars.h"
// #include "nativehelper/String.h"
// #include "utils/Log.h"
// #include "utils/misc.h"
// #include "utils/String.h"
// #include "utils/Trace.h"
//
// extern "C" int capget(cap_user_header_t hdrp, cap_user_data_t datap);
// extern "C" int capset(cap_user_header_t hdrp, const cap_user_data_t datap);
//
// using ::android::base::StringPrintf;
//
// namespace android {
//
// // ----------------------------------------------------------------------------
//

// static class typedvalue_offsets_t {
//   jfieldID mType;
//   jfieldID mData;
//   jfieldID mString;
//   jfieldID mAssetCookie;
//   jfieldID mResourceId;
//   jfieldID mChangingConfigurations;
//   jfieldID mDensity;
// }
// static final typedvalue_offsets_t gTypedValueOffsets = new typedvalue_offsets_t();
//
// static class assetfiledescriptor_offsets_t {
//   jfieldID mFd;
//   jfieldID mStartOffset;
//   jfieldID mLength;
// }
// static final assetfiledescriptor_offsets_t gAssetFileDescriptorOffsets = new assetfiledescriptor_offsets_t();
//
// static class assetmanager_offsets_t
// {
//   jfieldID mObject;
// };
// // This is also used by asset_manager.cpp.
// static final assetmanager_offsets_t gAssetManagerOffsets = new assetmanager_offsets_t();
//
// static class apkassetsfields {
//   jfieldID native_ptr;
// }
// static final apkassetsfields gApkAssetsFields = new apkassetsfields();
//
// static class sparsearray_offsets_t {
//   jclass classObject;
//   jmethodID constructor;
//   jmethodID put;
// }
// static final sparsearray_offsets_t gSparseArrayOffsets = new sparsearray_offsets_t();
//
// static class configuration_offsets_t {
//   jclass classObject;
//   jmethodID constructor;
//   jfieldID mSmallestScreenWidthDpOffset;
//   jfieldID mScreenWidthDpOffset;
//   jfieldID mScreenHeightDpOffset;
// }
// static final configuration_offsets_t gConfigurationOffsets = new configuration_offsets_t();
//
// jclass g_stringClass = nullptr;
//
// // ----------------------------------------------------------------------------

  @Implementation
  protected static void createSystemAssetsInZygoteLocked() {
    _AssetManager28_ _assetManagerStatic_ = reflector(_AssetManager28_.class);
    AssetManager sSystem = _assetManagerStatic_.getSystem();
    if (sSystem != null) {
      return;
    }

    if (systemCppAssetManager2 == null) {
      // first time! let the framework create a CppAssetManager2 and an AssetManager, which we'll
      // hang on to.
      directlyOn(AssetManager.class, "createSystemAssetsInZygoteLocked");
      cachedSystemApkAssets = _assetManagerStatic_.getSystemApkAssets();
      cachedSystemApkAssetsSet = _assetManagerStatic_.getSystemApkAssetsSet();
    } else {
      // reuse the shared system CppAssetManager2; create a new AssetManager around it.
      _assetManagerStatic_.setSystemApkAssets(cachedSystemApkAssets);
      _assetManagerStatic_.setSystemApkAssetsSet(cachedSystemApkAssetsSet);

      sSystem = ReflectionHelpers.callConstructor(AssetManager.class,
          ClassParameter.from(boolean.class, true /*sentinel*/));
      sSystem.setApkAssets(cachedSystemApkAssets, false /*invalidateCaches*/);
      ReflectionHelpers.setStaticField(AssetManager.class, "sSystem", sSystem);
    }
  }

  @Resetter
  public static void reset() {
    // todo: ShadowPicker doesn't discriminate properly between concrete shadow classes for resetters...
    if (!useLegacy() && RuntimeEnvironment.getApiLevel() >= P) {
      _AssetManager28_ _assetManagerStatic_ = reflector(_AssetManager28_.class);
      _assetManagerStatic_.setSystemApkAssetsSet(null);
      _assetManagerStatic_.setSystemApkAssets(null);
      _assetManagerStatic_.setSystem(null);
    }
  }

  // Java asset cookies have 0 as an invalid cookie, but TypedArray expects < 0.
  static int ApkAssetsCookieToJavaCookie(ApkAssetsCookie cookie) {
    return cookie.intValue() != kInvalidCookie ? (cookie.intValue() + 1) : -1;
  }

  static ApkAssetsCookie JavaCookieToApkAssetsCookie(int cookie) {
    return ApkAssetsCookie.forInt(cookie > 0 ? (cookie - 1) : kInvalidCookie);
  }

  // This is called by zygote (running as user root) as part of preloadResources.
// static void NativeVerifySystemIdmaps(JNIEnv* /*env*/, jclass /*clazz*/) {
  @Implementation(minSdk = P)
  protected static void nativeVerifySystemIdmaps() {
    return;

    // todo: maybe implement?
    // switch (pid_t pid = fork()) {
    //   case -1:
    //     PLOG(ERROR) << "failed to fork for idmap";
    //     break;
    //
    //   // child
    //   case 0: {
    //     struct __user_cap_header_struct capheader;
    //     struct __user_cap_data_struct capdata;
    //
    //     memset(&capheader, 0, sizeof(capheader));
    //     memset(&capdata, 0, sizeof(capdata));
    //
    //     capheader.version = _LINUX_CAPABILITY_VERSION;
    //     capheader.pid = 0;
    //
    //     if (capget(&capheader, &capdata) != 0) {
    //       PLOG(ERROR) << "capget";
    //       exit(1);
    //     }
    //
    //     capdata.effective = capdata.permitted;
    //     if (capset(&capheader, &capdata) != 0) {
    //       PLOG(ERROR) << "capset";
    //       exit(1);
    //     }
    //
    //     if (setgid(AID_SYSTEM) != 0) {
    //       PLOG(ERROR) << "setgid";
    //       exit(1);
    //     }
    //
    //     if (setuid(AID_SYSTEM) != 0) {
    //       PLOG(ERROR) << "setuid";
    //       exit(1);
    //     }
    //
    //     // Generic idmap parameters
    //     char* argv[8];
    //     int argc = 0;
    //     struct stat st;
    //
    //     memset(argv, 0, sizeof(argv));
    //     argv[argc++] = AssetManager.IDMAP_BIN;
    //     argv[argc++] = "--scan";
    //     argv[argc++] = AssetManager.TARGET_PACKAGE_NAME;
    //     argv[argc++] = AssetManager.TARGET_APK_PATH;
    //     argv[argc++] = AssetManager.IDMAP_DIR;
    //
    //     // Directories to scan for overlays: if OVERLAY_THEME_DIR_PROPERTY is defined,
    //     // use OVERLAY_DIR/<value of OVERLAY_THEME_DIR_PROPERTY> in addition to OVERLAY_DIR.
    //     String overlay_theme_path = base.GetProperty(AssetManager.OVERLAY_THEME_DIR_PROPERTY,
    //         "");
    //     if (!overlay_theme_path.empty()) {
    //       overlay_theme_path = String(AssetManager.OVERLAY_DIR) + "/" + overlay_theme_path;
    //       if (stat(overlay_theme_path, &st) == 0) {
    //         argv[argc++] = overlay_theme_path;
    //       }
    //     }
    //
    //     if (stat(AssetManager.OVERLAY_DIR, &st) == 0) {
    //       argv[argc++] = AssetManager.OVERLAY_DIR;
    //     }
    //
    //     if (stat(AssetManager.PRODUCT_OVERLAY_DIR, &st) == 0) {
    //       argv[argc++] = AssetManager.PRODUCT_OVERLAY_DIR;
    //     }
    //
    //     // Finally, invoke idmap (if any overlay directory exists)
    //     if (argc > 5) {
    //       execv(AssetManager.IDMAP_BIN, (char* const*)argv);
    //       PLOG(ERROR) << "failed to execv for idmap";
    //       exit(1); // should never get here
    //     } else {
    //       exit(0);
    //     }
    //   } break;
    //
    //   // parent
    //   default:
    //     waitpid(pid, null, 0);
    //     break;
    // }
  }

  static int CopyValue(/*JNIEnv* env,*/ ApkAssetsCookie cookie, Res_value value, int ref,
      int type_spec_flags, ResTable_config config, TypedValue out_typed_value) {
    out_typed_value.type = value.dataType;
    out_typed_value.assetCookie = ApkAssetsCookieToJavaCookie(cookie);
    out_typed_value.data = value.data;
    out_typed_value.string = null;
    out_typed_value.resourceId = ref;
    out_typed_value.changingConfigurations = type_spec_flags;
    if (config != null) {
      out_typed_value.density = config.density;
    }
    return (int) (ApkAssetsCookieToJavaCookie(cookie));
  }

  //  @Override
  //  protected int addAssetPathNative(String path) {
  //    throw new UnsupportedOperationException(); // todo
  //  }

  @Override
  Collection<Path> getAllAssetDirs() {
    ApkAssets[] apkAssetsArray = reflector(_AssetManager28_.class, realAssetManager).getApkAssets();

    ArrayList<Path> assetDirs = new ArrayList<>();
    for (ApkAssets apkAssets : apkAssetsArray) {
      long apk_assets_native_ptr = ((ShadowArscApkAssets9) Shadow.extract(apkAssets)).getNativePtr();
      CppApkAssets cppApkAssets = Registries.NATIVE_APK_ASSETS_REGISTRY.getNativeObject(apk_assets_native_ptr);

      if (new File(cppApkAssets.GetPath()).isFile()) {
        assetDirs.add(Fs.forJar(Paths.get(cppApkAssets.GetPath())).getPath("assets"));
      } else {
        assetDirs.add(Paths.get(cppApkAssets.GetPath()));
      }
    }
    return assetDirs;
  }

  @Override
  List<AssetPath> getAssetPaths() {
    return AssetManagerForJavaObject(realAssetManager).getAssetPaths();
  }

// ----------------------------------------------------------------------------

  // interface AAssetManager {}
  //
  // // Let the opaque type AAssetManager refer to a guarded AssetManager2 instance.
  // static class GuardedAssetManager implements AAssetManager {
  //   CppAssetManager2 guarded_assetmanager = new CppAssetManager2();
  // }

  static CppAssetManager2 NdkAssetManagerForJavaObject(/* JNIEnv* env,*/ AssetManager jassetmanager) {
    // long assetmanager_handle = env.GetLongField(jassetmanager, gAssetManagerOffsets.mObject);
    long assetmanager_handle = ReflectionHelpers.getField(jassetmanager, "mObject");
    CppAssetManager2 am = Registries.NATIVE_ASSET_MANAGER_REGISTRY.getNativeObject(assetmanager_handle);
    if (am == null) {
      throw new IllegalStateException("AssetManager has been finalized!");
    }
    return am;
  }

  static CppAssetManager2 AssetManagerForJavaObject(/* JNIEnv* env,*/ AssetManager jassetmanager) {
    return NdkAssetManagerForJavaObject(jassetmanager);
  }

  static CppAssetManager2 AssetManagerFromLong(long ptr) {
    // return *AssetManagerForNdkAssetManager(reinterpret_cast<AAssetManager>(ptr));
    return Registries.NATIVE_ASSET_MANAGER_REGISTRY.getNativeObject(ptr);
  }

  static ParcelFileDescriptor ReturnParcelFileDescriptor(/* JNIEnv* env,*/ Asset asset,
      long[] out_offsets) throws FileNotFoundException {
    final Ref<Long> start_offset = new Ref<>(0L);
    final Ref<Long> length = new Ref<>(0L);
    FileDescriptor fd = asset.openFileDescriptor(start_offset, length);
    // asset.reset();

    if (fd == null) {
      throw new FileNotFoundException(
          "This file can not be opened as a file descriptor; it is probably compressed");
    }

    long[] offsets = out_offsets; // reinterpret_cast<long*>(env.GetPrimitiveArrayCritical(out_offsets, 0));
    if (offsets == null) {
      // close(fd);
      return null;
    }

    offsets[0] = start_offset.get();
    offsets[1] = length.get();

    // env.ReleasePrimitiveArrayCritical(out_offsets, offsets, 0);

    FileDescriptor file_desc = fd; // jniCreateFileDescriptor(env, fd);
    // if (file_desc == null) {
    //   close(fd);
    //   return null;
    // }

    // TODO: consider doing this
    // return new ParcelFileDescriptor(file_desc);
    return ParcelFileDescriptor.open(asset.getFile(), ParcelFileDescriptor.MODE_READ_ONLY);
  }

  /**
   * Used for the creation of system assets.
   */
  @Implementation(minSdk = P)
  protected void __constructor__(boolean sentinel) {
    inNonSystemConstructor = true;
    try {
      // call real constructor so field initialization happens.
      invokeConstructor(
          AssetManager.class, realAssetManager, ClassParameter.from(boolean.class, sentinel));
    } finally {
      inNonSystemConstructor = false;
    }
  }

  // static jint NativeGetGlobalAssetCount(JNIEnv* /*env*/, jobject /*clazz*/) {
  @Implementation(minSdk = P)
  protected static int getGlobalAssetCount() {
    return Asset.getGlobalCount();
  }

  // static jobject NativeGetAssetAllocations(JNIEnv* env, jobject /*clazz*/) {
  @Implementation(minSdk = P)
  protected static String getAssetAllocations() {
    String alloc = Asset.getAssetAllocations();
    if (alloc.length() <= 0) {
      return null;
    }
    return alloc;
  }

  // static jint NativeGetGlobalAssetManagerCount(JNIEnv* /*env*/, jobject /*clazz*/) {
  @Implementation(minSdk = P)
  protected static int getGlobalAssetManagerCount() {
    // TODO(adamlesinski): Switch to AssetManager2.
    return CppAssetManager.getGlobalCount();
  }

  // static jlong NativeCreate(JNIEnv* /*env*/, jclass /*clazz*/) {
  @Implementation(minSdk = P)
  protected static long nativeCreate() {
    // AssetManager2 needs to be protected by a lock. To avoid cache misses, we allocate the lock and
    // AssetManager2 in a contiguous block (GuardedAssetManager).
    // return reinterpret_cast<long>(new GuardedAssetManager());

    long cppAssetManagerRef;

    // we want to share a single instance of the system CppAssetManager2
    if (inNonSystemConstructor) {
      CppAssetManager2 appAssetManager = new CppAssetManager2();
      cppAssetManagerRef = Registries.NATIVE_ASSET_MANAGER_REGISTRY.register(appAssetManager);
    } else {
      if (systemCppAssetManager2 == null) {
        systemCppAssetManager2 = new CppAssetManager2();
        systemCppAssetManager2Ref =
            Registries.NATIVE_ASSET_MANAGER_REGISTRY.register(systemCppAssetManager2);
      }

      cppAssetManagerRef = systemCppAssetManager2Ref;
    }

    return cppAssetManagerRef;
  }

    // static void NativeDestroy(JNIEnv* /*env*/, jclass /*clazz*/, jlong ptr) {
  @Implementation(minSdk = P)
  protected static void nativeDestroy(long ptr) {
    if (ptr == systemCppAssetManager2Ref) {
      // don't destroy the shared system CppAssetManager2!
      return;
    }

    // delete reinterpret_cast<GuardedAssetManager*>(ptr);
    Registries.NATIVE_ASSET_MANAGER_REGISTRY.unregister(ptr);
  }

  // static void NativeSetApkAssets(JNIEnv* env, jclass /*clazz*/, jlong ptr,
//                                jobjectArray apk_assets_array, jboolean invalidate_caches) {
  @Implementation(minSdk = P)
  protected static void nativeSetApkAssets(long ptr, @NonNull android.content.res.ApkAssets[] apk_assets_array,
      boolean invalidate_caches) {
    ATRACE_NAME("AssetManager::SetApkAssets");

    int apk_assets_len = apk_assets_array.length;
    List<CppApkAssets> apk_assets = new ArrayList<>();
    // apk_assets.reserve(apk_assets_len);
    for (int i = 0; i < apk_assets_len; i++) {
      android.content.res.ApkAssets apkAssets = apk_assets_array[i]; // env.GetObjectArrayElement(apk_assets_array, i);
      if (apkAssets == null) {
        throw new NullPointerException(String.format("ApkAssets at index %d is null", i));
      }

      long apk_assets_native_ptr = ((ShadowArscApkAssets9) Shadow.extract(apkAssets)).getNativePtr();
      // if (env.ExceptionCheck()) {
      //   return;
      // }
      apk_assets.add(Registries.NATIVE_APK_ASSETS_REGISTRY.getNativeObject(apk_assets_native_ptr));
    }

    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    assetmanager.SetApkAssets(apk_assets, invalidate_caches);
  }

  // static void NativeSetConfiguration(JNIEnv* env, jclass /*clazz*/, jlong ptr, jint mcc, jint mnc,
//                                    jstring locale, jint orientation, jint touchscreen, jint density,
//                                    jint keyboard, jint keyboard_hidden, jint navigation,
//                                    jint screen_width, jint screen_height,
//                                    jint smallest_screen_width_dp, jint screen_width_dp,
//                                    jint screen_height_dp, jint screen_layout, jint ui_mode,
//                                    jint color_mode, jint major_version) {
  @Implementation(minSdk = P)
  protected static void nativeSetConfiguration(long ptr, int mcc, int mnc,
      @Nullable String locale, int orientation, int touchscreen, int density, int keyboard,
      int keyboard_hidden, int navigation, int screen_width, int screen_height,
      int smallest_screen_width_dp, int screen_width_dp, int screen_height_dp, int screen_layout,
      int ui_mode, int color_mode, int major_version) {
    ATRACE_NAME("AssetManager::SetConfiguration");

    ResTable_config configuration = new ResTable_config();
    // memset(&configuration, 0, sizeof(configuration));
    configuration.mcc = (short) (mcc);
    configuration.mnc = (short) (mnc);
    configuration.orientation = (byte) (orientation);
    configuration.touchscreen = (byte) (touchscreen);
    configuration.density = (short) (density);
    configuration.keyboard = (byte) (keyboard);
    configuration.inputFlags = (byte) (keyboard_hidden);
    configuration.navigation = (byte) (navigation);
    configuration.screenWidth = (short) (screen_width);
    configuration.screenHeight = (short) (screen_height);
    configuration.smallestScreenWidthDp = (short) (smallest_screen_width_dp);
    configuration.screenWidthDp = (short) (screen_width_dp);
    configuration.screenHeightDp = (short) (screen_height_dp);
    configuration.screenLayout = (byte) (screen_layout);
    configuration.uiMode = (byte) (ui_mode);
    configuration.colorMode = (byte) (color_mode);
    configuration.sdkVersion = (short) (major_version);

    if (locale != null) {
      String locale_utf8 = locale;
      CHECK(locale_utf8 != null);
      configuration.setBcp47Locale(locale_utf8);
    }

    // Constants duplicated from Java class android.content.res.Configuration.
    int kScreenLayoutRoundMask = 0x300;
    int kScreenLayoutRoundShift = 8;

    // In Java, we use a 32bit integer for screenLayout, while we only use an 8bit integer
    // in C++. We must extract the round qualifier out of the Java screenLayout and put it
    // into screenLayout2.
    configuration.screenLayout2 =
        (byte) ((screen_layout & kScreenLayoutRoundMask) >> kScreenLayoutRoundShift);

    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    assetmanager.SetConfiguration(configuration);
  }

  // static jobject NativeGetAssignedPackageIdentifiers(JNIEnv* env, jclass /*clazz*/, jlong ptr) {
  @Implementation(minSdk = P)
  protected static @NonNull SparseArray<String> nativeGetAssignedPackageIdentifiers(
      long ptr) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);

    SparseArray<String> sparse_array =
        new SparseArray<>();

    if (sparse_array == null) {
      // An exception is pending.
      return null;
    }

    assetmanager.ForEachPackage((String package_name, byte package_id) -> {
      String jpackage_name = package_name; // env.NewStringUTF(package_name);
      if (jpackage_name == null) {
        // An exception is pending.
        return;
      }

      // env.CallVoidMethod(sparse_array, gSparseArrayOffsets.put, (int) (package_id),
      //     jpackage_name);
      sparse_array.put(package_id, jpackage_name);
    });
    return sparse_array;
  }

  // static jobjectArray NativeList(JNIEnv* env, jclass /*clazz*/, jlong ptr, jstring path) {
  @Implementation(minSdk = P)
  protected static @Nullable String[] nativeList(long ptr, @NonNull String path)
      throws IOException {
    String path_utf8 = path;
    if (path_utf8 == null) {
      // This will throw NPE.
      return null;
    }

    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    AssetDir asset_dir =
        assetmanager.OpenDir(path_utf8);
    if (asset_dir == null) {
      throw new FileNotFoundException(path_utf8);
    }

    int file_count = asset_dir.getFileCount();

    String[] array = new String[file_count]; // env.NewObjectArray(file_count, g_stringClass, null);
    // if (array == null) {
    //   return null;
    // }

    for (int i = 0; i < file_count; i++) {
      String java_string = asset_dir.getFileName(i).string();

      // Check for errors creating the strings (if malformed or no memory).
      // if (env.ExceptionCheck()) {
      //   return null;
      // }

      // env.SetObjectArrayElement(array, i, java_string);
      array[i] = java_string;

      // If we have a large amount of string in our array, we might overflow the
      // local reference table of the VM.
      // env.DeleteLocalRef(java_string);
    }
    return array;
  }

  // static jlong NativeOpenAsset(JNIEnv* env, jclass /*clazz*/, jlong ptr, jstring asset_path,
//                              jint access_mode) {
  @Implementation(minSdk = P)
  protected static long nativeOpenAsset(long ptr, @NonNull String asset_path, int access_mode)
      throws FileNotFoundException {
    String asset_path_utf8 = asset_path;
    if (asset_path_utf8 == null) {
      // This will throw NPE.
      return 0;
    }

    ATRACE_NAME(String.format("AssetManager::OpenAsset(%s)", asset_path_utf8));

    if (access_mode != Asset.AccessMode.ACCESS_UNKNOWN.mode()
        && access_mode != Asset.AccessMode.ACCESS_RANDOM.mode()
        && access_mode != Asset.AccessMode.ACCESS_STREAMING.mode()
        && access_mode != Asset.AccessMode.ACCESS_BUFFER.mode()) {
      throw new IllegalArgumentException("Bad access mode");
    }

    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    Asset asset =
        assetmanager.Open(asset_path_utf8, Asset.AccessMode.fromInt(access_mode));
    if (!isTruthy(asset)) {
      throw new FileNotFoundException(asset_path_utf8);
    }
    return Registries.NATIVE_ASSET_REGISTRY.register(asset);
  }

  // static jobject NativeOpenAssetFd(JNIEnv* env, jclass /*clazz*/, jlong ptr, jstring asset_path,
//                                  jlongArray out_offsets) {
  @Implementation(minSdk = P)
  protected static ParcelFileDescriptor nativeOpenAssetFd(long ptr,
      @NonNull String asset_path, long[] out_offsets) throws IOException {
    String asset_path_utf8 = asset_path;
    if (asset_path_utf8 == null) {
      // This will throw NPE.
      return null;
    }

    ATRACE_NAME(String.format("AssetManager::OpenAssetFd(%s)", asset_path_utf8));

    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    Asset asset = assetmanager.Open(asset_path_utf8, Asset.AccessMode.ACCESS_RANDOM);
    if (!isTruthy(asset)) {
      throw new FileNotFoundException(asset_path_utf8);
    }
    return ReturnParcelFileDescriptor(asset, out_offsets);
  }

  // static jlong NativeOpenNonAsset(JNIEnv* env, jclass /*clazz*/, jlong ptr, jint jcookie,
//                                 jstring asset_path, jint access_mode) {
  @Implementation(minSdk = P)
  protected static long nativeOpenNonAsset(long ptr, int jcookie, @NonNull String asset_path,
      int access_mode) throws FileNotFoundException {
    ApkAssetsCookie cookie = JavaCookieToApkAssetsCookie(jcookie);
    String asset_path_utf8 = asset_path;
    if (asset_path_utf8 == null) {
      // This will throw NPE.
      return 0;
    }

    ATRACE_NAME(String.format("AssetManager::OpenNonAsset(%s)", asset_path_utf8));

    if (access_mode != Asset.AccessMode.ACCESS_UNKNOWN.mode()
        && access_mode != Asset.AccessMode.ACCESS_RANDOM.mode()
        && access_mode != Asset.AccessMode.ACCESS_STREAMING.mode()
        && access_mode != Asset.AccessMode.ACCESS_BUFFER.mode()) {
      throw new IllegalArgumentException("Bad access mode");
    }

    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    Asset asset;
    if (cookie.intValue() != kInvalidCookie) {
      asset = assetmanager.OpenNonAsset(asset_path_utf8, cookie,
          Asset.AccessMode.fromInt(access_mode));
    } else {
      asset = assetmanager.OpenNonAsset(asset_path_utf8,
          Asset.AccessMode.fromInt(access_mode));
    }

    if (!isTruthy(asset)) {
      throw new FileNotFoundException(asset_path_utf8);
    }
    return Registries.NATIVE_ASSET_REGISTRY.register(asset);
  }

  // static jobject NativeOpenNonAssetFd(JNIEnv* env, jclass /*clazz*/, jlong ptr, jint jcookie,
//                                     jstring asset_path, jlongArray out_offsets) {
  @Implementation(minSdk = P)
  protected static @Nullable ParcelFileDescriptor nativeOpenNonAssetFd(long ptr, int jcookie,
      @NonNull String asset_path, @NonNull long[] out_offsets) throws IOException {
    ApkAssetsCookie cookie = JavaCookieToApkAssetsCookie(jcookie);
    String asset_path_utf8 = asset_path;
    if (asset_path_utf8 == null) {
      // This will throw NPE.
      return null;
    }

    ATRACE_NAME(String.format("AssetManager::OpenNonAssetFd(%s)", asset_path_utf8));

    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    Asset asset;
    if (cookie.intValue() != kInvalidCookie) {
      asset = assetmanager.OpenNonAsset(asset_path_utf8, cookie, Asset.AccessMode.ACCESS_RANDOM);
    } else {
      asset = assetmanager.OpenNonAsset(asset_path_utf8, Asset.AccessMode.ACCESS_RANDOM);
    }

    if (!isTruthy(asset)) {
      throw new FileNotFoundException(asset_path_utf8);
    }
    return ReturnParcelFileDescriptor(asset, out_offsets);
  }

  // static jlong NativeOpenXmlAsset(JNIEnv* env, jobject /*clazz*/, jlong ptr, jint jcookie,
//                                 jstring asset_path) {
  @Implementation(minSdk = P)
  protected static long nativeOpenXmlAsset(long ptr, int jcookie, @NonNull String asset_path)
      throws FileNotFoundException {
    ApkAssetsCookie cookie = JavaCookieToApkAssetsCookie(jcookie);
    String asset_path_utf8 = asset_path;
    if (asset_path_utf8 == null) {
      // This will throw NPE.
      return 0;
    }

    ATRACE_NAME(String.format("AssetManager::OpenXmlAsset(%s)", asset_path_utf8));

    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    Asset asset;
    if (cookie.intValue() != kInvalidCookie) {
      asset = assetmanager.OpenNonAsset(asset_path_utf8, cookie, Asset.AccessMode.ACCESS_RANDOM);
    } else {
      Ref<ApkAssetsCookie> cookieRef = new Ref<>(cookie);
      asset = assetmanager.OpenNonAsset(asset_path_utf8, Asset.AccessMode.ACCESS_RANDOM, cookieRef);
      cookie = cookieRef.get();
    }

    if (!isTruthy(asset)) {
      throw new FileNotFoundException(asset_path_utf8);
    }

    // May be nullptr.
    DynamicRefTable dynamic_ref_table = assetmanager.GetDynamicRefTableForCookie(cookie);

    ResXMLTree xml_tree = new ResXMLTree(dynamic_ref_table);
    int err = xml_tree.setTo(asset.getBuffer(true), (int) asset.getLength(), true);
    // asset.reset();

    if (err != NO_ERROR) {
      throw new FileNotFoundException("Corrupt XML binary file");
    }
    return NATIVE_RES_XML_TREES.register(xml_tree);
  }

  // static jint NativeGetResourceValue(JNIEnv* env, jclass /*clazz*/, jlong ptr, jint resid,
//                                    jshort density, jobject typed_value,
//                                    jboolean resolve_references) {
  @Implementation(minSdk = P)
  protected static int nativeGetResourceValue(long ptr, @AnyRes int resid, short density,
      @NonNull TypedValue typed_value, boolean resolve_references) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    final Ref<Res_value> value = new Ref<>(null);
    final Ref<ResTable_config> selected_config = new Ref<>(null);
    final Ref<Integer> flags = new Ref<>(0);
    ApkAssetsCookie cookie =
        assetmanager.GetResource(resid, false /*may_be_bag*/,
            (short) (density), value, selected_config, flags);
    if (cookie.intValue() == kInvalidCookie) {
      return ApkAssetsCookieToJavaCookie(K_INVALID_COOKIE);
    }

    final Ref<Integer> ref = new Ref<>(resid);
    if (resolve_references) {
      cookie = assetmanager.ResolveReference(cookie, value, selected_config, flags, ref);
      if (cookie.intValue() == kInvalidCookie) {
        return ApkAssetsCookieToJavaCookie(K_INVALID_COOKIE);
      }
    }
    return CopyValue(cookie, value.get(), ref.get(), flags.get(), selected_config.get(), typed_value);
  }

  // static jint NativeGetResourceBagValue(JNIEnv* env, jclass /*clazz*/, jlong ptr, jint resid,
//                                       jint bag_entry_id, jobject typed_value) {
  @Implementation(minSdk = P)
  protected static int nativeGetResourceBagValue(long ptr, @AnyRes int resid, int bag_entry_id,
      @NonNull TypedValue typed_value) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    ResolvedBag bag = assetmanager.GetBag(resid);
    if (bag == null) {
      return ApkAssetsCookieToJavaCookie(K_INVALID_COOKIE);
    }

    final Ref<Integer> type_spec_flags = new Ref<>(bag.type_spec_flags);
    ApkAssetsCookie cookie = K_INVALID_COOKIE;
    Res_value bag_value = null;
    for (ResolvedBag.Entry entry : bag.entries) {
      if (entry.key == (int) (bag_entry_id)) {
        cookie = entry.cookie;
        bag_value = entry.value;

        // Keep searching (the old implementation did that).
      }
    }

    if (cookie.intValue() == kInvalidCookie) {
      return ApkAssetsCookieToJavaCookie(K_INVALID_COOKIE);
    }

    final Ref<Res_value> value = new Ref<>(bag_value);
    final Ref<Integer> ref = new Ref<>(resid);
    final Ref<ResTable_config> selected_config = new Ref<>(null);
    cookie = assetmanager.ResolveReference(cookie, value, selected_config, type_spec_flags, ref);
    if (cookie.intValue() == kInvalidCookie) {
      return ApkAssetsCookieToJavaCookie(K_INVALID_COOKIE);
    }
    return CopyValue(cookie, value.get(), ref.get(), type_spec_flags.get(), null, typed_value);
  }

  // static jintArray NativeGetStyleAttributes(JNIEnv* env, jclass /*clazz*/, jlong ptr, jint resid) {
  @Implementation(minSdk = P)
  protected static @Nullable @AttrRes int[] nativeGetStyleAttributes(long ptr,
      @StyleRes int resid) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    ResolvedBag bag = assetmanager.GetBag(resid);
    if (bag == null) {
      return null;
    }

    int[] array = new int[bag.entry_count];
    // if (env.ExceptionCheck()) {
    //   return null;
    // }

    for (int i = 0; i < bag.entry_count; i++) {
      int attr_resid = bag.entries[i].key;
      // env.SetIntArrayRegion(array, i, 1, &attr_resid);
      array[i] = attr_resid;
    }
    return array;
  }

  // static jobjectArray NativeGetResourceStringArray(JNIEnv* env, jclass /*clazz*/, jlong ptr,
//                                                  jint resid) {
  @Implementation(minSdk = P)
  protected static @Nullable String[] nativeGetResourceStringArray(long ptr,
      @ArrayRes int resid) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    ResolvedBag bag = assetmanager.GetBag(resid);
    if (bag == null) {
      return null;
    }

    String[] array = new String[bag.entry_count];
    if (array == null) {
      return null;
    }

    for (int i = 0; i < bag.entry_count; i++) {
      ResolvedBag.Entry entry = bag.entries[i];

      // Resolve any references to their final value.
      final Ref<Res_value> value = new Ref<>(entry.value);
      final Ref<ResTable_config> selected_config = new Ref<>(null);
      final Ref<Integer> flags = new Ref<>(0);
      final Ref<Integer> ref = new Ref<>(0);
      ApkAssetsCookie cookie =
          assetmanager.ResolveReference(entry.cookie, value, selected_config, flags, ref);
      if (cookie.intValue() == kInvalidCookie) {
        return null;
      }

      if (value.get().dataType == Res_value.TYPE_STRING) {
        CppApkAssets apk_assets = assetmanager.GetApkAssets().get(cookie.intValue());
        ResStringPool pool = apk_assets.GetLoadedArsc().GetStringPool();

        String java_string = null;
        int str_len;
        String str_utf8 = pool.stringAt(value.get().data);
        if (str_utf8 != null) {
          java_string = str_utf8;
        } else {
          String str_utf16 = pool.stringAt(value.get().data);
          java_string = str_utf16;
        }

        // // Check for errors creating the strings (if malformed or no memory).
        // if (env.ExceptionCheck()) {
        //   return null;
        // }

        // env.SetObjectArrayElement(array, i, java_string);
        array[i] = java_string;

        // If we have a large amount of string in our array, we might overflow the
        // local reference table of the VM.
        // env.DeleteLocalRef(java_string);
      }
    }
    return array;
  }

  // static jintArray NativeGetResourceStringArrayInfo(JNIEnv* env, jclass /*clazz*/, jlong ptr,
//                                                   jint resid) {
  @Implementation(minSdk = P)
  protected static @Nullable int[] nativeGetResourceStringArrayInfo(long ptr,
      @ArrayRes int resid) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    ResolvedBag bag = assetmanager.GetBag(resid);
    if (bag == null) {
      return null;
    }

    int[] array = new int[bag.entry_count * 2];
    // if (array == null) {
    //   return null;
    // }

    int[] buffer = array; //reinterpret_cast<int*>(env.GetPrimitiveArrayCritical(array, null));
    // if (buffer == null) {
    //   return null;
    // }

    for (int i = 0; i < bag.entry_count; i++) {
      ResolvedBag.Entry entry = bag.entries[i];
      final Ref<Res_value> value = new Ref<>(entry.value);
      final Ref<ResTable_config> selected_config = new Ref<>(null);
      final Ref<Integer> flags = new Ref<>(0);
      final Ref<Integer> ref = new Ref<>(0);
      ApkAssetsCookie cookie =
          assetmanager.ResolveReference(entry.cookie, value, selected_config, flags, ref);
      if (cookie.intValue() == kInvalidCookie) {
        // env.ReleasePrimitiveArrayCritical(array, buffer, JNI_ABORT);
        return null;
      }

      int string_index = -1;
      if (value.get().dataType == Res_value.TYPE_STRING) {
        string_index = (int) (value.get().data);
      }

      buffer[i * 2] = ApkAssetsCookieToJavaCookie(cookie);
      buffer[(i * 2) + 1] = string_index;
    }
    // env.ReleasePrimitiveArrayCritical(array, buffer, 0);
    return array;
  }

  // static jintArray NativeGetResourceIntArray(JNIEnv* env, jclass /*clazz*/, jlong ptr, jint resid) {
  @Implementation(minSdk = P)
  protected static @Nullable int[] nativeGetResourceIntArray(long ptr, @ArrayRes int resid) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    ResolvedBag bag = assetmanager.GetBag(resid);
    if (bag == null) {
      return null;
    }

    int[] array = new int[bag.entry_count];
    // if (array == null) {
    //   return null;
    // }

    int[] buffer = array; // reinterpret_cast<int*>(env.GetPrimitiveArrayCritical(array, null));
    // if (buffer == null) {
    //   return null;
    // }

    for (int i = 0; i < bag.entry_count; i++) {
      ResolvedBag.Entry entry = bag.entries[i];
      final Ref<Res_value> value = new Ref<>(entry.value);
      final Ref<ResTable_config> selected_config = new Ref<>(null);
      final Ref<Integer> flags = new Ref<>(0);
      final Ref<Integer> ref = new Ref<>(0);
      ApkAssetsCookie cookie =
          assetmanager.ResolveReference(entry.cookie, value, selected_config, flags, ref);
      if (cookie.intValue() == kInvalidCookie) {
        // env.ReleasePrimitiveArrayCritical(array, buffer, JNI_ABORT);
        return null;
      }

      if (value.get().dataType >= Res_value.TYPE_FIRST_INT && value.get().dataType <= Res_value.TYPE_LAST_INT) {
        buffer[i] = (int) (value.get().data);
      }
    }
    // env.ReleasePrimitiveArrayCritical(array, buffer, 0);
    return array;
  }

  // static jint NativeGetResourceArraySize(JNIEnv* /*env*/, jclass /*clazz*/, jlong ptr, jint resid) {
  @Implementation(minSdk = P)
  protected static int nativeGetResourceArraySize(long ptr, @ArrayRes int resid) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    ResolvedBag bag = assetmanager.GetBag(resid);
    if (bag == null) {
      return -1;
    }
    return (int) (bag.entry_count);
  }

  // static jint NativeGetResourceArray(JNIEnv* env, jclass /*clazz*/, jlong ptr, jint resid,
//                                    jintArray out_data) {
  @Implementation(minSdk = P)
  protected static int nativeGetResourceArray(long ptr, @ArrayRes int resid,
      @NonNull int[] out_data) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    ResolvedBag bag = assetmanager.GetBag(resid);
    if (bag == null) {
      return -1;
    }

    int out_data_length = out_data.length;
    // if (env.ExceptionCheck()) {
    //   return -1;
    // }

    if ((int) (bag.entry_count) > out_data_length * STYLE_NUM_ENTRIES) {
      throw new IllegalArgumentException("Input array is not large enough");
    }

    int[] buffer = out_data; // reinterpret_cast<int*>(env.GetPrimitiveArrayCritical(out_data, null));
    if (buffer == null) {
      return -1;
    }

    int[] cursor = buffer;
    for (int i = 0; i < bag.entry_count; i++) {
      ResolvedBag.Entry entry = bag.entries[i];
      final Ref<Res_value> value = new Ref<>(entry.value);
      final Ref<ResTable_config> selected_config = new Ref<>(new ResTable_config());
      selected_config.get().density = 0;
      final Ref<Integer> flags = new Ref<>(bag.type_spec_flags);
      final Ref<Integer> ref = new Ref<>(0);
      ApkAssetsCookie cookie =
          assetmanager.ResolveReference(entry.cookie, value, selected_config, flags, ref);
      if (cookie.intValue() == kInvalidCookie) {
        // env.ReleasePrimitiveArrayCritical(out_data, buffer, JNI_ABORT);
        return -1;
      }

      // Deal with the special @null value -- it turns back to TYPE_NULL.
      if (value.get().dataType == Res_value.TYPE_REFERENCE && value.get().data == 0) {
        value.set(Res_value.NULL_VALUE);
      }

      int offset = i * STYLE_NUM_ENTRIES;
      cursor[offset + STYLE_TYPE] = (int) (value.get().dataType);
      cursor[offset + STYLE_DATA] = (int) (value.get().data);
      cursor[offset + STYLE_ASSET_COOKIE] = ApkAssetsCookieToJavaCookie(cookie);
      cursor[offset + STYLE_RESOURCE_ID] = (int) (ref.get());
      cursor[offset + STYLE_CHANGING_CONFIGURATIONS] = (int) (flags.get());
      cursor[offset + STYLE_DENSITY] = (int) (selected_config.get().density);
      // cursor += STYLE_NUM_ENTRIES;
    }
    // env.ReleasePrimitiveArrayCritical(out_data, buffer, 0);
    return (int) (bag.entry_count);
  }

  // static jint NativeGetResourceIdentifier(JNIEnv* env, jclass /*clazz*/, jlong ptr, jstring name,
//                                         jstring def_type, jstring def_package) {
  @Implementation(minSdk = P)
  protected static @AnyRes int nativeGetResourceIdentifier(long ptr, @NonNull String name,
      @Nullable String def_type, @Nullable String def_package) {
    String name_utf8 = name;
    if (name_utf8 == null) {
      // This will throw NPE.
      return 0;
    }

    String type = null;
    if (def_type != null) {
      String type_utf8 = def_type;
      CHECK(type_utf8 != null);
      type = type_utf8;
    }

    String package_ = null;
    if (def_package != null) {
      String package_utf8 = def_package;
      CHECK(package_utf8 != null);
      package_ = package_utf8;
    }
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    return (int) (assetmanager.GetResourceId(name_utf8, type, package_));
  }

  // static jstring NativeGetResourceName(JNIEnv* env, jclass /*clazz*/, jlong ptr, jint resid) {
  @Implementation(minSdk = P)
  protected static @Nullable String nativeGetResourceName(long ptr, @AnyRes int resid) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    CppAssetManager2.ResourceName name = new ResourceName();
    if (!assetmanager.GetResourceName(resid, name)) {
      return null;
    }

    StringBuilder result = new StringBuilder();
    if (name.package_ != null) {
      result.append(name.package_/*, name.package_len*/);
    }

    if (name.type != null /*|| name.type16 != null*/) {
      if (!(result.length() == 0)) {
        result.append(":");
      }

      // if (name.type != null) {
        result.append(name.type/*, name.type_len*/);
      // } else {
      //   result.append( /*util.Utf16ToUtf8(StringPiece16(*/ name.type16 /*, name.type_len))*/);
      // }
    }

    if (name.entry != null /*|| name.entry16 != null*/) {
      if (!(result.length() == 0)) {
        result.append("/");
      }

      // if (name.entry != null) {
        result.append(name.entry/*, name.entry_len*/);
      // } else {
      //   result.append( /*util.Utf16ToUtf8(StringPiece16(*/ name.entry16 /*, name.entry_len)*/);
      // }
    }
    return result.toString();
  }

  // static jstring NativeGetResourcePackageName(JNIEnv* env, jclass /*clazz*/, jlong ptr, jint resid) {
  @Implementation(minSdk = P)
  protected static @Nullable String nativeGetResourcePackageName(long ptr,
      @AnyRes int resid) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    final ResourceName name = new ResourceName();
    if (!assetmanager.GetResourceName(resid, name)) {
      return null;
    }

    if (name.package_ != null) {
      return name.package_;
    }
    return null;
  }

  // static jstring NativeGetResourceTypeName(JNIEnv* env, jclass /*clazz*/, jlong ptr, jint resid) {
  @Implementation(minSdk = P)
  protected static @Nullable String nativeGetResourceTypeName(long ptr, @AnyRes int resid) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    final ResourceName name = new ResourceName();
    if (!assetmanager.GetResourceName(resid, name)) {
      return null;
    }

    if (name.type != null) {
      return name.type;
    // } else if (name.get().type16 != null) {
    //   return name.get().type16; // env.NewString(reinterpret_cast<jchar*>(name.type16), name.type_len);
    }
    return null;
  }

  // static jstring NativeGetResourceEntryName(JNIEnv* env, jclass /*clazz*/, jlong ptr, jint resid) {
  @Implementation(minSdk = P)
  protected static @Nullable String nativeGetResourceEntryName(long ptr, @AnyRes int resid) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    final ResourceName name = new ResourceName();
    if (!assetmanager.GetResourceName(resid, name)) {
      return null;
    }

    if (name.entry != null) {
      return name.entry;
    // } else if (name.entry16 != null) {
    //   return name.entry16; // env.NewString(reinterpret_cast<jchar*>(name.entry16), name.entry_len);
    }
    return null;
  }

  // static jobjectArray NativeGetLocales(JNIEnv* env, jclass /*class*/, jlong ptr,
//                                      jboolean exclude_system) {
  @Implementation(minSdk = P)
  protected static @Nullable String[] nativeGetLocales(long ptr, boolean exclude_system) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    Set<String> locales =
        assetmanager.GetResourceLocales(exclude_system, true /*merge_equivalent_languages*/);

    String[] array = new String[locales.size()]; // env.NewObjectArray(locales.size(), g_stringClass, null);
    // if (array == null) {
    //   return null;
    // }

    int idx = 0;
    for (String locale : locales) {
      String java_string = locale;
      if (java_string == null) {
        return null;
      }
      // env.SetObjectArrayElement(array, idx++, java_string);
      array[idx++] = java_string;
      // env.DeleteLocalRef(java_string);
    }
    return array;
  }

  static Configuration ConstructConfigurationObject(/* JNIEnv* env,*/ ResTable_config config) {
    // jobject result =
    //     env.NewObject(gConfigurationOffsets.classObject, gConfigurationOffsets.constructor);
    Configuration result = new Configuration();
    // if (result == null) {
    //   return null;
    // }

    result.smallestScreenWidthDp = config.smallestScreenWidthDp;
    result.screenWidthDp = config.screenWidthDp;
    result.screenHeightDp = config.screenHeightDp;
    return result;
  }

  // static jobjectArray NativeGetSizeConfigurations(JNIEnv* env, jclass /*clazz*/, jlong ptr) {
  @Implementation(minSdk = P)
  protected static @Nullable Configuration[] nativeGetSizeConfigurations(long ptr) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    Set<ResTable_config> configurations =
        assetmanager.GetResourceConfigurations(true /*exclude_system*/, false /*exclude_mipmap*/);

    Configuration[] array = new Configuration[configurations.size()];
    // env.NewObjectArray(configurations.size(), gConfigurationOffsets.classObject, null);
    // if (array == null) {
    //   return null;
    // }

    int idx = 0;
    for (ResTable_config configuration : configurations) {
      Configuration java_configuration = ConstructConfigurationObject(configuration);
      // if (java_configuration == null) {
      //   return null;
      // }

      // env.SetObjectArrayElement(array, idx++, java_configuration);
      array[idx++] = java_configuration;
      // env.DeleteLocalRef(java_configuration);
    }
    return array;
  }

  // static void NativeApplyStyle(JNIEnv* env, jclass /*clazz*/, jlong ptr, jlong theme_ptr,
//                              jint def_style_attr, jint def_style_resid, jlong xml_parser_ptr,
//                              jintArray java_attrs, jlong out_values_ptr, jlong out_indices_ptr) {
  @Implementation(minSdk = P)
  protected static void nativeApplyStyle(long ptr, long theme_ptr, @AttrRes int def_style_attr,
      @StyleRes int def_style_resid, long xml_parser_ptr, @NonNull int[] java_attrs,
      long out_values_ptr, long out_indices_ptr) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    Theme theme = Registries.NATIVE_THEME9_REGISTRY.getNativeObject(theme_ptr);
    CHECK(theme.GetAssetManager() == assetmanager);
    // (void) assetmanager;

    ResXMLParser xml_parser =
        xml_parser_ptr == 0 ? null : NATIVE_RES_XML_PARSERS.getNativeObject(xml_parser_ptr);
    // int[] out_values = reinterpret_cast<int*>(out_values_ptr);
    // int[] out_indices = reinterpret_cast<int*>(out_indices_ptr);
    ShadowVMRuntime shadowVMRuntime = Shadow.extract(VMRuntime.getRuntime());
    int[] out_values = (int[])shadowVMRuntime.getObjectForAddress(out_values_ptr);
    int[] out_indices = (int[])shadowVMRuntime.getObjectForAddress(out_indices_ptr);

    int attrs_len = java_attrs.length;
    int[] attrs = java_attrs; // reinterpret_cast<int*>(env.GetPrimitiveArrayCritical(java_attrs, null));
    // if (attrs == null) {
    //   return;
    // }

    ApplyStyle(theme, xml_parser, (int) (def_style_attr),
        (int) (def_style_resid), attrs, attrs_len,
        out_values, out_indices);
    // env.ReleasePrimitiveArrayCritical(java_attrs, attrs, JNI_ABORT);
  }

  // static jboolean NativeResolveAttrs(JNIEnv* env, jclass /*clazz*/, jlong ptr, jlong theme_ptr,
//                                    jint def_style_attr, jint def_style_resid, jintArray java_values,
//                                    jintArray java_attrs, jintArray out_java_values,
//                                    jintArray out_java_indices) {
  @Implementation(minSdk = P)
  protected static boolean nativeResolveAttrs(long ptr, long theme_ptr,
      @AttrRes int def_style_attr, @StyleRes int def_style_resid, @Nullable int[] java_values,
      @NonNull int[] java_attrs, @NonNull int[] out_java_values, @NonNull int[] out_java_indices) {
    int attrs_len = java_attrs.length;
    int out_values_len = out_java_values.length;
    if (out_values_len < (attrs_len * STYLE_NUM_ENTRIES)) {
      throw new IndexOutOfBoundsException("outValues too small");
    }

    int[] attrs = java_attrs; // reinterpret_cast<int*>(env.GetPrimitiveArrayCritical(java_attrs, null));
    if (attrs == null) {
      return JNI_FALSE;
    }

    int[] values = null;
    int values_len = 0;
    if (java_values != null) {
      values_len = java_values.length;
      values = java_values; // reinterpret_cast<int*>(env.GetPrimitiveArrayCritical(java_values, null));
      if (values == null) {
        // env.ReleasePrimitiveArrayCritical(java_attrs, attrs, JNI_ABORT);
        return JNI_FALSE;
      }
    }

    int[] out_values = out_java_values;
    // reinterpret_cast<int*>(env.GetPrimitiveArrayCritical(out_java_values, null));
    if (out_values == null) {
      // env.ReleasePrimitiveArrayCritical(java_attrs, attrs, JNI_ABORT);
      // if (values != null) {
      //   env.ReleasePrimitiveArrayCritical(java_values, values, JNI_ABORT);
      // }
      return JNI_FALSE;
    }

    int[] out_indices = null;
    if (out_java_indices != null) {
      int out_indices_len = out_java_indices.length;
      if (out_indices_len > attrs_len) {
        out_indices = out_java_indices;
        // reinterpret_cast<int*>(env.GetPrimitiveArrayCritical(out_java_indices, null));
        if (out_indices == null) {
          // env.ReleasePrimitiveArrayCritical(java_attrs, attrs, JNI_ABORT);
          // if (values != null) {
          //   env.ReleasePrimitiveArrayCritical(java_values, values, JNI_ABORT);
          // }
          // env.ReleasePrimitiveArrayCritical(out_java_values, out_values, JNI_ABORT);
          return JNI_FALSE;
        }
      }
    }

    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    Theme theme = Registries.NATIVE_THEME9_REGISTRY.getNativeObject(theme_ptr);
    CHECK(theme.GetAssetManager() == assetmanager);
    // (void) assetmanager;

    boolean result = ResolveAttrs(
        theme, (int) (def_style_attr), (int) (def_style_resid),
        values, values_len, attrs,
        attrs_len, out_values, out_indices);
    // if (out_indices != null) {
    //   env.ReleasePrimitiveArrayCritical(out_java_indices, out_indices, 0);
    // }

    // env.ReleasePrimitiveArrayCritical(out_java_values, out_values, 0);
    // if (values != null) {
    //   env.ReleasePrimitiveArrayCritical(java_values, values, JNI_ABORT);
    // }
    // env.ReleasePrimitiveArrayCritical(java_attrs, attrs, JNI_ABORT);
    return result ? JNI_TRUE : JNI_FALSE;
  }

  // static jboolean NativeRetrieveAttributes(JNIEnv* env, jclass /*clazz*/, jlong ptr,
//                                          jlong xml_parser_ptr, jintArray java_attrs,
//                                          jintArray out_java_values, jintArray out_java_indices) {
  @Implementation(minSdk = P)
  protected static boolean nativeRetrieveAttributes(long ptr, long xml_parser_ptr,
      @NonNull int[] java_attrs, @NonNull int[] out_java_values, @NonNull int[] out_java_indices) {
    int attrs_len = java_attrs.length;
    int out_values_len = out_java_values.length;
    if (out_values_len < (attrs_len * STYLE_NUM_ENTRIES)) {
      throw new IndexOutOfBoundsException("outValues too small");
    }

    int[] attrs = java_attrs; // reinterpret_cast<int*>(env.GetPrimitiveArrayCritical(java_attrs, null));
    if (attrs == null) {
      return JNI_FALSE;
    }

    int[] out_values = out_java_values;
    // reinterpret_cast<int*>(env.GetPrimitiveArrayCritical(out_java_values, null));
    if (out_values == null) {
      // env.ReleasePrimitiveArrayCritical(java_attrs, attrs, JNI_ABORT);
      return JNI_FALSE;
    }

    int[] out_indices = null;
    if (out_java_indices != null) {
      int out_indices_len = out_java_indices.length;
      if (out_indices_len > attrs_len) {
        out_indices = out_java_indices;
        // reinterpret_cast<int*>(env.GetPrimitiveArrayCritical(out_java_indices, null));
        if (out_indices == null) {
          // env.ReleasePrimitiveArrayCritical(java_attrs, attrs, JNI_ABORT);
          // env.ReleasePrimitiveArrayCritical(out_java_values, out_values, JNI_ABORT);
          return JNI_FALSE;
        }
      }
    }

    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    ResXMLParser xml_parser = NATIVE_RES_XML_PARSERS.getNativeObject(xml_parser_ptr);

    boolean result = RetrieveAttributes(assetmanager, xml_parser,
        attrs, attrs_len,
        out_values,
        out_indices);

    // if (out_indices != null) {
    //   env.ReleasePrimitiveArrayCritical(out_java_indices, out_indices, 0);
    // }
    // env.ReleasePrimitiveArrayCritical(out_java_values, out_values, 0);
    // env.ReleasePrimitiveArrayCritical(java_attrs, attrs, JNI_ABORT);
    return result;
  }

  // static jlong NativeThemeCreate(JNIEnv* /*env*/, jclass /*clazz*/, jlong ptr) {
  @Implementation(minSdk = P)
  protected static long nativeThemeCreate(long ptr) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    return Registries.NATIVE_THEME9_REGISTRY.register(assetmanager.NewTheme());
  }

  // static void NativeThemeDestroy(JNIEnv* /*env*/, jclass /*clazz*/, jlong theme_ptr) {
  @Implementation(minSdk = P)
  protected static void nativeThemeDestroy(long theme_ptr) {
    Registries.NATIVE_THEME9_REGISTRY.unregister(theme_ptr);
  }

  // static void NativeThemeApplyStyle(JNIEnv* env, jclass /*clazz*/, jlong ptr, jlong theme_ptr,
//                                   jint resid, jboolean force) {
  @Implementation(minSdk = P)
  protected static void nativeThemeApplyStyle(long ptr, long theme_ptr, @StyleRes int resid,
      boolean force) {
    // AssetManager is accessed via the theme, so grab an explicit lock here.
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    Theme theme = Registries.NATIVE_THEME9_REGISTRY.getNativeObject(theme_ptr);
    CHECK(theme.GetAssetManager() == assetmanager);
    // (void) assetmanager;
    theme.ApplyStyle(resid, force);

    // TODO(adamlesinski): Consider surfacing exception when result is failure.
    // CTS currently expects no exceptions from this method.
    // std::string error_msg = StringPrintf("Failed to apply style 0x%08x to theme", resid);
    // throw new IllegalArgumentException(error_msg.c_str());
  }

  // static void NativeThemeCopy(JNIEnv* env, jclass /*clazz*/, jlong dst_theme_ptr,
//                             jlong src_theme_ptr) {
  @Implementation(minSdk = P)
  protected static void nativeThemeCopy(long dst_theme_ptr, long src_theme_ptr) {
    Theme dst_theme = Registries.NATIVE_THEME9_REGISTRY.getNativeObject(dst_theme_ptr);
    Theme src_theme = Registries.NATIVE_THEME9_REGISTRY.getNativeObject(src_theme_ptr);
    if (!dst_theme.SetTo(src_theme)) {
      throw new IllegalArgumentException("Themes are from different AssetManagers");
    }
  }

  // static void NativeThemeClear(JNIEnv* /*env*/, jclass /*clazz*/, jlong theme_ptr) {
  @Implementation(minSdk = P)
  protected static void nativeThemeClear(long themePtr) {
    Registries.NATIVE_THEME9_REGISTRY.getNativeObject(themePtr).Clear();
  }

  // static jint NativeThemeGetAttributeValue(JNIEnv* env, jclass /*clazz*/, jlong ptr, jlong theme_ptr,
//                                          jint resid, jobject typed_value,
//                                          jboolean resolve_references) {
  @Implementation(minSdk = P)
  protected static int nativeThemeGetAttributeValue(long ptr, long theme_ptr,
      @AttrRes int resid, @NonNull TypedValue typed_value, boolean resolve_references) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    Theme theme = Registries.NATIVE_THEME9_REGISTRY.getNativeObject(theme_ptr);
    CHECK(theme.GetAssetManager() == assetmanager);
    // (void) assetmanager; // huh?

    final Ref<Res_value> value = new Ref<>(null);
    final Ref<Integer> flags = new Ref<>(null);
    ApkAssetsCookie cookie = theme.GetAttribute(resid, value, flags);
    if (cookie.intValue() == kInvalidCookie) {
      return ApkAssetsCookieToJavaCookie(K_INVALID_COOKIE);
    }

    final Ref<Integer> ref = new Ref<>(0);
    if (resolve_references) {
      final Ref<ResTable_config> selected_config = new Ref<>(null);
      cookie =
          theme.GetAssetManager().ResolveReference(cookie, value, selected_config, flags, ref);
      if (cookie.intValue() == kInvalidCookie) {
        return ApkAssetsCookieToJavaCookie(K_INVALID_COOKIE);
      }
    }
    return CopyValue(cookie, value.get(), ref.get(), flags.get(), null, typed_value);
  }

  // static void NativeThemeDump(JNIEnv* /*env*/, jclass /*clazz*/, jlong ptr, jlong theme_ptr,
//                             jint priority, jstring tag, jstring prefix) {
  @Implementation(minSdk = P)
  protected static void nativeThemeDump(long ptr, long theme_ptr, int priority, String tag,
      String prefix) {
    CppAssetManager2 assetmanager = AssetManagerFromLong(ptr);
    Theme theme = Registries.NATIVE_THEME9_REGISTRY.getNativeObject(theme_ptr);
    CHECK(theme.GetAssetManager() == assetmanager);
    // (void) assetmanager;
    // (void) theme;
    // (void) priority;
    // (void) tag;
    // (void) prefix;
  }

  // static jint NativeThemeGetChangingConfigurations(JNIEnv* /*env*/, jclass /*clazz*/,
//                                                  jlong theme_ptr) {
  @Implementation(minSdk = P)
  protected static @NativeConfig int nativeThemeGetChangingConfigurations(long theme_ptr) {
    Theme theme = Registries.NATIVE_THEME9_REGISTRY.getNativeObject(theme_ptr);
    return (int) (theme.GetChangingConfigurations());
  }

  // static void NativeAssetDestroy(JNIEnv* /*env*/, jclass /*clazz*/, jlong asset_ptr) {
  @Implementation(minSdk = P)
  protected static void nativeAssetDestroy(long asset_ptr) {
    Registries.NATIVE_ASSET_REGISTRY.unregister(asset_ptr);
  }

  // static jint NativeAssetReadChar(JNIEnv* /*env*/, jclass /*clazz*/, jlong asset_ptr) {
  @Implementation(minSdk = P)
  protected static int nativeAssetReadChar(long asset_ptr) {
    Asset asset = Registries.NATIVE_ASSET_REGISTRY.getNativeObject(asset_ptr);
    byte[] b = new byte[1];
    int res = asset.read(b, 1);
    return res == 1 ? (int) (b[0]) & 0xff : -1;
  }

  // static jint NativeAssetRead(JNIEnv* env, jclass /*clazz*/, jlong asset_ptr, jbyteArray java_buffer,
//                             jint offset, jint len) {
  @Implementation(minSdk = P)
  protected static int nativeAssetRead(long asset_ptr, byte[] java_buffer, int offset, int len)
      throws IOException {
    if (len == 0) {
      return 0;
    }

    int buffer_len = java_buffer.length;
    if (offset < 0 || offset >= buffer_len || len < 0 || len > buffer_len ||
        offset > buffer_len - len) {
      throw new IndexOutOfBoundsException();
    }

    // ScopedByteArrayRW byte_array(env, java_buffer);
    // if (byte_array.get() == null) {
    //   return -1;
    // }

    Asset asset = Registries.NATIVE_ASSET_REGISTRY.getNativeObject(asset_ptr);
    // sint res = asset.read(byte_array.get() + offset, len);
    int res = asset.read(java_buffer, offset, len);
    if (res < 0) {
      throw new IOException();
    }
    return res > 0 ? (int) (res) : -1;
  }

  // static jlong NativeAssetSeek(JNIEnv* env, jclass /*clazz*/, jlong asset_ptr, jlong offset,
//                              jint whence) {
  @Implementation(minSdk = P)
  protected static long nativeAssetSeek(long asset_ptr, long offset, int whence) {
    Asset asset = Registries.NATIVE_ASSET_REGISTRY.getNativeObject(asset_ptr);
    return asset.seek(
        (offset), (whence > 0 ? SEEK_END : (whence < 0 ? SEEK_SET : SEEK_CUR)));
  }

  // static jlong NativeAssetGetLength(JNIEnv* /*env*/, jclass /*clazz*/, jlong asset_ptr) {
  @Implementation(minSdk = P)
  protected static long nativeAssetGetLength(long asset_ptr) {
    Asset asset = Registries.NATIVE_ASSET_REGISTRY.getNativeObject(asset_ptr);
    return asset.getLength();
  }

  // static jlong NativeAssetGetRemainingLength(JNIEnv* /*env*/, jclass /*clazz*/, jlong asset_ptr) {
  @Implementation(minSdk = P)
  protected static long nativeAssetGetRemainingLength(long asset_ptr) {
    Asset asset = Registries.NATIVE_ASSET_REGISTRY.getNativeObject(asset_ptr);
    return asset.getRemainingLength();
  }

// ----------------------------------------------------------------------------

  // JNI registration.
  // static JNINativeMethod gAssetManagerMethods[] = {
  //     // AssetManager setup methods.
  //     {"nativeCreate", "()J", (void*)NativeCreate},
  //   {"nativeDestroy", "(J)V", (void*)NativeDestroy},
  //   {"nativeSetApkAssets", "(J[Landroid/content/res/ApkAssets;Z)V", (void*)NativeSetApkAssets},
  //   {"nativeSetConfiguration", "(JIILjava/lang/String;IIIIIIIIIIIIIII)V",
  //   (void*)NativeSetConfiguration},
  //   {"nativeGetAssignedPackageIdentifiers", "(J)Landroid/util/SparseArray;",
  //   (void*)NativeGetAssignedPackageIdentifiers},
  //
  //   // AssetManager file methods.
  //   {"nativeList", "(JLjava/lang/String;)[Ljava/lang/String;", (void*)NativeList},
  //   {"nativeOpenAsset", "(JLjava/lang/String;I)J", (void*)NativeOpenAsset},
  //   {"nativeOpenAssetFd", "(JLjava/lang/String;[J)Landroid/os/ParcelFileDescriptor;",
  //   (void*)NativeOpenAssetFd},
  //   {"nativeOpenNonAsset", "(JILjava/lang/String;I)J", (void*)NativeOpenNonAsset},
  //   {"nativeOpenNonAssetFd", "(JILjava/lang/String;[J)Landroid/os/ParcelFileDescriptor;",
  //   (void*)NativeOpenNonAssetFd},
  //   {"nativeOpenXmlAsset", "(JILjava/lang/String;)J", (void*)NativeOpenXmlAsset},
  //
  //   // AssetManager resource methods.
  //   {"nativeGetResourceValue", "(JISLandroid/util/TypedValue;Z)I", (void*)NativeGetResourceValue},
  //   {"nativeGetResourceBagValue", "(JIILandroid/util/TypedValue;)I",
  //   (void*)NativeGetResourceBagValue},
  //   {"nativeGetStyleAttributes", "(JI)[I", (void*)NativeGetStyleAttributes},
  //   {"nativeGetResourceStringArray", "(JI)[Ljava/lang/String;",
  //   (void*)NativeGetResourceStringArray},
  //   {"nativeGetResourceStringArrayInfo", "(JI)[I", (void*)NativeGetResourceStringArrayInfo},
  //   {"nativeGetResourceIntArray", "(JI)[I", (void*)NativeGetResourceIntArray},
  //   {"nativeGetResourceArraySize", "(JI)I", (void*)NativeGetResourceArraySize},
  //   {"nativeGetResourceArray", "(JI[I)I", (void*)NativeGetResourceArray},
  //
  //   // AssetManager resource name/ID methods.
  //   {"nativeGetResourceIdentifier", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)I",
  //   (void*)NativeGetResourceIdentifier},
  //   {"nativeGetResourceName", "(JI)Ljava/lang/String;", (void*)NativeGetResourceName},
  //   {"nativeGetResourcePackageName", "(JI)Ljava/lang/String;", (void*)NativeGetResourcePackageName},
  //   {"nativeGetResourceTypeName", "(JI)Ljava/lang/String;", (void*)NativeGetResourceTypeName},
  //   {"nativeGetResourceEntryName", "(JI)Ljava/lang/String;", (void*)NativeGetResourceEntryName},
  //   {"nativeGetLocales", "(JZ)[Ljava/lang/String;", (void*)NativeGetLocales},
  //   {"nativeGetSizeConfigurations", "(J)[Landroid/content/res/Configuration;",
  //   (void*)NativeGetSizeConfigurations},
  //
  //   // Style attribute related methods.
  //   {"nativeApplyStyle", "(JJIIJ[IJJ)V", (void*)NativeApplyStyle},
  //   {"nativeResolveAttrs", "(JJII[I[I[I[I)Z", (void*)NativeResolveAttrs},
  //   {"nativeRetrieveAttributes", "(JJ[I[I[I)Z", (void*)NativeRetrieveAttributes},
  //
  //   // Theme related methods.
  //   {"nativeThemeCreate", "(J)J", (void*)NativeThemeCreate},
  //   {"nativeThemeDestroy", "(J)V", (void*)NativeThemeDestroy},
  //   {"nativeThemeApplyStyle", "(JJIZ)V", (void*)NativeThemeApplyStyle},
  //   {"nativeThemeCopy", "(JJ)V", (void*)NativeThemeCopy},
  //   {"nativeThemeClear", "(J)V", (void*)NativeThemeClear},
  //   {"nativeThemeGetAttributeValue", "(JJILandroid/util/TypedValue;Z)I",
  //   (void*)NativeThemeGetAttributeValue},
  //   {"nativeThemeDump", "(JJILjava/lang/String;Ljava/lang/String;)V", (void*)NativeThemeDump},
  //   {"nativeThemeGetChangingConfigurations", "(J)I", (void*)NativeThemeGetChangingConfigurations},
  //
  //   // AssetInputStream methods.
  //   {"nativeAssetDestroy", "(J)V", (void*)NativeAssetDestroy},
  //   {"nativeAssetReadChar", "(J)I", (void*)NativeAssetReadChar},
  //   {"nativeAssetRead", "(J[BII)I", (void*)NativeAssetRead},
  //   {"nativeAssetSeek", "(JJI)J", (void*)NativeAssetSeek},
  //   {"nativeAssetGetLength", "(J)J", (void*)NativeAssetGetLength},
  //   {"nativeAssetGetRemainingLength", "(J)J", (void*)NativeAssetGetRemainingLength},
  //
  //   // System/idmap related methods.
  //   {"nativeVerifySystemIdmaps", "()V", (void*)NativeVerifySystemIdmaps},
  //
  //   // Global management/debug methods.
  //   {"getGlobalAssetCount", "()I", (void*)NativeGetGlobalAssetCount},
  //   {"getAssetAllocations", "()Ljava/lang/String;", (void*)NativeGetAssetAllocations},
  //   {"getGlobalAssetManagerCount", "()I", (void*)NativeGetGlobalAssetManagerCount},
  //   };
  //
  //   int register_android_content_AssetManager(JNIEnv* env) {
  //   jclass apk_assets_class = FindClassOrDie(env, "android/content/res/ApkAssets");
  //   gApkAssetsFields.native_ptr = GetFieldIDOrDie(env, apk_assets_class, "mNativePtr", "J");
  //
  //   jclass typedValue = FindClassOrDie(env, "android/util/TypedValue");
  //   gTypedValueOffsets.mType = GetFieldIDOrDie(env, typedValue, "type", "I");
  //   gTypedValueOffsets.mData = GetFieldIDOrDie(env, typedValue, "data", "I");
  //   gTypedValueOffsets.mString =
  //   GetFieldIDOrDie(env, typedValue, "string", "Ljava/lang/CharSequence;");
  //   gTypedValueOffsets.mAssetCookie = GetFieldIDOrDie(env, typedValue, "assetCookie", "I");
  //   gTypedValueOffsets.mResourceId = GetFieldIDOrDie(env, typedValue, "resourceId", "I");
  //   gTypedValueOffsets.mChangingConfigurations =
  //   GetFieldIDOrDie(env, typedValue, "changingConfigurations", "I");
  //   gTypedValueOffsets.mDensity = GetFieldIDOrDie(env, typedValue, "density", "I");
  //
  //   jclass assetFd = FindClassOrDie(env, "android/content/res/AssetFileDescriptor");
  //   gAssetFileDescriptorOffsets.mFd =
  //   GetFieldIDOrDie(env, assetFd, "mFd", "Landroid/os/ParcelFileDescriptor;");
  //   gAssetFileDescriptorOffsets.mStartOffset = GetFieldIDOrDie(env, assetFd, "mStartOffset", "J");
  //   gAssetFileDescriptorOffsets.mLength = GetFieldIDOrDie(env, assetFd, "mLength", "J");
  //
  //   jclass assetManager = FindClassOrDie(env, "android/content/res/AssetManager");
  //   gAssetManagerOffsets.mObject = GetFieldIDOrDie(env, assetManager, "mObject", "J");
  //
  //   jclass stringClass = FindClassOrDie(env, "java/lang/String");
  //   g_stringClass = MakeGlobalRefOrDie(env, stringClass);
  //
  //   jclass sparseArrayClass = FindClassOrDie(env, "android/util/SparseArray");
  //   gSparseArrayOffsets.classObject = MakeGlobalRefOrDie(env, sparseArrayClass);
  //   gSparseArrayOffsets.constructor =
  //   GetMethodIDOrDie(env, gSparseArrayOffsets.classObject, "<init>", "()V");
  //   gSparseArrayOffsets.put =
  //   GetMethodIDOrDie(env, gSparseArrayOffsets.classObject, "put", "(ILjava/lang/Object;)V");
  //
  //   jclass configurationClass = FindClassOrDie(env, "android/content/res/Configuration");
  //   gConfigurationOffsets.classObject = MakeGlobalRefOrDie(env, configurationClass);
  //   gConfigurationOffsets.constructor = GetMethodIDOrDie(env, configurationClass, "<init>", "()V");
  //   gConfigurationOffsets.mSmallestScreenWidthDpOffset =
  //   GetFieldIDOrDie(env, configurationClass, "smallestScreenWidthDp", "I");
  //   gConfigurationOffsets.mScreenWidthDpOffset =
  //   GetFieldIDOrDie(env, configurationClass, "screenWidthDp", "I");
  //   gConfigurationOffsets.mScreenHeightDpOffset =
  //   GetFieldIDOrDie(env, configurationClass, "screenHeightDp", "I");
  //
  //   return RegisterMethodsOrDie(env, "android/content/res/AssetManager", gAssetManagerMethods,
  //   NELEM(gAssetManagerMethods));
  //   }

}; // namespace android
