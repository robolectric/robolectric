package org.robolectric.res.android;

import static org.robolectric.res.android.Asset.toIntExact;
import static org.robolectric.res.android.CppAssetManager.FileType.kFileTypeDirectory;
import static org.robolectric.res.android.Util.ALOGD;
import static org.robolectric.res.android.Util.ALOGE;
import static org.robolectric.res.android.Util.ALOGI;
import static org.robolectric.res.android.Util.ALOGV;
import static org.robolectric.res.android.Util.ALOGW;
import static org.robolectric.res.android.Util.ATRACE_CALL;
import static org.robolectric.res.android.Util.LOG_FATAL_IF;
import static org.robolectric.res.android.Util.isTruthy;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import javax.annotation.Nullable;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.android.Asset.AccessMode;
import org.robolectric.res.android.AssetDir.FileInfo;
import org.robolectric.res.android.ZipFileRO.ZipEntryRO;
import org.robolectric.util.PerfStatsCollector;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/AssetManager.cpp
@SuppressWarnings("NewApi")
public class CppAssetManager {

  private static final boolean kIsDebug = false;

  enum FileType {
    kFileTypeUnknown,
    kFileTypeNonexistent,       // i.e. ENOENT
    kFileTypeRegular,
    kFileTypeDirectory,
    kFileTypeCharDev,
    kFileTypeBlockDev,
    kFileTypeFifo,
    kFileTypeSymlink,
    kFileTypeSocket,
  }


  // transliterated from https://cs.corp.google.com/android/frameworks/base/libs/androidfw/include/androidfw/AssetManager.h
  private static class asset_path {
//    asset_path() : path(""), type(kFileTypeRegular), idmap(""),
//      isSystemOverlay(false), isSystemAsset(false) {}


    public asset_path() {
      this(new String8(), FileType.kFileTypeRegular, new String8(""), false, false);
    }

    public asset_path(String8 path, FileType fileType, String8 idmap,
        boolean isSystemOverlay,
        boolean isSystemAsset) {
      this.path = path;
      this.type = fileType;
      this.idmap = idmap;
      this.isSystemOverlay = isSystemOverlay;
      this.isSystemAsset = isSystemAsset;
    }

    String8 path;
    FileType type;
    String8 idmap;
    boolean isSystemOverlay;
    boolean isSystemAsset;

    @Override
    public String toString() {
      return "asset_path{" +
          "path=" + path +
          ", type=" + type +
          ", idmap='" + idmap + '\'' +
          ", isSystemOverlay=" + isSystemOverlay +
          ", isSystemAsset=" + isSystemAsset +
          '}';
    }
  }

  private final Object mLock = new Object();

  // unlike AssetManager.cpp, this is shared between CppAssetManager instances, and is used
  // to cache ResTables between tests.
  private static final ZipSet mZipSet = new ZipSet();

  private final List<asset_path> mAssetPaths = new ArrayList<>();
  private String mLocale;

  private ResTable mResources;
  private ResTable_config mConfig = new ResTable_config();


  //  static final boolean kIsDebug = false;
//  
  static final String kAssetsRoot = "assets";
  static final String kAppZipName = null; //"classes.jar";
  static final String kSystemAssets = "android.jar";
  //  static final char* kResourceCache = "resource-cache";
//  
  static final String kExcludeExtension = ".EXCLUDE";
//  

  // static Asset final kExcludedAsset = (Asset*) 0xd000000d;
  static final Asset kExcludedAsset = Asset.EXCLUDED_ASSET;


 static volatile int gCount = 0;

//  final char* RESOURCES_FILENAME = "resources.arsc";
//  final char* IDMAP_BIN = "/system/bin/idmap";
//  final char* OVERLAY_DIR = "/vendor/overlay";
//  final char* OVERLAY_THEME_DIR_PROPERTY = "ro.boot.vendor.overlay.theme";
//  final char* TARGET_PACKAGE_NAME = "android";
//  final char* TARGET_APK_PATH = "/system/framework/framework-res.apk";
//  final char* IDMAP_DIR = "/data/resource-cache";
//  
//  namespace {
//  
  String8 idmapPathForPackagePath(final String8 pkgPath) {
    // TODO: implement this?
    return pkgPath;
//    const char* root = getenv("ANDROID_DATA");
//    LOG_ALWAYS_FATAL_IF(root == NULL, "ANDROID_DATA not set");
//    String8 path(root);
//    path.appendPath(kResourceCache);
//    char buf[256]; // 256 chars should be enough for anyone...
//    strncpy(buf, pkgPath.string(), 255);
//    buf[255] = '\0';
//    char* filename = buf;
//    while (*filename && *filename == '/') {
//      ++filename;
//    }
//    char* p = filename;
//    while (*p) {
//      if (*p == '/') {
//           *p = '@';
//      }
//      ++p;
//    }
//    path.appendPath(filename);
//    path.append("@idmap");
//    return path;
  }
//  
//  /*
//   * Like strdup(), but uses C++ "new" operator instead of malloc.
//   */
//  static char* strdupNew(final char* str) {
//      char* newStr;
//      int len;
//  
//      if (str == null)
//          return null;
//  
//      len = strlen(str);
//      newStr = new char[len+1];
//      memcpy(newStr, str, len+1);
//  
//      return newStr;
//  }
//  
//  } // namespace
//  
//  /*
//   * ===========================================================================
//   *      AssetManager
//   * ===========================================================================
//   */

  public static int getGlobalCount() {
    return gCount;
  }

//  AssetManager() :
//          mLocale(null), mResources(null), mConfig(new ResTable_config) {
//      int count = android_atomic_inc(&gCount) + 1;
//      if (kIsDebug) {
//          ALOGI("Creating AssetManager %s #%d\n", this, count);
//      }
//      memset(mConfig, 0, sizeof(ResTable_config));
//  }
//  
//  ~AssetManager() {
//      int count = android_atomic_dec(&gCount);
//      if (kIsDebug) {
//          ALOGI("Destroying AssetManager in %s #%d\n", this, count);
//      } else {
//          ALOGI("Destroying AssetManager in %s #%d\n", this, count);
//      }
//      // Manually close any fd paths for which we have not yet opened their zip (which
//      // will take ownership of the fd and close it when done).
//      for (size_t i=0; i<mAssetPaths.size(); i++) {
//          ALOGV("Cleaning path #%d: fd=%d, zip=%p", (int)i, mAssetPaths[i].rawFd,
//                  mAssetPaths[i].zip.get());
//          if (mAssetPaths[i].rawFd >= 0 && mAssetPaths[i].zip == NULL) {
//              close(mAssetPaths[i].rawFd);
//          }
//      }
//
//      delete mConfig;
//      delete mResources;
//  
//      // don't have a String class yet, so make sure we clean up
//      delete[] mLocale;
//  }

  public boolean addAssetPath(String8 path, Ref<Integer> cookie, boolean appAsLib) {
    return addAssetPath(path, cookie, appAsLib, false);
  }

