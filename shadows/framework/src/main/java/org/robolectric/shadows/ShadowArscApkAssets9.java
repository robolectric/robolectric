package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Util.ATRACE_NAME;
import static org.robolectric.res.android.Util.JNI_TRUE;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.ApkAssets;
import android.content.res.AssetManager;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.ApkAssetsCache;
import org.robolectric.res.android.Asset;
import org.robolectric.res.android.CppApkAssets;
import org.robolectric.res.android.Registries;
import org.robolectric.res.android.ResXMLTree;
import org.robolectric.shadows.ShadowApkAssets.Picker;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

// transliterated from
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/core/jni/android_content_res_ApkAssets.cpp

/** Shadow for {@link ApkAssets} for Android P+ */
@Implements(
    value = ApkAssets.class,
    minSdk = P,
    shadowPicker = Picker.class,
    isInAndroidSdk = false)
public class ShadowArscApkAssets9 extends ShadowApkAssets {
  // #define ATRACE_TAG ATRACE_TAG_RESOURCES
  //
  // #include "android-base/macros.h"
  // #include "android-base/stringprintf.h"
  // #include "android-base/unique_fd.h"
  // #include "androidfw/ApkAssets.h"
  // #include "utils/misc.h"
  // #include "utils/Trace.h"
  //
  // #include "core_jni_helpers.h"
  // #include "jni.h"
  // #include "nativehelper/ScopedUtfChars.h"
  //
  // using ::android::base::unique_fd;
  //
  // namespace android {

  // TODO: just use the ApkAssets constants. For some unknown reason these cannot be found
  private static final int PROPERTY_SYSTEM = 1 << 0;
  private static final int PROPERTY_DYNAMIC = 1 << 1;
  private static final int PROPERTY_OVERLAY = 1 << 3;

  protected static final String FRAMEWORK_APK_PATH =
      ReflectionHelpers.getStaticField(AssetManager.class, "FRAMEWORK_APK_PATH");

  @RealObject private ApkAssets realApkAssets;

  long getNativePtr() {
    return reflector(_ApkAssets_.class, realApkAssets).getNativePtr();
  }

  /** Reflector interface for {@link ApkAssets}'s internals. */
  @ForType(ApkAssets.class)
  interface _ApkAssets_ {
    @Static
    @Direct
    ApkAssets loadFromPath(String finalPath, boolean system);

    @Static
    @Direct
    ApkAssets loadFromPath(String finalPath, int flags);

    @Accessor("mNativePtr")
    long getNativePtr();
  }

  /**
   * Necessary to shadow this method because the framework path is hard-coded. Called from
   * AssetManager.createSystemAssetsInZygoteLocked() in P+.
   */
  @Implementation(maxSdk = Q)
  protected static ApkAssets loadFromPath(String path, boolean system) throws IOException {
    if (FRAMEWORK_APK_PATH.equals(path)) {
      path = RuntimeEnvironment.getAndroidFrameworkJarPath().toString();
    }

    return reflector(_ApkAssets_.class).loadFromPath(path, system);
  }

  @Implementation(minSdk = R)
  protected static ApkAssets loadFromPath(String path, int flags) throws IOException {
    if (FRAMEWORK_APK_PATH.equals(path)) {
      path = RuntimeEnvironment.getAndroidFrameworkJarPath().toString();
    }
    return reflector(_ApkAssets_.class).loadFromPath(path, flags);
  }

  // static jlong NativeLoad(JNIEnv* env, jclass /*clazz*/, jstring java_path, jboolean system,
  //                         jboolean force_shared_lib, jboolean overlay) {

