package org.robolectric.res.android;

import static org.robolectric.res.android.ApkAssetsCookie.K_INVALID_COOKIE;
import static org.robolectric.res.android.ApkAssetsCookie.kInvalidCookie;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.ResourceUtils.ExtractResourceName;
import static org.robolectric.res.android.ResourceUtils.fix_package_id;
import static org.robolectric.res.android.ResourceUtils.get_entry_id;
import static org.robolectric.res.android.ResourceUtils.get_package_id;
import static org.robolectric.res.android.ResourceUtils.get_type_id;
import static org.robolectric.res.android.ResourceUtils.is_internal_resid;
import static org.robolectric.res.android.ResourceUtils.is_valid_resid;
import static org.robolectric.res.android.Util.ATRACE_CALL;
import static org.robolectric.res.android.Util.dtohl;
import static org.robolectric.res.android.Util.dtohs;
import static org.robolectric.res.android.Util.isTruthy;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.robolectric.res.Fs;
import org.robolectric.res.android.AssetDir.FileInfo;
import org.robolectric.res.android.CppApkAssets.ForEachFileCallback;
import org.robolectric.res.android.CppAssetManager.FileType;
import org.robolectric.res.android.CppAssetManager2.ResolvedBag.Entry;
import org.robolectric.res.android.Idmap.LoadedIdmap;
import org.robolectric.res.android.LoadedArsc.DynamicPackageEntry;
import org.robolectric.res.android.LoadedArsc.LoadedPackage;
import org.robolectric.res.android.LoadedArsc.TypeSpec;
import org.robolectric.res.android.ResourceTypes.ResTable_entry;
import org.robolectric.res.android.ResourceTypes.ResTable_map;
import org.robolectric.res.android.ResourceTypes.ResTable_map_entry;
import org.robolectric.res.android.ResourceTypes.ResTable_type;
import org.robolectric.res.android.ResourceTypes.Res_value;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/include/androidfw/AssetManager2.h
// and https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/AssetManager2.cpp
@SuppressWarnings("NewApi")
public class CppAssetManager2 {
//  #define ATRACE_TAG ATRACE_TAG_RESOURCES
//  
//  #include "androidfw/AssetManager2.h"

//#include <array>
//#include <limits>
//#include <set>
//#include <unordered_map>
//
//#include "androidfw/ApkAssets.h"
//#include "androidfw/Asset.h"
//#include "androidfw/AssetManager.h"
//#include "androidfw/ResourceTypes.h"
//#include "androidfw/Util.h"
//
//namespace android {
//
//class Theme;
//
//using ApkAssetsCookie = int32_t;
//
//enum : ApkAssetsCookie {
  //};

  // Holds a bag that has been merged with its parent, if one exists.
  public static class ResolvedBag {
    // A single key-value entry in a bag.
    public static class Entry {
      // The key, as described in ResTable_map.name.
      public int key;

      public Res_value value = new Res_value();

      // The resource ID of the origin style associated with the given entry
      public int style;

      // Which ApkAssets this entry came from.
      public ApkAssetsCookie cookie;

      ResStringPool key_pool;
      ResStringPool type_pool;

      public ResolvedBag.Entry copy() {
        Entry entry = new Entry();
        entry.key = key;
        entry.value = value.copy();
        entry.cookie = cookie == null ? null : ApkAssetsCookie.forInt(cookie.intValue());
        entry.key_pool = key_pool;
        entry.type_pool = type_pool;
        return entry;
      }

      @Override
      public String toString() {
        return "Entry{" +
            "key=" + key +
            ", value=" + value +
            '}';
      }
    };

    // Denotes the configuration axis that this bag varies with.
    // If a configuration changes with respect to one of these axis,
    // the bag should be reloaded.
    public int type_spec_flags;

    // The number of entries in this bag. Access them by indexing into `entries`.
    public int entry_count;

    // The array of entries for this bag. An empty array is a neat trick to force alignment
    // of the Entry structs that follow this structure and avoids a bunch of casts.
    public Entry[] entries;
  };

  // AssetManager2 is the main entry point for accessing assets and resources.
  // AssetManager2 provides caching of resources retrieved via the underlying ApkAssets.
//  class AssetManager2 : public .AAssetManager {
//   public:
  public static class ResourceName {
    public String package_ = null;
    // int package_len = 0;

    public String type = null;
    // public String type16 = null;
    // int type_len = 0;

    public String entry = null;
    // public String entry16 = null;
    // int entry_len = 0;
  };

  public CppAssetManager2() {
  }


  public final List<CppApkAssets> GetApkAssets() { return apk_assets_; }

  final ResTable_config GetConfiguration() { return configuration_; }

// private:
//  DISALLOW_COPY_AND_ASSIGN(AssetManager2);

  // The ordered list of ApkAssets to search. These are not owned by the AssetManager, and must
  // have a longer lifetime.
  private List<CppApkAssets> apk_assets_;

  // A collection of configurations and their associated ResTable_type that match the current
  // AssetManager configuration.
  static class FilteredConfigGroup {
    final List<ResTable_config> configurations = new ArrayList<>();
    final List<ResTable_type> types = new ArrayList<>();
  }

  // Represents an single package.
  static class ConfiguredPackage {
    // A pointer to the immutable, loaded package info.
    LoadedPackage loaded_package_;

    // A mutable AssetManager-specific list of configurations that match the AssetManager's
    // current configuration. This is used as an optimization to avoid checking every single
    // candidate configuration when looking up resources.
    ByteBucketArray<FilteredConfigGroup> filtered_configs_;

    public ConfiguredPackage(LoadedPackage package_) {
      this.loaded_package_ = package_;
    }
  }

  // Represents a logical package, which can be made up of many individual packages. Each package
  // in a PackageGroup shares the same package name and package ID.
  static class PackageGroup {
    // The set of packages that make-up this group.
    final List<ConfiguredPackage> packages_ = new ArrayList<>();

    // The cookies associated with each package in the group. They share the same order as
    // packages_.
    final List<ApkAssetsCookie> cookies_ = new ArrayList<>();

    // A library reference table that contains build-package ID to runtime-package ID mappings.
    DynamicRefTable dynamic_ref_table;
  }

  // DynamicRefTables for shared library package resolution.
  // These are ordered according to apk_assets_. The mappings may change depending on what is
  // in apk_assets_, therefore they must be stored in the AssetManager and not in the
  // immutable ApkAssets class.
  final private List<PackageGroup> package_groups_ = new ArrayList<>();

  // An array mapping package ID to index into package_groups. This keeps the lookup fast
  // without taking too much memory.
//  private std.array<byte, std.numeric_limits<byte>.max() + 1> package_ids_;
  final private byte[] package_ids_ = new byte[256];

  // The current configuration set for this AssetManager. When this changes, cached resources
  // may need to be purged.
  private ResTable_config configuration_ = new ResTable_config();

  // Cached set of bags. These are cached because they can inherit keys from parent bags,
  // which involves some calculation.
//  private std.unordered_map<int, util.unique_cptr<ResolvedBag>> cached_bags_;
  final private Map<Integer, ResolvedBag> cached_bags_ = new HashMap<>();
  //  };

  // final ResolvedBag.Entry* begin(final ResolvedBag* bag) { return bag.entries; }
  //
  // final ResolvedBag.Entry* end(final ResolvedBag* bag) {
  //  return bag.entries + bag.entry_count;
  // }
  //
  // }  // namespace android
  //
  // #endif // ANDROIDFW_ASSETMANAGER2_H_

  //
  //  #include <set>
  //
  //  #include "android-base/logging.h"
  //  #include "android-base/stringprintf.h"
  //  #include "utils/ByteOrder.h"
  //  #include "utils/Trace.h"
  //
  //  #ifdef _WIN32
  //  #ifdef ERROR
  //  #undef ERROR
  //  #endif
  //  #endif
  //
  //  #include "androidfw/ResourceUtils.h"
  //
  //  namespace android {
  static class FindEntryResult {
    // A pointer to the resource table entry for this resource.
    // If the size of the entry is > sizeof(ResTable_entry), it can be cast to
    // a ResTable_map_entry and processed as a bag/map.
    ResTable_entry entry;

    // The configuration for which the resulting entry was defined. This is already swapped to host
    // endianness.
    ResTable_config config;

    // The bitmask of configuration axis with which the resource value varies.
    int type_flags;

    // The dynamic package ID map for the package from which this resource came from.
    DynamicRefTable dynamic_ref_table;