  public boolean addAssetPath(
      final String8 path, @Nullable Ref<Integer> cookie, boolean appAsLib, boolean isSystemAsset) {
    synchronized (mLock) {

      asset_path ap = new asset_path();

      String8 realPath = path;
      if (kAppZipName != null) {
        realPath.appendPath(kAppZipName);
      }
      ap.type = getFileType(realPath.string());
      if (ap.type == FileType.kFileTypeRegular) {
        ap.path = realPath;
      } else {
        ap.path = path;
        ap.type = getFileType(path.string());
        if (ap.type != kFileTypeDirectory && ap.type != FileType.kFileTypeRegular) {
          ALOGW("Asset path %s is neither a directory nor file (type=%s).",
              path.toString(), ap.type.name());
          return false;
        }
      }

      // Skip if we have it already.
      for (int i = 0; i < mAssetPaths.size(); i++) {
        if (mAssetPaths.get(i).path.equals(ap.path)) {
          if (cookie != null) {
            cookie.set(i + 1);
          }
          return true;
        }
      }

      ALOGV("In %s Asset %s path: %s", this,
          ap.type.name(), ap.path.toString());

      ap.isSystemAsset = isSystemAsset;
      /*int apPos =*/ mAssetPaths.add(ap);

      // new paths are always added at the end
      if (cookie != null) {
        cookie.set(mAssetPaths.size());
      }

      // TODO: implement this?
      //#ifdef __ANDROID__
      // Load overlays, if any
      //asset_path oap;
      //for (int idx = 0; mZipSet.getOverlay(ap.path, idx, & oap)
      //  ; idx++){
      //  oap.isSystemAsset = isSystemAsset;
      //  mAssetPaths.add(oap);
      // }
      //#endif

      if (mResources != null) {
        // appendPathToResTable(mAssetPaths.editItemAt(apPos), appAsLib);
        appendPathToResTable(ap, appAsLib);
      }

      return true;
    }
  }

  //
//  boolean addOverlayPath(final String8 packagePath, Ref<Integer> cookie)
//  {
//      final String8 idmapPath = idmapPathForPackagePath(packagePath);
//
//      synchronized (mLock) {
//
//        for (int i = 0; i < mAssetPaths.size(); ++i) {
//          if (mAssetPaths.get(i).idmap.equals(idmapPath)) {
//             cookie.set(i + 1);
//            return true;
//          }
//        }
//
//        Asset idmap = null;
//        if ((idmap = openAssetFromFileLocked(idmapPath, Asset.AccessMode.ACCESS_BUFFER)) == null) {
//          ALOGW("failed to open idmap file %s\n", idmapPath.string());
//          return false;
//        }
//
//        String8 targetPath;
//        String8 overlayPath;
//        if (!ResTable.getIdmapInfo(idmap.getBuffer(false), idmap.getLength(),
//            null, null, null, & targetPath, &overlayPath)){
//          ALOGW("failed to read idmap file %s\n", idmapPath.string());
//          // delete idmap;
//          return false;
//        }
//        // delete idmap;
//
//        if (overlayPath != packagePath) {
//          ALOGW("idmap file %s inconcistent: expected path %s does not match actual path %s\n",
//              idmapPath.string(), packagePath.string(), overlayPath.string());
//          return false;
//        }
//        if (access(targetPath.string(), R_OK) != 0) {
//          ALOGW("failed to access file %s: %s\n", targetPath.string(), strerror(errno));
//          return false;
//        }
//        if (access(idmapPath.string(), R_OK) != 0) {
//          ALOGW("failed to access file %s: %s\n", idmapPath.string(), strerror(errno));
//          return false;
//        }
//        if (access(overlayPath.string(), R_OK) != 0) {
//          ALOGW("failed to access file %s: %s\n", overlayPath.string(), strerror(errno));
//          return false;
//        }
//
//        asset_path oap;
//        oap.path = overlayPath;
//        oap.type = .getFileType(overlayPath.string());
//        oap.idmap = idmapPath;
//  #if 0
//        ALOGD("Overlay added: targetPath=%s overlayPath=%s idmapPath=%s\n",
//            targetPath.string(), overlayPath.string(), idmapPath.string());
//  #endif
//        mAssetPaths.add(oap);
//      *cookie = static_cast <int>(mAssetPaths.size());
//
//        if (mResources != null) {
//          appendPathToResTable(oap);
//        }
//
//        return true;
//      }
//   }
//  
//  boolean createIdmap(final char* targetApkPath, final char* overlayApkPath,
//          uint32_t targetCrc, uint32_t overlayCrc, uint32_t** outData, int* outSize)
//  {
//      AutoMutex _l(mLock);
//      final String8 paths[2] = { String8(targetApkPath), String8(overlayApkPath) };
//      Asset* assets[2] = {null, null};
//      boolean ret = false;
//      {
//          ResTable tables[2];
//  
//          for (int i = 0; i < 2; ++i) {
//              asset_path ap;
//              ap.type = kFileTypeRegular;
//              ap.path = paths[i];
//              assets[i] = openNonAssetInPathLocked("resources.arsc",
//                      Asset.ACCESS_BUFFER, ap);
//              if (assets[i] == null) {
//                  ALOGW("failed to find resources.arsc in %s\n", ap.path.string());
//                  goto exit;
//              }
//              if (tables[i].add(assets[i]) != NO_ERROR) {
//                  ALOGW("failed to add %s to resource table", paths[i].string());
//                  goto exit;
//              }
//          }
//          ret = tables[0].createIdmap(tables[1], targetCrc, overlayCrc,
//                  targetApkPath, overlayApkPath, (void**)outData, outSize) == NO_ERROR;
//      }
//  
//  exit:
//      delete assets[0];
//      delete assets[1];
//      return ret;
//  }
//  
  public boolean addDefaultAssets(String systemAssetsPath) {
    String8 path = new String8(systemAssetsPath);
    return addAssetPath(path, null, false /* appAsLib */, true /* isSystemAsset */);
  }
//  
//  int nextAssetPath(final int cookie) final
//  {
//      AutoMutex _l(mLock);
//      final int next = static_cast<int>(cookie) + 1;
//      return next > mAssetPaths.size() ? -1 : next;
//  }
//  
//  String8 getAssetPath(final int cookie) final
//  {
//      AutoMutex _l(mLock);
//      final int which = static_cast<int>(cookie) - 1;
//      if (which < mAssetPaths.size()) {
//          return mAssetPaths[which].path;
//      }
//      return String8();
//  }

  void setLocaleLocked(final String locale) {
//      if (mLocale != null) {
//          delete[] mLocale;
//      }

    mLocale = /*strdupNew*/(locale);
    updateResourceParamsLocked();
  }

  public void setConfiguration(final ResTable_config config, final String locale) {
    synchronized (mLock) {
      mConfig = config;
      if (isTruthy(locale)) {
        setLocaleLocked(locale);
      } else {
        if (config.language[0] != 0) {
//          byte[] spec = new byte[RESTABLE_MAX_LOCALE_LEN];
          String spec = config.getBcp47Locale(false);
          setLocaleLocked(spec);
        } else {
          updateResourceParamsLocked();
        }
      }
    }
  }

  @VisibleForTesting
  public void getConfiguration(Ref<ResTable_config> outConfig) {
    synchronized (mLock) {
      outConfig.set(mConfig);
    }
  }

