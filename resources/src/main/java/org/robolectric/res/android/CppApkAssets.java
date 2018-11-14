package org.robolectric.res.android;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/include/androidfw/ApkAssets.h
// and https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/ApkAssets.cpp

import static org.robolectric.res.android.CppAssetManager.FileType.kFileTypeDirectory;
import static org.robolectric.res.android.CppAssetManager.FileType.kFileTypeRegular;
import static org.robolectric.res.android.Util.CHECK;
import static org.robolectric.res.android.ZipFileRO.OpenArchive;
import static org.robolectric.res.android.ZipFileRO.kCompressDeflated;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import org.robolectric.res.android.Asset.AccessMode;
import org.robolectric.res.android.CppAssetManager.FileType;
import org.robolectric.res.android.Idmap.LoadedIdmap;
import org.robolectric.res.android.ZipFileRO.ZipEntryRO;
import org.robolectric.util.PerfStatsCollector;

//
// #ifndef APKASSETS_H_
// #define APKASSETS_H_
//
// #include <memory>
// #include <string>
//
// #include "android-base/macros.h"
// #include "ziparchive/zip_archive.h"
//
// #include "androidfw/Asset.h"
// #include "androidfw/LoadedArsc.h"
// #include "androidfw/misc.h"
//
// namespace android {
//
// // Holds an APK.
@SuppressWarnings("NewApi")
public class CppApkAssets {
  private static final String kResourcesArsc = "resources.arsc";
//  public:
//   static std::unique_ptr<const ApkAssets> Load(const String& path, bool system = false);
//   static std::unique_ptr<const ApkAssets> LoadAsSharedLibrary(const String& path,
//                                                               bool system = false);
//
//   std::unique_ptr<Asset> Open(const String& path,
//                               Asset::AccessMode mode = Asset::AccessMode::ACCESS_RANDOM) const;
//
//   bool ForEachFile(const String& path,
//                    const std::function<void(const StringPiece&, FileType)>& f) const;


  public CppApkAssets(ZipArchiveHandle zip_handle_, String path_) {
    this.zip_handle_ = zip_handle_;
    this.path_ = path_;
  }

  public String GetPath() { return path_; }

  // This is never nullptr.
  public LoadedArsc GetLoadedArsc() {
    return loaded_arsc_;
  }

  //  private:
//   DISALLOW_COPY_AND_ASSIGN(ApkAssets);
//
//   static std::unique_ptr<const ApkAssets> LoadImpl(const String& path, bool system,
//                                                    bool load_as_shared_library);
//
//   ApkAssets() = default;
//
//   struct ZipArchivePtrCloser {
//     void operator()(::ZipArchiveHandle handle) { ::CloseArchive(handle); }
//   };
//
//   using ZipArchivePtr =
//       std::unique_ptr<typename std::remove_pointer<::ZipArchiveHandle>::type, ZipArchivePtrCloser>;

  ZipArchiveHandle zip_handle_;
  private String path_;
  Asset resources_asset_;
  Asset idmap_asset_;
  private LoadedArsc loaded_arsc_;
  // };
//
// }  // namespace android
//
// #endif /* APKASSETS_H_ */
//
// #define ATRACE_TAG ATRACE_TAG_RESOURCES
//
// #include "androidfw/ApkAssets.h"
//
// #include <algorithm>
//
// #include "android-base/logging.h"
// #include "utils/FileMap.h"
// #include "utils/Trace.h"
// #include "ziparchive/zip_archive.h"
//
// #include "androidfw/Asset.h"
// #include "androidfw/Util.h"
//
// namespace android {
//
// Creates an ApkAssets.
// If `system` is true, the package is marked as a system package, and allows some functions to
// filter out this package when computing what configurations/resources are available.
// std::unique_ptr<const ApkAssets> ApkAssets::Load(const String& path, bool system) {
  public static CppApkAssets Load(String path, boolean system) {
    return LoadImpl(/*{}*/-1 /*fd*/, path, null, null, system, false /*load_as_shared_library*/);
  }

