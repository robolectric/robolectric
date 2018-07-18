package org.robolectric.res.android;

import static org.robolectric.res.android.ResourceUtils.get_package_id;
import static org.robolectric.res.android.ResourceUtils.make_resid;
import static org.robolectric.res.android.Util.dtohl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.robolectric.res.android.ResourceTypes.ResTable_entry;
import org.robolectric.res.android.ResourceTypes.ResTable_type;
import org.robolectric.res.android.ResourceTypes.ResTable_typeSpec;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-8.1.0_r22/libs/androidfw/include/androidfw/LoadedArsc.h
// and https://android.googlesource.com/platform/frameworks/base/+/android-8.1.0_r22/libs/androidfw/LoadedArsc.cpp
public class LoadedArsc {

  //#ifndef LOADEDARSC_H_
//#define LOADEDARSC_H_
//
//#include <memory>
//#include <set>
//#include <vector>
//
//#include "android-base/macros.h"
//
//#include "androidfw/ByteBucketArray.h"
//#include "androidfw/Chunk.h"
//#include "androidfw/ResourceTypes.h"
//#include "androidfw/Util.h"
//
//namespace android {
//
  static class DynamicPackageEntry {

    // public:
    //
    // DynamicPackageEntry() =default;
    //
    // DynamicPackageEntry(String&package_name, int package_id)
    //     :
    //
    // package_name(std::move(package_name)),
    //
    // package_id(package_id) {
    // }

    String package_name;
    int package_id = 0;
  }

  static class LoadedArscEntry {

    // A pointer to the resource table entry for this resource.
    // If the size of the entry is > sizeof(ResTable_entry), it can be cast to
    // a ResTable_map_entry and processed as a bag/map.
    ResTable_entry entry = null;

    // The dynamic package ID map for the package from which this resource came from.
    DynamicRefTable dynamic_ref_table = null;

    // The string pool reference to the type's name. This uses a different string pool than
    // the global string pool, but this is hidden from the caller.
    StringPoolRef type_string_ref;

    // The string pool reference to the entry's name. This uses a different string pool than
    // the global string pool, but this is hidden from the caller.
    StringPoolRef entry_string_ref;
  }

//class LoadedArsc;

  //// Read-only view into a resource table. This class validates all data
//// when loading, including offsets and lengths.
//class LoadedArsc {
// public:
//  // Load a resource table from memory pointed to by `data` of size `len`.
//  // The lifetime of `data` must out-live the LoadedArsc returned from this method.
//  // If `system` is set to true, the LoadedArsc is considered as a system provided resource.
//  // If `load_as_shared_library` is set to true, the application package (0x7f) is treated
//  // as a shared library (0x00). When loaded into an AssetManager, the package will be assigned an
//  // ID.
//  static std::unique_ptr<const LoadedArsc> Load(const void* data, size_t len, boolean system = false,
//                                                boolean load_as_shared_library = false);
//
//  ~LoadedArsc();
//
//  // Returns the string pool where all string resource values
//  // (Res_value::dataType == Res_value::TYPE_STRING) are indexed.
  ResStringPool GetStringPool() {
    return global_string_pool_;
  }

  //
//  // Finds the resource with ID `resid` with the best value for configuration `config`.
//  // The parameter `out_entry` will be filled with the resulting resource entry.
//  // The resource entry can be a simple entry (ResTable_entry) or a complex bag
//  // (ResTable_entry_map).
//  boolean FindEntry(uint32_t resid, const ResTable_config& config, LoadedArscEntry* out_entry,
//                 ResTable_config* selected_config, uint32_t* out_flags);
//
//  // Gets a pointer to the name of the package in `resid`, or nullptr if the package doesn't exist.
//  const LoadedPackage* GetPackageForId(uint32_t resid);
//
//  // Returns true if this is a system provided resource.
//  boolean IsSystem() { return system_; }
//
//  // Returns a vector of LoadedPackage pointers, representing the packages in this LoadedArsc.
  List<LoadedPackage> GetPackages() {
    return packages_;
  }

  //
// private:
//  DISALLOW_COPY_AND_ASSIGN(LoadedArsc);
//
//  LoadedArsc() = default;
//  boolean LoadTable(const Chunk& chunk, boolean load_as_shared_library);
//
  ResStringPool global_string_pool_;
  List<LoadedPackage> packages_;
//  boolean system_ = false;
//};
//
//}  // namespace android
//
//#endif /* LOADEDARSC_H_ */