  /*
   * Open an asset.
   *
   * The data could be in any asset path. Each asset path could be:
   *  - A directory on disk.
   *  - A Zip archive, uncompressed or compressed.
   *
   * If the file is in a directory, it could have a .gz suffix, meaning it is compressed.
   *
   * We should probably reject requests for "illegal" filenames, e.g. those
   * with illegal characters or "../" backward relative paths.
   */
  public Asset open(final String fileName, AccessMode mode) {
    synchronized (mLock) {
      LOG_FATAL_IF(mAssetPaths.isEmpty(), "No assets added to AssetManager");

      String8 assetName = new String8(kAssetsRoot);
      assetName.appendPath(fileName);
      /*
       * For each top-level asset path, search for the asset.
       */
      int i = mAssetPaths.size();
      while (i > 0) {
        i--;
        ALOGV("Looking for asset '%s' in '%s'\n",
            assetName.string(), mAssetPaths.get(i).path.string());
        Asset pAsset = openNonAssetInPathLocked(assetName.string(), mode,
            mAssetPaths.get(i));
        if (pAsset != null) {
          return Objects.equals(pAsset, kExcludedAsset) ? null : pAsset;
        }
      }

      return null;
    }
  }

  /*
   * Open a non-asset file as if it were an asset.
   *
   * The "fileName" is the partial path starting from the application name.
   */
  public Asset openNonAsset(final String fileName, AccessMode mode, Ref<Integer> outCookie) {
    synchronized (mLock) {
      //      AutoMutex _l(mLock);

      LOG_FATAL_IF(mAssetPaths.isEmpty(), "No assets added to AssetManager");

      /*
       * For each top-level asset path, search for the asset.
       */

      int i = mAssetPaths.size();
      while (i > 0) {
        i--;
        ALOGV("Looking for non-asset '%s' in '%s'\n", fileName,
            mAssetPaths.get(i).path.string());
        Asset pAsset = openNonAssetInPathLocked(
            fileName, mode, mAssetPaths.get(i));
        if (pAsset != null) {
          if (outCookie != null) {
            outCookie.set(i + 1);
          }
          return pAsset != kExcludedAsset ? pAsset : null;
        }
      }

      return null;
    }
  }

  public Asset openNonAsset(final int cookie, final String fileName, AccessMode mode) {
    final int which = cookie - 1;

    synchronized (mLock) {
      LOG_FATAL_IF(mAssetPaths.isEmpty(), "No assets added to AssetManager");

      if (which < mAssetPaths.size()) {
        ALOGV("Looking for non-asset '%s' in '%s'\n", fileName,
            mAssetPaths.get(which).path.string());
        Asset pAsset = openNonAssetInPathLocked(
            fileName, mode, mAssetPaths.get(which));
        if (pAsset != null) {
          return pAsset != kExcludedAsset ? pAsset : null;
        }
      }

      return null;
    }
  }

  /*
   * Get the type of a file
   */
  FileType getFileType(final String fileName) {
    // deviate from Android CPP implementation here. Assume fileName is a complete path
    // rather than limited to just asset namespace
    File assetFile = new File(fileName);
    if (!assetFile.exists()) {
      return FileType.kFileTypeNonexistent;
    } else if (assetFile.isFile()) {
      return FileType.kFileTypeRegular;
    } else if (assetFile.isDirectory()) {
      return kFileTypeDirectory;
    }
    return FileType.kFileTypeNonexistent;
//      Asset pAsset = null;
//
//      /*
//       * Open the asset.  This is less efficient than simply finding the
//       * file, but it's not too bad (we don't uncompress or mmap data until
//       * the first read() call).
//       */
//      pAsset = open(fileName, Asset.AccessMode.ACCESS_STREAMING);
//      // delete pAsset;
//
//      if (pAsset == null) {
//          return FileType.kFileTypeNonexistent;
//      } else {
//          return FileType.kFileTypeRegular;
//      }
  }

  boolean appendPathToResTable(final asset_path ap, boolean appAsLib) {
    return PerfStatsCollector.getInstance()
        .measure(
            "load binary " + (ap.isSystemAsset ? "framework" : "app") + " resources",
            () -> appendPathToResTable_measured(ap, appAsLib));
  }

  boolean appendPathToResTable_measured(final asset_path ap, boolean appAsLib) {
    // TODO: properly handle reading system resources
//    if (!ap.isSystemAsset) {
//      URL resource = getClass().getResource("/resources.ap_"); // todo get this from asset_path
//      // System.out.println("Reading ARSC file  from " + resource);
//      LOG_FATAL_IF(resource == null, "Could not find resources.ap_");
//      try {
//        ZipFile zipFile = new ZipFile(resource.getFile());
//        ZipEntry arscEntry = zipFile.getEntry("resources.arsc");
//        InputStream inputStream = zipFile.getInputStream(arscEntry);
//        mResources.add(inputStream, mResources.getTableCount() + 1);
//      } catch (IOException e) {
//        throw new RuntimeException(e);
//      }
//    } else {
//      try {
//        ZipFile zipFile = new ZipFile(ap.path.string());
//        ZipEntry arscEntry = zipFile.getEntry("resources.arsc");
//        InputStream inputStream = zipFile.getInputStream(arscEntry);
//        mResources.add(inputStream, mResources.getTableCount() + 1);
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    }
//    return false;

    // skip those ap's that correspond to system overlays
    if (ap.isSystemOverlay) {
      return true;
    }

    Asset ass = null;
    ResTable sharedRes = null;
    boolean shared = true;
    boolean onlyEmptyResources = true;
//      ATRACE_NAME(ap.path.string());
    Asset idmap = openIdmapLocked(ap);
    int nextEntryIdx = mResources.getTableCount();
    ALOGV("Looking for resource asset in '%s'\n", ap.path.string());
    if (ap.type != kFileTypeDirectory /*&& ap.rawFd < 0*/) {
      if (nextEntryIdx == 0) {
        // The first item is typically the framework resources,
        // which we want to avoid parsing every time.
        sharedRes = mZipSet.getZipResourceTable(ap.path);
        if (sharedRes != null) {
          // skip ahead the number of system overlay packages preloaded
          nextEntryIdx = sharedRes.getTableCount();
        }
      }
      if (sharedRes == null) {
        ass = mZipSet.getZipResourceTableAsset(ap.path);
        if (ass == null) {
          ALOGV("loading resource table %s\n", ap.path.string());
          ass = openNonAssetInPathLocked("resources.arsc",
              AccessMode.ACCESS_BUFFER,
              ap);
          if (ass != null && ass != kExcludedAsset) {
            ass = mZipSet.setZipResourceTableAsset(ap.path, ass);
          }
        }

        if (nextEntryIdx == 0 && ass != null) {
          // If this is the first resource table in the asset
          // manager, then we are going to cache it so that we
          // can quickly copy it out for others.
          ALOGV("Creating shared resources for %s", ap.path.string());
          sharedRes = new ResTable();
          sharedRes.add(ass, idmap, nextEntryIdx + 1, false, false, false);
//  #ifdef __ANDROID__
//                  final char* data = getenv("ANDROID_DATA");
//                  LOG_ALWAYS_FATAL_IF(data == null, "ANDROID_DATA not set");
//                  String8 overlaysListPath(data);
//                  overlaysListPath.appendPath(kResourceCache);
//                  overlaysListPath.appendPath("overlays.list");
//                  addSystemOverlays(overlaysListPath.string(), ap.path, sharedRes, nextEntryIdx);
//  #endif
          sharedRes = mZipSet.setZipResourceTable(ap.path, sharedRes);
        }
      }
    } else {
      ALOGV("loading resource table %s\n", ap.path.string());
      ass = openNonAssetInPathLocked("resources.arsc",
          AccessMode.ACCESS_BUFFER,
          ap);
      shared = false;
    }