  // Creates an ApkAssets, but forces any package with ID 0x7f to be loaded as a shared library.
  // If `system` is true, the package is marked as a system package, and allows some functions to
  // filter out this package when computing what configurations/resources are available.
// std::unique_ptr<const ApkAssets> ApkAssets::LoadAsSharedLibrary(const String& path,
//                                                                 bool system) {
  public static CppApkAssets LoadAsSharedLibrary(String path,
      boolean system) {
    return LoadImpl(/*{}*/ -1 /*fd*/, path, null, null, system, true /*load_as_shared_library*/);
  }

  // Creates an ApkAssets from an IDMAP, which contains the original APK path, and the overlay
  // data.
  // If `system` is true, the package is marked as a system package, and allows some functions to
  // filter out this package when computing what configurations/resources are available.
// std::unique_ptr<const ApkAssets> ApkAssets::LoadOverlay(const std::string& idmap_path,
//                                                         bool system) {
  public static CppApkAssets LoadOverlay(String idmap_path,
      boolean system) {
    throw new UnsupportedOperationException();
    // Asset idmap_asset = CreateAssetFromFile(idmap_path);
    // if (idmap_asset == null) {
    //   return {};
    // }
    //
    // StringPiece idmap_data(
    //     reinterpret_cast<char*>(idmap_asset.getBuffer(true /*wordAligned*/)),
    //     static_cast<size_t>(idmap_asset.getLength()));
    // LoadedIdmap loaded_idmap = LoadedIdmap.Load(idmap_data);
    // if (loaded_idmap == null) {
    //   System.err.println( + "failed to load IDMAP " + idmap_path;
    //   return {};
    // }
    // return LoadImpl({} /*fd*/, loaded_idmap.OverlayApkPath(), std.move(idmap_asset),
    //     std.move(loaded_idmap), system, false /*load_as_shared_library*/);
  }

  // Creates an ApkAssets from the given file descriptor, and takes ownership of the file
  // descriptor. The `friendly_name` is some name that will be used to identify the source of
  // this ApkAssets in log messages and other debug scenarios.
  // If `system` is true, the package is marked as a system package, and allows some functions to
  // filter out this package when computing what configurations/resources are available.
  // If `force_shared_lib` is true, any package with ID 0x7f is loaded as a shared library.
// std::unique_ptr<const ApkAssets> ApkAssets::LoadFromFd(unique_fd fd,
//                                                        const std::string& friendly_name,
//                                                        bool system, bool force_shared_lib) {
//   public static ApkAssets LoadFromFd(unique_fd fd,
//       String friendly_name,
//       boolean system, boolean force_shared_lib) {
//     return LoadImpl(std.move(fd), friendly_name, null /*idmap_asset*/, null /*loaded_idmap*/,
//         system, force_shared_lib);
//   }

  // std::unique_ptr<Asset> ApkAssets::CreateAssetFromFile(const std::string& path) {
  static Asset CreateAssetFromFile(String path) {
    throw new UnsupportedOperationException();
    // unique_fd fd(base.utf8.open(path.c_str(), O_RDONLY | O_BINARY | O_CLOEXEC));
    // if (fd == -1) {
    //   System.err.println( + "Failed to open file '" + path + "': " + SystemErrorCodeToString(errno);
    //   return {};
    // }
    //
    // long file_len = lseek64(fd, 0, SEEK_END);
    // if (file_len < 0) {
    //   System.err.println( + "Failed to get size of file '" + path + "': " + SystemErrorCodeToString(errno);
    //   return {};
    // }
    //
    // std.unique_ptr<FileMap> file_map = util.make_unique<FileMap>();
    // if (!file_map.create(path.c_str(), fd, 0, static_cast<size_t>(file_len), true /*readOnly*/)) {
    //   System.err.println( + "Failed to mmap file '" + path + "': " + SystemErrorCodeToString(errno);
    //   return {};
    // }
    // return Asset.createFromUncompressedMap(std.move(file_map), Asset.AccessMode.ACCESS_RANDOM);
  }