    // The string pool reference to the type's name. This uses a different string pool than
    // the global string pool, but this is hidden from the caller.
    StringPoolRef type_string_ref;

    // The string pool reference to the entry's name. This uses a different string pool than
    // the global string pool, but this is hidden from the caller.
    StringPoolRef entry_string_ref;
  }

//  AssetManager2() { memset(&configuration_, 0, sizeof(configuration_)); }

  // Sets/resets the underlying ApkAssets for this AssetManager. The ApkAssets
  // are not owned by the AssetManager, and must have a longer lifetime.
  //
  // Only pass invalidate_caches=false when it is known that the structure
  // change in ApkAssets is due to a safe addition of resources with completely
  // new resource IDs.
//  boolean SetApkAssets(final List<ApkAssets> apk_assets, boolean invalidate_caches = true);
  public boolean SetApkAssets(final List<CppApkAssets> apk_assets, boolean invalidate_caches) {
    apk_assets_ = apk_assets;
    BuildDynamicRefTable();
    RebuildFilterList();
    if (invalidate_caches) {
//      InvalidateCaches(static_cast<int>(-1));
      InvalidateCaches(-1);
    }
    return true;
  }

  // Assigns package IDs to all shared library ApkAssets.
  // Should be called whenever the ApkAssets are changed.
//  void BuildDynamicRefTable();
  void BuildDynamicRefTable() {
    package_groups_.clear();
//    package_ids_.fill(0xff);
    for (int i = 0; i < package_ids_.length; i++) {
      package_ids_[i] = (byte) 0xff;
    }

    // 0x01 is reserved for the android package.
    int next_package_id = 0x02;
    final int apk_assets_count = apk_assets_.size();
    for (int i = 0; i < apk_assets_count; i++) {
      final LoadedArsc loaded_arsc = apk_assets_.get(i).GetLoadedArsc();
//      for (final std.unique_ptr<final LoadedPackage>& package_ :
      for (final LoadedPackage package_ :
          loaded_arsc.GetPackages()) {
        // Get the package ID or assign one if a shared library.
        int package_id;
        if (package_.IsDynamic()) {
          package_id = next_package_id++;
        } else {
          package_id = package_.GetPackageId();
        }

        // Add the mapping for package ID to index if not present.
        byte idx = package_ids_[package_id];
        if (idx == (byte) 0xff) {
          // package_ids_[package_id] = idx = static_cast<byte>(package_groups_.size());
          package_ids_[package_id] = idx = (byte) package_groups_.size();
          // DynamicRefTable& ref_table = package_groups_.back().dynamic_ref_table;
          // ref_table.mAssignedPackageId = package_id;
          // ref_table.mAppAsLib = package->IsDynamic() && package->GetPackageId() == 0x7f;
          DynamicRefTable ref_table = new DynamicRefTable((byte) package_id,
              package_.IsDynamic() && package_.GetPackageId() == 0x7f);
          PackageGroup newPackageGroup = new PackageGroup();
          newPackageGroup.dynamic_ref_table = ref_table;

          package_groups_.add(newPackageGroup);
        }
        PackageGroup package_group = package_groups_.get(idx);

        // Add the package and to the set of packages with the same ID.
        // package_group->packages_.push_back(ConfiguredPackage{package.get(), {}});
        // package_group.cookies_.push_back(static_cast<ApkAssetsCookie>(i));
        package_group.packages_.add(new ConfiguredPackage(package_));
        package_group.cookies_.add(ApkAssetsCookie.forInt(i));

        // Add the package name . build time ID mappings.
        for (final DynamicPackageEntry entry : package_.GetDynamicPackageMap()) {
          // String package_name(entry.package_name.c_str(), entry.package_name.size());
          package_group.dynamic_ref_table.mEntries.put(
              entry.package_name, (byte) entry.package_id);
        }
      }
    }

    // Now assign the runtime IDs so that we have a build-time to runtime ID map.
    for (PackageGroup iter : package_groups_) {
      String package_name = iter.packages_.get(0).loaded_package_.GetPackageName();
      for (PackageGroup iter2 : package_groups_) {
        iter2.dynamic_ref_table.addMapping(package_name,
            iter.dynamic_ref_table.mAssignedPackageId);
      }
    }
  }

// void AssetManager2::DumpToLog() const {
//   base::ScopedLogSeverity _log(base::INFO);
//
//   LOG(INFO) << base::StringPrintf("AssetManager2(this=%p)", this);
//
//   std::string list;
//   for (const auto& apk_assets : apk_assets_) {
//     base::StringAppendF(&list, "%s,", apk_assets->GetPath().c_str());
//   }
//   LOG(INFO) << "ApkAssets: " << list;
//
//   list = "";
//   for (size_t i = 0; i < package_ids_.size(); i++) {
//     if (package_ids_[i] != 0xff) {
//       base::StringAppendF(&list, "%02x -> %d, ", (int)i, package_ids_[i]);
//     }
//   }
//   LOG(INFO) << "Package ID map: " << list;
//
//   for (const auto& package_group: package_groups_) {
//     list = "";
//     for (const auto& package : package_group.packages_) {
//       const LoadedPackage* loaded_package = package.loaded_package_;
//       base::StringAppendF(&list, "%s(%02x%s), ", loaded_package->GetPackageName().c_str(),
//                           loaded_package->GetPackageId(),
//                           (loaded_package->IsDynamic() ? " dynamic" : ""));
//     }
//     LOG(INFO) << base::StringPrintf("PG (%02x): ",
//                                     package_group.dynamic_ref_table.mAssignedPackageId)
//               << list;
//   }
// }

  // Returns the string pool for the given asset cookie.
  // Use the string pool returned here with a valid Res_value object of type Res_value.TYPE_STRING.
//  final ResStringPool GetStringPoolForCookie(ApkAssetsCookie cookie) const;
  final ResStringPool GetStringPoolForCookie(ApkAssetsCookie cookie) {
    if (cookie.intValue() < 0 || cookie.intValue() >= apk_assets_.size()) {
      return null;
    }
    return apk_assets_.get(cookie.intValue()).GetLoadedArsc().GetStringPool();
  }

  // Returns the DynamicRefTable for the given package ID.
  // This may be nullptr if the APK represented by `cookie` has no resource table.
//  final DynamicRefTable GetDynamicRefTableForPackage(int package_id) const;
  final DynamicRefTable GetDynamicRefTableForPackage(int package_id) {
    if (package_id >= package_ids_.length) {
      return null;
    }

    final int idx = package_ids_[package_id];
    if (idx == 0xff) {
      return null;
    }
    return package_groups_.get(idx).dynamic_ref_table;
  }

  // Returns the DynamicRefTable for the ApkAssets represented by the cookie.
//  final DynamicRefTable GetDynamicRefTableForCookie(ApkAssetsCookie cookie) const;
  public final DynamicRefTable GetDynamicRefTableForCookie(ApkAssetsCookie cookie) {
    for (final PackageGroup package_group : package_groups_) {
      for (final ApkAssetsCookie package_cookie : package_group.cookies_) {
        if (package_cookie == cookie) {
          return package_group.dynamic_ref_table;
        }
      }
    }
    return null;
  }

  // Sets/resets the configuration for this AssetManager. This will cause all
  // caches that are related to the configuration change to be invalidated.
//  void SetConfiguration(final ResTable_config& configuration);
  public void SetConfiguration(final ResTable_config configuration) {
    final int diff = configuration_.diff(configuration);
    configuration_ = configuration;

    if (isTruthy(diff)) {
      RebuildFilterList();
//      InvalidateCaches(static_cast<int>(diff));
      InvalidateCaches(diff);
    }
  }

  // Returns all configurations for which there are resources defined. This includes resource
  // configurations in all the ApkAssets set for this AssetManager.
  // If `exclude_system` is set to true, resource configurations from system APKs
  // ('android' package, other libraries) will be excluded from the list.
  // If `exclude_mipmap` is set to true, resource configurations defined for resource type 'mipmap'
  // will be excluded from the list.
//  Set<ResTable_config> GetResourceConfigurations(boolean exclude_system = false,
//                                                 boolean exclude_mipmap = false);
  public Set<ResTable_config> GetResourceConfigurations(boolean exclude_system,
      boolean exclude_mipmap) {
    // ATRACE_NAME("AssetManager::GetResourceConfigurations");
    Set<ResTable_config> configurations = new HashSet<>();
    for (final PackageGroup package_group : package_groups_) {
      for (final ConfiguredPackage package_ : package_group.packages_) {
        if (exclude_system && package_.loaded_package_.IsSystem()) {
          continue;
        }
        package_.loaded_package_.CollectConfigurations(exclude_mipmap, configurations);
      }
    }
    return configurations;
  }