  //    /*
//   * Copyright (C) 2016 The Android Open Source Project
//   *
//   * Licensed under the Apache License, Version 2.0 (the "License");
//   * you may not use this file except in compliance with the License.
//   * You may obtain a copy of the License at
//   *
//   *      http://www.apache.org/licenses/LICENSE-2.0
//   *
//   * Unless required by applicable law or agreed to in writing, software
//   * distributed under the License is distributed on an "AS IS" BASIS,
//   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   * See the License for the specific language governing permissions and
//   * limitations under the License.
//   */
//
//  #define ATRACE_TAG ATRACE_TAG_RESOURCES
//
//  #include "androidfw/LoadedArsc.h"
//
//  #include <cstddef>
//  #include <limits>
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
//  #include "androidfw/ByteBucketArray.h"
//  #include "androidfw/Chunk.h"
//  #include "androidfw/ResourceUtils.h"
//  #include "androidfw/Util.h"
//
//  using android::base::StringPrintf;
//
//  namespace android {
//
//  constexpr const static int kAppPackageId = 0x7f;
//
//  // Element of a TypeSpec array. See TypeSpec.
  static class Type {

    // The configuration for which this type defines entries.
    // This is already converted to host endianness.
    ResTable_config configuration;

    // Pointer to the mmapped data where entry definitions are kept.
    ResTable_type type;
  }

  //
//  // TypeSpec is going to be immediately proceeded by
//  // an array of Type structs, all in the same block of memory.
  static class TypeSpec {

    // Pointer to the mmapped data where flags are kept.
    // Flags denote whether the resource entry is public
    // and under which configurations it varies.
    ResTable_typeSpec type_spec;

    // The number of types that follow this struct.
    // There is a type for each configuration
    // that entries are defined for.
    int type_count;

    // Trick to easily access a variable number of Type structs
    // proceeding this struct, and to ensure their alignment.
    //  const Type types[0];
    Type[] types;
  }
//
//  // TypeSpecPtr points to the block of memory that holds
//  // a TypeSpec struct, followed by an array of Type structs.
//  // TypeSpecPtr is a managed pointer that knows how to delete
//  // itself.
//  using TypeSpecPtr = util::unique_cptr<TypeSpec>;
//
//  namespace {
//
//  // Builder that helps accumulate Type structs and then create a single
//  // contiguous block of memory to store both the TypeSpec struct and
//  // the Type structs.
//  class TypeSpecPtrBuilder {
//   public:
//    TypeSpecPtrBuilder(const ResTable_typeSpec* header) : header_(header) {}
//
//    void AddType(const ResTable_type* type) {
//      ResTable_config config;
//      config.copyFromDtoH(type->config);
//      types_.push_back(Type{config, type});
//    }
//
//    TypeSpecPtr Build() {
//      // Check for overflow.
//      if ((std::numeric_limits<size_t>::max() - sizeof(TypeSpec)) / sizeof(Type) < types_.size()) {
//        return {};
//      }
//      TypeSpec* type_spec = (TypeSpec*)::malloc(sizeof(TypeSpec) + (types_.size() * sizeof(Type)));
//      type_spec->type_spec = header_;
//      type_spec->type_count = types_.size();
//      memcpy(type_spec + 1, types_.data(), types_.size() * sizeof(Type));
//      return TypeSpecPtr(type_spec);
//    }
//
//   private:
//    DISALLOW_COPY_AND_ASSIGN(TypeSpecPtrBuilder);
//
//    const ResTable_typeSpec* header_;
//    std::vector<Type> types_;
//  };
//
//  }  // namespace

  static class LoadedPackage {

    // friend class LoadedArsc;

    // public:
    boolean FindEntry(byte type_idx, short entry_idx, ResTable_config config,
        Ref<LoadedArscEntry> out_entry, Ref<ResTable_config> out_selected_config,
        int out_flags) {
      throw new UnsupportedOperationException();
    }

    // Returns the string pool where type names are stored.
    ResStringPool GetTypeStringPool() {
      return type_string_pool_;
    }

    // Returns the string pool where the names of resource entries are stored.
    ResStringPool GetKeyStringPool() {
      return key_string_pool_;
    }

    String GetPackageName() {
      return package_name_;
    }

    int GetPackageId() {
      return package_id_;
    }

    // Returns true if this package is dynamic (shared library) and needs to have an ID assigned.
    boolean IsDynamic() {
      return dynamic_;
    }

    // Returns true if this package originates from a system provided resource.
    boolean IsSystem() {
      return system_;
    }

