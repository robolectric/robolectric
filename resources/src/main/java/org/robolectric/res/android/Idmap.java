package org.robolectric.res.android;

// transliterated from
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/Idmap.cpp and
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/include/androidfw/Idmap.h

import static org.robolectric.res.android.Util.ATRACE_CALL;
import static org.robolectric.res.android.Util.SIZEOF_CPTR;
import static org.robolectric.res.android.Util.SIZEOF_INT;
import static org.robolectric.res.android.Util.dtohl;
import static org.robolectric.res.android.Util.dtohs;
import static org.robolectric.res.android.Util.logError;

import java.util.HashMap;
import java.util.Map;
import org.robolectric.res.android.ResourceTypes.IdmapEntry_header;
import org.robolectric.res.android.ResourceTypes.Idmap_header;

// #define ATRACE_TAG ATRACE_TAG_RESOURCES
//
// #include "androidfw/Idmap.h"
//
// #include "android-base/logging.h"
// #include "android-base/stringprintf.h"
// #include "utils/ByteOrder.h"
// #include "utils/Trace.h"
//
// #ifdef _WIN32
// #ifdef ERROR
// #undef ERROR
// #endif
// #endif
//
// #include "androidfw/ResourceTypes.h"
//
// using ::android::base::StringPrintf;
//
// namespace android {
class Idmap {

  static boolean is_valid_package_id(short id) {
    return id != 0 && id <= 255;
  }

  static boolean is_valid_type_id(short id) {
    // Type IDs and package IDs have the same constraints in the IDMAP.
    return is_valid_package_id(id);
  }

  // Represents a loaded/parsed IDMAP for a Runtime Resource Overlay (RRO).
  // An RRO and its target APK have different resource IDs assigned to their resources. Overlaying
  // a resource is done by resource name. An IDMAP is a generated mapping between the resource IDs
  // of the RRO and the target APK for each resource with the same name.
  // A LoadedIdmap can be set alongside the overlay's LoadedArsc to allow the overlay ApkAssets to
  // masquerade as the target ApkAssets resources.
  static class LoadedIdmap {
    Idmap_header header_ = null;
    String overlay_apk_path_;
    final Map<Byte, IdmapEntry_header> type_map_ = new HashMap<>();

    public LoadedIdmap(Idmap_header header_) {
      this.header_ = header_;
    }

    // Performs a lookup of the expected entry ID for the given IDMAP entry header.
    // Returns true if the mapping exists and fills `output_entry_id` with the result.
    static boolean Lookup(IdmapEntry_header header, int input_entry_id,
        final Ref<Integer> output_entry_id) {
      if (input_entry_id < dtohs(header.entry_id_offset)) {
        // After applying the offset, the entry is not present.
        return false;
      }

      input_entry_id -= dtohs(header.entry_id_offset);
      if (input_entry_id >= dtohs(header.entry_count)) {
        // The entry is not present.
        return false;
      }

      int result = dtohl(header.entries[input_entry_id]);
      if (result == 0xffffffff) {
        return false;
      }
      output_entry_id.set(result);
      return true;
    }

    static boolean is_word_aligned(int offset) {
      return (offset & 0x03) == 0;
    }