  @Implementation(maxSdk = Q)
  protected static long nativeLoad(
      String path, boolean system, boolean forceSharedLib, boolean overlay) throws IOException {
    if (path == null) {
      return 0;
    }

    long cachedApkAssetsPtr = ApkAssetsCache.get(path, system, RuntimeEnvironment.getApiLevel());
    if (cachedApkAssetsPtr != -1) {
      return cachedApkAssetsPtr;
    }

    ATRACE_NAME(String.format("LoadApkAssets(%s)", path));

    CppApkAssets apk_assets;
    try {
      if (overlay) {
        apk_assets = CppApkAssets.LoadOverlay(path, system);
      } else if (forceSharedLib) {
        apk_assets = CppApkAssets.LoadAsSharedLibrary(path, system);
      } else {
        apk_assets = CppApkAssets.Load(path, system);
      }
    } catch (OutOfMemoryError e) {
      OutOfMemoryError outOfMemoryError = new OutOfMemoryError("Failed to load " + path);
      outOfMemoryError.initCause(e);
      throw outOfMemoryError;
    }

    if (apk_assets == null) {
      String error_msg = String.format("Failed to load asset path %s", path);
      throw new IOException(error_msg);
    }
    long ptr = Registries.NATIVE_APK_ASSETS_REGISTRY.register(apk_assets);
    ApkAssetsCache.put(path, system, RuntimeEnvironment.getApiLevel(), ptr);
    return ptr;
  }

  @Implementation(minSdk = R)
  protected static long nativeLoad(
      int format,
      String javaPath,
      int flags,
      @ClassName("android.content.res.loader.AssetsProvider") Object assetsProvider)
      throws IOException {
    boolean system = (flags & PROPERTY_SYSTEM) == PROPERTY_SYSTEM;
    boolean overlay = (flags & PROPERTY_OVERLAY) == PROPERTY_OVERLAY;
    boolean forceSharedLib = (flags & PROPERTY_DYNAMIC) == PROPERTY_DYNAMIC;
    return nativeLoad(javaPath, system, forceSharedLib, overlay);
  }

  // static jlong NativeLoadFromFd(JNIEnv* env, jclass /*clazz*/, jobject file_descriptor,
  //                               jstring friendly_name, jboolean system, jboolean
  // force_shared_lib) {
  @Implementation(maxSdk = Q)
  protected static long nativeLoadFromFd(
      FileDescriptor file_descriptor,
      String friendly_name,
      boolean system,
      boolean force_shared_lib) {
    String friendly_name_utf8 = friendly_name;
    if (friendly_name_utf8 == null) {
      return 0;
    }

    throw new UnsupportedOperationException();
    // ATRACE_NAME(String.format("LoadApkAssetsFd(%s)", friendly_name_utf8));
    //
    // int fd = jniGetFDFromFileDescriptor(env, file_descriptor);
    // if (fd < 0) {
    //   throw new IllegalArgumentException("Bad FileDescriptor");
    // }
    //
    // unique_fd dup_fd(.dup(fd));
    // if (dup_fd < 0) {
    //   throw new IOException(errno);
    //   return 0;
    // }
    //
    // ApkAssets apk_assets = ApkAssets.LoadFromFd(std.move(dup_fd),
    //                                                                     friendly_name_utf8,
    //                                                                     system,
    // force_shared_lib);
    // if (apk_assets == null) {
    //   String error_msg = String.format("Failed to load asset path %s from fd %d",
    //                                              friendly_name_utf8, dup_fd.get());
    //   throw new IOException(error_msg);
    //   return 0;
    // }
    // return ShadowArscAssetManager9.NATIVE_APK_ASSETS_REGISTRY.getNativeObjectId(apk_assets);
  }

  // static jlong NativeLoadFromFd(JNIEnv* env, jclass /*clazz*/, const format_type_t format,
  //                             jobject file_descriptor, jstring friendly_name,
  //                             const jint property_flags, jobject assets_provider)
  @Implementation(minSdk = R)
  protected static long nativeLoadFd(
      int format,
      FileDescriptor fileDescriptor,
      String friendlyName,
      int propertyFlags,
      @ClassName("android.content.res.loader.AssetsProvider") Object assetsProvider)
      throws IOException {
    CppApkAssets apkAssets = CppApkAssets.loadArscFromFd((FileDescriptor) fileDescriptor);
    if (apkAssets == null) {
      String errorMessage =
          String.format("Failed to load from the file descriptor %s", fileDescriptor);
      throw new IOException(errorMessage);
    }
    return Registries.NATIVE_APK_ASSETS_REGISTRY.register(apkAssets);
  }