    // Returns the map of package name to package ID used in this LoadedPackage. At runtime, a
    // package could have been assigned a different package ID than what this LoadedPackage was
    // compiled with. AssetManager rewrites the package IDs so that they are compatible at runtime.
    List<DynamicPackageEntry> GetDynamicPackageMap() {
      return dynamic_package_map_;
    }

    // Populates a set of ResTable_config structs, possibly excluding configurations defined for
    // the mipmap type.
    // void CollectConfigurations(boolean exclude_mipmap, Set<ResTable_config> out_configs);

    // Populates a set of strings representing locales.
    // If `canonicalize` is set to true, each locale is transformed into its canonical format
    // before being inserted into the set. This may cause some equivalent locales to de-dupe.
    // void CollectLocales(boolean canonicalize, Set<String> out_locales);

    // Finds the entry with the specified type name and entry name. The names are in UTF-16 because
    // the underlying ResStringPool API expects this. For now this is acceptable, but since
    // the default policy in AAPT2 is to build UTF-8 string pools, this needs to change.
    // Returns a partial resource ID, with the package ID left as 0x00. The caller is responsible
    // for patching the correct package ID to the resource ID.
    // int FindEntryByName(String type_name, String entry_name);

    // private:

    // DISALLOW_COPY_AND_ASSIGN(LoadedPackage);

    // static std::unique_ptr<LoadedPackage> Load(const Chunk&chunk);

    // LoadedPackage() =default;

    ResStringPool type_string_pool_;
    ResStringPool key_string_pool_;
    String package_name_;
    int package_id_ = -1;
    int type_id_offset_ = 0;
    boolean dynamic_ = false;
    boolean system_ = false;

    ByteBucketArray<TypeSpec> type_specs_;
    List<DynamicPackageEntry> dynamic_package_map_ = new ArrayList<>();

//  boolean LoadedPackage::FindEntry(uint8_t type_idx, uint16_t entry_idx, const ResTable_config& config,
//                                LoadedArscEntry* out_entry, ResTable_config* out_selected_config,
//                                uint32_t* out_flags) {
//    ATRACE_CALL();
//
//    // If the type IDs are offset in this package, we need to take that into account when searching
//    // for a type.
//    const TypeSpecPtr& ptr = type_specs_[type_idx - type_id_offset_];
//    if (ptr == nullptr) {
//      return false;
//    }
//
//    // Don't bother checking if the entry ID is larger than
//    // the number of entries.
//    if (entry_idx >= dtohl(ptr->type_spec->entryCount)) {
//      return false;
//    }
//
//    const ResTable_config* best_config = nullptr;
//    const ResTable_type* best_type = nullptr;
//    uint32_t best_offset = 0;
//
//    for (uint32_t i = 0; i < ptr->type_count; i++) {
//      const Type* type = &ptr->types[i];
//
//      if (type->configuration.match(config) &&
//          (best_config == nullptr || type->configuration.isBetterThan(*best_config, &config))) {
//        // The configuration matches and is better than the previous selection.
//        // Find the entry value if it exists for this configuration.
//        size_t entry_count = dtohl(type->type->entryCount);
//        if (entry_idx < entry_count) {
//          const uint32_t* entry_offsets = reinterpret_cast<const uint32_t*>(
//              reinterpret_cast<const uint8_t*>(type->type) + dtohs(type->type->header.headerSize));
//          const uint32_t offset = dtohl(entry_offsets[entry_idx]);
//          if (offset != ResTable_type::NO_ENTRY) {
//            // There is an entry for this resource, record it.
//            best_config = &type->configuration;
//            best_type = type->type;
//            best_offset = offset + dtohl(type->type->entriesStart);
//          }
//        }
//      }
//    }
//
//    if (best_type == nullptr) {
//      return false;
//    }
//
//    const uint32_t* flags = reinterpret_cast<const uint32_t*>(ptr->type_spec + 1);
//    *out_flags = dtohl(flags[entry_idx]);
//    *out_selected_config = *best_config;
//
//    const ResTable_entry* best_entry = reinterpret_cast<const ResTable_entry*>(
//        reinterpret_cast<const uint8_t*>(best_type) + best_offset);
//    out_entry->entry = best_entry;
//    out_entry->type_string_ref = StringPoolRef(&type_string_pool_, best_type->id - 1);
//    out_entry->entry_string_ref = StringPoolRef(&key_string_pool_, dtohl(best_entry->key.index));
//    return true;
//  }
//
//  // The destructor gets generated into arbitrary translation units
//  // if left implicit, which causes the compiler to complain about
//  // forward declarations and incomplete types.
//  LoadedArsc::~LoadedArsc() {}