    if ((ass != null || sharedRes != null) && ass != kExcludedAsset) {
      ALOGV("Installing resource asset %s in to table %s\n", ass, mResources);
      if (sharedRes != null) {
        ALOGV("Copying existing resources for %s", ap.path.string());
        mResources.add(sharedRes, ap.isSystemAsset);
      } else {
        ALOGV("Parsing resources for %s", ap.path.string());
        mResources.add(ass, idmap, nextEntryIdx + 1, !shared, appAsLib, ap.isSystemAsset);
      }
      onlyEmptyResources = false;

//          if (!shared) {
//              delete ass;
//          }
    } else {
      ALOGV("Installing empty resources in to table %s\n", mResources);
      mResources.addEmpty(nextEntryIdx + 1);
    }

//      if (idmap != null) {
//          delete idmap;
//      }
    return onlyEmptyResources;
  }

  final ResTable getResTable(boolean required) {
    ResTable rt = mResources;
    if (isTruthy(rt)) {
      return rt;
    }

    // Iterate through all asset packages, collecting resources from each.

    synchronized (mLock) {
      if (mResources != null) {
        return mResources;
      }

      if (required) {
        LOG_FATAL_IF(mAssetPaths.isEmpty(), "No assets added to AssetManager");
      }

      PerfStatsCollector.getInstance().measure("load binary resources", () -> {
        mResources = new ResTable();
        updateResourceParamsLocked();

        boolean onlyEmptyResources = true;
        final int N = mAssetPaths.size();
        for (int i = 0; i < N; i++) {
          boolean empty = appendPathToResTable(mAssetPaths.get(i), false);
          onlyEmptyResources = onlyEmptyResources && empty;
        }

        if (required && onlyEmptyResources) {
          ALOGW("Unable to find resources file resources.arsc");
//          delete mResources;
          mResources = null;
        }
      });

      return mResources;
    }
  }

  void updateResourceParamsLocked() {
    ATRACE_CALL();
    ResTable res = mResources;
    if (!isTruthy(res)) {
      return;
    }

    if (isTruthy(mLocale)) {
      mConfig.setBcp47Locale(mLocale);
    } else {
      mConfig.clearLocale();
    }

    res.setParameters(mConfig);
  }

  Asset openIdmapLocked(asset_path ap) {
    Asset ass = null;
    if (ap.idmap.length() != 0) {
      ass = openAssetFromFileLocked(ap.idmap, AccessMode.ACCESS_BUFFER);
      if (isTruthy(ass)) {
        ALOGV("loading idmap %s\n", ap.idmap.string());
      } else {
        ALOGW("failed to load idmap %s\n", ap.idmap.string());
      }
    }
    return ass;
  }

//  void addSystemOverlays(final char* pathOverlaysList,
//          final String8& targetPackagePath, ResTable* sharedRes, int offset) final
//  {
//      FILE* fin = fopen(pathOverlaysList, "r");
//      if (fin == null) {
//          return;
//      }
//  
//  #ifndef _WIN32
//      if (TEMP_FAILURE_RETRY(flock(fileno(fin), LOCK_SH)) != 0) {
//          fclose(fin);
//          return;
//      }
//  #endif
//      char buf[1024];
//      while (fgets(buf, sizeof(buf), fin)) {
//          // format of each line:
//          //   <path to apk><space><path to idmap><newline>
//          char* space = strchr(buf, ' ');
//          char* newline = strchr(buf, '\n');
//          asset_path oap;
//  
//          if (space == null || newline == null || newline < space) {
//              continue;
//          }
//  
//          oap.path = String8(buf, space - buf);
//          oap.type = kFileTypeRegular;
//          oap.idmap = String8(space + 1, newline - space - 1);
//          oap.isSystemOverlay = true;
//  
//          Asset* oass = final_cast<AssetManager*>(this).
//              openNonAssetInPathLocked("resources.arsc",
//                      Asset.ACCESS_BUFFER,
//                      oap);
//  
//          if (oass != null) {
//              Asset* oidmap = openIdmapLocked(oap);
//              offset++;
//              sharedRes.add(oass, oidmap, offset + 1, false);
//              final_cast<AssetManager*>(this).mAssetPaths.add(oap);
//              final_cast<AssetManager*>(this).mZipSet.addOverlay(targetPackagePath, oap);
//              delete oidmap;
//          }
//      }
//  
//  #ifndef _WIN32
//      TEMP_FAILURE_RETRY(flock(fileno(fin), LOCK_UN));
//  #endif
//      fclose(fin);
//  }

  public final ResTable getResources() {
    return getResources(true);
  }

  final ResTable getResources(boolean required) {
    final ResTable rt = getResTable(required);
    return rt;
  }

  //  boolean isUpToDate()