  // Returns all the locales for which there are resources defined. This includes resource
  // locales in all the ApkAssets set for this AssetManager.
  // If `exclude_system` is set to true, resource locales from system APKs
  // ('android' package, other libraries) will be excluded from the list.
  // If `merge_equivalent_languages` is set to true, resource locales will be canonicalized
  // and de-duped in the resulting list.
//  Set<String> GetResourceLocales(boolean exclude_system = false,
//                                 boolean merge_equivalent_languages = false);
  public Set<String> GetResourceLocales(boolean exclude_system,
      boolean merge_equivalent_languages) {
    ATRACE_CALL();
    Set<String> locales = new HashSet<>();
    for (final PackageGroup package_group : package_groups_) {
      for (final ConfiguredPackage package_ : package_group.packages_) {
        if (exclude_system && package_.loaded_package_.IsSystem()) {
          continue;
        }
        package_.loaded_package_.CollectLocales(merge_equivalent_languages, locales);
      }
    }
    return locales;
  }

  // Searches the set of APKs loaded by this AssetManager and opens the first one found located
  // in the assets/ directory.
  // `mode` controls how the file is opened.
  //
  // NOTE: The loaded APKs are searched in reverse order.
//  Asset Open(final String filename, Asset.AccessMode mode);
  public Asset Open(final String filename, Asset.AccessMode mode) {
    final String new_path = "assets/" + filename;
    return OpenNonAsset(new_path, mode);
  }

  // Opens a file within the assets/ directory of the APK specified by `cookie`.
  // `mode` controls how the file is opened.
//  Asset Open(final String filename, ApkAssetsCookie cookie,
//             Asset.AccessMode mode);
  Asset Open(final String filename, ApkAssetsCookie cookie,
      Asset.AccessMode mode) {
    final String new_path = "assets/" + filename;
    return OpenNonAsset(new_path, cookie, mode);
  }

  // Opens the directory specified by `dirname`. The result is an AssetDir that is the combination
  // of all directories matching `dirname` under the assets/ directory of every ApkAssets loaded.
  // The entries are sorted by their ASCII name.
//  AssetDir OpenDir(final String dirname);
  public AssetDir OpenDir(final String dirname) {
    ATRACE_CALL();

    String full_path = "assets/" + dirname;
    // std.unique_ptr<SortedVector<AssetDir.FileInfo>> files =
    //     util.make_unique<SortedVector<AssetDir.FileInfo>>();
    SortedVector<FileInfo> files = new SortedVector<>();

    // Start from the back.
    for (CppApkAssets apk_assets : apk_assets_) {
      // auto func = [&](final String& name, FileType type) {
      ForEachFileCallback func = (final String name, FileType type) -> {
        AssetDir.FileInfo info = new FileInfo();
        info.setFileName(new String8(name));
        info.setFileType(type);
        info.setSourceName(new String8(apk_assets.GetPath()));
        files.add(info);
      };

      if (!apk_assets.ForEachFile(full_path, func)) {
        return new AssetDir();
      }
    }

    // std.unique_ptr<AssetDir> asset_dir = util.make_unique<AssetDir>();
    AssetDir asset_dir = new AssetDir();
    asset_dir.setFileList(files);
    return asset_dir;
  }

  // Searches the set of APKs loaded by this AssetManager and opens the first one found.
  // `mode` controls how the file is opened.
  // `out_cookie` is populated with the cookie of the APK this file was found in.
  //
  // NOTE: The loaded APKs are searched in reverse order.
//  Asset OpenNonAsset(final String filename, Asset.AccessMode mode,
//                     ApkAssetsCookie* out_cookie = null);
  // Search in reverse because that's how we used to do it and we need to preserve behaviour.
  // This is unfortunate, because ClassLoaders delegate to the parent first, so the order
  // is inconsistent for split APKs.
  public Asset OpenNonAsset(final String filename,
      Asset.AccessMode mode,
      Ref<ApkAssetsCookie> out_cookie) {
    ATRACE_CALL();
    for (int i = apk_assets_.size() - 1; i >= 0; i--) {
      Asset asset = apk_assets_.get(i).Open(filename, mode);
      if (isTruthy(asset)) {
        if (out_cookie != null) {
          out_cookie.set(ApkAssetsCookie.forInt(i));
        }
        return asset;
      }
    }

    if (out_cookie != null) {
      out_cookie.set(K_INVALID_COOKIE);
    }
    return null;
  }

  public Asset OpenNonAsset(final String filename, Asset.AccessMode mode) {
    return OpenNonAsset(filename, mode, null);
  }

  // Opens a file in the APK specified by `cookie`. `mode` controls how the file is opened.
  // This is typically used to open a specific AndroidManifest.xml, or a binary XML file
  // referenced by a resource lookup with GetResource().
//  Asset OpenNonAsset(final String filename, ApkAssetsCookie cookie,
//                     Asset.AccessMode mode);
  public Asset OpenNonAsset(final String filename,
      ApkAssetsCookie cookie, Asset.AccessMode mode) {
    ATRACE_CALL();
    if (cookie.intValue() < 0 || cookie.intValue() >= apk_assets_.size()) {
      return null;
    }
    return apk_assets_.get(cookie.intValue()).Open(filename, mode);
  }

  // template <typename Func>
  public interface PackageFunc {
    void apply(String package_name, byte package_id);
  }

  public void ForEachPackage(PackageFunc func) {
    for (PackageGroup package_group : package_groups_) {
      func.apply(package_group.packages_.get(0).loaded_package_.GetPackageName(),
          package_group.dynamic_ref_table.mAssignedPackageId);
    }
  }