  // static jstring NativeGetAssetPath(JNIEnv* env, jclass /*clazz*/, jlong ptr) {
  @Implementation
  protected static String nativeGetAssetPath(long ptr) {
    CppApkAssets apk_assets = Registries.NATIVE_APK_ASSETS_REGISTRY.getNativeObject(ptr);
    return apk_assets.GetPath();
  }

  // static jlong NativeGetStringBlock(JNIEnv* /*env*/, jclass /*clazz*/, jlong ptr) {
  @Implementation
  protected static long nativeGetStringBlock(long ptr) {
    CppApkAssets apk_assets = Registries.NATIVE_APK_ASSETS_REGISTRY.getNativeObject(ptr);
    return apk_assets.GetLoadedArsc().GetStringPool().getNativePtr();
  }

  // static jboolean NativeIsUpToDate(JNIEnv* /*env*/, jclass /*clazz*/, jlong ptr) {
  @Implementation
  protected static boolean nativeIsUpToDate(long ptr) {
    // (void)apk_assets;
    return JNI_TRUE;
  }

  // static jlong NativeOpenXml(JNIEnv* env, jclass /*clazz*/, jlong ptr, jstring file_name) {
  @Implementation
  protected static long nativeOpenXml(long ptr, String file_name) throws FileNotFoundException {
    String path_utf8 = file_name;
    if (path_utf8 == null) {
      return 0;
    }

    CppApkAssets apk_assets = Registries.NATIVE_APK_ASSETS_REGISTRY.getNativeObject(ptr);
    Asset asset = apk_assets.Open(path_utf8, Asset.AccessMode.ACCESS_RANDOM);
    if (asset == null) {
      throw new FileNotFoundException(path_utf8);
    }

    // DynamicRefTable is only needed when looking up resource references. Opening an XML file
    // directly from an ApkAssets has no notion of proper resource references.
    ResXMLTree xml_tree =
        new ResXMLTree(null); // util.make_unique<ResXMLTree>(nullptr /*dynamicRefTable*/);
    int err = xml_tree.setTo(asset.getBuffer(true), (int) asset.getLength(), true);
    // asset.reset();

    if (err != NO_ERROR) {
      throw new FileNotFoundException("Corrupt XML binary file");
    }
    return Registries.NATIVE_RES_XML_TREES.register(
        xml_tree); // reinterpret_cast<jlong>(xml_tree.release());
  }

  // // JNI registration.
  // static const JNINativeMethod gApkAssetsMethods[] = {
  //     {"nativeLoad", "(Ljava/lang/String;ZZZ)J", (void*)NativeLoad},
  //     {"nativeLoadFromFd", "(Ljava/io/FileDescriptor;Ljava/lang/String;ZZ)J",
  //         (void*)NativeLoadFromFd},
  //     {"nativeDestroy", "(J)V", (void*)NativeDestroy},
  //     {"nativeGetAssetPath", "(J)Ljava/lang/String;", (void*)NativeGetAssetPath},
  //     {"nativeGetStringBlock", "(J)J", (void*)NativeGetStringBlock},
  //     {"nativeIsUpToDate", "(J)Z", (void*)NativeIsUpToDate},
  //     {"nativeOpenXml", "(JLjava/lang/String;)J", (void*)NativeOpenXml},
  // };
  //
  // int register_android_content_res_ApkAssets(JNIEnv* env) {
  //   return RegisterMethodsOrDie(env, "android/content/res/ApkAssets", gApkAssetsMethods,
  //                               arraysize(gApkAssetsMethods));
  // }
  //
  // }  // namespace android
}