//  {
//      AutoMutex _l(mLock);
//      return mZipSet.isUpToDate();
//  }
//  
//  void getLocales(Vector<String8>* locales, boolean includeSystemLocales) final
//  {
//      ResTable* res = mResources;
//      if (res != null) {
//          res.getLocales(locales, includeSystemLocales, true /* mergeEquivalentLangs */);
//      }
//  }
//  
  /*
   * Open a non-asset file as if it were an asset, searching for it in the
   * specified app.
   *
   * Pass in a null values for "appName" if the common app directory should
   * be used.
   */
  static Asset openNonAssetInPathLocked(final String fileName, AccessMode mode,
      final asset_path ap) {
    Asset pAsset = null;

      /* look at the filesystem on disk */
    if (ap.type == kFileTypeDirectory) {
      String8 path = new String8(ap.path);
      path.appendPath(fileName);

      pAsset = openAssetFromFileLocked(path, mode);

      if (pAsset == null) {
              /* try again, this time with ".gz" */
        path.append(".gz");
        pAsset = openAssetFromFileLocked(path, mode);
      }

      if (pAsset != null) {
        //printf("FOUND NA '%s' on disk\n", fileName);
        pAsset.setAssetSource(path);
      }

      /* look inside the zip file */
    } else {
      String8 path = new String8(fileName);

          /* check the appropriate Zip file */
      ZipFileRO pZip = getZipFileLocked(ap);
      if (pZip != null) {
        //printf("GOT zip, checking NA '%s'\n", (final char*) path);
        ZipEntryRO entry = pZip.findEntryByName(path.string());
        if (entry != null) {
          //printf("FOUND NA in Zip file for %s\n", appName ? appName : kAppCommon);
          pAsset = openAssetFromZipLocked(pZip, entry, mode, path);
          pZip.releaseEntry(entry);
        }
      }

      if (pAsset != null) {
              /* create a "source" name, for debug/display */
        pAsset.setAssetSource(
            createZipSourceNameLocked(ap.path, new String8(), new String8(fileName)));
      }
    }

    return pAsset;
  }

  /*
   * Create a "source name" for a file from a Zip archive.
   */
  static String8 createZipSourceNameLocked(final String8 zipFileName,
      final String8 dirName, final String8 fileName) {
    String8 sourceName = new String8("zip:");
    sourceName.append(zipFileName.string());
    sourceName.append(":");
    if (dirName.length() > 0) {
      sourceName.appendPath(dirName.string());
    }
    sourceName.appendPath(fileName.string());
    return sourceName;
  }

  /*
   * Create a path to a loose asset (asset-base/app/rootDir).
   */
  static String8 createPathNameLocked(final asset_path ap, final String rootDir) {
    String8 path = new String8(ap.path);
    if (rootDir != null) {
      path.appendPath(rootDir);
    }
    return path;
  }

  /*
   * Return a pointer to one of our open Zip archives.  Returns null if no
   * matching Zip file exists.
   */
  static ZipFileRO getZipFileLocked(final asset_path ap) {
    ALOGV("getZipFileLocked() in %s\n", CppAssetManager.class);

    return mZipSet.getZip(ap.path.string());
  }

  /*
   * Try to open an asset from a file on disk.
   *
   * If the file is compressed with gzip, we seek to the start of the
   * deflated data and pass that in (just like we would for a Zip archive).
   *
   * For uncompressed data, we may already have an mmap()ed version sitting
   * around.  If so, we want to hand that to the Asset instead.
   *
   * This returns null if the file doesn't exist, couldn't be opened, or
   * claims to be a ".gz" but isn't.
   */
  static Asset openAssetFromFileLocked(final String8 pathName,
      AccessMode mode) {
    Asset pAsset = null;

    if (pathName.getPathExtension().toLowerCase().equals(".gz")) {
      //printf("TRYING '%s'\n", (final char*) pathName);
      pAsset = Asset.createFromCompressedFile(pathName.string(), mode);
    } else {
      //printf("TRYING '%s'\n", (final char*) pathName);
      pAsset = Asset.createFromFile(pathName.string(), mode);
    }

    return pAsset;
  }

  /*
   * Given an entry in a Zip archive, create a new Asset object.
   *
   * If the entry is uncompressed, we may want to create or share a
   * slice of shared memory.
   */
  static Asset openAssetFromZipLocked(final ZipFileRO pZipFile,
      final ZipEntryRO entry, AccessMode mode, final String8 entryName) {
    Asset pAsset = null;

    // TODO: look for previously-created shared memory slice?
    final Ref<Short> method = new Ref<>((short) 0);
    final Ref<Long> uncompressedLen = new Ref<>(0L);

    //printf("USING Zip '%s'\n", pEntry.getFileName());

    if (!pZipFile.getEntryInfo(entry, method, uncompressedLen, null, null,
        null, null)) {
      ALOGW("getEntryInfo failed\n");
      return null;
    }

    //return Asset.createFromZipEntry(pZipFile, entry, entryName);
    FileMap dataMap = pZipFile.createEntryFileMap(entry);
//      if (dataMap == null) {
//          ALOGW("create map from entry failed\n");
//          return null;
//      }
//
    if (method.get() == ZipFileRO.kCompressStored) {
      pAsset = Asset.createFromUncompressedMap(dataMap, mode);
      ALOGV("Opened uncompressed entry %s in zip %s mode %s: %s", entryName.string(),
          pZipFile.mFileName, mode, pAsset);
    } else {
      pAsset = Asset.createFromCompressedMap(dataMap, toIntExact(uncompressedLen.get()), mode);
      ALOGV("Opened compressed entry %s in zip %s mode %s: %s", entryName.string(),
          pZipFile.mFileName, mode, pAsset);
    }
    if (pAsset == null) {
         /* unexpected */
      ALOGW("create from segment failed\n");
    }

    return pAsset;
  }

  /*
   * Open a directory in the asset namespace.
   *
   * An "asset directory" is simply the combination of all asset paths' "assets/" directories.
   *
   * Pass in "" for the root dir.
   */
  public AssetDir openDir(final String dirName) {
    synchronized (mLock) {

      AssetDir pDir = null;
      final Ref<SortedVector<AssetDir.FileInfo>> pMergedInfo;

      LOG_FATAL_IF(mAssetPaths.isEmpty(), "No assets added to AssetManager");
      Preconditions.checkNotNull(dirName);

      //printf("+++ openDir(%s) in '%s'\n", dirName, (final char*) mAssetBase);

      pDir = new AssetDir();

      /*
       * Scan the various directories, merging what we find into a single
       * vector.  We want to scan them in reverse priority order so that
       * the ".EXCLUDE" processing works correctly.  Also, if we decide we
       * want to remember where the file is coming from, we'll get the right
       * version.
       *
       * We start with Zip archives, then do loose files.
       */
      pMergedInfo = new Ref<>(new SortedVector<AssetDir.FileInfo>());

      int i = mAssetPaths.size();
      while (i > 0) {
        i--;
        final asset_path ap = mAssetPaths.get(i);
        if (ap.type == FileType.kFileTypeRegular) {
          ALOGV("Adding directory %s from zip %s", dirName, ap.path.string());
          scanAndMergeZipLocked(pMergedInfo, ap, kAssetsRoot, dirName);
        } else {
          ALOGV("Adding directory %s from dir %s", dirName, ap.path.string());
          scanAndMergeDirLocked(pMergedInfo, ap, kAssetsRoot, dirName);
        }
      }

//  #if 0
//        printf("FILE LIST:\n");
//        for (i = 0; i < (int) pMergedInfo.size(); i++) {
//          printf(" %d: (%d) '%s'\n", i,
//              pMergedInfo.itemAt(i).getFileType(),
//              ( final char*)pMergedInfo.itemAt(i).getFileName());
//        }
//  #endif

      pDir.setFileList(pMergedInfo.get());
      return pDir;
    }
  }

  //