  // Finds the best entry for `resid` from the set of ApkAssets. The entry can be a simple
  // Res_value, or a complex map/bag type. If successful, it is available in `out_entry`.
  // Returns kInvalidCookie on failure. Otherwise, the return value is the cookie associated with
  // the ApkAssets in which the entry was found.
  //
  // `density_override` overrides the density of the current configuration when doing a search.
  //
  // When `stop_at_first_match` is true, the first match found is selected and the search
  // terminates. This is useful for methods that just look up the name of a resource and don't
  // care about the value. In this case, the value of `FindEntryResult::type_flags` is incomplete
  // and should not be used.
  //
  // NOTE: FindEntry takes care of ensuring that structs within FindEntryResult have been properly
  // bounds-checked. Callers of FindEntry are free to trust the data if this method succeeds.
//  ApkAssetsCookie FindEntry(int resid, short density_override, boolean stop_at_first_match,
//                            LoadedArscEntry* out_entry, ResTable_config out_selected_config,
//                            int* out_flags);
  private ApkAssetsCookie FindEntry(int resid, short density_override,
      final Ref<FindEntryResult> out_entry) {
    ATRACE_CALL();

    // Might use this if density_override != 0.
    ResTable_config density_override_config;

    // Select our configuration or generate a density override configuration.
    ResTable_config desired_config = configuration_;
    if (density_override != 0 && density_override != configuration_.density) {
      density_override_config = configuration_;
      density_override_config.density = density_override;
      desired_config = density_override_config;
    }

    if (!is_valid_resid(resid)) {
      System.err.println(String.format("Invalid ID 0x%08x.", resid));
      return K_INVALID_COOKIE;
    }

    final int package_id = get_package_id(resid);
    final int type_idx = (byte) (get_type_id(resid) - 1);
    final int entry_idx = get_entry_id(resid);

    final byte package_idx = package_ids_[package_id];
    if (package_idx == (byte) 0xff) {
      System.err.println(
          String.format("No package ID %02x found for ID 0x%08x.", package_id, resid));
      return K_INVALID_COOKIE;
    }

    final PackageGroup package_group = package_groups_.get(package_idx);
    final int package_count = package_group.packages_.size();

    ApkAssetsCookie best_cookie = K_INVALID_COOKIE;
    LoadedPackage best_package = null;
    ResTable_type best_type = null;
    ResTable_config best_config = null;
    ResTable_config best_config_copy;
    int best_offset = 0;
    int type_flags = 0;

    // If desired_config is the same as the set configuration, then we can use our filtered list
    // and we don't need to match the configurations, since they already matched.
    boolean use_fast_path = desired_config == configuration_;

    for (int pi = 0; pi < package_count; pi++) {
      ConfiguredPackage loaded_package_impl = package_group.packages_.get(pi);
      LoadedPackage loaded_package = loaded_package_impl.loaded_package_;
      ApkAssetsCookie cookie = package_group.cookies_.get(pi);

      // If the type IDs are offset in this package, we need to take that into account when searching
      // for a type.
      TypeSpec type_spec = loaded_package.GetTypeSpecByTypeIndex(type_idx);
      if (Util.UNLIKELY(type_spec == null)) {
        continue;
      }

      int local_entry_idx = entry_idx;

      // If there is an IDMAP supplied with this package, translate the entry ID.
      if (type_spec.idmap_entries != null) {
        if (!LoadedIdmap
            .Lookup(type_spec.idmap_entries, local_entry_idx, new Ref<>(local_entry_idx))) {
          // There is no mapping, so the resource is not meant to be in this overlay package.
          continue;
        }
      }

      type_flags |= type_spec.GetFlagsForEntryIndex(local_entry_idx);

      // If the package is an overlay, then even configurations that are the same MUST be chosen.
      boolean package_is_overlay = loaded_package.IsOverlay();

      FilteredConfigGroup filtered_group = loaded_package_impl.filtered_configs_.get(type_idx);
      if (use_fast_path) {
        List<ResTable_config> candidate_configs = filtered_group.configurations;
        int type_count = candidate_configs.size();
        for (int i = 0; i < type_count; i++) {
          ResTable_config this_config = candidate_configs.get(i);

          // We can skip calling ResTable_config.match() because we know that all candidate
          // configurations that do NOT match have been filtered-out.
          if ((best_config == null || this_config.isBetterThan(best_config, desired_config)) ||
              (package_is_overlay && this_config.compare(best_config) == 0)) {
            // The configuration matches and is better than the previous selection.
            // Find the entry value if it exists for this configuration.
            ResTable_type type_chunk = filtered_group.types.get(i);
            int offset = LoadedPackage.GetEntryOffset(type_chunk, local_entry_idx);
            if (offset == ResTable_type.NO_ENTRY) {
              continue;
            }

            best_cookie = cookie;
            best_package = loaded_package;
            best_type = type_chunk;
            best_config = this_config;
            best_offset = offset;
          }
        }
      } else {
        // This is the slower path, which doesn't use the filtered list of configurations.
        // Here we must read the ResTable_config from the mmapped APK, convert it to host endianness
        // and fill in any new fields that did not exist when the APK was compiled.
        // Furthermore when selecting configurations we can't just record the pointer to the
        // ResTable_config, we must copy it.
        // auto iter_end = type_spec.types + type_spec.type_count;
        //   for (auto iter = type_spec.types; iter != iter_end; ++iter) {
        for (ResTable_type type : type_spec.types) {
          ResTable_config this_config = ResTable_config.fromDtoH(type.config);

          if (this_config.match(desired_config)) {
            if ((best_config == null || this_config.isBetterThan(best_config, desired_config)) ||
                (package_is_overlay && this_config.compare(best_config) == 0)) {
              // The configuration matches and is better than the previous selection.
              // Find the entry value if it exists for this configuration.
              int offset = LoadedPackage.GetEntryOffset(type, local_entry_idx);
              if (offset == ResTable_type.NO_ENTRY) {
                continue;
              }

              best_cookie = cookie;
              best_package = loaded_package;
              best_type = type;
              best_config_copy = this_config;
              best_config = best_config_copy;
              best_offset = offset;
            }
          }
        }
      }
    }

    if (Util.UNLIKELY(best_cookie.intValue() == kInvalidCookie)) {
      return K_INVALID_COOKIE;
    }

    ResTable_entry best_entry = LoadedPackage.GetEntryFromOffset(best_type, best_offset);
    if (Util.UNLIKELY(best_entry == null)) {
      return K_INVALID_COOKIE;
    }

    FindEntryResult out_entry_ = new FindEntryResult();
    out_entry_.entry = best_entry;
    out_entry_.config = best_config;
    out_entry_.type_flags = type_flags;
    out_entry_.type_string_ref = new StringPoolRef(best_package.GetTypeStringPool(), best_type.id - 1);
    out_entry_.entry_string_ref =
        new StringPoolRef(best_package.GetKeyStringPool(), best_entry.key.index);
    out_entry_.dynamic_ref_table = package_group.dynamic_ref_table;
    out_entry.set(out_entry_);
    return best_cookie;
  }

  // Populates the `out_name` parameter with resource name information.
  // Utf8 strings are preferred, and only if they are unavailable are
  // the Utf16 variants populated.
  // Returns false if the resource was not found or the name was missing/corrupt.
//  boolean GetResourceName(int resid, ResourceName* out_name);
  public boolean GetResourceName(int resid, ResourceName out_name) {
    final Ref<FindEntryResult> entryRef = new Ref<>(null);
    ApkAssetsCookie cookie = FindEntry(resid, (short) 0 /* density_override */, entryRef);
    if (cookie.intValue() == kInvalidCookie) {
      return false;
    }

    final LoadedPackage package_ =
        apk_assets_.get(cookie.intValue()).GetLoadedArsc().GetPackageById(get_package_id(resid));
    if (package_ == null) {
      return false;
    }

    out_name.package_ = package_.GetPackageName();
    // out_name.package_len = out_name.package_.length();

    FindEntryResult entry = entryRef.get();
    out_name.type = entry.type_string_ref.string();
    // out_name.type_len = out_name.type == null ? 0 : out_name.type.length();
    // out_name.type16 = null;
    if (out_name.type == null) {
      // out_name.type16 = entry.type_string_ref.string();
      // out_name.type_len = out_name.type16 == null ? 0 : out_name.type16.length();
      // if (out_name.type16 == null) {
        return false;
      // }
    }

    out_name.entry = entry.entry_string_ref.string();
    // out_name.entry_len = out_name.entry == null ? 0 : out_name.entry.length();
    // out_name.entry16 = null;
    if (out_name.entry == null) {
      // out_name.entry16 = entry.entry_string_ref.string();
      // out_name.entry_len = out_name.entry16 == null ? 0 : out_name.entry16.length();
      // if (out_name.entry16 == null) {
        return false;
      // }
    }
    return true;
  }

  // Populates `out_flags` with the bitmask of configuration axis that this resource varies with.
  // See ResTable_config for the list of configuration axis.
  // Returns false if the resource was not found.
//  boolean GetResourceFlags(int resid, int* out_flags);
  boolean GetResourceFlags(int resid, Ref<Integer> out_flags) {
    final Ref<FindEntryResult> entry = new Ref<>(null);
    ApkAssetsCookie cookie = FindEntry(resid, (short) 0 /* density_override */, entry);
    if (cookie.intValue() != kInvalidCookie) {
      out_flags.set(entry.get().type_flags);
      // this makes no sense, not a boolean:
      // return cookie;
    }
    // this makes no sense, not a boolean:
    // return kInvalidCookie;

    return cookie.intValue() != kInvalidCookie;
  }


  // Retrieves the best matching resource with ID `resid`. The resource value is filled into
  // `out_value` and the configuration for the selected value is populated in `out_selected_config`.
  // `out_flags` holds the same flags as retrieved with GetResourceFlags().
  // If `density_override` is non-zero, the configuration to match against is overridden with that
  // density.
  //
  // Returns a valid cookie if the resource was found. If the resource was not found, or if the
  // resource was a map/bag type, then kInvalidCookie is returned. If `may_be_bag` is false,
  // this function logs if the resource was a map/bag type before returning kInvalidCookie.
//  ApkAssetsCookie GetResource(int resid, boolean may_be_bag, short density_override,
//                              Res_value out_value, ResTable_config out_selected_config,
//                              int* out_flags);
  public ApkAssetsCookie GetResource(int resid, boolean may_be_bag,
      short density_override, Ref<Res_value> out_value,
      final Ref<ResTable_config> out_selected_config,
      final Ref<Integer> out_flags) {
    final Ref<FindEntryResult> entry = new Ref<>(null);
    ApkAssetsCookie cookie = FindEntry(resid, density_override, entry);
    if (cookie.intValue() == kInvalidCookie) {
      return K_INVALID_COOKIE;
    }

    if (isTruthy(dtohl(entry.get().entry.flags) & ResTable_entry.FLAG_COMPLEX)) {
      if (!may_be_bag) {
        System.err.println(String.format("Resource %08x is a complex map type.", resid));
        return K_INVALID_COOKIE;
      }

      // Create a reference since we can't represent this complex type as a Res_value.
      out_value.set(new Res_value((byte) Res_value.TYPE_REFERENCE, resid));
      out_selected_config.set(new ResTable_config(entry.get().config));
      out_flags.set(entry.get().type_flags);
      return cookie;
    }

    // final Res_value device_value = reinterpret_cast<final Res_value>(
    //     reinterpret_cast<final byte*>(entry.entry) + dtohs(entry.entry.size));
    // out_value.copyFrom_dtoh(*device_value);
    Res_value device_value = entry.get().entry.getResValue();
    out_value.set(device_value.copy());

    // Convert the package ID to the runtime assigned package ID.
    entry.get().dynamic_ref_table.lookupResourceValue(out_value);

    out_selected_config.set(new ResTable_config(entry.get().config));
    out_flags.set(entry.get().type_flags);
    return cookie;
  }

