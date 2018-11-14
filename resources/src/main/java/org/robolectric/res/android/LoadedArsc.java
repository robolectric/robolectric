package org.robolectric.res.android;

import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Errors.NO_INIT;
import static org.robolectric.res.android.ResourceTypes.RES_STRING_POOL_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_LIBRARY_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_PACKAGE_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_TYPE_SPEC_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_TABLE_TYPE_TYPE;
import static org.robolectric.res.android.ResourceTypes.kResTableTypeMinSize;
import static org.robolectric.res.android.ResourceUtils.make_resid;
import static org.robolectric.res.android.Util.UNLIKELY;
import static org.robolectric.res.android.Util.dtohl;
import static org.robolectric.res.android.Util.dtohs;
import static org.robolectric.res.android.Util.isTruthy;
import static org.robolectric.res.android.Util.logError;
import static org.robolectric.res.android.Util.logWarning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.robolectric.res.android.Chunk.Iterator;
import org.robolectric.res.android.Idmap.LoadedIdmap;
import org.robolectric.res.android.ResourceTypes.IdmapEntry_header;
import org.robolectric.res.android.ResourceTypes.ResStringPool_header;
import org.robolectric.res.android.ResourceTypes.ResTable_entry;
import org.robolectric.res.android.ResourceTypes.ResTable_header;
import org.robolectric.res.android.ResourceTypes.ResTable_lib_entry;
import org.robolectric.res.android.ResourceTypes.ResTable_lib_header;
import org.robolectric.res.android.ResourceTypes.ResTable_map;
import org.robolectric.res.android.ResourceTypes.ResTable_map_entry;
import org.robolectric.res.android.ResourceTypes.ResTable_package;
import org.robolectric.res.android.ResourceTypes.ResTable_sparseTypeEntry;
import org.robolectric.res.android.ResourceTypes.ResTable_type;
import org.robolectric.res.android.ResourceTypes.ResTable_typeSpec;
import org.robolectric.res.android.ResourceTypes.Res_value;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/include/androidfw/LoadedArsc.h
// and https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/LoadedArsc.cpp
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

    DynamicPackageEntry(String package_name, int package_id) {
      this.package_name = package_name;
      this.package_id = package_id;
    }

    String package_name;
    int package_id = 0;
  }

  // TypeSpec is going to be immediately proceeded by
// an array of Type structs, all in the same block of memory.
  static class TypeSpec {

    public static final int SIZEOF = ResTable_typeSpec.SIZEOF + IdmapEntry_header.SIZEOF;
    
    // Pointer to the mmapped data where flags are kept.
    // Flags denote whether the resource entry is public
    // and under which configurations it varies.
    ResTable_typeSpec type_spec;

    // Pointer to the mmapped data where the IDMAP mappings for this type
    // exist. May be nullptr if no IDMAP exists.
    IdmapEntry_header idmap_entries;

    // The number of types that follow this struct.
    // There is a type for each configuration that entries are defined for.
    int type_count;

    // Trick to easily access a variable number of Type structs
    // proceeding this struct, and to ensure their alignment.
    // ResTable_type* types[0];
    ResTable_type[] types;

    int GetFlagsForEntryIndex(int entry_index) {
      if (entry_index >= dtohl(type_spec.entryCount)) {
        return 0;
      }

      // uint32_t* flags = reinterpret_cast<uint32_t*>(type_spec + 1);
      int[] flags = type_spec.getSpecFlags();
      return flags[entry_index];
    }
  }

  // Returns the string pool where all string resource values
  // (Res_value::dataType == Res_value::TYPE_STRING) are indexed.
  public ResStringPool GetStringPool() {
    return global_string_pool_;
  }

  // Returns a vector of LoadedPackage pointers, representing the packages in this LoadedArsc.
  List<LoadedPackage> GetPackages() {
    return packages_;
  }

  // Returns true if this is a system provided resource.
  boolean IsSystem() {
    return system_;
  }

  //
// private:
//  DISALLOW_COPY_AND_ASSIGN(LoadedArsc);
//
//  LoadedArsc() = default;
//   bool LoadTable(const Chunk& chunk, const LoadedIdmap* loaded_idmap, bool load_as_shared_library);
//
  final ResStringPool global_string_pool_ = new ResStringPool();
  final List<LoadedPackage> packages_ = new ArrayList<>();
  boolean system_ = false;