//  /*
//   * Open a directory in the non-asset namespace.
//   *
//   * An "asset directory" is simply the combination of all asset paths' "assets/" directories.
//   *
//   * Pass in "" for the root dir.
//   */
//  AssetDir* openNonAssetDir(final int cookie, final char* dirName)
//  {
//      AutoMutex _l(mLock);
//  
//      AssetDir* pDir = null;
//      SortedVector<AssetDir.FileInfo>* pMergedInfo = null;
//  
//      LOG_FATAL_IF(mAssetPaths.isEmpty(), "No assets added to AssetManager");
//      assert(dirName != null);
//  
//      //printf("+++ openDir(%s) in '%s'\n", dirName, (final char*) mAssetBase);
//  
//      pDir = new AssetDir;
//  
//      pMergedInfo = new SortedVector<AssetDir.FileInfo>;
//  
//      final int which = static_cast<int>(cookie) - 1;
//  
//      if (which < mAssetPaths.size()) {
//          final asset_path& ap = mAssetPaths.itemAt(which);
//          if (ap.type == kFileTypeRegular) {
//              ALOGV("Adding directory %s from zip %s", dirName, ap.path.string());
//              scanAndMergeZipLocked(pMergedInfo, ap, null, dirName);
//          } else {
//              ALOGV("Adding directory %s from dir %s", dirName, ap.path.string());
//              scanAndMergeDirLocked(pMergedInfo, ap, null, dirName);
//          }
//      }
//  
//  #if 0
//      printf("FILE LIST:\n");
//      for (i = 0; i < (int) pMergedInfo.size(); i++) {
//          printf(" %d: (%d) '%s'\n", i,
//              pMergedInfo.itemAt(i).getFileType(),
//              (final char*) pMergedInfo.itemAt(i).getFileName());
//      }
//  #endif
//  
//      pDir.setFileList(pMergedInfo);
//      return pDir;
//  }
//  
  /*
   * Scan the contents of the specified directory and merge them into the
   * "pMergedInfo" vector, removing previous entries if we find "exclude"
   * directives.
   *
   * Returns "false" if we found nothing to contribute.
   */
  boolean scanAndMergeDirLocked(Ref<SortedVector<AssetDir.FileInfo>> pMergedInfoRef,
      final asset_path ap, final String rootDir, final String dirName) {
    SortedVector<AssetDir.FileInfo> pMergedInfo = pMergedInfoRef.get();
    assert (pMergedInfo != null);

    //printf("scanAndMergeDir: %s %s %s\n", ap.path.string(), rootDir, dirName);

    String8 path = createPathNameLocked(ap, rootDir);
    if (dirName.charAt(0) != '\0') {
      path.appendPath(dirName);
    }

    SortedVector<AssetDir.FileInfo> pContents = scanDirLocked(path);
    if (pContents == null) {
      return false;
    }

    // if we wanted to do an incremental cache fill, we would do it here

      /*
       * Process "exclude" directives.  If we find a filename that ends with
       * ".EXCLUDE", we look for a matching entry in the "merged" set, and
       * remove it if we find it.  We also delete the "exclude" entry.
       */
    int i, count, exclExtLen;

    count = pContents.size();
    exclExtLen = kExcludeExtension.length();
    for (i = 0; i < count; i++) {
      final String name;
      int nameLen;

      name = pContents.itemAt(i).getFileName().string();
      nameLen = name.length();
      if (name.endsWith(kExcludeExtension)) {
        String8 match = new String8(name, nameLen - exclExtLen);
        int matchIdx;

        matchIdx = AssetDir.FileInfo.findEntry(pMergedInfo, match);
        if (matchIdx > 0) {
          ALOGV("Excluding '%s' [%s]\n",
              pMergedInfo.itemAt(matchIdx).getFileName().string(),
              pMergedInfo.itemAt(matchIdx).getSourceName().string());
          pMergedInfo.removeAt(matchIdx);
        } else {
          //printf("+++ no match on '%s'\n", (final char*) match);
        }

        ALOGD("HEY: size=%d removing %d\n", (int) pContents.size(), i);
        pContents.removeAt(i);
        i--;        // adjust "for" loop
        count--;    //  and loop limit
      }
    }

    mergeInfoLocked(pMergedInfoRef, pContents);

    return true;
  }

  /*
   * Scan the contents of the specified directory, and stuff what we find
   * into a newly-allocated vector.
   *
   * Files ending in ".gz" will have their extensions removed.
   *
   * We should probably think about skipping files with "illegal" names,
   * e.g. illegal characters (/\:) or excessive length.
   *
   * Returns null if the specified directory doesn't exist.
   */
  SortedVector<AssetDir.FileInfo> scanDirLocked(final String8 path) {

    String8 pathCopy = new String8(path);
    SortedVector<AssetDir.FileInfo> pContents = null;
    //DIR* dir;
    File dir;
    FileType fileType;

    ALOGV("Scanning dir '%s'\n", path.string());

    dir = new File(path.string());
    if (!dir.exists()) {
      return null;
    }

    pContents = new SortedVector<>();

    for (File entry : dir.listFiles()) {
      if (entry == null) {
        break;
      }

//          if (strcmp(entry.d_name, ".") == 0 ||
//              strcmp(entry.d_name, "..") == 0)
//              continue;

//  #ifdef _DIRENT_HAVE_D_TYPE
//          if (entry.d_type == DT_REG)
//              fileType = kFileTypeRegular;
//          else if (entry.d_type == DT_DIR)
//              fileType = kFileTypeDirectory;
//          else
//              fileType = kFileTypeUnknown;
//  #else
      // stat the file
      fileType = getFileType(pathCopy.appendPath(entry.getName()).string());
//  #endif

      if (fileType != FileType.kFileTypeRegular && fileType != kFileTypeDirectory) {
        continue;
      }

      AssetDir.FileInfo info = new AssetDir.FileInfo();
      info.set(new String8(entry.getName()), fileType);
      if (info.getFileName().getPathExtension().equalsIgnoreCase(".gz")) {
        info.setFileName(info.getFileName().getBasePath());
      }
      info.setSourceName(pathCopy.appendPath(info.getFileName().string()));
      pContents.add(info);
    }

    return pContents;
  }

  /*
   * Scan the contents out of the specified Zip archive, and merge what we
   * find into "pMergedInfo".  If the Zip archive in question doesn't exist,
   * we return immediately.
   *
   * Returns "false" if we found nothing to contribute.
   */
  boolean scanAndMergeZipLocked(Ref<SortedVector<AssetDir.FileInfo>> pMergedInfo,
      final asset_path ap, final String rootDir, final String baseDirName) {
    ZipFileRO pZip;
    List<String8> dirs = new ArrayList<>();
    //AssetDir.FileInfo info = new FileInfo();
    SortedVector<AssetDir.FileInfo> contents = new SortedVector<>();
    String8 sourceName;
    String8 zipName;
    String8 dirName = new String8();

    pZip = mZipSet.getZip(ap.path.string());
    if (pZip == null) {
      ALOGW("Failure opening zip %s\n", ap.path.string());
      return false;
    }

    zipName = ZipSet.getPathName(ap.path.string());

      /* convert "sounds" to "rootDir/sounds" */
    if (rootDir != null) {
      dirName = new String8(rootDir);
    }

    dirName.appendPath(baseDirName);

    /*
     * Scan through the list of files, looking for a match.  The files in
     * the Zip table of contents are not in sorted order, so we have to
     * process the entire list.  We're looking for a string that begins
     * with the characters in "dirName", is followed by a '/', and has no
     * subsequent '/' in the stuff that follows.
     *
     * What makes this especially fun is that directories are not stored
     * explicitly in Zip archives, so we have to infer them from context.
     * When we see "sounds/foo.wav" we have to leave a note to ourselves
     * to insert a directory called "sounds" into the list.  We store
     * these in temporary vector so that we only return each one once.
     *
     * Name comparisons are case-sensitive to match UNIX filesystem
     * semantics.
     */
    int dirNameLen = dirName.length();
    final Ref<Enumeration<? extends ZipEntry>> iterationCookie = new Ref<>(null);
    if (!pZip.startIteration(iterationCookie, dirName.string(), null)) {
      ALOGW("ZipFileRO.startIteration returned false");
      return false;
    }

    ZipEntryRO entry;
    while ((entry = pZip.nextEntry(iterationCookie.get())) != null) {

      final Ref<String> nameBuf = new Ref<>(null);

      if (pZip.getEntryFileName(entry, nameBuf) != 0) {
        // TODO: fix this if we expect to have long names
        ALOGE("ARGH: name too long?\n");
        continue;
      }

//      System.out.printf("Comparing %s in %s?\n", nameBuf.get(), dirName.string());
      if (!nameBuf.get().startsWith(dirName.string() + '/')) {
        // not matching
        continue;
      }
      if (dirNameLen == 0 || nameBuf.get().charAt(dirNameLen) == '/') {
        int cp = 0;
        int nextSlashIndex;

        //cp = nameBuf + dirNameLen;
        cp += dirNameLen;
        if (dirNameLen != 0) {
          cp++;       // advance past the '/'
        }

        nextSlashIndex = nameBuf.get().indexOf('/', cp);
        //xxx this may break if there are bare directory entries
        if (nextSlashIndex == -1) {
          /* this is a file in the requested directory */
          String8 fileName = new String8(nameBuf.get()).getPathLeaf();
          if (fileName.string().isEmpty()) {
            // ignore
            continue;
          }
          AssetDir.FileInfo info = new FileInfo();
          info.set(fileName, FileType.kFileTypeRegular);

          info.setSourceName(
              createZipSourceNameLocked(zipName, dirName, info.getFileName()));

          contents.add(info);
          //printf("FOUND: file '%s'\n", info.getFileName().string());
        } else {
          /* this is a subdir; add it if we don't already have it*/
          String8 subdirName = new String8(nameBuf.get().substring(cp, nextSlashIndex));
          int j;
          int N = dirs.size();

          for (j = 0; j < N; j++) {
            if (subdirName.equals(dirs.get(j))) {
              break;
            }
          }
          if (j == N) {
            dirs.add(subdirName);
          }

          //printf("FOUND: dir '%s'\n", subdirName.string());
        }
      }
    }

    pZip.endIteration(iterationCookie);

      /*
       * Add the set of unique directories.
       */
    for (int i = 0; i < dirs.size(); i++) {
      AssetDir.FileInfo info = new FileInfo();
      info.set(dirs.get(i), kFileTypeDirectory);
      info.setSourceName(
          createZipSourceNameLocked(zipName, dirName, info.getFileName()));
      contents.add(info);
    }

    mergeInfoLocked(pMergedInfo, contents);

    return true;

  }


  /*
   * Merge two vectors of FileInfo.
   *
   * The merged contents will be stuffed into *pMergedInfo.
   *
   * If an entry for a file exists in both "pMergedInfo" and "pContents",
   * we use the newer "pContents" entry.
   */
  void mergeInfoLocked(Ref<SortedVector<AssetDir.FileInfo>> pMergedInfoRef,
      final SortedVector<AssetDir.FileInfo> pContents) {
      /*
       * Merge what we found in this directory with what we found in
       * other places.
       *
       * Two basic approaches:
       * (1) Create a new array that holds the unique values of the two
       *     arrays.
       * (2) Take the elements from pContents and shove them into pMergedInfo.
       *
       * Because these are vectors of complex objects, moving elements around
       * inside the vector requires finalructing new objects and allocating
       * storage for members.  With approach #1, we're always adding to the
       * end, whereas with #2 we could be inserting multiple elements at the
       * front of the vector.  Approach #1 requires a full copy of the
       * contents of pMergedInfo, but approach #2 requires the same copy for
       * every insertion at the front of pMergedInfo.
       *
       * (We should probably use a SortedVector interface that allows us to
       * just stuff items in, trusting us to maintain the sort order.)
       */
    SortedVector<AssetDir.FileInfo> pNewSorted;
    int mergeMax, contMax;
    int mergeIdx, contIdx;

    SortedVector<AssetDir.FileInfo> pMergedInfo = pMergedInfoRef.get();
    pNewSorted = new SortedVector<>();
    mergeMax = pMergedInfo.size();
    contMax = pContents.size();
    mergeIdx = contIdx = 0;

    while (mergeIdx < mergeMax || contIdx < contMax) {
      if (mergeIdx == mergeMax) {
              /* hit end of "merge" list, copy rest of "contents" */
        pNewSorted.add(pContents.itemAt(contIdx));
        contIdx++;
      } else if (contIdx == contMax) {
              /* hit end of "cont" list, copy rest of "merge" */
        pNewSorted.add(pMergedInfo.itemAt(mergeIdx));
        mergeIdx++;
      } else if (pMergedInfo.itemAt(mergeIdx) == pContents.itemAt(contIdx)) {
              /* items are identical, add newer and advance both indices */
        pNewSorted.add(pContents.itemAt(contIdx));
        mergeIdx++;
        contIdx++;
      } else if (pMergedInfo.itemAt(mergeIdx).isLessThan(pContents.itemAt(contIdx))) {
              /* "merge" is lower, add that one */
        pNewSorted.add(pMergedInfo.itemAt(mergeIdx));
        mergeIdx++;
      } else {
              /* "cont" is lower, add that one */
        assert (pContents.itemAt(contIdx).isLessThan(pMergedInfo.itemAt(mergeIdx)));
        pNewSorted.add(pContents.itemAt(contIdx));
        contIdx++;
      }
    }

      /*
       * Overwrite the "merged" list with the new stuff.
       */
    pMergedInfoRef.set(pNewSorted);

//  #if 0       // for Vector, rather than SortedVector
//      int i, j;
//      for (i = pContents.size() -1; i >= 0; i--) {
//          boolean add = true;
//
//          for (j = pMergedInfo.size() -1; j >= 0; j--) {
//              /* case-sensitive comparisons, to behave like UNIX fs */
//              if (strcmp(pContents.itemAt(i).mFileName,
//                         pMergedInfo.itemAt(j).mFileName) == 0)
//              {
//                  /* match, don't add this entry */
//                  add = false;
//                  break;
//              }
//          }
//
//          if (add)
//              pMergedInfo.add(pContents.itemAt(i));
//      }
//  #endif
  }

  /*
   * ===========================================================================
   *      SharedZip
   * ===========================================================================
   */

  static class SharedZip /*: public RefBase */ {

    final String mPath;
    final ZipFileRO mZipFile;
    final long mModWhen;

    Asset mResourceTableAsset;
    ResTable mResourceTable;

    List<asset_path> mOverlays;

    final static Object gLock = new Object();
    final static Map<String8, WeakReference<SharedZip>> gOpen = new HashMap<>();

    public SharedZip(String path, long modWhen) {
      this.mPath = path;
      this.mModWhen = modWhen;
      this.mResourceTableAsset = null;
      this.mResourceTable = null;

      if (kIsDebug) {
        ALOGI("Creating SharedZip %s %s\n", this, mPath);
      }
      ALOGV("+++ opening zip '%s'\n", mPath);
      this.mZipFile = ZipFileRO.open(mPath);
      if (mZipFile == null) {
        ALOGD("failed to open Zip archive '%s'\n", mPath);
      }
    }

    static SharedZip get(final String8 path) {
      return get(path, true);
    }

    static SharedZip get(final String8 path, boolean createIfNotPresent) {
      synchronized (gLock) {
        long modWhen = getFileModDate(path.string());
        WeakReference<SharedZip> ref = gOpen.get(path);
        SharedZip zip = ref == null ? null : ref.get();
        if (zip != null && zip.mModWhen == modWhen) {
          return zip;
        }
        if (zip == null && !createIfNotPresent) {
          return null;
        }
        zip = new SharedZip(path.string(), modWhen);
        gOpen.put(path, new WeakReference<>(zip));
        return zip;

      }

    }

    ZipFileRO getZip() {
      return mZipFile;
    }

    Asset getResourceTableAsset() {
      synchronized (gLock) {
        ALOGV("Getting from SharedZip %s resource asset %s\n", this, mResourceTableAsset);
        return mResourceTableAsset;
      }
    }

    Asset setResourceTableAsset(Asset asset) {
      synchronized (gLock) {
        if (mResourceTableAsset == null) {
          // This is not thread safe the first time it is called, so
          // do it here with the global lock held.
          asset.getBuffer(true);
          mResourceTableAsset = asset;
          return asset;
        }
      }
      return mResourceTableAsset;
    }

    ResTable getResourceTable() {
      ALOGV("Getting from SharedZip %s resource table %s\n", this, mResourceTable);
      return mResourceTable;
    }

    ResTable setResourceTable(ResTable res) {
      synchronized (gLock) {
        if (mResourceTable == null) {
          mResourceTable = res;
          return res;
        }
      }
      return mResourceTable;
    }

//  boolean SharedZip.isUpToDate()
//  {
//      time_t modWhen = getFileModDate(mPath.string());
//      return mModWhen == modWhen;
//  }
//
//  void SharedZip.addOverlay(final asset_path& ap)
//  {
//      mOverlays.add(ap);
//  }
//
//  boolean SharedZip.getOverlay(int idx, asset_path* out) final
//  {
//      if (idx >= mOverlays.size()) {
//          return false;
//      }
//      *out = mOverlays[idx];
//      return true;
//  }
//
//  SharedZip.~SharedZip()
//  {
//      if (kIsDebug) {
//          ALOGI("Destroying SharedZip %s %s\n", this, (final char*)mPath);
//      }
//      if (mResourceTable != null) {
//          delete mResourceTable;
//      }
//      if (mResourceTableAsset != null) {
//          delete mResourceTableAsset;
//      }
//      if (mZipFile != null) {
//          delete mZipFile;
//          ALOGV("Closed '%s'\n", mPath.string());
//      }
//  }

    @Override
    public String toString() {
      String id = Integer.toString(System.identityHashCode(this), 16);
      return "SharedZip{mPath='" + mPath + "\', id=0x" + id + "}";
    }
  }


  /*
 * Manage a set of Zip files.  For each file we need a pointer to the
 * ZipFile and a time_t with the file's modification date.
 *
 * We currently only have two zip files (current app, "common" app).
 * (This was originally written for 8, based on app/locale/vendor.)
 */
  static class ZipSet {

    final List<String> mZipPath = new ArrayList<>();
    final List<SharedZip> mZipFile = new ArrayList<>();

  /*
   * ===========================================================================
   *      ZipSet
   * ===========================================================================
   */

    /*
     * Destructor.  Close any open archives.
     */
//  ZipSet.~ZipSet(void)
    @Override
    protected void finalize() {
      int N = mZipFile.size();
      for (int i = 0; i < N; i++) {
        closeZip(i);
      }
    }

    /*
     * Close a Zip file and reset the entry.
     */
    void closeZip(int idx) {
      mZipFile.set(idx, null);
    }


    /*
     * Retrieve the appropriate Zip file from the set.
     */
    synchronized ZipFileRO getZip(final String path) {
      int idx = getIndex(path);
      SharedZip zip = mZipFile.get(idx);
      if (zip == null) {
        zip = SharedZip.get(new String8(path));
        mZipFile.set(idx, zip);
      }
      return zip.getZip();
    }

    synchronized Asset getZipResourceTableAsset(final String8 path) {
      int idx = getIndex(path.string());
      SharedZip zip = mZipFile.get(idx);
      if (zip == null) {
        zip = SharedZip.get(path);
        mZipFile.set(idx, zip);
      }
      return zip.getResourceTableAsset();
    }

    synchronized Asset setZipResourceTableAsset(final String8 path, Asset asset) {
      int idx = getIndex(path.string());
      SharedZip zip = mZipFile.get(idx);
      // doesn't make sense to call before previously accessing.
      return zip.setResourceTableAsset(asset);
    }

    synchronized ResTable getZipResourceTable(final String8 path) {
      int idx = getIndex(path.string());
      SharedZip zip = mZipFile.get(idx);
      if (zip == null) {
        zip = SharedZip.get(path);
        mZipFile.set(idx, zip);
      }
      return zip.getResourceTable();
    }

    synchronized ResTable setZipResourceTable(final String8 path, ResTable res) {
      int idx = getIndex(path.string());
      SharedZip zip = mZipFile.get(idx);
      // doesn't make sense to call before previously accessing.
      return zip.setResourceTable(res);
    }

    /*
     * Generate the partial pathname for the specified archive.  The caller
     * gets to prepend the asset root directory.
     *
     * Returns something like "common/en-US-noogle.jar".
     */
    static String8 getPathName(final String zipPath) {
      return new String8(zipPath);
    }

    //
//  boolean ZipSet.isUpToDate()
//  {
//      final int N = mZipFile.size();
//      for (int i=0; i<N; i++) {
//          if (mZipFile[i] != null && !mZipFile[i].isUpToDate()) {
//              return false;
//          }
//      }
//      return true;
//  }
//
//  void ZipSet.addOverlay(final String8& path, final asset_path& overlay)
//  {
//      int idx = getIndex(path);
//      sp<SharedZip> zip = mZipFile[idx];
//      zip.addOverlay(overlay);
//  }
//
//  boolean ZipSet.getOverlay(final String8& path, int idx, asset_path* out) final
//  {
//      sp<SharedZip> zip = SharedZip.get(path, false);
//      if (zip == null) {
//          return false;
//      }
//      return zip.getOverlay(idx, out);
//  }
//
  /*
   * Compute the zip file's index.
   *
   * "appName", "locale", and "vendor" should be set to null to indicate the
   * default directory.
   */
    int getIndex(final String zip) {
      final int N = mZipPath.size();
      for (int i = 0; i < N; i++) {
        if (Objects.equals(mZipPath.get(i), zip)) {
          return i;
        }
      }

      mZipPath.add(zip);
      mZipFile.add(null);

      return mZipPath.size() - 1;
    }

  }

  private static long getFileModDate(String path) {
    try {
      return Files.getLastModifiedTime(Paths.get(path)).toMillis();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<AssetPath> getAssetPaths() {
    synchronized (mLock) {
      ArrayList<AssetPath> assetPaths = new ArrayList<>(mAssetPaths.size());
      for (asset_path asset_path : mAssetPaths) {
        FsFile fsFile;
        switch (asset_path.type) {
          case kFileTypeDirectory:
            fsFile = Fs.newFile(asset_path.path.string());
            break;
          case kFileTypeRegular:
            fsFile = Fs.newFile(asset_path.path.string());
            break;
          default:
            throw new IllegalStateException("Unsupported type " + asset_path.type + " for + "
                + asset_path.path.string());
        }
        assetPaths.add(new AssetPath(fsFile, asset_path.isSystemAsset));
      }
      return assetPaths;
    }
  }
}