    static boolean IsValidIdmapHeader(StringPiece data) {
      throw new UnsupportedOperationException();
//   if (!is_word_aligned(data.data())) {
//     LOG(ERROR) << "Idmap header is not word aligned.";
//     return false;
//   }
//
//   if (data.size() < sizeof(Idmap_header)) {
//     LOG(ERROR) << "Idmap header is too small.";
//     return false;
//   }
//
//   const Idmap_header* header = reinterpret_cast<const Idmap_header*>(data.data());
//   if (dtohl(header->magic) != kIdmapMagic) {
//     LOG(ERROR) << StringPrintf("Invalid Idmap file: bad magic value (was 0x%08x, expected 0x%08x)",
//                                dtohl(header->magic), kIdmapMagic);
//     return false;
//   }
//
//   if (dtohl(header->version) != kIdmapCurrentVersion) {
//     // We are strict about versions because files with this format are auto-generated and don't need
//     // backwards compatibility.
//     LOG(ERROR) << StringPrintf("Version mismatch in Idmap (was 0x%08x, expected 0x%08x)",
//                                dtohl(header->version), kIdmapCurrentVersion);
//     return false;
//   }
//
//   if (!is_valid_package_id(dtohs(header->target_package_id))) {
//     LOG(ERROR) << StringPrintf("Target package ID in Idmap is invalid: 0x%02x",
//                                dtohs(header->target_package_id));
//     return false;
//   }
//
//   if (dtohs(header->type_count) > 255) {
//     LOG(ERROR) << StringPrintf("Idmap has too many type mappings (was %d, max 255)",
//                                (int)dtohs(header->type_count));
//     return false;
//   }
//   return true;
    }

// LoadedIdmap::LoadedIdmap(const Idmap_header* header) : header_(header) {
//   size_t length = strnlen(reinterpret_cast<const char*>(header_->overlay_path),
//                           arraysize(header_->overlay_path));
//   overlay_apk_path_.assign(reinterpret_cast<const char*>(header_->overlay_path), length);
// }
    // Loads an IDMAP from a chunk of memory. Returns nullptr if the IDMAP data was malformed.
    LoadedIdmap Load(StringPiece idmap_data) {
      ATRACE_CALL();
      if (!IsValidIdmapHeader(idmap_data)) {
        return emptyBraces();
      }

      // Idmap_header header = reinterpret_cast<const Idmap_header*>(idmap_data.data());
      Idmap_header header = idmap_data.asIdmap_header();

      // Can't use make_unique because LoadedImpl constructor is private.
      LoadedIdmap loaded_idmap = new LoadedIdmap(header);

  // const byte* data_ptr = reinterpret_cast<const byte*>(idmap_data.data()) + sizeof(*header);
      StringPiece data_ptr = new StringPiece(idmap_data.myBuf(),
          idmap_data.myOffset() + SIZEOF_CPTR);
      // int data_size = idmap_data.size() - sizeof(*header);
      int data_size = idmap_data.size() - SIZEOF_CPTR;

      int type_maps_encountered = 0;
      while (data_size >= IdmapEntry_header.SIZEOF) {
        if (!is_word_aligned(data_ptr.myOffset())) {
          logError("Type mapping in Idmap is not word aligned");
          return emptyBraces();
        }

        // Validate the type IDs.
    // IdmapEntry_header entry_header = reinterpret_cast<const IdmapEntry_header*>(data_ptr);
        IdmapEntry_header entry_header = new IdmapEntry_header(data_ptr.myBuf(), data_ptr.myOffset());
        if (!is_valid_type_id(dtohs(entry_header.target_type_id)) || !is_valid_type_id(dtohs(entry_header.overlay_type_id))) {
          logError(String.format("Invalid type map (0x%02x -> 0x%02x)",
              dtohs(entry_header.target_type_id),
              dtohs(entry_header.overlay_type_id)));
          return emptyBraces();
        }

        // Make sure there is enough space for the entries declared in the header.
        if ((data_size - SIZEOF_CPTR) / SIZEOF_INT <
            (dtohs(entry_header.entry_count))) {
          logError(String.format("Idmap too small for the number of entries (%d)",
              (int) dtohs(entry_header.entry_count)));
          return emptyBraces();
        }

        // Only add a non-empty overlay.
        if (dtohs(entry_header.entry_count) != 0) {
          // loaded_idmap.type_map_[static_cast<byte>(dtohs(entry_header.overlay_type_id))] =
          //     entry_header;
          loaded_idmap.type_map_.put((byte) dtohs(entry_header.overlay_type_id),
              entry_header);
        }

        // int entry_size_bytes =
        //     sizeof(*entry_header) + (dtohs(entry_header.entry_count) * SIZEOF_INT);
        int entry_size_bytes =
            SIZEOF_CPTR + (dtohs(entry_header.entry_count) * SIZEOF_INT);
        data_ptr = new StringPiece(data_ptr.myBuf(), data_ptr.myOffset() + entry_size_bytes);
        data_size -= entry_size_bytes;
        type_maps_encountered++;
      }

      // Verify that we parsed all the type maps.
      if (type_maps_encountered != dtohs(header.type_count)) {
        logError("Parsed " + type_maps_encountered + " type maps but expected "
            + (int) dtohs(header.type_count));
        return emptyBraces();
      }
      // return std.move(loaded_idmap);
      return loaded_idmap;
    }

    private LoadedIdmap emptyBraces() {
      return new LoadedIdmap(null);
    }

    // Returns the package ID for which this overlay should apply.
    int TargetPackageId() {
      return dtohs(header_.target_package_id);
    }

    // Returns the path to the RRO (Runtime Resource Overlay) APK for which this IDMAP was generated.
    String OverlayApkPath() {
      return overlay_apk_path_;
    }

    // Returns the mapping of target entry ID to overlay entry ID for the given target type.
    IdmapEntry_header GetEntryMapForType(byte type_id) {
      // auto iter = type_map_.find(type_id);
      // if (iter != type_map_.end()) {
      //   return iter.second;
      // }
      // return null;
      return type_map_.get(type_id);
    }
//
// }  // namespace android
  }
}