//};
//
//}  // namespace android
//
//#endif /* LOADEDARSC_H_ */

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

  static final int kAppPackageId = 0x7f;


//  namespace {

  // Builder that helps accumulate Type structs and then create a single
  // contiguous block of memory to store both the TypeSpec struct and
  // the Type structs.
  static class TypeSpecPtrBuilder {
    // public:
    TypeSpecPtrBuilder(ResTable_typeSpec header, IdmapEntry_header idmap_header) {
      this.header_ = header;
      this.idmap_header_ = idmap_header;
    }

    void AddType(ResTable_type type) {
      types_.add(type);
    }

    TypeSpec Build() {
      // Check for overflow.
      // using ElementType = ResTable_type*;
      // if ((std.numeric_limits<size_t>.max() - sizeof(TypeSpec)) / sizeof(ElementType) <
      //     types_.size()) {
      if ((Integer.MAX_VALUE - TypeSpec.SIZEOF) / 4 < types_.size()) {
        return null; // {} ;
      }
      // TypeSpec* type_spec =
      //     (TypeSpec*).malloc(sizeof(TypeSpec) + (types_.size() * sizeof(ElementType)));
      TypeSpec type_spec = new TypeSpec();
      type_spec.types = new ResTable_type[types_.size()];
      type_spec.type_spec = header_;
      type_spec.idmap_entries = idmap_header_;
      type_spec.type_count = types_.size();
      // memcpy(type_spec + 1, types_.data(), types_.size() * sizeof(ElementType));
      for (int i = 0; i < type_spec.types.length; i++) {
        type_spec.types[i] = types_.get(i);
        
      }
      return type_spec;
    }

    // private:
    // DISALLOW_COPY_AND_ASSIGN(TypeSpecPtrBuilder);

    ResTable_typeSpec header_;
    IdmapEntry_header idmap_header_;
    final List<ResTable_type> types_ = new ArrayList<>();
  };

//  }  // namespace

  // Precondition: The header passed in has already been verified, so reading any fields and trusting