  /**
   * Measure performance implications of loading {@link CppApkAssets}.
   */
  static CppApkAssets LoadImpl(
      int fd, String path, Asset idmap_asset,
      LoadedIdmap loaded_idmap, boolean system, boolean load_as_shared_library) {
    return PerfStatsCollector.getInstance()
        .measure(
            "load binary " + (system ? "framework" : "app") + " resources",
            () ->
                LoadImpl_measured(
                    fd, path, idmap_asset, loaded_idmap, system, load_as_shared_library));
  }

  // std::unique_ptr<const ApkAssets> ApkAssets::LoadImpl(
  //     unique_fd fd, const std::string& path, std::unique_ptr<Asset> idmap_asset,
  //     std::unique_ptr<const LoadedIdmap> loaded_idmap, bool system, bool load_as_shared_library) {
  static CppApkAssets LoadImpl_measured(
      int fd, String path, Asset idmap_asset,
      LoadedIdmap loaded_idmap, boolean system, boolean load_as_shared_library) {
    Ref<ZipArchiveHandle> unmanaged_handle = new Ref<>(null);
    int result;
    if (fd >= 0) {
      throw new UnsupportedOperationException();
      // result =
      //   OpenArchiveFd(fd.release(), path, &unmanaged_handle, true /*assume_ownership*/);
    } else {
      result = OpenArchive(path, unmanaged_handle);
    }

    if (result != 0) {
      System.err.println("Failed to open APK '" + path + "' " + ErrorCodeString(result));
      return null;
    }

    // Wrap the handle in a unique_ptr so it gets automatically closed.
    CppApkAssets loaded_apk = new CppApkAssets(unmanaged_handle.get(), path);

    // Find the resource table.
    String entry_name = kResourcesArsc;
    Ref<ZipEntry> entry = new Ref<>(null);
    // result = FindEntry(loaded_apk.zip_handle_.get(), entry_name, &entry);
    result = ZipFileRO.FindEntry(loaded_apk.zip_handle_, entry_name, entry);
    if (result != 0) {
      // There is no resources.arsc, so create an empty LoadedArsc and return.
      loaded_apk.loaded_arsc_ = LoadedArsc.CreateEmpty();
      return loaded_apk;
    }

    if (entry.get().getMethod() == kCompressDeflated) {
      System.out.println(kResourcesArsc + " in APK '" + path + "' is compressed.");
    }

    // Open the resource table via mmap unless it is compressed. This logic is taken care of by Open.
    loaded_apk.resources_asset_ = loaded_apk.Open(kResourcesArsc, Asset.AccessMode.ACCESS_BUFFER);
    if (loaded_apk.resources_asset_ == null) {
      System.err.println("Failed to open '" + kResourcesArsc + "' in APK '" + path + "'.");
      return null;
    }

    // Must retain ownership of the IDMAP Asset so that all pointers to its mmapped data remain valid.
    loaded_apk.idmap_asset_ = idmap_asset;

  // const StringPiece data(
  //       reinterpret_cast<const char*>(loaded_apk.resources_asset_.getBuffer(true /*wordAligned*/)),
  //       loaded_apk.resources_asset_.getLength());
    StringPiece data = new StringPiece(
        ByteBuffer.wrap(loaded_apk.resources_asset_.getBuffer(true /*wordAligned*/))
            .order(ByteOrder.LITTLE_ENDIAN),
        0 /*(int) loaded_apk.resources_asset_.getLength()*/);
    loaded_apk.loaded_arsc_ =
        LoadedArsc.Load(data, loaded_idmap, system, load_as_shared_library);
    if (loaded_apk.loaded_arsc_ == null) {
      System.err.println("Failed to load '" + kResourcesArsc + "' in APK '" + path + "'.");
      return null;
    }

    // Need to force a move for mingw32.
    return loaded_apk;
  }

  private static String ErrorCodeString(int result) {
    return "Error " + result;
  }

