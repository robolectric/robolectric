package org.robolectric.res.android;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-8.1.0_r22/libs/androidfw/include/androidfw/AssetManager2.h
// and https://android.googlesource.com/platform/frameworks/base/+/android-8.1.0_r22/libs/androidfw/AssetManager2.cpp
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
//  kInvalidCookie = -1,
//};
//
//// Holds a bag that has been merged with its parent, if one exists.
//struct ResolvedBag {
//  // A single key-value entry in a bag.
//  struct Entry {
//    // The key, as described in ResTable_map::name.
//    uint32_t key;
//
//    Res_value value;
//
//    // Which ApkAssets this entry came from.
//    ApkAssetsCookie cookie;
//
//    ResStringPool* key_pool;
//    ResStringPool* type_pool;
//  };
//
//  // Denotes the configuration axis that this bag varies with.
//  // If a configuration changes with respect to one of these axis,
//  // the bag should be reloaded.
//  uint32_t type_spec_flags;
//
//  // The number of entries in this bag. Access them by indexing into `entries`.
//  uint32_t entry_count;
//
//  // The array of entries for this bag. An empty array is a neat trick to force alignment
//  // of the Entry structs that follow this structure and avoids a bunch of casts.
//  Entry entries[0];
//};
//
//// AssetManager2 is the main entry point for accessing assets and resources.
//// AssetManager2 provides caching of resources retrieved via the underlying
//// ApkAssets.
//class AssetManager2 : public ::AAssetManager {
// public:
//  struct ResourceName {
//    const char* package = nullptr;
//    size_t package_len = 0u;
//
//    const char* type = nullptr;
//    const char16_t* type16 = nullptr;
//    size_t type_len = 0u;
//
//    const char* entry = nullptr;
//    const char16_t* entry16 = nullptr;
//    size_t entry_len = 0u;
//  };
//
//  AssetManager2();
//
//  // Sets/resets the underlying ApkAssets for this AssetManager. The ApkAssets
//  // are not owned by the AssetManager, and must have a longer lifetime.
//  //
//  // Only pass invalidate_caches=false when it is known that the structure
//  // change in ApkAssets is due to a safe addition of resources with completely
//  // new resource IDs.
//  bool SetApkAssets(const std::vector<const ApkAssets*>& apk_assets, bool invalidate_caches = true);
//
//  inline const std::vector<const ApkAssets*> GetApkAssets() const { return apk_assets_; }
//
//  // Returns the string pool for the given asset cookie.
//  // Use the string pool returned here with a valid Res_value object of
//  // type Res_value::TYPE_STRING.
//  const ResStringPool* GetStringPoolForCookie(ApkAssetsCookie cookie) const;
//
//  // Returns the DynamicRefTable for the given package ID.
//  const DynamicRefTable* GetDynamicRefTableForPackage(uint32_t package_id) const;
//
//  // Returns the DynamicRefTable for the ApkAssets represented by the cookie.
//  const DynamicRefTable* GetDynamicRefTableForCookie(ApkAssetsCookie cookie) const;
//
//  // Sets/resets the configuration for this AssetManager. This will cause all
//  // caches that are related to the configuration change to be invalidated.
//  void SetConfiguration(const ResTable_config& configuration);
//
//  inline const ResTable_config& GetConfiguration() const { return configuration_; }
//
//  // Returns all configurations for which there are resources defined. This includes resource
//  // configurations in all the ApkAssets set for this AssetManager.
//  // If `exclude_system` is set to true, resource configurations from system APKs
//  // ('android' package, other libraries) will be excluded from the list.
//  // If `exclude_mipmap` is set to true, resource configurations defined for resource type 'mipmap'
//  // will be excluded from the list.
//  std::set<ResTable_config> GetResourceConfigurations(bool exclude_system = false,
//                                                      bool exclude_mipmap = false);
//
//  // Returns all the locales for which there are resources defined. This includes resource
//  // locales in all the ApkAssets set for this AssetManager.
//  // If `exclude_system` is set to true, resource locales from system APKs
//  // ('android' package, other libraries) will be excluded from the list.
//  // If `merge_equivalent_languages` is set to true, resource locales will be canonicalized
//  // and de-duped in the resulting list.
//  std::set<std::string> GetResourceLocales(bool exclude_system = false,
//                                           bool merge_equivalent_languages = false);
//
//  // Searches the set of APKs loaded by this AssetManager and opens the first one found located
//  // in the assets/ directory.
//  // `mode` controls how the file is opened.
//  //
//  // NOTE: The loaded APKs are searched in reverse order.
//  std::unique_ptr<Asset> Open(const std::string& filename, Asset::AccessMode mode);
//
//  // Opens a file within the assets/ directory of the APK specified by `cookie`.
//  // `mode` controls how the file is opened.
//  std::unique_ptr<Asset> Open(const std::string& filename, ApkAssetsCookie cookie,
//                              Asset::AccessMode mode);
//
//  // Opens the directory specified by `dirname`. The result is an AssetDir that is the combination
//  // of all directories matching `dirname` under the assets/ directory of every ApkAssets loaded.
//  // The entries are sorted by their ASCII name.
//  std::unique_ptr<AssetDir> OpenDir(const std::string& dirname);
//
//  // Searches the set of APKs loaded by this AssetManager and opens the first one found.
//  // `mode` controls how the file is opened.
//  // `out_cookie` is populated with the cookie of the APK this file was found in.
//  //
//  // NOTE: The loaded APKs are searched in reverse order.
//  std::unique_ptr<Asset> OpenNonAsset(const std::string& filename, Asset::AccessMode mode,
//                                      ApkAssetsCookie* out_cookie = nullptr);
//
//  // Opens a file in the APK specified by `cookie`. `mode` controls how the file is opened.
//  // This is typically used to open a specific AndroidManifest.xml, or a binary XML file
//  // referenced by a resource lookup with GetResource().
//  std::unique_ptr<Asset> OpenNonAsset(const std::string& filename, ApkAssetsCookie cookie,
//                                      Asset::AccessMode mode);
//
//  // Populates the `out_name` parameter with resource name information.
//  // Utf8 strings are preferred, and only if they are unavailable are
//  // the Utf16 variants populated.
//  // Returns false if the resource was not found or the name was missing/corrupt.
//  bool GetResourceName(uint32_t resid, ResourceName* out_name);
//
//  // Populates `out_flags` with the bitmask of configuration axis that this resource varies with.
//  // See ResTable_config for the list of configuration axis.
//  // Returns false if the resource was not found.
//  bool GetResourceFlags(uint32_t resid, uint32_t* out_flags);
//
//  // Finds the resource ID assigned to `resource_name`.
//  // `resource_name` must be of the form '[package:][type/]entry'.
//  // If no package is specified in `resource_name`, then `fallback_package` is used as the package.
//  // If no type is specified in `resource_name`, then `fallback_type` is used as the type.
//  // Returns 0x0 if no resource by that name was found.
//  uint32_t GetResourceId(const std::string& resource_name, const std::string& fallback_type = {},
//                         const std::string& fallback_package = {});
//
//  // Retrieves the best matching resource with ID `resid`. The resource value is filled into
//  // `out_value` and the configuration for the selected value is populated in `out_selected_config`.
//  // `out_flags` holds the same flags as retrieved with GetResourceFlags().
//  // If `density_override` is non-zero, the configuration to match against is overridden with that
//  // density.
//  //
//  // Returns a valid cookie if the resource was found. If the resource was not found, or if the
//  // resource was a map/bag type, then kInvalidCookie is returned. If `may_be_bag` is false,
//  // this function logs if the resource was a map/bag type before returning kInvalidCookie.
//  ApkAssetsCookie GetResource(uint32_t resid, bool may_be_bag, uint16_t density_override,
//                              Res_value* out_value, ResTable_config* out_selected_config,
//                              uint32_t* out_flags);
//
//  // Resolves the resource reference in `in_out_value` if the data type is
//  // Res_value::TYPE_REFERENCE.
//  // `cookie` is the ApkAssetsCookie of the reference in `in_out_value`.
//  // `in_out_value` is the reference to resolve. The result is placed back into this object.
//  // `in_out_flags` is the type spec flags returned from calls to GetResource() or
//  // GetResourceFlags(). Configuration flags of the values pointed to by the reference
//  // are OR'd together with `in_out_flags`.
//  // `in_out_config` is populated with the configuration for which the resolved value was defined.
//  // `out_last_reference` is populated with the last reference ID before resolving to an actual
//  // value.
//  // Returns the cookie of the APK the resolved resource was defined in, or kInvalidCookie if
//  // it was not found.
//  ApkAssetsCookie ResolveReference(ApkAssetsCookie cookie, Res_value* in_out_value,
//                                   ResTable_config* in_out_selected_config, uint32_t* in_out_flags,
//                                   uint32_t* out_last_reference);
//
//  // Retrieves the best matching bag/map resource with ID `resid`.
//  // This method will resolve all parent references for this bag and merge keys with the child.
//  // To iterate over the keys, use the following idiom:
//  //
//  //  const AssetManager2::ResolvedBag* bag = asset_manager->GetBag(id);
//  //  if (bag != nullptr) {
//  //    for (auto iter = begin(bag); iter != end(bag); ++iter) {
//  //      ...
//  //    }
//  //  }
//  const ResolvedBag* GetBag(uint32_t resid);
//
//  // Creates a new Theme from this AssetManager.
//  std::unique_ptr<Theme> NewTheme();
//
//  void DumpToLog() const;
//
// private:
//  DISALLOW_COPY_AND_ASSIGN(AssetManager2);
//
//  // Finds the best entry for `resid` amongst all the ApkAssets. The entry can be a simple
//  // Res_value, or a complex map/bag type.
//  //
//  // `density_override` overrides the density of the current configuration when doing a search.
//  //
//  // When `stop_at_first_match` is true, the first match found is selected and the search
//  // terminates. This is useful for methods that just look up the name of a resource and don't
//  // care about the value. In this case, the value of `out_flags` is incomplete and should not
//  // be used.
//  //
//  // `out_flags` stores the resulting bitmask of configuration axis with which the resource
//  // value varies.
//  ApkAssetsCookie FindEntry(uint32_t resid, uint16_t density_override, bool stop_at_first_match,
//                            LoadedArscEntry* out_entry, ResTable_config* out_selected_config,
//                            uint32_t* out_flags);
//
//  // Assigns package IDs to all shared library ApkAssets.
//  // Should be called whenever the ApkAssets are changed.
//  void BuildDynamicRefTable();
//
//  // Purge all resources that are cached and vary by the configuration axis denoted by the
//  // bitmask `diff`.
//  void InvalidateCaches(uint32_t diff);
//
//  // The ordered list of ApkAssets to search. These are not owned by the AssetManager, and must
//  // have a longer lifetime.
//  std::vector<const ApkAssets*> apk_assets_;
//
//  struct PackageGroup {
//    std::vector<const LoadedPackage*> packages_;
//    std::vector<ApkAssetsCookie> cookies_;
//    DynamicRefTable dynamic_ref_table;
//  };
//
//  // DynamicRefTables for shared library package resolution.
//  // These are ordered according to apk_assets_. The mappings may change depending on what is
//  // in apk_assets_, therefore they must be stored in the AssetManager and not in the
//  // immutable ApkAssets class.
//  std::vector<PackageGroup> package_groups_;
//
//  // An array mapping package ID to index into package_groups. This keeps the lookup fast
//  // without taking too much memory.
//  std::array<uint8_t, std::numeric_limits<uint8_t>::max() + 1> package_ids_;
//
//  // The current configuration set for this AssetManager. When this changes, cached resources
//  // may need to be purged.
//  ResTable_config configuration_;
//
//  // Cached set of bags. These are cached because they can inherit keys from parent bags,
//  // which involves some calculation.
//  std::unordered_map<uint32_t, util::unique_cptr<ResolvedBag>> cached_bags_;
//};
//
//class Theme {
//  friend class AssetManager2;
//
// public:
//  // Applies the style identified by `resid` to this theme. This can be called
//  // multiple times with different styles. By default, any theme attributes that
//  // are already defined before this call are not overridden. If `force` is set
//  // to true, this behavior is changed and all theme attributes from the style at
//  // `resid` are applied.
//  // Returns false if the style failed to apply.
//  bool ApplyStyle(uint32_t resid, bool force = false);
//
//  // Sets this Theme to be a copy of `o` if `o` has the same AssetManager as this Theme.
//  // Returns false if the AssetManagers of the Themes were not compatible.
//  bool SetTo(const Theme& o);
//
//  void Clear();
//
//  inline const AssetManager2* GetAssetManager() const { return asset_manager_; }
//
//  inline AssetManager2* GetAssetManager() { return asset_manager_; }
//
//  // Returns a bit mask of configuration changes that will impact this
//  // theme (and thus require completely reloading it).
//  inline uint32_t GetChangingConfigurations() const { return type_spec_flags_; }
//
//  // Retrieve a value in the theme. If the theme defines this value,
//  // returns an asset cookie indicating which ApkAssets it came from
//  // and populates `out_value` with the value. If `out_flags` is non-null,
//  // populates it with a bitmask of the configuration axis the resource
//  // varies with.
//  //
//  // If the attribute is not found, returns kInvalidCookie.
//  //
//  // NOTE: This function does not do reference traversal. If you want
//  // to follow references to other resources to get the "real" value to
//  // use, you need to call ResolveReference() after this function.
//  ApkAssetsCookie GetAttribute(uint32_t resid, Res_value* out_value,
//                               uint32_t* out_flags = nullptr) const;
//
//  // This is like AssetManager2::ResolveReference(), but also takes
//  // care of resolving attribute references to the theme.
//  ApkAssetsCookie ResolveAttributeReference(ApkAssetsCookie cookie, Res_value* in_out_value,
//                                            ResTable_config* in_out_selected_config = nullptr,
//                                            uint32_t* in_out_type_spec_flags = nullptr,
//                                            uint32_t* out_last_ref = nullptr);
//
// private:
//  DISALLOW_COPY_AND_ASSIGN(Theme);
//
//  // Called by AssetManager2.
//  explicit inline Theme(AssetManager2* asset_manager) : asset_manager_(asset_manager) {}
//
//  struct Entry {
//    ApkAssetsCookie cookie;
//    uint32_t type_spec_flags;
//    Res_value value;
//  };
//
//  struct Type {
//    // Use uint32_t for fewer cycles when loading from memory.
//    uint32_t entry_count;
//    uint32_t entry_capacity;
//    Entry entries[0];
//  };
//
//  static constexpr const size_t kPackageCount = std::numeric_limits<uint8_t>::max() + 1;
//  static constexpr const size_t kTypeCount = std::numeric_limits<uint8_t>::max() + 1;
//
//  struct Package {
//    // Each element of Type will be a dynamically sized object
//    // allocated to have the entries stored contiguously with the Type.
//    std::array<util::unique_cptr<Type>, kTypeCount> types;
//  };
//
//  AssetManager2* asset_manager_;
//  uint32_t type_spec_flags_ = 0u;
//  std::array<std::unique_ptr<Package>, kPackageCount> packages_;
//};
//
//inline const ResolvedBag::Entry* begin(const ResolvedBag* bag) { return bag->entries; }
//
//inline const ResolvedBag::Entry* end(const ResolvedBag* bag) {
//  return bag->entries + bag->entry_count;
//}
//
//}  // namespace android
//
//#endif /* ANDROIDFW_ASSETMANAGER2_H_ */


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
//
//  AssetManager2::AssetManager2() { memset(&configuration_, 0, sizeof(configuration_)); }
//
//  bool AssetManager2::SetApkAssets(const std::vector<const ApkAssets*>& apk_assets,
//                                   bool invalidate_caches) {
//    apk_assets_ = apk_assets;
//    BuildDynamicRefTable();
//    if (invalidate_caches) {
//      InvalidateCaches(static_cast<uint32_t>(-1));
//    }
//    return true;
//  }
//
//  void AssetManager2::BuildDynamicRefTable() {
//    package_groups_.clear();
//    package_ids_.fill(0xff);
//
//    // 0x01 is reserved for the android package.
//    int next_package_id = 0x02;
//    const size_t apk_assets_count = apk_assets_.size();
//    for (size_t i = 0; i < apk_assets_count; i++) {
//      const ApkAssets* apk_asset = apk_assets_[i];
//      for (const std::unique_ptr<const LoadedPackage>& package :
//           apk_asset->GetLoadedArsc()->GetPackages()) {
//        // Get the package ID or assign one if a shared library.
//        int package_id;
//        if (package->IsDynamic()) {
//          package_id = next_package_id++;
//        } else {
//          package_id = package->GetPackageId();
//        }
//
//        // Add the mapping for package ID to index if not present.
//        uint8_t idx = package_ids_[package_id];
//        if (idx == 0xff) {
//          package_ids_[package_id] = idx = static_cast<uint8_t>(package_groups_.size());
//          package_groups_.push_back({});
//          package_groups_.back().dynamic_ref_table.mAssignedPackageId = package_id;
//        }
//        PackageGroup* package_group = &package_groups_[idx];
//
//        // Add the package and to the set of packages with the same ID.
//        package_group->packages_.push_back(package.get());
//        package_group->cookies_.push_back(static_cast<ApkAssetsCookie>(i));
//
//        // Add the package name -> build time ID mappings.
//        for (const DynamicPackageEntry& entry : package->GetDynamicPackageMap()) {
//          String16 package_name(entry.package_name.c_str(), entry.package_name.size());
//          package_group->dynamic_ref_table.mEntries.replaceValueFor(
//              package_name, static_cast<uint8_t>(entry.package_id));
//        }
//      }
//    }
//
//    // Now assign the runtime IDs so that we have a build-time to runtime ID map.
//    const auto package_groups_end = package_groups_.end();
//    for (auto iter = package_groups_.begin(); iter != package_groups_end; ++iter) {
//      const std::string& package_name = iter->packages_[0]->GetPackageName();
//      for (auto iter2 = package_groups_.begin(); iter2 != package_groups_end; ++iter2) {
//        iter2->dynamic_ref_table.addMapping(String16(package_name.c_str(), package_name.size()),
//                                            iter->dynamic_ref_table.mAssignedPackageId);
//      }
//    }
//  }
//
//  void AssetManager2::DumpToLog() const {
//    base::ScopedLogSeverity _log(base::INFO);
//
//    std::string list;
//    for (size_t i = 0; i < package_ids_.size(); i++) {
//      if (package_ids_[i] != 0xff) {
//        base::StringAppendF(&list, "%02x -> %d, ", (int) i, package_ids_[i]);
//      }
//    }
//    LOG(INFO) << "Package ID map: " << list;
//
//    for (const auto& package_group: package_groups_) {
//        list = "";
//        for (const auto& package : package_group.packages_) {
//          base::StringAppendF(&list, "%s(%02x), ", package->GetPackageName().c_str(), package->GetPackageId());
//        }
//        LOG(INFO) << base::StringPrintf("PG (%02x): ", package_group.dynamic_ref_table.mAssignedPackageId) << list;
//    }
//  }
//
//  const ResStringPool* AssetManager2::GetStringPoolForCookie(ApkAssetsCookie cookie) const {
//    if (cookie < 0 || static_cast<size_t>(cookie) >= apk_assets_.size()) {
//      return nullptr;
//    }
//    return apk_assets_[cookie]->GetLoadedArsc()->GetStringPool();
//  }
//
//  const DynamicRefTable* AssetManager2::GetDynamicRefTableForPackage(uint32_t package_id) const {
//    if (package_id >= package_ids_.size()) {
//      return nullptr;
//    }
//
//    const size_t idx = package_ids_[package_id];
//    if (idx == 0xff) {
//      return nullptr;
//    }
//    return &package_groups_[idx].dynamic_ref_table;
//  }
//
//  const DynamicRefTable* AssetManager2::GetDynamicRefTableForCookie(ApkAssetsCookie cookie) const {
//    for (const PackageGroup& package_group : package_groups_) {
//      for (const ApkAssetsCookie& package_cookie : package_group.cookies_) {
//        if (package_cookie == cookie) {
//          return &package_group.dynamic_ref_table;
//        }
//      }
//    }
//    return nullptr;
//  }
//
//  void AssetManager2::SetConfiguration(const ResTable_config& configuration) {
//    const int diff = configuration_.diff(configuration);
//    configuration_ = configuration;
//
//    if (diff) {
//      InvalidateCaches(static_cast<uint32_t>(diff));
//    }
//  }
//
//  std::set<ResTable_config> AssetManager2::GetResourceConfigurations(bool exclude_system,
//                                                                     bool exclude_mipmap) {
//    ATRACE_CALL();
//    std::set<ResTable_config> configurations;
//    for (const PackageGroup& package_group : package_groups_) {
//      for (const LoadedPackage* package : package_group.packages_) {
//        if (exclude_system && package->IsSystem()) {
//          continue;
//        }
//        package->CollectConfigurations(exclude_mipmap, &configurations);
//      }
//    }
//    return configurations;
//  }
//
//  std::set<std::string> AssetManager2::GetResourceLocales(bool exclude_system,
//                                                          bool merge_equivalent_languages) {
//    ATRACE_CALL();
//    std::set<std::string> locales;
//    for (const PackageGroup& package_group : package_groups_) {
//      for (const LoadedPackage* package : package_group.packages_) {
//        if (exclude_system && package->IsSystem()) {
//          continue;
//        }
//        package->CollectLocales(merge_equivalent_languages, &locales);
//      }
//    }
//    return locales;
//  }
//
//  std::unique_ptr<Asset> AssetManager2::Open(const std::string& filename, Asset::AccessMode mode) {
//    const std::string new_path = "assets/" + filename;
//    return OpenNonAsset(new_path, mode);
//  }
//
//  std::unique_ptr<Asset> AssetManager2::Open(const std::string& filename, ApkAssetsCookie cookie,
//                                             Asset::AccessMode mode) {
//    const std::string new_path = "assets/" + filename;
//    return OpenNonAsset(new_path, cookie, mode);
//  }
//
//  std::unique_ptr<AssetDir> AssetManager2::OpenDir(const std::string& dirname) {
//    ATRACE_CALL();
//
//    std::string full_path = "assets/" + dirname;
//    std::unique_ptr<SortedVector<AssetDir::FileInfo>> files =
//        util::make_unique<SortedVector<AssetDir::FileInfo>>();
//
//    // Start from the back.
//    for (auto iter = apk_assets_.rbegin(); iter != apk_assets_.rend(); ++iter) {
//      const ApkAssets* apk_assets = *iter;
//
//      auto func = [&](const StringPiece& name, FileType type) {
//        AssetDir::FileInfo info;
//        info.setFileName(String8(name.data(), name.size()));
//        info.setFileType(type);
//        info.setSourceName(String8(apk_assets->GetPath().c_str()));
//        files->add(info);
//      };
//
//      if (!apk_assets->ForEachFile(full_path, func)) {
//        return {};
//      }
//    }
//
//    std::unique_ptr<AssetDir> asset_dir = util::make_unique<AssetDir>();
//    asset_dir->setFileList(files.release());
//    return asset_dir;
//  }
//
//  // Search in reverse because that's how we used to do it and we need to preserve behaviour.
//  // This is unfortunate, because ClassLoaders delegate to the parent first, so the order
//  // is inconsistent for split APKs.
//  std::unique_ptr<Asset> AssetManager2::OpenNonAsset(const std::string& filename,
//                                                     Asset::AccessMode mode,
//                                                     ApkAssetsCookie* out_cookie) {
//    ATRACE_CALL();
//    for (int32_t i = apk_assets_.size() - 1; i >= 0; i--) {
//      std::unique_ptr<Asset> asset = apk_assets_[i]->Open(filename, mode);
//      if (asset) {
//        if (out_cookie != nullptr) {
//          *out_cookie = i;
//        }
//        return asset;
//      }
//    }
//
//    if (out_cookie != nullptr) {
//      *out_cookie = kInvalidCookie;
//    }
//    return {};
//  }
//
//  std::unique_ptr<Asset> AssetManager2::OpenNonAsset(const std::string& filename,
//                                                     ApkAssetsCookie cookie, Asset::AccessMode mode) {
//    ATRACE_CALL();
//    if (cookie < 0 || static_cast<size_t>(cookie) >= apk_assets_.size()) {
//      return {};
//    }
//    return apk_assets_[cookie]->Open(filename, mode);
//  }
//
//  ApkAssetsCookie AssetManager2::FindEntry(uint32_t resid, uint16_t density_override,
//                                           bool stop_at_first_match, LoadedArscEntry* out_entry,
//                                           ResTable_config* out_selected_config,
//                                           uint32_t* out_flags) {
//    ATRACE_CALL();
//
//    // Might use this if density_override != 0.
//    ResTable_config density_override_config;
//
//    // Select our configuration or generate a density override configuration.
//    ResTable_config* desired_config = &configuration_;
//    if (density_override != 0 && density_override != configuration_.density) {
//      density_override_config = configuration_;
//      density_override_config.density = density_override;
//      desired_config = &density_override_config;
//    }
//
//    if (!is_valid_resid(resid)) {
//      LOG(ERROR) << base::StringPrintf("Invalid ID 0x%08x.", resid);
//      return kInvalidCookie;
//    }
//
//    const uint32_t package_id = get_package_id(resid);
//    const uint8_t type_idx = get_type_id(resid) - 1;
//    const uint16_t entry_id = get_entry_id(resid);
//
//    const uint8_t idx = package_ids_[package_id];
//    if (idx == 0xff) {
//      LOG(ERROR) << base::StringPrintf("No package ID %02x found for ID 0x%08x.", package_id, resid);
//      return kInvalidCookie;
//    }
//
//    LoadedArscEntry best_entry;
//    ResTable_config best_config;
//    ApkAssetsCookie best_cookie = kInvalidCookie;
//    uint32_t cumulated_flags = 0u;
//
//    const PackageGroup& package_group = package_groups_[idx];
//    const size_t package_count = package_group.packages_.size();
//    for (size_t i = 0; i < package_count; i++) {
//      LoadedArscEntry current_entry;
//      ResTable_config current_config;
//      uint32_t current_flags = 0;
//
//      const LoadedPackage* loaded_package = package_group.packages_[i];
//      if (!loaded_package->FindEntry(type_idx, entry_id, *desired_config, &current_entry,
//                                     &current_config, &current_flags)) {
//        continue;
//      }
//
//      cumulated_flags |= current_flags;
//
//      if (best_cookie == kInvalidCookie || current_config.isBetterThan(best_config, desired_config)) {
//        best_entry = current_entry;
//        best_config = current_config;
//        best_cookie = package_group.cookies_[i];
//        if (stop_at_first_match) {
//          break;
//        }
//      }
//    }
//
//    if (best_cookie == kInvalidCookie) {
//      return kInvalidCookie;
//    }
//
//    *out_entry = best_entry;
//    out_entry->dynamic_ref_table = &package_group.dynamic_ref_table;
//    *out_selected_config = best_config;
//    *out_flags = cumulated_flags;
//    return best_cookie;
//  }
//
//  bool AssetManager2::GetResourceName(uint32_t resid, ResourceName* out_name) {
//    ATRACE_CALL();
//
//    LoadedArscEntry entry;
//    ResTable_config config;
//    uint32_t flags = 0u;
//    ApkAssetsCookie cookie = FindEntry(resid, 0u /* density_override */,
//                                       true /* stop_at_first_match */, &entry, &config, &flags);
//    if (cookie == kInvalidCookie) {
//      return false;
//    }
//
//    const LoadedPackage* package = apk_assets_[cookie]->GetLoadedArsc()->GetPackageForId(resid);
//    if (package == nullptr) {
//      return false;
//    }
//
//    out_name->package = package->GetPackageName().data();
//    out_name->package_len = package->GetPackageName().size();
//
//    out_name->type = entry.type_string_ref.string8(&out_name->type_len);
//    out_name->type16 = nullptr;
//    if (out_name->type == nullptr) {
//      out_name->type16 = entry.type_string_ref.string16(&out_name->type_len);
//      if (out_name->type16 == nullptr) {
//        return false;
//      }
//    }
//
//    out_name->entry = entry.entry_string_ref.string8(&out_name->entry_len);
//    out_name->entry16 = nullptr;
//    if (out_name->entry == nullptr) {
//      out_name->entry16 = entry.entry_string_ref.string16(&out_name->entry_len);
//      if (out_name->entry16 == nullptr) {
//        return false;
//      }
//    }
//    return true;
//  }
//
//  bool AssetManager2::GetResourceFlags(uint32_t resid, uint32_t* out_flags) {
//    LoadedArscEntry entry;
//    ResTable_config config;
//    ApkAssetsCookie cookie = FindEntry(resid, 0u /* density_override */,
//                                       false /* stop_at_first_match */, &entry, &config, out_flags);
//    return cookie != kInvalidCookie;
//  }
//
//  ApkAssetsCookie AssetManager2::GetResource(uint32_t resid, bool may_be_bag,
//                                             uint16_t density_override, Res_value* out_value,
//                                             ResTable_config* out_selected_config,
//                                             uint32_t* out_flags) {
//    ATRACE_CALL();
//
//    LoadedArscEntry entry;
//    ResTable_config config;
//    uint32_t flags = 0u;
//    ApkAssetsCookie cookie =
//        FindEntry(resid, density_override, false /* stop_at_first_match */, &entry, &config, &flags);
//    if (cookie == kInvalidCookie) {
//      return kInvalidCookie;
//    }
//
//    if (dtohl(entry.entry->flags) & ResTable_entry::FLAG_COMPLEX) {
//      if (!may_be_bag) {
//        LOG(ERROR) << base::StringPrintf("Resource %08x is a complex map type.", resid);
//        return kInvalidCookie;
//      }
//
//      // Create a reference since we can't represent this complex type as a Res_value.
//      out_value->dataType = Res_value::TYPE_REFERENCE;
//      out_value->data = resid;
//      *out_selected_config = config;
//      *out_flags = flags;
//      return cookie;
//    }
//
//    const Res_value* device_value = reinterpret_cast<const Res_value*>(
//        reinterpret_cast<const uint8_t*>(entry.entry) + dtohs(entry.entry->size));
//    out_value->copyFrom_dtoh(*device_value);
//
//    // Convert the package ID to the runtime assigned package ID.
//    entry.dynamic_ref_table->lookupResourceValue(out_value);
//
//    *out_selected_config = config;
//    *out_flags = flags;
//    return cookie;
//  }
//
//  ApkAssetsCookie AssetManager2::ResolveReference(ApkAssetsCookie cookie, Res_value* in_out_value,
//                                                  ResTable_config* in_out_selected_config,
//                                                  uint32_t* in_out_flags,
//                                                  uint32_t* out_last_reference) {
//    ATRACE_CALL();
//    constexpr const int kMaxIterations = 20;
//
//    *out_last_reference = 0u;
//    for (size_t iteration = 0u; in_out_value->dataType == Res_value::TYPE_REFERENCE &&
//                                in_out_value->data != 0u && iteration < kMaxIterations;
//         iteration++) {
//      if (out_last_reference != nullptr) {
//        *out_last_reference = in_out_value->data;
//      }
//      uint32_t new_flags = 0u;
//      cookie = GetResource(in_out_value->data, true /*may_be_bag*/, 0u /*density_override*/,
//                           in_out_value, in_out_selected_config, &new_flags);
//      if (cookie == kInvalidCookie) {
//        return kInvalidCookie;
//      }
//      if (in_out_flags != nullptr) {
//        *in_out_flags |= new_flags;
//      }
//      if (*out_last_reference == in_out_value->data) {
//        // This reference can't be resolved, so exit now and let the caller deal with it.
//        return cookie;
//      }
//    }
//    return cookie;
//  }
//
//  const ResolvedBag* AssetManager2::GetBag(uint32_t resid) {
//    ATRACE_CALL();
//
//    auto cached_iter = cached_bags_.find(resid);
//    if (cached_iter != cached_bags_.end()) {
//      return cached_iter->second.get();
//    }
//
//    LoadedArscEntry entry;
//    ResTable_config config;
//    uint32_t flags = 0u;
//    ApkAssetsCookie cookie = FindEntry(resid, 0u /* density_override */,
//                                       false /* stop_at_first_match */, &entry, &config, &flags);
//    if (cookie == kInvalidCookie) {
//      return nullptr;
//    }
//
//    // Check that the size of the entry header is at least as big as
//    // the desired ResTable_map_entry. Also verify that the entry
//    // was intended to be a map.
//    if (dtohs(entry.entry->size) < sizeof(ResTable_map_entry) ||
//        (dtohs(entry.entry->flags) & ResTable_entry::FLAG_COMPLEX) == 0) {
//      // Not a bag, nothing to do.
//      return nullptr;
//    }
//
//    const ResTable_map_entry* map = reinterpret_cast<const ResTable_map_entry*>(entry.entry);
//    const ResTable_map* map_entry =
//        reinterpret_cast<const ResTable_map*>(reinterpret_cast<const uint8_t*>(map) + map->size);
//    const ResTable_map* const map_entry_end = map_entry + dtohl(map->count);
//
//    uint32_t parent_resid = dtohl(map->parent.ident);
//    if (parent_resid == 0) {
//      // There is no parent, meaning there is nothing to inherit and we can do a simple
//      // copy of the entries in the map.
//      const size_t entry_count = map_entry_end - map_entry;
//      util::unique_cptr<ResolvedBag> new_bag{reinterpret_cast<ResolvedBag*>(
//          malloc(sizeof(ResolvedBag) + (entry_count * sizeof(ResolvedBag::Entry))))};
//      ResolvedBag::Entry* new_entry = new_bag->entries;
//      for (; map_entry != map_entry_end; ++map_entry) {
//        uint32_t new_key = dtohl(map_entry->name.ident);
//        if (!is_internal_resid(new_key)) {
//          // Attributes, arrays, etc don't have a resource id as the name. They specify
//          // other data, which would be wrong to change via a lookup.
//          if (entry.dynamic_ref_table->lookupResourceId(&new_key) != NO_ERROR) {
//            LOG(ERROR) << base::StringPrintf("Failed to resolve key 0x%08x in bag 0x%08x.", new_key, resid);
//            return nullptr;
//          }
//        }
//        new_entry->cookie = cookie;
//        new_entry->value.copyFrom_dtoh(map_entry->value);
//        new_entry->key = new_key;
//        new_entry->key_pool = nullptr;
//        new_entry->type_pool = nullptr;
//        ++new_entry;
//      }
//      new_bag->type_spec_flags = flags;
//      new_bag->entry_count = static_cast<uint32_t>(entry_count);
//      ResolvedBag* result = new_bag.get();
//      cached_bags_[resid] = std::move(new_bag);
//      return result;
//    }
//
//    // In case the parent is a dynamic reference, resolve it.
//    entry.dynamic_ref_table->lookupResourceId(&parent_resid);
//
//    // Get the parent and do a merge of the keys.
//    const ResolvedBag* parent_bag = GetBag(parent_resid);
//    if (parent_bag == nullptr) {
//      // Failed to get the parent that should exist.
//      LOG(ERROR) << base::StringPrintf("Failed to find parent 0x%08x of bag 0x%08x.", parent_resid, resid);
//      return nullptr;
//    }
//
//    // Combine flags from the parent and our own bag.
//    flags |= parent_bag->type_spec_flags;
//
//    // Create the max possible entries we can make. Once we construct the bag,
//    // we will realloc to fit to size.
//    const size_t max_count = parent_bag->entry_count + dtohl(map->count);
//    ResolvedBag* new_bag = reinterpret_cast<ResolvedBag*>(
//        malloc(sizeof(ResolvedBag) + (max_count * sizeof(ResolvedBag::Entry))));
//    ResolvedBag::Entry* new_entry = new_bag->entries;
//
//    const ResolvedBag::Entry* parent_entry = parent_bag->entries;
//    const ResolvedBag::Entry* const parent_entry_end = parent_entry + parent_bag->entry_count;
//
//    // The keys are expected to be in sorted order. Merge the two bags.
//    while (map_entry != map_entry_end && parent_entry != parent_entry_end) {
//      uint32_t child_key = dtohl(map_entry->name.ident);
//      if (!is_internal_resid(child_key)) {
//        if (entry.dynamic_ref_table->lookupResourceId(&child_key) != NO_ERROR) {
//          LOG(ERROR) << base::StringPrintf("Failed to resolve key 0x%08x in bag 0x%08x.", child_key, resid);
//          return nullptr;
//        }
//      }
//
//      if (child_key <= parent_entry->key) {
//        // Use the child key if it comes before the parent
//        // or is equal to the parent (overrides).
//        new_entry->cookie = cookie;
//        new_entry->value.copyFrom_dtoh(map_entry->value);
//        new_entry->key = child_key;
//        new_entry->key_pool = nullptr;
//        new_entry->type_pool = nullptr;
//        ++map_entry;
//      } else {
//        // Take the parent entry as-is.
//        memcpy(new_entry, parent_entry, sizeof(*new_entry));
//      }
//
//      if (child_key >= parent_entry->key) {
//        // Move to the next parent entry if we used it or it was overridden.
//        ++parent_entry;
//      }
//      // Increment to the next entry to fill.
//      ++new_entry;
//    }
//
//    // Finish the child entries if they exist.
//    while (map_entry != map_entry_end) {
//      uint32_t new_key = dtohl(map_entry->name.ident);
//      if (!is_internal_resid(new_key)) {
//        if (entry.dynamic_ref_table->lookupResourceId(&new_key) != NO_ERROR) {
//          LOG(ERROR) << base::StringPrintf("Failed to resolve key 0x%08x in bag 0x%08x.", new_key, resid);
//          return nullptr;
//        }
//      }
//      new_entry->cookie = cookie;
//      new_entry->value.copyFrom_dtoh(map_entry->value);
//      new_entry->key = new_key;
//      new_entry->key_pool = nullptr;
//      new_entry->type_pool = nullptr;
//      ++map_entry;
//      ++new_entry;
//    }
//
//    // Finish the parent entries if they exist.
//    if (parent_entry != parent_entry_end) {
//      // Take the rest of the parent entries as-is.
//      const size_t num_entries_to_copy = parent_entry_end - parent_entry;
//      memcpy(new_entry, parent_entry, num_entries_to_copy * sizeof(*new_entry));
//      new_entry += num_entries_to_copy;
//    }
//
//    // Resize the resulting array to fit.
//    const size_t actual_count = new_entry - new_bag->entries;
//    if (actual_count != max_count) {
//      new_bag = reinterpret_cast<ResolvedBag*>(
//          realloc(new_bag, sizeof(ResolvedBag) + (actual_count * sizeof(ResolvedBag::Entry))));
//    }
//
//    util::unique_cptr<ResolvedBag> final_bag{new_bag};
//    final_bag->type_spec_flags = flags;
//    final_bag->entry_count = static_cast<uint32_t>(actual_count);
//    ResolvedBag* result = final_bag.get();
//    cached_bags_[resid] = std::move(final_bag);
//    return result;
//  }
//
//  static bool Utf8ToUtf16(const StringPiece& str, std::u16string* out) {
//    ssize_t len =
//        utf8_to_utf16_length(reinterpret_cast<const uint8_t*>(str.data()), str.size(), false);
//    if (len < 0) {
//      return false;
//    }
//    out->resize(static_cast<size_t>(len));
//    utf8_to_utf16(reinterpret_cast<const uint8_t*>(str.data()), str.size(), &*out->begin(),
//                  static_cast<size_t>(len + 1));
//    return true;
//  }
//
//  uint32_t AssetManager2::GetResourceId(const std::string& resource_name,
//                                        const std::string& fallback_type,
//                                        const std::string& fallback_package) {
//    StringPiece package_name, type, entry;
//    if (!ExtractResourceName(resource_name, &package_name, &type, &entry)) {
//      return 0u;
//    }
//
//    if (entry.empty()) {
//      return 0u;
//    }
//
//    if (package_name.empty()) {
//      package_name = fallback_package;
//    }
//
//    if (type.empty()) {
//      type = fallback_type;
//    }
//
//    std::u16string type16;
//    if (!Utf8ToUtf16(type, &type16)) {
//      return 0u;
//    }
//
//    std::u16string entry16;
//    if (!Utf8ToUtf16(entry, &entry16)) {
//      return 0u;
//    }
//
//    const StringPiece16 kAttr16 = u"attr";
//    const static std::u16string kAttrPrivate16 = u"^attr-private";
//
//    for (const PackageGroup& package_group : package_groups_) {
//      for (const LoadedPackage* package : package_group.packages_) {
//        if (package_name != package->GetPackageName()) {
//          // All packages in the same group are expected to have the same package name.
//          break;
//        }
//
//        uint32_t resid = package->FindEntryByName(type16, entry16);
//        if (resid == 0u && kAttr16 == type16) {
//          // Private attributes in libraries (such as the framework) are sometimes encoded
//          // under the type '^attr-private' in order to leave the ID space of public 'attr'
//          // free for future additions. Check '^attr-private' for the same name.
//          resid = package->FindEntryByName(kAttrPrivate16, entry16);
//        }
//
//        if (resid != 0u) {
//          return fix_package_id(resid, package_group.dynamic_ref_table.mAssignedPackageId);
//        }
//      }
//    }
//    return 0u;
//  }
//
//  void AssetManager2::InvalidateCaches(uint32_t diff) {
//    if (diff == 0xffffffffu) {
//      // Everything must go.
//      cached_bags_.clear();
//      return;
//    }
//
//    // Be more conservative with what gets purged. Only if the bag has other possible
//    // variations with respect to what changed (diff) should we remove it.
//    for (auto iter = cached_bags_.cbegin(); iter != cached_bags_.cend();) {
//      if (diff & iter->second->type_spec_flags) {
//        iter = cached_bags_.erase(iter);
//      } else {
//        ++iter;
//      }
//    }
//  }
//
//  std::unique_ptr<Theme> AssetManager2::NewTheme() { return std::unique_ptr<Theme>(new Theme(this)); }
//
//  bool Theme::ApplyStyle(uint32_t resid, bool force) {
//    ATRACE_CALL();
//
//    const ResolvedBag* bag = asset_manager_->GetBag(resid);
//    if (bag == nullptr) {
//      return false;
//    }
//
//    // Merge the flags from this style.
//    type_spec_flags_ |= bag->type_spec_flags;
//
//    // On the first iteration, verify the attribute IDs and
//    // update the entry count in each type.
//    const auto bag_iter_end = end(bag);
//    for (auto bag_iter = begin(bag); bag_iter != bag_iter_end; ++bag_iter) {
//      const uint32_t attr_resid = bag_iter->key;
//
//      // If the resource ID passed in is not a style, the key can be
//      // some other identifier that is not a resource ID.
//      if (!is_valid_resid(attr_resid)) {
//        return false;
//      }
//
//      const uint32_t package_idx = get_package_id(attr_resid);
//
//      // The type ID is 1-based, so subtract 1 to get an index.
//      const uint32_t type_idx = get_type_id(attr_resid) - 1;
//      const uint32_t entry_idx = get_entry_id(attr_resid);
//
//      std::unique_ptr<Package>& package = packages_[package_idx];
//      if (package == nullptr) {
//        package.reset(new Package());
//      }
//
//      util::unique_cptr<Type>& type = package->types[type_idx];
//      if (type == nullptr) {
//        // Set the initial capacity to take up a total amount of 1024 bytes.
//        constexpr uint32_t kInitialCapacity = (1024u - sizeof(Type)) / sizeof(Entry);
//        const uint32_t initial_capacity = std::max(entry_idx, kInitialCapacity);
//        type.reset(
//            reinterpret_cast<Type*>(calloc(sizeof(Type) + (initial_capacity * sizeof(Entry)), 1)));
//        type->entry_capacity = initial_capacity;
//      }
//
//      // Set the entry_count to include this entry. We will populate
//      // and resize the array as necessary in the next pass.
//      if (entry_idx + 1 > type->entry_count) {
//        // Increase the entry count to include this.
//        type->entry_count = entry_idx + 1;
//      }
//    }
//
//    // On the second pass, we will realloc to fit the entry counts
//    // and populate the structures.
//    for (auto bag_iter = begin(bag); bag_iter != bag_iter_end; ++bag_iter) {
//      const uint32_t attr_resid = bag_iter->key;
//      const uint32_t package_idx = get_package_id(attr_resid);
//      const uint32_t type_idx = get_type_id(attr_resid) - 1;
//      const uint32_t entry_idx = get_entry_id(attr_resid);
//      Package* package = packages_[package_idx].get();
//      util::unique_cptr<Type>& type = package->types[type_idx];
//      if (type->entry_count != type->entry_capacity) {
//        // Resize to fit the actual entries that will be included.
//        Type* type_ptr = type.release();
//        type.reset(reinterpret_cast<Type*>(
//            realloc(type_ptr, sizeof(Type) + (type_ptr->entry_count * sizeof(Entry)))));
//        if (type->entry_capacity < type->entry_count) {
//          // Clear the newly allocated memory (which does not get zero initialized).
//          // We need to do this because we |= type_spec_flags.
//          memset(type->entries + type->entry_capacity, 0,
//                 sizeof(Entry) * (type->entry_count - type->entry_capacity));
//        }
//        type->entry_capacity = type->entry_count;
//      }
//      Entry& entry = type->entries[entry_idx];
//      if (force || entry.value.dataType == Res_value::TYPE_NULL) {
//        entry.cookie = bag_iter->cookie;
//        entry.type_spec_flags |= bag->type_spec_flags;
//        entry.value = bag_iter->value;
//      }
//    }
//    return true;
//  }
//
//  ApkAssetsCookie Theme::GetAttribute(uint32_t resid, Res_value* out_value,
//                                      uint32_t* out_flags) const {
//    constexpr const int kMaxIterations = 20;
//
//    uint32_t type_spec_flags = 0u;
//
//    for (int iterations_left = kMaxIterations; iterations_left > 0; iterations_left--) {
//      if (!is_valid_resid(resid)) {
//        return kInvalidCookie;
//      }
//
//      const uint32_t package_idx = get_package_id(resid);
//
//      // Type ID is 1-based, subtract 1 to get the index.
//      const uint32_t type_idx = get_type_id(resid) - 1;
//      const uint32_t entry_idx = get_entry_id(resid);
//
//      const Package* package = packages_[package_idx].get();
//      if (package == nullptr) {
//        return kInvalidCookie;
//      }
//
//      const Type* type = package->types[type_idx].get();
//      if (type == nullptr) {
//        return kInvalidCookie;
//      }
//
//      if (entry_idx >= type->entry_count) {
//        return kInvalidCookie;
//      }
//
//      const Entry& entry = type->entries[entry_idx];
//      type_spec_flags |= entry.type_spec_flags;
//
//      switch (entry.value.dataType) {
//        case Res_value::TYPE_NULL:
//          return kInvalidCookie;
//
//        case Res_value::TYPE_ATTRIBUTE:
//          resid = entry.value.data;
//          break;
//
//        case Res_value::TYPE_DYNAMIC_ATTRIBUTE: {
//          // Resolve the dynamic attribute to a normal attribute
//          // (with the right package ID).
//          resid = entry.value.data;
//          const DynamicRefTable* ref_table =
//              asset_manager_->GetDynamicRefTableForPackage(package_idx);
//          if (ref_table == nullptr || ref_table->lookupResourceId(&resid) != NO_ERROR) {
//            LOG(ERROR) << base::StringPrintf("Failed to resolve dynamic attribute 0x%08x", resid);
//            return kInvalidCookie;
//          }
//        } break;
//
//        case Res_value::TYPE_DYNAMIC_REFERENCE: {
//          // Resolve the dynamic reference to a normal reference
//          // (with the right package ID).
//          out_value->dataType = Res_value::TYPE_REFERENCE;
//          out_value->data = entry.value.data;
//          const DynamicRefTable* ref_table =
//              asset_manager_->GetDynamicRefTableForPackage(package_idx);
//          if (ref_table == nullptr || ref_table->lookupResourceId(&out_value->data) != NO_ERROR) {
//            LOG(ERROR) << base::StringPrintf("Failed to resolve dynamic reference 0x%08x",
//                                             out_value->data);
//            return kInvalidCookie;
//          }
//
//          if (out_flags != nullptr) {
//            *out_flags = type_spec_flags;
//          }
//          return entry.cookie;
//        }
//
//        default:
//          *out_value = entry.value;
//          if (out_flags != nullptr) {
//            *out_flags = type_spec_flags;
//          }
//          return entry.cookie;
//      }
//    }
//
//    LOG(WARNING) << base::StringPrintf("Too many (%d) attribute references, stopped at: 0x%08x",
//                                       kMaxIterations, resid);
//    return kInvalidCookie;
//  }
//
//  ApkAssetsCookie Theme::ResolveAttributeReference(ApkAssetsCookie cookie, Res_value* in_out_value,
//                                                   ResTable_config* in_out_selected_config,
//                                                   uint32_t* in_out_type_spec_flags,
//                                                   uint32_t* out_last_ref) {
//    if (in_out_value->dataType == Res_value::TYPE_ATTRIBUTE) {
//      uint32_t new_flags;
//      cookie = GetAttribute(in_out_value->data, in_out_value, &new_flags);
//      if (cookie == kInvalidCookie) {
//        return kInvalidCookie;
//      }
//
//      if (in_out_type_spec_flags != nullptr) {
//        *in_out_type_spec_flags |= new_flags;
//      }
//    }
//    return asset_manager_->ResolveReference(cookie, in_out_value, in_out_selected_config,
//                                            in_out_type_spec_flags, out_last_ref);
//  }
//
//  void Theme::Clear() {
//    type_spec_flags_ = 0u;
//    for (std::unique_ptr<Package>& package : packages_) {
//      package.reset();
//    }
//  }
//
//  bool Theme::SetTo(const Theme& o) {
//    if (this == &o) {
//      return true;
//    }
//
//    if (asset_manager_ != o.asset_manager_) {
//      return false;
//    }
//
//    type_spec_flags_ = o.type_spec_flags_;
//
//    for (size_t p = 0; p < packages_.size(); p++) {
//      const Package* package = o.packages_[p].get();
//      if (package == nullptr) {
//        packages_[p].reset();
//        continue;
//      }
//
//      for (size_t t = 0; t < package->types.size(); t++) {
//        const Type* type = package->types[t].get();
//        if (type == nullptr) {
//          packages_[p]->types[t].reset();
//          continue;
//        }
//
//        const size_t type_alloc_size = sizeof(Type) + (type->entry_capacity * sizeof(Entry));
//        void* copied_data = malloc(type_alloc_size);
//        memcpy(copied_data, type, type_alloc_size);
//        packages_[p]->types[t].reset(reinterpret_cast<Type*>(copied_data));
//      }
//    }
//    return true;
//  }
//
//  }  // namespace android
}