// the ResChunk_header is safe.
  static boolean VerifyResTableType(ResTable_type header) {
    if (header.id == 0) {
      logError("RES_TABLE_TYPE_TYPE has invalid ID 0.");
      return false;
    }

    int entry_count = dtohl(header.entryCount);
    // if (entry_count > std.numeric_limits<uint16_t>.max()) {
    if (entry_count > 0xffff) {
      logError("RES_TABLE_TYPE_TYPE has too many entries (" + entry_count + ").");
      return false;
    }

    // Make sure that there is enough room for the entry offsets.
    int offsets_offset = dtohs(header.header.headerSize);
    int entries_offset = dtohl(header.entriesStart);
    int offsets_length = 4 * entry_count;

    if (offsets_offset > entries_offset || entries_offset - offsets_offset < offsets_length) {
      logError("RES_TABLE_TYPE_TYPE entry offsets overlap actual entry data.");
      return false;
    }

    if (entries_offset > dtohl(header.header.size)) {
      logError("RES_TABLE_TYPE_TYPE entry offsets extend beyond chunk.");
      return false;
    }

    if (isTruthy(entries_offset & 0x03)) {
      logError("RES_TABLE_TYPE_TYPE entries start at unaligned address.");
      return false;
    }
    return true;
  }

  static boolean VerifyResTableEntry(ResTable_type type, int entry_offset) {
    // Check that the offset is aligned.
    if (isTruthy(entry_offset & 0x03)) {
      logError("Entry at offset " + entry_offset + " is not 4-byte aligned.");
      return false;
    }

    // Check that the offset doesn't overflow.
    // if (entry_offset > std.numeric_limits<int>.max() - dtohl(type.entriesStart)) {
    if (entry_offset > Integer.MAX_VALUE - dtohl(type.entriesStart)) {
      // Overflow in offset.
      logError("Entry at offset " + entry_offset + " is too large.");
      return false;
    }

    int chunk_size = dtohl(type.header.size);

    entry_offset += dtohl(type.entriesStart);
    if (entry_offset > chunk_size - ResTable_entry.SIZEOF) {
      logError("Entry at offset " + entry_offset
          + " is too large. No room for ResTable_entry.");
      return false;
    }

    // ResTable_entry* entry = reinterpret_cast<ResTable_entry*>(
    //       reinterpret_cast<uint8_t*>(type) + entry_offset);
    ResTable_entry entry = new ResTable_entry(type.myBuf(), type.myOffset() + entry_offset);

    int entry_size = dtohs(entry.size);
    // if (entry_size < sizeof(*entry)) {
    if (entry_size < ResTable_entry.SIZEOF) {
      logError("ResTable_entry size " + entry_size + " at offset " + entry_offset
          + " is too small.");
      return false;
    }

    if (entry_size > chunk_size || entry_offset > chunk_size - entry_size) {
      logError("ResTable_entry size " + entry_size + " at offset " + entry_offset
          + " is too large.");
      return false;
    }

    if (entry_size < ResTable_map_entry.BASE_SIZEOF) {
      // There needs to be room for one Res_value struct.
      if (entry_offset + entry_size > chunk_size - Res_value.SIZEOF) {
        logError("No room for Res_value after ResTable_entry at offset " + entry_offset
            + " for type " + (int) type.id + ".");
        return false;
      }

      // Res_value value =
      //       reinterpret_cast<Res_value*>(reinterpret_cast<uint8_t*>(entry) + entry_size);
      Res_value value =
          new Res_value(entry.myBuf(), entry.myOffset() + ResTable_entry.SIZEOF);
      int value_size = dtohs(value.size);
      if (value_size < Res_value.SIZEOF) {
        logError("Res_value at offset " + entry_offset + " is too small.");
        return false;
      }

      if (value_size > chunk_size || entry_offset + entry_size > chunk_size - value_size) {
        logError("Res_value size " + value_size + " at offset " + entry_offset
            + " is too large.");
        return false;
      }
    } else {
      ResTable_map_entry map = new ResTable_map_entry(entry.myBuf(), entry.myOffset());
      int map_entry_count = dtohl(map.count);
      int map_entries_start = entry_offset + entry_size;
      if (isTruthy(map_entries_start & 0x03)) {
        logError("Map entries at offset " + entry_offset + " start at unaligned offset.");
        return false;
      }

      // Each entry is sizeof(ResTable_map) big.
      if (map_entry_count > ((chunk_size - map_entries_start) / ResTable_map.SIZEOF)) {
        logError("Too many map entries in ResTable_map_entry at offset " + entry_offset + ".");
        return false;
      }
    }
    return true;
  }

  static class LoadedPackage {
    // private:

    // DISALLOW_COPY_AND_ASSIGN(LoadedPackage);

    // LoadedPackage();

    ResStringPool type_string_pool_ = new ResStringPool();
    ResStringPool key_string_pool_ = new ResStringPool();
    String package_name_;
    int package_id_ = -1;
    int type_id_offset_ = 0;
    boolean dynamic_ = false;
    boolean system_ = false;
    boolean overlay_ = false;

    // final ByteBucketArray<TypeSpec> type_specs_ = new ByteBucketArray<TypeSpec>() {
    //   @Override
    //   TypeSpec newInstance() {
    //     return new TypeSpec();
    //   }
    // };
    final Map<Integer, TypeSpec> type_specs_ = new HashMap<>();
    final List<DynamicPackageEntry> dynamic_package_map_ = new ArrayList<>();

    ResTable_entry GetEntry(ResTable_type type_chunk,
        short entry_index) {
      int entry_offset = GetEntryOffset(type_chunk, entry_index);
      if (entry_offset == ResTable_type.NO_ENTRY) {
        return null;
      }
      return GetEntryFromOffset(type_chunk, entry_offset);
    }

    static int GetEntryOffset(ResTable_type type_chunk, int entry_index) {
      // The configuration matches and is better than the previous selection.
      // Find the entry value if it exists for this configuration.
      int entry_count = dtohl(type_chunk.entryCount);
      int offsets_offset = dtohs(type_chunk.header.headerSize);

      // Check if there is the desired entry in this type.

      if (isTruthy(type_chunk.flags & ResTable_type.FLAG_SPARSE)) {
        // This is encoded as a sparse map, so perform a binary search.
        // ResTable_sparseTypeEntry sparse_indices =
        //     reinterpret_cast<ResTable_sparseTypeEntry*>(
        //         reinterpret_cast<uint8_t*>(type_chunk) + offsets_offset);
        // ResTable_sparseTypeEntry* sparse_indices_end = sparse_indices + entry_count;
        // ResTable_sparseTypeEntry* result =
        //     std.lower_bound(sparse_indices, sparse_indices_end, entry_index,
        //         [](ResTable_sparseTypeEntry& entry, short entry_idx) {
        //   return dtohs(entry.idx) < entry_idx;
        // });
        ResTable_sparseTypeEntry result = null;
        for (int i = 0; i < entry_count; i++) {
          ResTable_sparseTypeEntry entry = new ResTable_sparseTypeEntry(type_chunk.myBuf(),
              type_chunk.myOffset() + offsets_offset);
          if (entry.idxOrOffset >= entry_index) {
            result = entry;
            break;
          }
        }

        if (result == null || dtohs(result.idxOrOffset) != entry_index) {
          // No entry found.
          return ResTable_type.NO_ENTRY;
        }

        // Extract the offset from the entry. Each offset must be a multiple of 4 so we store it as
        // the real offset divided by 4.
        // return int{dtohs(result.offset)} * 4u;
        return dtohs(result.idxOrOffset) * 4;
      }

      // This type is encoded as a dense array.
      if (entry_index >= entry_count) {
        // This entry cannot be here.
        return ResTable_type.NO_ENTRY;
      }

      // int* entry_offsets = reinterpret_cast<int*>(
      //     reinterpret_cast<uint8_t*>(type_chunk) + offsets_offset);
      // return dtohl(entry_offsets[entry_index]);
      return dtohl(type_chunk.entryOffset(entry_index));
    }

    static ResTable_entry GetEntryFromOffset(ResTable_type type_chunk,
        int offset) {
      if (UNLIKELY(!VerifyResTableEntry(type_chunk, offset))) {
        return null;
      }
      // return reinterpret_cast<ResTable_entry*>(reinterpret_cast<uint8_t*>(type_chunk) +
      //     offset + dtohl(type_chunk.entriesStart));
      return new ResTable_entry(type_chunk.myBuf(),
          type_chunk.myOffset() + offset + dtohl(type_chunk.entriesStart));
    }

    void CollectConfigurations(boolean exclude_mipmap,
        Set<ResTable_config> out_configs) {
      String kMipMap = "mipmap";
      int type_count = type_specs_.size();
      for (int i = 0; i < type_count; i++) {
        TypeSpec type_spec = type_specs_.get(i);
        if (type_spec != null) {
          if (exclude_mipmap) {
            int type_idx = type_spec.type_spec.id - 1;
            final Ref<Integer> type_name_len = new Ref<>(0);
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

        for (ResTable_type iter : type_spec.types) {
          ResTable_config config = ResTable_config.fromDtoH(iter.config);
          out_configs.add(config);
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
          for (ResTable_type iter : type_spec.types) {
            ResTable_config configuration = ResTable_config.fromDtoH(iter.config);
            if (configuration.locale() != 0) {
              temp_locale = configuration.getBcp47Locale(canonicalize);
              String locale = temp_locale;
              out_locales.add(locale);
            }
          }
        }
      }
    }

    // Finds the entry with the specified type name and entry name. The names are in UTF-16 because
    // the underlying ResStringPool API expects this. For now this is acceptable, but since
    // the default policy in AAPT2 is to build UTF-8 string pools, this needs to change.
    // Returns a partial resource ID, with the package ID left as 0x00. The caller is responsible
    // for patching the correct package ID to the resource ID.
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

      for (ResTable_type iter : type_spec.types) {
        ResTable_type type = iter;
        int entry_count = type.entryCount;

        for (int entry_idx = 0; entry_idx < entry_count; entry_idx++) {
          // const uint32_t* entry_offsets = reinterpret_cast<const uint32_t*>(
          //     reinterpret_cast<const uint8_t*>(type.type) + dtohs(type.type.header.headerSize));
          // ResTable_type entry_offsets = new ResTable_type(type.myBuf(),
          //     type.myOffset() + type.header.headerSize);
          // int offset = dtohl(entry_offsets[entry_idx]);
          int offset = dtohl(type.entryOffset(entry_idx));
          if (offset != ResTable_type.NO_ENTRY) {
            // const ResTable_entry* entry =
            //     reinterpret_cast<const ResTable_entry*>(reinterpret_cast<const uint8_t*>(type.type) +
            //     dtohl(type.type.entriesStart) + offset);
            ResTable_entry entry =
                new ResTable_entry(type.myBuf(), type.myOffset() +
                    dtohl(type.entriesStart) + offset);
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

    static LoadedPackage Load(Chunk chunk,
        LoadedIdmap loaded_idmap,
        boolean system, boolean load_as_shared_library) {
      // ATRACE_NAME("LoadedPackage::Load");
      LoadedPackage loaded_package = new LoadedPackage();

      // typeIdOffset was added at some point, but we still must recognize apps built before this
      // was added.
      // constexpr int kMinPackageSize =
      //     sizeof(ResTable_package) - sizeof(ResTable_package.typeIdOffset);
      final int kMinPackageSize = ResTable_package.SIZEOF - 4;
      // ResTable_package header = chunk.header<ResTable_package, kMinPackageSize>();
      ResTable_package header = chunk.asResTable_package(kMinPackageSize);
      if (header == null) {
        logError("RES_TABLE_PACKAGE_TYPE too small.");
        return emptyBraces();
      }

      loaded_package.system_ = system;

      loaded_package.package_id_ = dtohl(header.id);
      if (loaded_package.package_id_ == 0 ||
          (loaded_package.package_id_ == kAppPackageId && load_as_shared_library)) {
        // Package ID of 0 means this is a shared library.
        loaded_package.dynamic_ = true;
      }

      if (loaded_idmap != null) {
        // This is an overlay and so it needs to pretend to be the target package.
        loaded_package.package_id_ = loaded_idmap.TargetPackageId();
        loaded_package.overlay_ = true;
      }

      if (header.header.headerSize >= ResTable_package.SIZEOF) {
        int type_id_offset = dtohl(header.typeIdOffset);
        // if (type_id_offset > std.numeric_limits<uint8_t>.max()) {
        if (type_id_offset > 255) {
          logError("RES_TABLE_PACKAGE_TYPE type ID offset too large.");
          return emptyBraces();
        }
        loaded_package.type_id_offset_ = type_id_offset;
      }

      loaded_package.package_name_ = Util
          .ReadUtf16StringFromDevice(header.name, header.name.length);

      // A map of TypeSpec builders, each associated with an type index.
      // We use these to accumulate the set of Types available for a TypeSpec, and later build a single,
      // contiguous block of memory that holds all the Types together with the TypeSpec.
      Map<Integer, TypeSpecPtrBuilder> type_builder_map = new HashMap<>();

      Chunk.Iterator iter = new Iterator(chunk.data_ptr(), chunk.data_size());
      while (iter.HasNext()) {
        Chunk child_chunk = iter.Next();
        switch (child_chunk.type()) {
          case RES_STRING_POOL_TYPE: {
            // uintptr_t pool_address =
            //     reinterpret_cast<uintptr_t>(child_chunk.header<ResChunk_header>());
            // uintptr_t header_address = reinterpret_cast<uintptr_t>(header);
            int pool_address =
                child_chunk.myOffset();
            int header_address = header.myOffset();
            if (pool_address == header_address + dtohl(header.typeStrings)) {
              // This string pool is the type string pool.
              int err = loaded_package.type_string_pool_.setTo(
                  child_chunk.myBuf(), child_chunk.myOffset(), child_chunk.size(), false);
              if (err != NO_ERROR) {
                logError("RES_STRING_POOL_TYPE for types corrupt.");
                return emptyBraces();
              }
            } else if (pool_address == header_address + dtohl(header.keyStrings)) {
              // This string pool is the key string pool.
              int err = loaded_package.key_string_pool_.setTo(
                  child_chunk.myBuf(), child_chunk.myOffset(), child_chunk.size(), false);
              if (err != NO_ERROR) {
                logError("RES_STRING_POOL_TYPE for keys corrupt.");
                return emptyBraces();
              }
            } else {
              logWarning("Too many RES_STRING_POOL_TYPEs found in RES_TABLE_PACKAGE_TYPE.");
            }
          } break;

          case RES_TABLE_TYPE_SPEC_TYPE: {
            ResTable_typeSpec type_spec = new ResTable_typeSpec(child_chunk.myBuf(),
                child_chunk.myOffset());
            if (type_spec == null) {
              logError("RES_TABLE_TYPE_SPEC_TYPE too small.");
              return emptyBraces();
            }

            if (type_spec.id == 0) {
              logError("RES_TABLE_TYPE_SPEC_TYPE has invalid ID 0.");
              return emptyBraces();
            }

            // if (loaded_package.type_id_offset_ + static_cast<int>(type_spec.id) >
            //     std.numeric_limits<uint8_t>.max()) {
            if (loaded_package.type_id_offset_ + type_spec.id > 255) {
              logError("RES_TABLE_TYPE_SPEC_TYPE has out of range ID.");
              return emptyBraces();
            }

            // The data portion of this chunk contains entry_count 32bit entries,
            // each one representing a set of flags.
            // Here we only validate that the chunk is well formed.
            int entry_count = dtohl(type_spec.entryCount);

            // There can only be 2^16 entries in a type, because that is the ID
            // space for entries (EEEE) in the resource ID 0xPPTTEEEE.
            // if (entry_count > std.numeric_limits<short>.max()) {
            if (entry_count > 0xffff) {
              logError("RES_TABLE_TYPE_SPEC_TYPE has too many entries (" + entry_count + ").");
              return emptyBraces();
            }

            if (entry_count * 4 /*sizeof(int)*/ > chunk.data_size()) {
              logError("RES_TABLE_TYPE_SPEC_TYPE too small to hold entries.");
              return emptyBraces();
            }

            // If this is an overlay, associate the mapping of this type to the target type
            // from the IDMAP.
            IdmapEntry_header idmap_entry_header = null;
            if (loaded_idmap != null) {
              idmap_entry_header = loaded_idmap.GetEntryMapForType(type_spec.id);
            }

            TypeSpecPtrBuilder builder_ptr = type_builder_map.get(type_spec.id - 1);
            if (builder_ptr == null) {
              // builder_ptr = util.make_unique<TypeSpecPtrBuilder>(type_spec, idmap_entry_header);
              builder_ptr = new TypeSpecPtrBuilder(type_spec, idmap_entry_header);
              type_builder_map.put(type_spec.id - 1, builder_ptr);
            } else {
              logWarning(String.format("RES_TABLE_TYPE_SPEC_TYPE already defined for ID %02x",
                  type_spec.id));
            }
          } break;

          case RES_TABLE_TYPE_TYPE: {
            // ResTable_type type = child_chunk.header<ResTable_type, kResTableTypeMinSize>();
            ResTable_type type = child_chunk.asResTable_type(kResTableTypeMinSize);
            if (type == null) {
              logError("RES_TABLE_TYPE_TYPE too small.");
              return emptyBraces();
            }

            if (!VerifyResTableType(type)) {
              return emptyBraces();
            }

            // Type chunks must be preceded by their TypeSpec chunks.
            TypeSpecPtrBuilder builder_ptr = type_builder_map.get(type.id - 1);
            if (builder_ptr != null) {
              builder_ptr.AddType(type);
            } else {
              logError(String.format(
                  "RES_TABLE_TYPE_TYPE with ID %02x found without preceding RES_TABLE_TYPE_SPEC_TYPE.",
                  type.id));
              return emptyBraces();
            }
          } break;

          case RES_TABLE_LIBRARY_TYPE: {
            ResTable_lib_header lib = child_chunk.asResTable_lib_header();
            if (lib == null) {
              logError("RES_TABLE_LIBRARY_TYPE too small.");
              return emptyBraces();
            }

            if (child_chunk.data_size() / ResTable_lib_entry.SIZEOF < dtohl(lib.count)) {
              logError("RES_TABLE_LIBRARY_TYPE too small to hold entries.");
              return emptyBraces();
            }

            // loaded_package.dynamic_package_map_.reserve(dtohl(lib.count));

            // ResTable_lib_entry entry_begin =
            //     reinterpret_cast<ResTable_lib_entry*>(child_chunk.data_ptr());
            ResTable_lib_entry entry_begin =
                child_chunk.asResTable_lib_entry();
            // ResTable_lib_entry entry_end = entry_begin + dtohl(lib.count);
            // for (auto entry_iter = entry_begin; entry_iter != entry_end; ++entry_iter) {
            for (ResTable_lib_entry entry_iter = entry_begin;
                entry_iter.myOffset() != entry_begin.myOffset() + dtohl(lib.count);
                entry_iter = new ResTable_lib_entry(entry_iter.myBuf(), entry_iter.myOffset() + ResTable_lib_entry.SIZEOF)) {
              String package_name =
                  Util.ReadUtf16StringFromDevice(entry_iter.packageName,
                      entry_iter.packageName.length);
              
              if (dtohl(entry_iter.packageId) >= 255) {
                logError(String.format(
                    "Package ID %02x in RES_TABLE_LIBRARY_TYPE too large for package '%s'.",
                    dtohl(entry_iter.packageId), package_name));
                return emptyBraces();
              }

              // loaded_package.dynamic_package_map_.emplace_back(std.move(package_name),
              //     dtohl(entry_iter.packageId));
              loaded_package.dynamic_package_map_.add(new DynamicPackageEntry(package_name,
                  dtohl(entry_iter.packageId)));
            }

          } break;

          default:
            logWarning(String.format("Unknown chunk type '%02x'.", chunk.type()));
            break;
        }
      }

      if (iter.HadError()) {
        logError(iter.GetLastError());
        if (iter.HadFatalError()) {
          return emptyBraces();
        }
      }

      // Flatten and construct the TypeSpecs.
      for (Entry<Integer, TypeSpecPtrBuilder> entry : type_builder_map.entrySet()) {
        byte type_idx = (byte) entry.getKey().byteValue();
        TypeSpec type_spec_ptr = entry.getValue().Build();
        if (type_spec_ptr == null) {
          logError("Too many type configurations, overflow detected.");
          return emptyBraces();
        }

        // We only add the type to the package if there is no IDMAP, or if the type is
        // overlaying something.
        if (loaded_idmap == null || type_spec_ptr.idmap_entries != null) {
          // If this is an overlay, insert it at the target type ID.
          if (type_spec_ptr.idmap_entries != null) {
            type_idx = (byte) (dtohs(type_spec_ptr.idmap_entries.target_type_id) - 1);
          }
          // loaded_package.type_specs_.editItemAt(type_idx) = std.move(type_spec_ptr);
          loaded_package.type_specs_.put((int) type_idx, type_spec_ptr);
        }
      }

      // return std.move(loaded_package);
      return loaded_package;
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

    // Returns true if this package is from an overlay ApkAssets.
    boolean IsOverlay() {
      return overlay_;
    }

    // Returns the map of package name to package ID used in this LoadedPackage. At runtime, a
    // package could have been assigned a different package ID than what this LoadedPackage was
    // compiled with. AssetManager rewrites the package IDs so that they are compatible at runtime.
    List<DynamicPackageEntry> GetDynamicPackageMap() {
      return dynamic_package_map_;
    }

    // type_idx is TT - 1 from 0xPPTTEEEE.
    TypeSpec GetTypeSpecByTypeIndex(int type_index) {
      // If the type IDs are offset in this package, we need to take that into account when searching
      // for a type.
      return type_specs_.get(type_index - type_id_offset_);
    }

    // template <typename Func>
    interface TypeSpecFunc {
      void apply(TypeSpec spec, byte index);
    }

    void ForEachTypeSpec(TypeSpecFunc f) {
      for (Integer i : type_specs_.keySet()) {
        TypeSpec ptr = type_specs_.get(i);
        if (ptr != null) {
          byte type_id = ptr.type_spec.id;
          if (ptr.idmap_entries != null) {
            type_id = (byte) ptr.idmap_entries.target_type_id;
          }
          f.apply(ptr, (byte) (type_id - 1));
        }
      }
    }

    private static LoadedPackage emptyBraces() {
      return new LoadedPackage();
    }
  }

  // Gets a pointer to the package with the specified package ID, or nullptr if no such package
  // exists.
  LoadedPackage GetPackageById(int package_id) {
    for (LoadedPackage loaded_package : packages_) {
      if (loaded_package.GetPackageId() == package_id) {
        return loaded_package;
      }
    }
    return null;
  }

  boolean LoadTable(Chunk chunk, LoadedIdmap loaded_idmap,
      boolean load_as_shared_library) {
    // ResTable_header header = chunk.header<ResTable_header>();
    ResTable_header header = chunk.asResTable_header();
    if (header == null) {
      logError("RES_TABLE_TYPE too small.");
      return false;
    }

    int package_count = dtohl(header.packageCount);
    int packages_seen = 0;

    // packages_.reserve(package_count);

    Chunk.Iterator iter = new Iterator(chunk.data_ptr(), chunk.data_size());
    while (iter.HasNext()) {
      Chunk child_chunk = iter.Next();
      switch (child_chunk.type()) {
        case RES_STRING_POOL_TYPE:
          // Only use the first string pool. Ignore others.
          if (global_string_pool_.getError() == NO_INIT) {
            ResStringPool_header resStringPool_header = child_chunk.asResStringPool_header();
            int err = global_string_pool_.setTo(resStringPool_header.myBuf(),
                resStringPool_header.myOffset(),
                child_chunk.size(), false);
            if (err != NO_ERROR) {
              logError("RES_STRING_POOL_TYPE corrupt.");
              return false;
            }
          } else {
            logWarning("Multiple RES_STRING_POOL_TYPEs found in RES_TABLE_TYPE.");
          }
          break;

        case RES_TABLE_PACKAGE_TYPE: {
          if (packages_seen + 1 > package_count) {
            logError("More package chunks were found than the " + package_count
                + " declared in the header.");
            return false;
          }
          packages_seen++;

          LoadedPackage loaded_package =
              LoadedPackage.Load(child_chunk, loaded_idmap, system_, load_as_shared_library);
          if (!isTruthy(loaded_package)) {
            return false;
          }
          packages_.add(loaded_package);
        } break;

        default:
          logWarning(String.format("Unknown chunk type '%02x'.", chunk.type()));
          break;
      }
    }

    if (iter.HadError()) {
      logError(iter.GetLastError());
      if (iter.HadFatalError()) {
        return false;
      }
    }
    return true;
  }

  // Read-only view into a resource table. This class validates all data
// when loading, including offsets and lengths.
//class LoadedArsc {
// public:
  // Load a resource table from memory pointed to by `data` of size `len`.
  // The lifetime of `data` must out-live the LoadedArsc returned from this method.
  // If `system` is set to true, the LoadedArsc is considered as a system provided resource.
  // If `load_as_shared_library` is set to true, the application package (0x7f) is treated
  // as a shared library (0x00). When loaded into an AssetManager, the package will be assigned an
  // ID.
  static LoadedArsc Load(StringPiece data,
      LoadedIdmap loaded_idmap /* = null */, boolean system /* = false */,
      boolean load_as_shared_library /* = false */) {
    // ATRACE_NAME("LoadedArsc::LoadTable");

    // Not using make_unique because the constructor is private.
    LoadedArsc loaded_arsc = new LoadedArsc();
    loaded_arsc.system_ = system;

    Chunk.Iterator iter = new Iterator(data, data.size());
    while (iter.HasNext()) {
      Chunk chunk = iter.Next();
      switch (chunk.type()) {
        case RES_TABLE_TYPE:
          if (!loaded_arsc.LoadTable(chunk, loaded_idmap, load_as_shared_library)) {
            return emptyBraces();
          }
          break;

        default:
          logWarning(String.format("Unknown chunk type '%02x'.", chunk.type()));
          break;
      }
    }

    if (iter.HadError()) {
      logError(iter.GetLastError());
      if (iter.HadFatalError()) {
        return emptyBraces();
      }
    }

    // Need to force a move for mingw32.
    // return std.move(loaded_arsc);
    return loaded_arsc;
  }

  // Create an empty LoadedArsc. This is used when an APK has no resources.arsc.
  static LoadedArsc CreateEmpty() {
    return new LoadedArsc();
  }

  // Populates a set of ResTable_config structs, possibly excluding configurations defined for
  // the mipmap type.
  // void CollectConfigurations(boolean exclude_mipmap, Set<ResTable_config> out_configs);

  // Populates a set of strings representing locales.
  // If `canonicalize` is set to true, each locale is transformed into its canonical format
  // before being inserted into the set. This may cause some equivalent locales to de-dupe.
  // void CollectLocales(boolean canonicalize, Set<String> out_locales);

  private static LoadedArsc emptyBraces() {
    return new LoadedArsc();
  }

}