  public Asset Open(String path, AccessMode mode) {
    CHECK(zip_handle_ != null);

    String name = path;
    ZipFileRO zipFileRO = ZipFileRO.open(zip_handle_.zipFile.getName());
    ZipEntryRO entry;
    entry = zipFileRO.findEntryByName(name);
    // int result = FindEntry(zip_handle_.get(), name, &entry);
    // if (result != 0) {
    //   LOG(ERROR) + "No entry '" + path + "' found in APK '" + path_ + "'";
    //   return {};
    // }
    if (entry == null) {
      return null;
    }

    if (entry.entry.getMethod() == kCompressDeflated) {
      // FileMap map = new FileMap();
      // if (!map.create(path_, .GetFileDescriptor(zip_handle_), entry.offset,
      //     entry.getCompressedSize(), true /*readOnly*/)) {
      //   LOG(ERROR) + "Failed to mmap file '" + path + "' in APK '" + path_ + "'";
      //   return {};
      // }
      FileMap map = zipFileRO.createEntryFileMap(entry);

      Asset asset =
          Asset.createFromCompressedMap(map, (int) entry.entry.getSize(), mode);
      if (asset == null) {
        System.err.println("Failed to decompress '" + path + "'.");
        return null;
      }
      return asset;
    } else {
      FileMap map = zipFileRO.createEntryFileMap(entry);

      // if (!map.create(path_, .GetFileDescriptor(zip_handle_.get()), entry.offset,
      //     entry.uncompressed_length, true /*readOnly*/)) {
      //   System.err.println("Failed to mmap file '" + path + "' in APK '" + path_ + "'");
      //   return null;
      // }

      Asset asset = Asset.createFromUncompressedMap(map, mode);
      if (asset == null) {
        System.err.println("Failed to mmap file '" + path + "' in APK '" + path_ + "'");
        return null;
      }
      return asset;
    }
  }

  interface ForEachFileCallback {
    void callback(String string, FileType fileType);
  }

  boolean ForEachFile(String root_path,
      ForEachFileCallback f) {
    CHECK(zip_handle_ != null);

    String root_path_full = root_path;
    // if (root_path_full.back() != '/') {
    if (!root_path_full.endsWith("/")) {
      root_path_full += '/';
    }

    String prefix = root_path_full;
    Enumeration<? extends ZipEntry> entries = zip_handle_.zipFile.entries();
    // if (StartIteration(zip_handle_.get(), &cookie, &prefix, null) != 0) {
    //   return false;
    // }
    if (!entries.hasMoreElements()) {
      return false;
    }

    // String name;
    // ZipEntry entry;

    // We need to hold back directories because many paths will contain them and we want to only
    // surface one.
    final Set<String> dirs = new HashSet<>();

    // int32_t result;
    // while ((result = Next(cookie, &entry, &name)) == 0) {
    while (entries.hasMoreElements()) {
      ZipEntry zipEntry =  entries.nextElement();
      if (!zipEntry.getName().startsWith(prefix)) {
        continue;
      }

      // StringPiece full_file_path(reinterpret_cast<const char*>(name.name), name.name_length);
      String full_file_path = zipEntry.getName();

      // StringPiece leaf_file_path = full_file_path.substr(root_path_full.size());
      String leaf_file_path = full_file_path.substring(root_path_full.length());

      if (!leaf_file_path.isEmpty()) {
        // auto iter = stdfind(leaf_file_path.begin(), leaf_file_path.end(), '/');

        // if (iter != leaf_file_path.end()) {
        //   stdstring dir =
        //       leaf_file_path.substr(0, stddistance(leaf_file_path.begin(), iter)).to_string();
        //   dirs.insert(stdmove(dir));
        if (zipEntry.isDirectory()) {
          dirs.add(leaf_file_path.substring(0, leaf_file_path.indexOf("/")));
        } else {
          f.callback(leaf_file_path, kFileTypeRegular);
        }
      }
    }
    // EndIteration(cookie);

    // Now present the unique directories.
    for (final String dir : dirs) {
      f.callback(dir, kFileTypeDirectory);
    }

    // -1 is end of iteration, anything else is an error.
    // return result == -1;
    return true;
  }
//
}  // namespace android