    void CollectConfigurations(boolean exclude_mipmap,
        Set<ResTable_config> out_configs) {
      String kMipMap = "mipmap";
      int type_count = type_specs_.size();
      for (int i = 0; i < type_count; i++) {
        TypeSpec type_spec = type_specs_.get(i);
        if (type_spec != null) {
          if (exclude_mipmap) {
            int type_idx = type_spec.type_spec.id - 1;
            Ref<Integer> type_name_len = new Ref<>(0);
            String type_name16 = type_string_pool_.stringAt(type_idx, type_name_len);
            if (type_name16 != null) {
              // if (kMipMap.compare(0, std::u16string::npos,type_name16, type_name_len) ==0){
              if (kMipMap.equals(type_name16)) {
                // This is a mipmap type, skip collection.
                continue;
              }
            }
            String type_name = type_string_pool_.string8At(type_idx, type_name_len);
            if (type_name != null) {
              // if (strncmp(type_name, "mipmap", type_name_len) == 0) {
              if ("mipmap".equals(type_name))
                // This is a mipmap type, skip collection.
                continue;
            }
          }
        }

        for (int j = 0; j < type_spec.type_count; j++) {
          // out_configs -> insert(type_spec -> types[j].configuration);
          out_configs.add(type_spec.types[j].configuration);
        }
      }
    }

    void CollectLocales(boolean canonicalize, Set<String> out_locales) {
      // char temp_locale[ RESTABLE_MAX_LOCALE_LEN];
      String temp_locale;
      int type_count = type_specs_.size();
      for (int i = 0; i < type_count; i++) {
        TypeSpec type_spec = type_specs_.get(i);
        if (type_spec != null) {
          for (int j = 0; j < type_spec.type_count; j++) {
            ResTable_config configuration = type_spec.types[j].configuration;
            if (configuration.locale() != 0) {
              temp_locale = configuration.getBcp47Locale(canonicalize);
              String locale = temp_locale;
              out_locales.add(locale);
            }
          }
        }
      }
    }