  // Resolves the resource reference in `in_out_value` if the data type is
  // Res_value::TYPE_REFERENCE.
  // `cookie` is the ApkAssetsCookie of the reference in `in_out_value`.
  // `in_out_value` is the reference to resolve. The result is placed back into this object.
  // `in_out_flags` is the type spec flags returned from calls to GetResource() or
  // GetResourceFlags(). Configuration flags of the values pointed to by the reference
  // are OR'd together with `in_out_flags`.
  // `in_out_config` is populated with the configuration for which the resolved value was defined.
  // `out_last_reference` is populated with the last reference ID before resolving to an actual
  // value. This is only initialized if the passed in `in_out_value` is a reference.
  // Returns the cookie of the APK the resolved resource was defined in, or kInvalidCookie if
  // it was not found.
//  ApkAssetsCookie ResolveReference(ApkAssetsCookie cookie, Res_value in_out_value,
//                                   ResTable_config in_out_selected_config, int* in_out_flags,
//                                   int* out_last_reference);
  public ApkAssetsCookie ResolveReference(ApkAssetsCookie cookie, Ref<Res_value> in_out_value,
      final Ref<ResTable_config> in_out_selected_config,
      final Ref<Integer> in_out_flags,
      final Ref<Integer> out_last_reference) {
    final int kMaxIterations = 20;

    for (int iteration = 0; in_out_value.get().dataType == Res_value.TYPE_REFERENCE &&
        in_out_value.get().data != 0 && iteration < kMaxIterations;
        iteration++) {
      out_last_reference.set(in_out_value.get().data);
      final Ref<Integer> new_flags = new Ref<>(0);
      cookie = GetResource(in_out_value.get().data, true /*may_be_bag*/, (short) 0 /*density_override*/,
          in_out_value, in_out_selected_config, new_flags);
      if (cookie.intValue() == kInvalidCookie) {
        return K_INVALID_COOKIE;
      }
      if (in_out_flags != null) {
        in_out_flags.set(in_out_flags.get() | new_flags.get());
      }
      if (out_last_reference.get() == in_out_value.get().data) {
        // This reference can't be resolved, so exit now and let the caller deal with it.
        return cookie;
      }
    }
    return cookie;
  }

  // AssetManager2::GetBag(resid) wraps this function to track which resource ids have already
  // been seen while traversing bag parents.
  //  final ResolvedBag* GetBag(int resid);
  public final ResolvedBag GetBag(int resid) {
    List<Integer> found_resids = new ArrayList<>();
    return GetBag(resid, found_resids);
  }

