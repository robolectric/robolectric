package org.robolectric.shadows;

import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Util.ATRACE_NAME;
import static org.robolectric.res.android.Util.JNI_TRUE;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.NonNull;
import android.content.res.ApkAssets;
import android.content.res.AssetManager;
import android.os.Build;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Objects;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.Asset;
import org.robolectric.res.android.CppApkAssets;
import org.robolectric.res.android.Registries;
import org.robolectric.res.android.ResXMLTree;
import org.robolectric.shadows.ShadowApkAssets.Picker;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

// transliterated from
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/core/jni/android_content_res_ApkAssets.cpp

@Implements(value = ApkAssets.class, minSdk = Build.VERSION_CODES.P,
    shadowPicker = Picker.class, isInAndroidSdk = false)
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

  private static final String FRAMEWORK_APK_PATH =
      ReflectionHelpers.getStaticField(AssetManager.class, "FRAMEWORK_APK_PATH");

  private static final HashMap<Key, WeakReference<ApkAssets>> cachedApkAssets =
      new HashMap<>();

  @RealObject private ApkAssets realApkAssets;

  long getNativePtr() {
    return reflector(_ApkAssets_.class, realApkAssets).getNativePtr();
  }

  /** Accessor interface for {@link ApkAssets}'s internals. */
  @ForType(ApkAssets.class)
  interface _ApkAssets_ {

    @Accessor("mNativePtr")
    long getNativePtr();
  }

  /**
   * Caching key for {@link ApkAssets}.
   */
  private static class Key {
    private final FileDescriptor fd;
    private final String path;
    private final boolean system;
    private final boolean load_as_shared_library;
    private final boolean overlay;

    public Key(FileDescriptor fd, String path, boolean system, boolean load_as_shared_library,
        boolean overlay) {
      this.fd = fd;
      this.path = path;
      this.system = system;
      this.load_as_shared_library = load_as_shared_library;
      this.overlay = overlay;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Key key = (Key) o;
      return system == key.system &&
          load_as_shared_library == key.load_as_shared_library &&
          overlay == key.overlay &&
          Objects.equals(fd, key.fd) &&
          Objects.equals(path, key.path);
    }

    @Override
    public int hashCode() {
      return Objects.hash(fd, path, system, load_as_shared_library, overlay);
    }
  }

  @FunctionalInterface
  private interface ApkAssetMaker {
    ApkAssets call();
  }

  private static ApkAssets getFromCacheOrLoad(Key key, ApkAssetMaker callable) {
    synchronized (cachedApkAssets) {
      WeakReference<ApkAssets> cachedRef = cachedApkAssets.get(key);
      ApkAssets apkAssets;
      if (cachedRef != null) {
        apkAssets = cachedRef.get();
        if (apkAssets != null) {
          return apkAssets;
        } else {
          cachedApkAssets.remove(key);
        }
      }

      apkAssets = callable.call();
      cachedApkAssets.put(key, new WeakReference<>(apkAssets));
      return apkAssets;
    }
  }

  @Implementation
  protected static ApkAssets loadFromPath(@NonNull String path) throws IOException {
    return getFromCacheOrLoad(
        new Key(null, path, false, false, false),
        () -> directlyOn(ApkAssets.class, "loadFromPath", ClassParameter.from(String.class, path)));
  }

  /**
   * Necessary to shadow this method because the framework path is hard-coded.
   * Called from AssetManager.createSystemAssetsInZygoteLocked() in P+.
   */
  @Implementation
  protected static ApkAssets loadFromPath(String path, boolean system)
      throws IOException {
    System.out.println(
        "Called loadFromPath("
            + path
            + ", " + system + "); mode="
            + (RuntimeEnvironment.useLegacyResources() ? "legacy" : "binary")
            + " sdk=" + RuntimeEnvironment.getApiLevel());

    if (FRAMEWORK_APK_PATH.equals(path)) {
      path = RuntimeEnvironment.getAndroidFrameworkJarPath();
    }

    String finalPath = path;
    return getFromCacheOrLoad(
        new Key(null, path, system, false, false),
        () -> directlyOn(ApkAssets.class, "loadFromPath",
            ClassParameter.from(String.class, finalPath),
            ClassParameter.from(boolean.class, system)));
  }

  @Implementation
  protected static @NonNull ApkAssets loadFromPath(@NonNull String path, boolean system,
      boolean forceSharedLibrary) throws IOException {
    return getFromCacheOrLoad(
        new Key(null, path, system, forceSharedLibrary, false),
        () -> directlyOn(ApkAssets.class, "loadFromPath",
            ClassParameter.from(String.class, path),
            ClassParameter.from(boolean.class, system),
            ClassParameter.from(boolean.class, forceSharedLibrary)));
  }

  @Implementation
  protected static ApkAssets loadFromFd(FileDescriptor fd,
      String friendlyName, boolean system, boolean forceSharedLibrary)
      throws IOException {
    return getFromCacheOrLoad(
        new Key(fd, friendlyName, system, forceSharedLibrary, false),
        () -> directlyOn(ApkAssets.class, "loadFromPath",
            ClassParameter.from(FileDescriptor.class, fd),
            ClassParameter.from(String.class, friendlyName),
            ClassParameter.from(boolean.class, system),
            ClassParameter.from(boolean.class, forceSharedLibrary)));
  }

  @Implementation
  protected static @NonNull ApkAssets loadOverlayFromPath(@NonNull String idmapPath, boolean system)
      throws IOException {
    throw new UnsupportedOperationException();
    // return getFromCacheOrLoad(
        // new Key(fd, friendlyName, system, forceSharedLibrary, false),
        // () -> directlyOn(ApkAssets.class, "loadFromPath",
        //     ClassParameter.from(FileDescriptor.class, fd),
        //     ClassParameter.from(String.class, friendlyName),
        //     ClassParameter.from(boolean.class, system),
        //     ClassParameter.from(boolean.class, forceSharedLibrary)));
  }

  // static jlong NativeLoad(JNIEnv* env, jclass /*clazz*/, jstring java_path, jboolean system,