    int FindEntryByName(String type_name, String entry_name) {
      int type_idx = type_string_pool_.indexOfString(type_name);
      if (type_idx < 0) {
        return 0;
      }

      int key_idx = key_string_pool_.indexOfString(entry_name);
      if (key_idx < 0) {
        return 0;
      }

      TypeSpec type_spec = type_specs_.get(type_idx);
      if (type_spec == null) {
        return 0;
      }

      for (int ti = 0; ti < type_spec.type_count; ti++) {
        Type type = type_spec.types[ti];
        int entry_count = type.type.entryCount;

        for (int entry_idx = 0; entry_idx < entry_count; entry_idx++) {
          // const uint32_t* entry_offsets = reinterpret_cast<const uint32_t*>(
          //     reinterpret_cast<const uint8_t*>(type.type) + dtohs(type.type.header.headerSize));
          ResTable_type entry_offsets = new ResTable_type(type.type.myBuf(),
              type.type.myOffset() + type.type.header.headerSize);
          // int offset = dtohl(entry_offsets[entry_idx]);
          int offset = dtohl(entry_offsets.entryOffset(entry_idx));
          if (offset != ResTable_type.NO_ENTRY) {
            // const ResTable_entry* entry =
            //     reinterpret_cast<const ResTable_entry*>(reinterpret_cast<const uint8_t*>(type.type) +
            //     dtohl(type.type.entriesStart) + offset);
            ResTable_entry entry =
                new ResTable_entry(type.type.myBuf(), type.type.myOffset() +
                    dtohl(type.type.entriesStart) + offset);
            if (dtohl(entry.key.index) == key_idx) {
              // The package ID will be overridden by the caller (due to runtime assignment of package
              // IDs for shared libraries).
              return make_resid((byte) 0x00, (byte) (type_idx + type_id_offset_ + 1), (short) entry_idx);
            }
          }
        }
      }
      return 0;
    }

//  std::unique_ptr<LoadedPackage> LoadedPackage::Load(const Chunk& chunk) {
//    ATRACE_CALL();
//    std::unique_ptr<LoadedPackage> loaded_package{new LoadedPackage()};
//
//    constexpr size_t kMinPackageSize =
//        sizeof(ResTable_package) - sizeof(ResTable_package::typeIdOffset);
//    const ResTable_package* header = chunk.header<ResTable_package, kMinPackageSize>();
//    if (header == nullptr) {
//      LOG(ERROR) << "Chunk RES_TABLE_PACKAGE_TYPE is too small.";
//      return {};
//    }
//
//    loaded_package->package_id_ = dtohl(header->id);
//    if (loaded_package->package_id_ == 0) {
//      // Package ID of 0 means this is a shared library.
//      loaded_package->dynamic_ = true;
//    }
//
//    if (header->header.headerSize >= sizeof(ResTable_package)) {
//      uint32_t type_id_offset = dtohl(header->typeIdOffset);
//      if (type_id_offset > std::numeric_limits<uint8_t>::max()) {
//        LOG(ERROR) << "Type ID offset in RES_TABLE_PACKAGE_TYPE is too large.";
//        return {};
//      }
//      loaded_package->type_id_offset_ = static_cast<int>(type_id_offset);
//    }
//
//    util::ReadUtf16StringFromDevice(header->name, arraysize(header->name),
//                                    &loaded_package->package_name_);
//
//    // A TypeSpec builder. We use this to accumulate the set of Types
//    // available for a TypeSpec, and later build a single, contiguous block
//    // of memory that holds all the Types together with the TypeSpec.
//    std::unique_ptr<TypeSpecPtrBuilder> types_builder;
//
//    // Keep track of the last seen type index. Since type IDs are 1-based,
//    // this records their index, which is 0-based (type ID - 1).
//    uint8_t last_type_idx = 0;
//
//    ChunkIterator iter(chunk.data_ptr(), chunk.data_size());
//    while (iter.HasNext()) {
//      const Chunk child_chunk = iter.Next();
//      switch (child_chunk.type()) {
//        case RES_STRING_POOL_TYPE: {
//          const uintptr_t pool_address =
//              reinterpret_cast<uintptr_t>(child_chunk.header<ResChunk_header>());
//          const uintptr_t header_address = reinterpret_cast<uintptr_t>(header);
//          if (pool_address == header_address + dtohl(header->typeStrings)) {
//            // This string pool is the type string pool.
//            status_t err = loaded_package->type_string_pool_.setTo(
//                child_chunk.header<ResStringPool_header>(), child_chunk.size());
//            if (err != NO_ERROR) {
//              LOG(ERROR) << "Corrupt package type string pool.";
//              return {};
//            }
//          } else if (pool_address == header_address + dtohl(header->keyStrings)) {
//            // This string pool is the key string pool.
//            status_t err = loaded_package->key_string_pool_.setTo(
//                child_chunk.header<ResStringPool_header>(), child_chunk.size());
//            if (err != NO_ERROR) {
//              LOG(ERROR) << "Corrupt package key string pool.";
//              return {};
//            }
//          } else {
//            LOG(WARNING) << "Too many string pool chunks found in package.";
//          }
//        } break;
//
//        case RES_TABLE_TYPE_SPEC_TYPE: {
//          ATRACE_NAME("LoadTableTypeSpec");
//
//          // Starting a new TypeSpec, so finish the old one if there was one.
//          if (types_builder) {
//            TypeSpecPtr type_spec_ptr = types_builder->Build();
//            if (type_spec_ptr == nullptr) {
//              LOG(ERROR) << "Too many type configurations, overflow detected.";
//              return {};
//            }
//            loaded_package->type_specs_.editItemAt(last_type_idx) = std::move(type_spec_ptr);
//
//            types_builder = {};
//            last_type_idx = 0;
//          }
//
//          const ResTable_typeSpec* type_spec = child_chunk.header<ResTable_typeSpec>();
//          if (type_spec == nullptr) {
//            LOG(ERROR) << "Chunk RES_TABLE_TYPE_SPEC_TYPE is too small.";
//            return {};
//          }
//
//          if (type_spec->id == 0) {
//            LOG(ERROR) << "Chunk RES_TABLE_TYPE_SPEC_TYPE has invalid ID 0.";
//            return {};
//          }
//
//          if (loaded_package->type_id_offset_ + static_cast<int>(type_spec->id) >
//              std::numeric_limits<uint8_t>::max()) {
//            LOG(ERROR) << "Chunk RES_TABLE_TYPE_SPEC_TYPE has out of range ID.";
//            return {};
//          }
//
//          // The data portion of this chunk contains entry_count 32bit entries,
//          // each one representing a set of flags.
//          // Here we only validate that the chunk is well formed.
//          const size_t entry_count = dtohl(type_spec->entryCount);
//
//          // There can only be 2^16 entries in a type, because that is the ID
//          // space for entries (EEEE) in the resource ID 0xPPTTEEEE.
//          if (entry_count > std::numeric_limits<uint16_t>::max()) {
//            LOG(ERROR) << "Too many entries in RES_TABLE_TYPE_SPEC_TYPE: " << entry_count << ".";
//            return {};
//          }
//
//          if (entry_count * sizeof(uint32_t) > chunk.data_size()) {
//            LOG(ERROR) << "Chunk too small to hold entries in RES_TABLE_TYPE_SPEC_TYPE.";
//            return {};
//          }
//
//          last_type_idx = type_spec->id - 1;
//          types_builder = util::make_unique<TypeSpecPtrBuilder>(type_spec);
//        } break;
//
//        case RES_TABLE_TYPE_TYPE: {
//          const ResTable_type* type = child_chunk.header<ResTable_type, kResTableTypeMinSize>();
//          if (type == nullptr) {
//            LOG(ERROR) << "Chunk RES_TABLE_TYPE_TYPE is too small.";
//            return {};
//          }
//
//          if (type->id == 0) {
//            LOG(ERROR) << "Chunk RES_TABLE_TYPE_TYPE has invalid ID 0.";
//            return {};
//          }
//
//          // Type chunks must be preceded by their TypeSpec chunks.
//          if (!types_builder || type->id - 1 != last_type_idx) {
//            LOG(ERROR) << "Found RES_TABLE_TYPE_TYPE chunk without "
//                          "RES_TABLE_TYPE_SPEC_TYPE.";
//            return {};
//          }
//
//          if (!VerifyType(child_chunk)) {
//            return {};
//          }
//
//          types_builder->AddType(type);
//        } break;
//
//        case RES_TABLE_LIBRARY_TYPE: {
//          const ResTable_lib_header* lib = child_chunk.header<ResTable_lib_header>();
//          if (lib == nullptr) {
//            LOG(ERROR) << "Chunk RES_TABLE_LIBRARY_TYPE is too small.";
//            return {};
//          }
//
//          if (child_chunk.data_size() / sizeof(ResTable_lib_entry) < dtohl(lib->count)) {
//            LOG(ERROR) << "Chunk too small to hold entries in RES_TABLE_LIBRARY_TYPE.";
//            return {};
//          }
//
//          loaded_package->dynamic_package_map_.reserve(dtohl(lib->count));
//
//          const ResTable_lib_entry* const entry_begin =
//              reinterpret_cast<const ResTable_lib_entry*>(child_chunk.data_ptr());
//          const ResTable_lib_entry* const entry_end = entry_begin + dtohl(lib->count);
//          for (auto entry_iter = entry_begin; entry_iter != entry_end; ++entry_iter) {
//            String package_name;
//            util::ReadUtf16StringFromDevice(entry_iter->packageName,
//                                            arraysize(entry_iter->packageName), &package_name);
//
//            if (dtohl(entry_iter->packageId) >= std::numeric_limits<uint8_t>::max()) {
//              LOG(ERROR) << base::StringPrintf(
//                  "Package ID %02x in RES_TABLE_LIBRARY_TYPE too large for package '%s'.",
//                  dtohl(entry_iter->packageId), package_name.c_str());
//              return {};
//            }
//
//            loaded_package->dynamic_package_map_.emplace_back(std::move(package_name),
//                                                              dtohl(entry_iter->packageId));
//          }
//
//        } break;
//
//        default:
//          LOG(WARNING) << base::StringPrintf("Unknown chunk type '%02x'.", chunk.type());
//          break;
//      }
//    }
//
//    // Finish the last TypeSpec.
//    if (types_builder) {
//      TypeSpecPtr type_spec_ptr = types_builder->Build();
//      if (type_spec_ptr == nullptr) {
//        LOG(ERROR) << "Too many type configurations, overflow detected.";
//        return {};
//      }
//      loaded_package->type_specs_.editItemAt(last_type_idx) = std::move(type_spec_ptr);
//    }
//
//    if (iter.HadError()) {
//      LOG(ERROR) << iter.GetLastError();
//      return {};
//    }
//    return loaded_package;
//  }
  }

//  boolean LoadedArsc::FindEntry(uint32_t resid, const ResTable_config& config,
//                             LoadedArscEntry* out_entry, ResTable_config* out_selected_config,
//                             uint32_t* out_flags) {
//    ATRACE_CALL();
//    const uint8_t package_id = get_package_id(resid);
//    const uint8_t type_id = get_type_id(resid);
//    const uint16_t entry_id = get_entry_id(resid);
//
//    if (type_id == 0) {
//      LOG(ERROR) << "Invalid ID 0x" << std::hex << resid << std::dec << ".";
//      return false;
//    }
//
//    for (const auto& loaded_package : packages_) {
//      if (loaded_package->package_id_ == package_id) {
//        return loaded_package->FindEntry(type_id - 1, entry_id, config, out_entry,
//                                         out_selected_config, out_flags);
//      }
//    }
//    return false;
//  }