  // Retrieves the best matching bag/map resource with ID `resid`.
  // This method will resolve all parent references for this bag and merge keys with the child.
  // To iterate over the keys, use the following idiom:
  //
  //  final ResolvedBag* bag = asset_manager.GetBag(id);
  //  if (bag != null) {
  //    for (auto iter = begin(bag); iter != end(bag); ++iter) {
  //      ...
  //    }
  //  }
  ResolvedBag GetBag(int resid, List<Integer> child_resids) {
    // ATRACE_NAME("AssetManager::GetBag");

    ResolvedBag cached_iter = cached_bags_.get(resid);
    if (cached_iter != null) {
      return cached_iter;
    }

    final Ref<FindEntryResult> entryRef = new Ref<>(null);
    ApkAssetsCookie cookie = FindEntry(resid, (short) 0 /* density_override */, entryRef);
    if (cookie.intValue() == kInvalidCookie) {
      return null;
    }

    FindEntryResult entry = entryRef.get();

    // Check that the size of the entry header is at least as big as
    // the desired ResTable_map_entry. Also verify that the entry
    // was intended to be a map.
    if (dtohs(entry.entry.size) < ResTable_map_entry.BASE_SIZEOF ||
        (dtohs(entry.entry.flags) & ResourceTypes.ResTable_entry.FLAG_COMPLEX) == 0) {
      // Not a bag, nothing to do.
      return null;
    }

    // final ResTable_map_entry map = reinterpret_cast<final ResTable_map_entry*>(entry.entry);
    // final ResTable_map map_entry =
    //     reinterpret_cast<final ResTable_map*>(reinterpret_cast<final byte*>(map) + map.size);
    // final ResTable_map map_entry_end = map_entry + dtohl(map.count);
    final ResTable_map_entry map = new ResTable_map_entry(entry.entry.myBuf(), entry.entry.myOffset());
    int curOffset = map.myOffset() + map.size;
    ResTable_map map_entry = null; // = new ResTable_map(map.myBuf(), curOffset);
    final int map_entry_end =
        curOffset + dtohl(map.count) * ResTable_map.SIZEOF;
    if (curOffset < map_entry_end) {
      map_entry = new ResTable_map(map.myBuf(), curOffset);
    }

    // Keep track of ids that have already been seen to prevent infinite loops caused by circular
    // dependencies between bags
    child_resids.add(resid);

    final Ref<Integer> parent_resid = new Ref<>(dtohl(map.parent.ident));
    if (parent_resid.get() == 0 || child_resids.contains(parent_resid.get())) {
      // There is no parent or that a circular dependency exist, meaning there is nothing to
      // inherit and we can do a simple copy of the entries in the map.
      final int entry_count = (map_entry_end - curOffset) / ResTable_map.SIZEOF;
      // util.unique_cptr<ResolvedBag> new_bag{reinterpret_cast<ResolvedBag*>(
      //     malloc(sizeof(ResolvedBag) + (entry_count * sizeof(ResolvedBag.Entry))))};
      ResolvedBag new_bag = new ResolvedBag();
      ResolvedBag.Entry[] new_entry = new_bag.entries = new Entry[entry_count];
      int i = 0;
      while (curOffset < map_entry_end) {
        map_entry = new ResTable_map(map_entry.myBuf(), curOffset);
        final Ref<Integer> new_key = new Ref<>(dtohl(map_entry.name.ident));
        if (!is_internal_resid(new_key.get())) {
          // Attributes, arrays, etc don't have a resource id as the name. They specify
          // other data, which would be wrong to change via a lookup.
          if (entry.dynamic_ref_table.lookupResourceId(new_key) != NO_ERROR) {
            System.err.println(
                String.format("Failed to resolve key 0x%08x in bag 0x%08x.", new_key.get(), resid));
            return null;
          }
        }
        Entry new_entry_ = new_entry[i] = new Entry();
        new_entry_.cookie = cookie;
        new_entry_.key = new_key.get();
        new_entry_.key_pool = null;
        new_entry_.type_pool = null;
        new_entry_.style = resid;
        new_entry_.value = map_entry.value.copy();
        final Ref<Res_value> valueRef = new Ref<>(new_entry_.value);
        int err = entry.dynamic_ref_table.lookupResourceValue(valueRef);
        new_entry_.value = valueRef.get();
        if (err != NO_ERROR) {
          System.err.println(
              String.format(
                  "Failed to resolve value t=0x%02x d=0x%08x for key 0x%08x.",
                  new_entry_.value.dataType, new_entry_.value.data, new_key.get()));
          return null;
        }
        // ++new_entry;
        ++i;

        final int size = dtohs(map_entry.value.size);
//      curOffset += size + sizeof(*map)-sizeof(map->value);
        curOffset += size + ResTable_map.SIZEOF-Res_value.SIZEOF;

      }
      new_bag.type_spec_flags = entry.type_flags;
      new_bag.entry_count = entry_count;
      ResolvedBag result = new_bag;
      cached_bags_.put(resid, new_bag);
      return result;
    }

    // In case the parent is a dynamic reference, resolve it.
    entry.dynamic_ref_table.lookupResourceId(parent_resid);

    // Get the parent and do a merge of the keys.
    final ResolvedBag parent_bag = GetBag(parent_resid.get(), child_resids);
    if (parent_bag == null) {
      // Failed to get the parent that should exist.
      System.err.println(
          String.format("Failed to find parent 0x%08x of bag 0x%08x.", parent_resid.get(), resid));
      return null;
    }

    // Create the max possible entries we can make. Once we construct the bag,
    // we will realloc to fit to size.
    final int max_count = parent_bag.entry_count + dtohl(map.count);
    // util::unique_cptr<ResolvedBag> new_bag{reinterpret_cast<ResolvedBag*>(
    //     malloc(sizeof(ResolvedBag) + (max_count * sizeof(ResolvedBag::Entry))))};
    ResolvedBag new_bag = new ResolvedBag();
    new_bag.entries = new Entry[max_count];
    final ResolvedBag.Entry[] new_entry = new_bag.entries;
    int newEntryIndex = 0;

  // const ResolvedBag::Entry* parent_entry = parent_bag->entries;
    int parentEntryIndex = 0;
    // final ResolvedBag.Entry parent_entry_end = parent_entry + parent_bag.entry_count;
    final int parentEntryCount = parent_bag.entry_count;

    // The keys are expected to be in sorted order. Merge the two bags.
    while (map_entry != null
        && curOffset != map_entry_end
        && parentEntryIndex != parentEntryCount) {
      map_entry = new ResTable_map(map_entry.myBuf(), curOffset);
      final Ref<Integer> child_keyRef = new Ref<>(dtohl(map_entry.name.ident));
      if (!is_internal_resid(child_keyRef.get())) {
        if (entry.dynamic_ref_table.lookupResourceId(child_keyRef) != NO_ERROR) {
          System.err.println(
              String.format(
                  "Failed to resolve key 0x%08x in bag 0x%08x.", child_keyRef.get(), resid));
          return null;
        }
      }
      int child_key = child_keyRef.get();

      Entry parent_entry = parent_bag.entries[parentEntryIndex];
      if (parent_entry == null) {
        parent_entry = new Entry();
      }

      if (child_key <= parent_entry.key) {
        // Use the child key if it comes before the parent
        // or is equal to the parent (overrides).
        Entry new_entry_ = new_entry[newEntryIndex] = new Entry();
        new_entry_.cookie = cookie;
        new_entry_.key = child_key;
        new_entry_.key_pool = null;
        new_entry_.type_pool = null;
        new_entry_.value = map_entry.value.copy();
        new_entry_.style = resid;
        final Ref<Res_value> valueRef = new Ref<>(new_entry_.value);
        int err = entry.dynamic_ref_table.lookupResourceValue(valueRef);
        new_entry_.value = valueRef.get();
        if (err != NO_ERROR) {
          System.err.println(
              String.format(
                  "Failed to resolve value t=0x%02x d=0x%08x for key 0x%08x.",
                  new_entry_.value.dataType, new_entry_.value.data, child_key));
          return null;
        }

        // ++map_entry;
        curOffset += map_entry.value.size + ResTable_map.SIZEOF - Res_value.SIZEOF;
      } else {
        // Take the parent entry as-is.
        // memcpy(new_entry, parent_entry, sizeof(*new_entry));
        new_entry[newEntryIndex] = parent_entry.copy();
      }

      if (child_key >= parent_entry.key) {
        // Move to the next parent entry if we used it or it was overridden.
        // ++parent_entry;
        ++parentEntryIndex;
        // parent_entry = parent_bag.entries[parentEntryIndex];
      }
      // Increment to the next entry to fill.
      // ++new_entry;
      ++newEntryIndex;
    }

    // Finish the child entries if they exist.
    while (map_entry != null && curOffset != map_entry_end) {
      map_entry = new ResTable_map(map_entry.myBuf(), curOffset);
      final Ref<Integer> new_key = new Ref<>(map_entry.name.ident);
      if (!is_internal_resid(new_key.get())) {
        if (entry.dynamic_ref_table.lookupResourceId(new_key) != NO_ERROR) {
          System.err.println(
              String.format("Failed to resolve key 0x%08x in bag 0x%08x.", new_key.get(), resid));
          return null;
        }
      }
      Entry new_entry_ = new_entry[newEntryIndex] = new Entry();
      new_entry_.cookie = cookie;
      new_entry_.key = new_key.get();
      new_entry_.key_pool = null;
      new_entry_.type_pool = null;
      new_entry_.value = map_entry.value.copy();
      new_entry_.style = resid;
      final Ref<Res_value> valueRef = new Ref<>(new_entry_.value);
      int err = entry.dynamic_ref_table.lookupResourceValue(valueRef);
      new_entry_.value = valueRef.get();
      if (err != NO_ERROR) {
        System.err.println(String.format(
            "Failed to resolve value t=0x%02x d=0x%08x for key 0x%08x.",
            new_entry_.value.dataType,
            new_entry_.value.data, new_key.get()));
        return null;
      }
      // ++map_entry;
      curOffset += map_entry.value.size + ResTable_map.SIZEOF - Res_value.SIZEOF;
      // ++new_entry;
      ++newEntryIndex;
    }

    // Finish the parent entries if they exist.
    while (parentEntryIndex != parent_bag.entry_count) {
      // Take the rest of the parent entries as-is.
      // final int num_entries_to_copy = parent_entry_end - parent_entry;
      // final int num_entries_to_copy = parent_bag.entry_count - parentEntryIndex;
      // memcpy(new_entry, parent_entry, num_entries_to_copy * sizeof(*new_entry));
      Entry parentEntry = parent_bag.entries[parentEntryIndex];
      new_entry[newEntryIndex] = parentEntry == null ? new Entry() : parentEntry.copy();
      // new_entry += num_entries_to_copy;
      ++newEntryIndex;
      ++parentEntryIndex;
    }

    // Resize the resulting array to fit.
    // final int actual_count = new_entry - new_bag.entries;
    final int actual_count = newEntryIndex;
    if (actual_count != max_count) {
      // new_bag.reset(reinterpret_cast<ResolvedBag*>(realloc(
      //     new_bag.release(), sizeof(ResolvedBag) + (actual_count * sizeof(ResolvedBag::Entry)))));
      Entry[] resizedEntries = new Entry[actual_count];
      System.arraycopy(new_bag.entries, 0, resizedEntries, 0, actual_count);
      new_bag.entries = resizedEntries;
    }

    // Combine flags from the parent and our own bag.
    new_bag.type_spec_flags = entry.type_flags | parent_bag.type_spec_flags;
    new_bag.entry_count = actual_count;
    ResolvedBag result2 = new_bag;
    // cached_bags_[resid] = std::move(new_bag);
    cached_bags_.put(resid, new_bag);
    return result2;
  }

  String GetResourceName(int resid) {
    ResourceName out_name = new ResourceName();
    if (GetResourceName(resid, out_name)) {
      return out_name.package_ + ":" + out_name.type + "@" + out_name.entry;
    } else {
      return null;
    }
  }

  @SuppressWarnings("DoNotCallSuggester")
  static boolean Utf8ToUtf16(final String str, Ref<String> out) {
    throw new UnsupportedOperationException();
    // ssize_t len =
    //     utf8_to_utf16_length(reinterpret_cast<final byte*>(str.data()), str.size(), false);
    // if (len < 0) {
    //   return false;
    // }
    // out.resize(static_cast<int>(len));
    // utf8_to_utf16(reinterpret_cast<final byte*>(str.data()), str.size(), &*out.begin(),
    //               static_cast<int>(len + 1));
    // return true;
  }