//                         jboolean force_shared_lib, jboolean overlay) {
  @Implementation
  protected static long nativeLoad(String java_path, boolean system,
      boolean force_shared_lib, boolean overlay) throws IOException {
    String path = java_path;
    if (path == null) {
      return 0;
    }

    ATRACE_NAME(String.format("LoadApkAssets(%s)", path));

    CppApkAssets apk_assets;
    try {
      if (overlay) {
        apk_assets = CppApkAssets.LoadOverlay(path, system);
      } else if (force_shared_lib) {
        apk_assets =
            CppApkAssets.LoadAsSharedLibrary(path, system);
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
    return Registries.NATIVE_APK_ASSETS_REGISTRY.register(apk_assets);
  }

  // static jlong NativeLoadFromFd(JNIEnv* env, jclass /*clazz*/, jobject file_descriptor,
//                               jstring friendly_name, jboolean system, jboolean force_shared_lib) {
  @Implementation
  protected static long nativeLoadFromFd(FileDescriptor file_descriptor,
      String friendly_name, boolean system, boolean force_shared_lib) {
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
    //                                                                     system, force_shared_lib);
    // if (apk_assets == null) {
    //   String error_msg = String.format("Failed to load asset path %s from fd %d",
    //                                              friendly_name_utf8, dup_fd.get());
    //   throw new IOException(error_msg);
    //   return 0;
    // }
    // return ShadowArscAssetManager9.NATIVE_APK_ASSETS_REGISTRY.getNativeObjectId(apk_assets);
  }

  // static void NativeDestroy(JNIEnv* /*env*/, jclass /*clazz*/, jlong ptr) {
  @Implementation
  protected static void nativeDestroy(long ptr) {
    // delete reinterpret_cast<ApkAssets>(ptr);
    Registries.NATIVE_APK_ASSETS_REGISTRY.unregister(ptr);
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
    CppApkAssets apk_assets = Registries.NATIVE_APK_ASSETS_REGISTRY.getNativeObject(ptr);
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

    CppApkAssets apk_assets =
        Registries.NATIVE_APK_ASSETS_REGISTRY.getNativeObject(ptr);
    Asset asset = apk_assets.Open(path_utf8,
        Asset.AccessMode.ACCESS_RANDOM);
    if (asset == null) {
      throw new FileNotFoundException(path_utf8);
    }

    // DynamicRefTable is only needed when looking up resource references. Opening an XML file
    // directly from an ApkAssets has no notion of proper resource references.
    ResXMLTree xml_tree = new ResXMLTree(null); // util.make_unique<ResXMLTree>(nullptr /*dynamicRefTable*/);
    int err = xml_tree.setTo(asset.getBuffer(true), (int) asset.getLength(), true);
    // asset.reset();

    if (err != NO_ERROR) {
      throw new FileNotFoundException("Corrupt XML binary file");
    }
    return Registries.NATIVE_RES_XML_TREES.register(xml_tree); // reinterpret_cast<jlong>(xml_tree.release());
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