    LoadedPackage GetPackageForId(int resid) {
     byte package_id = get_package_id(resid);
     for (LoadedPackage loaded_package : packages_) {
       if (loaded_package.package_id_ == package_id) {
         return loaded_package;
       }
     }
     return null;
    }


    //
//  static boolean VerifyType(const Chunk& chunk) {
//    ATRACE_CALL();
//    const ResTable_type* header = chunk.header<ResTable_type, kResTableTypeMinSize>();
//
//    const size_t entry_count = dtohl(header->entryCount);
//    if (entry_count > std::numeric_limits<uint16_t>::max()) {
//      LOG(ERROR) << "Too many entries in RES_TABLE_TYPE_TYPE.";
//      return false;
//    }
//
//    // Make sure that there is enough room for the entry offsets.
//    const size_t offsets_offset = chunk.header_size();
//    const size_t entries_offset = dtohl(header->entriesStart);
//    const size_t offsets_length = sizeof(uint32_t) * entry_count;
//
//    if (offsets_offset + offsets_length > entries_offset) {
//      LOG(ERROR) << "Entry offsets overlap actual entry data.";
//      return false;
//    }
//
//    if (entries_offset > chunk.size()) {
//      LOG(ERROR) << "Entry offsets extend beyond chunk.";
//      return false;
//    }
//
//    if (entries_offset & 0x03) {
//      LOG(ERROR) << "Entries start at unaligned address.";
//      return false;
//    }
//
//    // Check each entry offset.
//    const uint32_t* offsets =
//        reinterpret_cast<const uint32_t*>(reinterpret_cast<const uint8_t*>(header) + offsets_offset);
//    for (size_t i = 0; i < entry_count; i++) {
//      uint32_t offset = dtohl(offsets[i]);
//      if (offset != ResTable_type::NO_ENTRY) {
//        // Check that the offset is aligned.
//        if (offset & 0x03) {
//          LOG(ERROR) << "Entry offset at index " << i << " is not 4-byte aligned.";
//          return false;
//        }
//
//        // Check that the offset doesn't overflow.
//        if (offset > std::numeric_limits<uint32_t>::max() - entries_offset) {
//          // Overflow in offset.
//          LOG(ERROR) << "Entry offset at index " << i << " is too large.";
//          return false;
//        }
//
//        offset += entries_offset;
//        if (offset > chunk.size() - sizeof(ResTable_entry)) {
//          LOG(ERROR) << "Entry offset at index " << i << " is too large. No room for ResTable_entry.";
//          return false;
//        }
//
//        const ResTable_entry* entry = reinterpret_cast<const ResTable_entry*>(
//            reinterpret_cast<const uint8_t*>(header) + offset);
//        const size_t entry_size = dtohs(entry->size);
//        if (entry_size < sizeof(*entry)) {
//          LOG(ERROR) << "ResTable_entry size " << entry_size << " is too small.";
//          return false;
//        }
//
//        // Check the declared entrySize.
//        if (entry_size > chunk.size() || offset > chunk.size() - entry_size) {
//          LOG(ERROR) << "ResTable_entry size " << entry_size << " is too large.";
//          return false;
//        }
//
//        // If this is a map entry, then keep validating.
//        if (entry_size >= sizeof(ResTable_map_entry)) {
//          const ResTable_map_entry* map = reinterpret_cast<const ResTable_map_entry*>(entry);
//          const size_t map_entry_count = dtohl(map->count);
//
//          size_t map_entries_start = offset + entry_size;
//          if (map_entries_start & 0x03) {
//            LOG(ERROR) << "Map entries start at unaligned offset.";
//            return false;
//          }
//
//          // Each entry is sizeof(ResTable_map) big.
//          if (map_entry_count > ((chunk.size() - map_entries_start) / sizeof(ResTable_map))) {
//            LOG(ERROR) << "Too many map entries in ResTable_map_entry.";
//            return false;
//          }
//
//          // Great, all the map entries fit!.
//        } else {
//          // There needs to be room for one Res_value struct.
//          if (offset + entry_size > chunk.size() - sizeof(Res_value)) {
//            LOG(ERROR) << "No room for Res_value after ResTable_entry.";
//            return false;
//          }
//
//          const Res_value* value = reinterpret_cast<const Res_value*>(
//              reinterpret_cast<const uint8_t*>(entry) + entry_size);
//          const size_t value_size = dtohs(value->size);
//          if (value_size < sizeof(Res_value)) {
//            LOG(ERROR) << "Res_value is too small.";
//            return false;
//          }
//
//          if (value_size > chunk.size() || offset + entry_size > chunk.size() - value_size) {
//            LOG(ERROR) << "Res_value size is too large.";
//            return false;
//          }
//        }
//      }
//    }
//    return true;
//  }