  // Finds the resource ID assigned to `resource_name`.
  // `resource_name` must be of the form '[package:][type/]entry'.
  // If no package is specified in `resource_name`, then `fallback_package` is used as the package.
  // If no type is specified in `resource_name`, then `fallback_type` is used as the type.
  // Returns 0x0 if no resource by that name was found.
//  int GetResourceId(final String resource_name, final String fallback_type = {},
//    final String fallback_package = {});
  @SuppressWarnings("NewApi")
  public int GetResourceId(final String resource_name,
      final String fallback_type,
      final String fallback_package) {
    final Ref<String> package_name = new Ref<>(null),
        type = new Ref<>(null),
        entry = new Ref<>(null);
    if (!ExtractResourceName(resource_name, package_name, type, entry)) {
      return 0;
    }

    if (entry.get().isEmpty()) {
      return 0;
    }

    if (package_name.get().isEmpty()) {
      package_name.set(fallback_package);
    }

    if (type.get().isEmpty()) {
      type.set(fallback_type);
    }

    String type16 = type.get();
    // if (!Utf8ToUtf16(type, &type16)) {
    //   return 0;
    // }

    String entry16 = entry.get();
    // if (!Utf8ToUtf16(entry, &entry16)) {
    //   return 0;
    // }

    final String kAttr16 = "attr";
    final String kAttrPrivate16 = "^attr-private";

    for (final PackageGroup package_group : package_groups_) {
      for (final ConfiguredPackage package_impl : package_group.packages_) {
        LoadedPackage package_= package_impl.loaded_package_;
        if (!Objects.equals(package_name.get(), package_.GetPackageName())) {
          // All packages in the same group are expected to have the same package name.
          break;
        }

        int resid = package_.FindEntryByName(type16, entry16);
        if (resid == 0 && Objects.equals(kAttr16, type16)) {
          // Private attributes in libraries (such as the framework) are sometimes encoded
          // under the type '^attr-private' in order to leave the ID space of public 'attr'
          // free for future additions. Check '^attr-private' for the same name.
          resid = package_.FindEntryByName(kAttrPrivate16, entry16);
        }

        if (resid != 0) {
          return fix_package_id(resid, package_group.dynamic_ref_table.mAssignedPackageId);
        }
      }
    }
    return 0;
  }

  // Triggers the re-construction of lists of types that match the set configuration.
  // This should always be called when mutating the AssetManager's configuration or ApkAssets set.
  void RebuildFilterList() {
    for (PackageGroup group : package_groups_) {
      for (ConfiguredPackage impl : group.packages_) {
        // // Destroy it.
        // impl.filtered_configs_.~ByteBucketArray();
        //
        // // Re-create it.
        // new (impl.filtered_configs_) ByteBucketArray<FilteredConfigGroup>();
        impl.filtered_configs_ =
            new ByteBucketArray<FilteredConfigGroup>(new FilteredConfigGroup()) {
              @Override
              FilteredConfigGroup newInstance() {
                return new FilteredConfigGroup();
              }
            };

        // Create the filters here.
        impl.loaded_package_.ForEachTypeSpec((TypeSpec spec, byte type_index) -> {
          FilteredConfigGroup configGroup = impl.filtered_configs_.editItemAt(type_index);
          // const auto iter_end = spec->types + spec->type_count;
          //   for (auto iter = spec->types; iter != iter_end; ++iter) {
          for (ResTable_type iter : spec.types) {
            ResTable_config this_config = ResTable_config.fromDtoH(iter.config);
            if (this_config.match(configuration_)) {
              configGroup.configurations.add(this_config);
              configGroup.types.add(iter);
            }
          }
        });
      }
    }
  }

  // Purge all resources that are cached and vary by the configuration axis denoted by the
  // bitmask `diff`.
//  void InvalidateCaches(int diff);
  private void InvalidateCaches(int diff) {
    if (diff == 0xffffffff) {
      // Everything must go.
      cached_bags_.clear();
      return;
    }

    // Be more conservative with what gets purged. Only if the bag has other possible
    // variations with respect to what changed (diff) should we remove it.
    // for (auto iter = cached_bags_.cbegin(); iter != cached_bags_.cend();) {
    for (Integer key : new ArrayList<>(cached_bags_.keySet())) {
      // if (diff & iter.second.type_spec_flags) {
      if (isTruthy(diff & cached_bags_.get(key).type_spec_flags)) {
        // iter = cached_bags_.erase(iter);
        cached_bags_.remove(key);
      }
    }
  }

  // Creates a new Theme from this AssetManager.
//  std.unique_ptr<Theme> NewTheme();
  public Theme NewTheme() {
    return new Theme(this);
  }

  public static class Theme {
    //  friend class AssetManager2;
//
// public:
//
//
//
//  final AssetManager2* GetAssetManager() { return asset_manager_; }
//
    public CppAssetManager2 GetAssetManager() { return asset_manager_; }
    //
//  // Returns a bit mask of configuration changes that will impact this
//  // theme (and thus require completely reloading it).
    public int GetChangingConfigurations() { return type_spec_flags_; }

// private:
//  private DISALLOW_COPY_AND_ASSIGN(Theme);

    // Called by AssetManager2.
//  private explicit Theme(AssetManager2* asset_manager) : asset_manager_(asset_manager) {}

    private final CppAssetManager2 asset_manager_;
    private int type_spec_flags_ = 0;
    //  std.array<std.unique_ptr<Package>, kPackageCount> packages_;
    private ThemePackage[] packages_ = new ThemePackage[kPackageCount];

    public Theme(CppAssetManager2 cppAssetManager2) {
      asset_manager_ = cppAssetManager2;
    }

    private static class ThemeEntry {
      static final int SIZEOF = 8 + Res_value.SIZEOF;

      ApkAssetsCookie cookie;
      int type_spec_flags;
      Res_value value;
    }

    private static class ThemeType {
      static final int SIZEOF_WITHOUT_ENTRIES = 8;

      int entry_count;
      ThemeEntry entries[];
    }

    //  static final int kPackageCount = std.numeric_limits<byte>.max() + 1;
    static final int kPackageCount = 256;
    //  static final int kTypeCount = std.numeric_limits<byte>.max() + 1;
    static final int kTypeCount = 256;

    private static class ThemePackage {
      // Each element of Type will be a dynamically sized object
      // allocated to have the entries stored contiguously with the Type.
      // std::array<util::unique_cptr<ThemeType>, kTypeCount> types;
      ThemeType[] types = new ThemeType[kTypeCount];
    }

    // Applies the style identified by `resid` to this theme. This can be called
    // multiple times with different styles. By default, any theme attributes that
    // are already defined before this call are not overridden. If `force` is set
    // to true, this behavior is changed and all theme attributes from the style at
    // `resid` are applied.
    // Returns false if the style failed to apply.
//  boolean ApplyStyle(int resid, boolean force = false);
    public boolean ApplyStyle(int resid, boolean force) {
      // ATRACE_NAME("Theme::ApplyStyle");

      final ResolvedBag bag = asset_manager_.GetBag(resid);
      if (bag == null) {
        return false;
      }

      // Merge the flags from this style.
      type_spec_flags_ |= bag.type_spec_flags;

      int last_type_idx = -1;
      int last_package_idx = -1;
      ThemePackage last_package = null;
      ThemeType last_type = null;

      // Iterate backwards, because each bag is sorted in ascending key ID order, meaning we will only
      // need to perform one resize per type.
      //     using reverse_bag_iterator = std::reverse_iterator<const ResolvedBag::Entry*>;
      // const auto bag_iter_end = reverse_bag_iterator(begin(bag));
      //     for (auto bag_iter = reverse_bag_iterator(end(bag)); bag_iter != bag_iter_end; ++bag_iter) {
      List<Entry> bagEntries = new ArrayList<>(Arrays.asList(bag.entries));
      Collections.reverse(bagEntries);
      for (ResolvedBag.Entry bag_iter : bagEntries) {
        //   final int attr_resid = bag_iter.key;
        final int attr_resid = bag_iter == null ? 0 : bag_iter.key;

        // If the resource ID passed in is not a style, the key can be some other identifier that is not
        // a resource ID. We should fail fast instead of operating with strange resource IDs.
        if (!is_valid_resid(attr_resid)) {
          return false;
        }

        // We don't use the 0-based index for the type so that we can avoid doing ID validation
        // upon lookup. Instead, we keep space for the type ID 0 in our data structures. Since
        // the construction of this type is guarded with a resource ID check, it will never be
        // populated, and querying type ID 0 will always fail.
        int package_idx = get_package_id(attr_resid);
        int type_idx = get_type_id(attr_resid);
        int entry_idx = get_entry_id(attr_resid);

        if (last_package_idx != package_idx) {
          ThemePackage package_ = packages_[package_idx];
          if (package_ == null) {
            package_ = packages_[package_idx] = new ThemePackage();
          }
          last_package_idx = package_idx;
          last_package = package_;
          last_type_idx = -1;
        }

        if (last_type_idx != type_idx) {
          ThemeType type = last_package.types[type_idx];
          if (type == null) {
            // Allocate enough memory to contain this entry_idx. Since we're iterating in reverse over
            // a sorted list of attributes, this shouldn't be resized again during this method call.
            // type.reset(reinterpret_cast<ThemeType*>(
            //     calloc(sizeof(ThemeType) + (entry_idx + 1) * sizeof(ThemeEntry), 1)));
            type = last_package.types[type_idx] = new ThemeType();
            type.entries = new ThemeEntry[entry_idx + 1];
            type.entry_count = entry_idx + 1;
          } else if (entry_idx >= type.entry_count) {
            // Reallocate the memory to contain this entry_idx. Since we're iterating in reverse over
            // a sorted list of attributes, this shouldn't be resized again during this method call.
            int new_count = entry_idx + 1;
            // type.reset(reinterpret_cast<ThemeType*>(
            //     realloc(type.release(), sizeof(ThemeType) + (new_count * sizeof(ThemeEntry)))));
            ThemeEntry[] oldEntries = type.entries;
            type.entries = new ThemeEntry[new_count];
            System.arraycopy(oldEntries, 0, type.entries, 0, oldEntries.length);

            // Clear out the newly allocated space (which isn't zeroed).
            // memset(type.entries + type.entry_count, 0,
            //     (new_count - type.entry_count) * sizeof(ThemeEntry));
            type.entry_count = new_count;
          }
          last_type_idx = type_idx;
          last_type = type;
        }

        ThemeEntry entry = last_type.entries[entry_idx];
        if (entry == null) {
          entry = last_type.entries[entry_idx] = new ThemeEntry();
          entry.value = new Res_value();
        }
        if (force || (entry.value.dataType == Res_value.TYPE_NULL &&
            entry.value.data != Res_value.DATA_NULL_EMPTY)) {
          entry.cookie = bag_iter.cookie;
          entry.type_spec_flags |= bag.type_spec_flags;
          entry.value = bag_iter.value;
        }
      }
      return true;
    }