  //  boolean LoadedArsc::LoadTable(const Chunk& chunk, boolean load_as_shared_library) {
//    ATRACE_CALL();
//    const ResTable_header* header = chunk.header<ResTable_header>();
//    if (header == nullptr) {
//      LOG(ERROR) << "Chunk RES_TABLE_TYPE is too small.";
//      return false;
//    }
//
//    const size_t package_count = dtohl(header->packageCount);
//    size_t packages_seen = 0;
//
//    packages_.reserve(package_count);
//
//    ChunkIterator iter(chunk.data_ptr(), chunk.data_size());
//    while (iter.HasNext()) {
//      const Chunk child_chunk = iter.Next();
//      switch (child_chunk.type()) {
//        case RES_STRING_POOL_TYPE:
//          // Only use the first string pool. Ignore others.
//          if (global_string_pool_.getError() == NO_INIT) {
//            status_t err = global_string_pool_.setTo(child_chunk.header<ResStringPool_header>(),
//                                                     child_chunk.size());
//            if (err != NO_ERROR) {
//              LOG(ERROR) << "Corrupt string pool.";
//              return false;
//            }
//          } else {
//            LOG(WARNING) << "Multiple string pool chunks found in resource table.";
//          }
//          break;
//
//        case RES_TABLE_PACKAGE_TYPE: {
//          if (packages_seen + 1 > package_count) {
//            LOG(ERROR) << "More package chunks were found than the " << package_count
//                       << " declared in the "
//                          "header.";
//            return false;
//          }
//          packages_seen++;
//
//          std::unique_ptr<LoadedPackage> loaded_package = LoadedPackage::Load(child_chunk);
//          if (!loaded_package) {
//            return false;
//          }
//
//          // Mark the package as dynamic if we are forcefully loading the Apk as a shared library.
//          if (loaded_package->package_id_ == kAppPackageId) {
//            loaded_package->dynamic_ = load_as_shared_library;
//          }
//          loaded_package->system_ = system_;
//          packages_.push_back(std::move(loaded_package));
//        } break;
//
//        default:
//          LOG(WARNING) << base::StringPrintf("Unknown chunk type '%02x'.", chunk.type());
//          break;
//      }
//    }
//
//    if (iter.HadError()) {
//      LOG(ERROR) << iter.GetLastError();
//      return false;
//    }
//    return true;
//  }
//
//  std::unique_ptr<const LoadedArsc> LoadedArsc::Load(const void* data, size_t len, boolean system,
//                                                     boolean load_as_shared_library) {
//    ATRACE_CALL();
//
//    // Not using make_unique because the constructor is private.
//    std::unique_ptr<LoadedArsc> loaded_arsc(new LoadedArsc());
//    loaded_arsc->system_ = system;
//
//    ChunkIterator iter(data, len);
//    while (iter.HasNext()) {
//      const Chunk chunk = iter.Next();
//      switch (chunk.type()) {
//        case RES_TABLE_TYPE:
//          if (!loaded_arsc->LoadTable(chunk, load_as_shared_library)) {
//            return {};
//          }
//          break;
//
//        default:
//          LOG(WARNING) << base::StringPrintf("Unknown chunk type '%02x'.", chunk.type());
//          break;
//      }
//    }
//
//    if (iter.HadError()) {
//      LOG(ERROR) << iter.GetLastError();
//      return {};
//    }
//
//    // Need to force a move for mingw32.
//    return std::move(loaded_arsc);
//  }
//
//  }  // namespace android

}