    // Retrieve a value in the theme. If the theme defines this value, returns an asset cookie
    // indicating which ApkAssets it came from and populates `out_value` with the value.
    // `out_flags` is populated with a bitmask of the configuration axis with which the resource
    // varies.
    //
    // If the attribute is not found, returns kInvalidCookie.
    //
    // NOTE: This function does not do reference traversal. If you want to follow references to other
    // resources to get the "real" value to use, you need to call ResolveReference() after this
    // function.
//  ApkAssetsCookie GetAttribute(int resid, Res_value* out_value,
//                               int* out_flags) const;
    public ApkAssetsCookie GetAttribute(int resid, Ref<Res_value> out_value,
        final Ref<Integer> out_flags) {
      int cnt = 20;

      int type_spec_flags = 0;

      do {
        int package_idx = get_package_id(resid);
        ThemePackage package_ = packages_[package_idx];
        if (package_ != null) {
          // The themes are constructed with a 1-based type ID, so no need to decrement here.
          int type_idx = get_type_id(resid);
          ThemeType type = package_.types[type_idx];
          if (type != null) {
            int entry_idx = get_entry_id(resid);
            if (entry_idx < type.entry_count) {
              ThemeEntry entry = type.entries[entry_idx];
              if (entry == null) {
                entry = new ThemeEntry();
                entry.value = new Res_value();
              }
              type_spec_flags |= entry.type_spec_flags;

              if (entry.value.dataType == Res_value.TYPE_ATTRIBUTE) {
                if (cnt > 0) {
                  cnt--;
                  resid = entry.value.data;
                  continue;
                }
                return K_INVALID_COOKIE;
              }

              // @null is different than @empty.
              if (entry.value.dataType == Res_value.TYPE_NULL &&
                  entry.value.data != Res_value.DATA_NULL_EMPTY) {
                return K_INVALID_COOKIE;
              }

              out_value.set(entry.value);
              out_flags.set(type_spec_flags);
              return entry.cookie;
            }
          }
        }
        break;
      } while (true);
      return K_INVALID_COOKIE;
    }

    // This is like ResolveReference(), but also takes
    // care of resolving attribute references to the theme.
//  ApkAssetsCookie ResolveAttributeReference(ApkAssetsCookie cookie, Res_value* in_out_value,
//                                            ResTable_config in_out_selected_config = null,
//                                            int* in_out_type_spec_flags = null,
//                                            int* out_last_ref = null);
    ApkAssetsCookie ResolveAttributeReference(ApkAssetsCookie cookie, Ref<Res_value> in_out_value,
        final Ref<ResTable_config> in_out_selected_config,
        final Ref<Integer> in_out_type_spec_flags,
        final Ref<Integer> out_last_ref) {
      if (in_out_value.get().dataType == Res_value.TYPE_ATTRIBUTE) {
        final Ref<Integer> new_flags = new Ref<>(0);
        cookie = GetAttribute(in_out_value.get().data, in_out_value, new_flags);
        if (cookie.intValue() == kInvalidCookie) {
          return K_INVALID_COOKIE;
        }

        if (in_out_type_spec_flags != null) {
//          *in_out_type_spec_flags |= new_flags;
          in_out_type_spec_flags.set(in_out_type_spec_flags.get() | new_flags.get());
        }
      }
      return asset_manager_.ResolveReference(cookie, in_out_value, in_out_selected_config,
          in_out_type_spec_flags, out_last_ref);
    }

    //  void Clear();
    public void Clear() {
      type_spec_flags_ = 0;
      for (int i = 0; i < packages_.length; i++) {
//        package_.reset();
        packages_[i] = null;
      }
    }

    // Sets this Theme to be a copy of `o` if `o` has the same AssetManager as this Theme.
    // Returns false if the AssetManagers of the Themes were not compatible.
//  boolean SetTo(final Theme& o);
    public boolean SetTo(final Theme o) {
      if (this == o) {
        return true;
      }

      type_spec_flags_ = o.type_spec_flags_;

      boolean copy_only_system = asset_manager_ != o.asset_manager_;

      // for (int p = 0; p < packages_.size(); p++) {
      //   final Package package_ = o.packages_[p].get();
      for (int p = 0; p < packages_.length; p++) {
        ThemePackage package_ = o.packages_[p];
        if (package_ == null || (copy_only_system && p != 0x01)) {
          // The other theme doesn't have this package, clear ours.
          packages_[p] = new ThemePackage();
          continue;
        }

        if (packages_[p] == null) {
          // The other theme has this package, but we don't. Make one.
          packages_[p] = new ThemePackage();
        }

        // for (int t = 0; t < package_.types.size(); t++) {
        // final Type type = package_.types[t].get();
        for (int t = 0; t < package_.types.length; t++) {
          ThemeType type = package_.types[t];
          if (type == null) {
            // The other theme doesn't have this type, clear ours.
            // packages_[p].types[t].reset();
            continue;
          }

          // Create a new type and update it to theirs.
          // const size_t type_alloc_size = sizeof(ThemeType) + (type->entry_count * sizeof(ThemeEntry));
          // void* copied_data = malloc(type_alloc_size);
          ThemeType copied_data = new ThemeType();
          copied_data.entry_count = type.entry_count;
          // memcpy(copied_data, type, type_alloc_size);
          ThemeEntry[] newEntries = copied_data.entries = new ThemeEntry[type.entry_count];
          for (int i = 0; i < type.entry_count; i++) {
            ThemeEntry entry = type.entries[i];
            ThemeEntry newEntry = new ThemeEntry();
            if (entry != null) {
              newEntry.cookie = entry.cookie;
              newEntry.type_spec_flags = entry.type_spec_flags;
              newEntry.value = entry.value.copy();
            } else {
              newEntry.value = Res_value.NULL_VALUE;
            }
            newEntries[i] = newEntry;
          }

          packages_[p].types[t] = copied_data;
          // packages_[p].types[t].reset(reinterpret_cast<Type*>(copied_data));
        }
      }
      return true;
    }

//
  }  // namespace android

  public List<AssetPath> getAssetPaths() {
    ArrayList<AssetPath> assetPaths = new ArrayList<>(apk_assets_.size());
    for (CppApkAssets apkAssets : apk_assets_) {
      Path path = Fs.fromUrl(apkAssets.GetPath());
      assetPaths.add(new AssetPath(path, apkAssets.GetLoadedArsc().IsSystem()));
    }
    return assetPaths;
  }

}
