package org.robolectric.res.android;

// transliterated from
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/include/androidfw/ResourceUtils.h
// and https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/ResourceUtils.cpp
class ResourceUtils {
  // Extracts the package, type, and name from a string of the format: [[package:]type/]name
  // Validation must be performed on each extracted piece.
  // Returns false if there was a syntax error.
//  boolean ExtractResourceName(String& str, String* out_package, String* out_type,
//                           String* out_entry);

  static int fix_package_id(int resid, int package_id) {
    return (resid & 0x00ffffff) | (package_id << 24);
  }

  static int get_package_id(int resid) {
//    return static_cast<int>((resid >> 24) & 0x000000ff);
    return resid >>> 24;
  }

  // The type ID is 1-based, so if the returned value is 0 it is invalid.
  static int get_type_id(int resid) {
//    return static_cast<int>((resid >> 16) & 0x000000ff);
    return (resid & 0x00FF0000) >>> 16;
  }

  static int get_entry_id(int resid) {
//    return static_cast<uint16_t>(resid & 0x0000ffff);
    return (short) (resid & 0x0000FFFF);
  }

  static boolean is_internal_resid(int resid) {
    return (resid & 0xffff0000) != 0 && (resid & 0x00ff0000) == 0;
  }

  static boolean is_valid_resid(int resid) {
    return (resid & 0x00ff0000) != 0 && (resid & 0xff000000) != 0;
  }

  static int make_resid(byte package_id, byte type_id, short entry_id) {
//    return (static_cast<int>(package_id) << 24) | (static_cast<int>(type_id) << 16) |
//        entry_id;
    return package_id << 24 | type_id << 16 | entry_id;
  }

//   bool ExtractResourceName(const StringPiece& str, StringPiece* out_package, StringPiece* out_type,
//                          StringPiece* out_entry) {
//   *out_package = "";
//   *out_type = "";
//   bool has_package_separator = false;
//   bool has_type_separator = false;
//   const char* start = str.data();
//   const char* end = start + str.size();
//   const char* current = start;
//   while (current != end) {
//     if (out_type->size() == 0 && *current == '/') {
//       has_type_separator = true;
//       out_type->assign(start, current - start);
//       start = current + 1;
//     } else if (out_package->size() == 0 && *current == ':') {
//       has_package_separator = true;
//       out_package->assign(start, current - start);
//       start = current + 1;
//     }
//     current++;
//   }
//   out_entry->assign(start, end - start);
//
//   return !(has_package_separator && out_package->empty()) &&
//          !(has_type_separator && out_type->empty());
// }

  static boolean ExtractResourceName(String str, Ref<String> out_package, Ref<String> out_type,
                           final Ref<String> out_entry) {
    out_package.set("");
    out_type.set("");
    boolean has_package_separator = false;
    boolean has_type_separator = false;
    int start = 0;
    int end = start + str.length();
    int current = start;
    while (current != end) {
      if (out_type.get().length() == 0 && str.charAt(current) == '/') {
        has_type_separator = true;
        out_type.set(str.substring(start, current));
        start = current + 1;
      } else if (out_package.get().length() == 0 && str.charAt(current) == ':') {
        has_package_separator = true;
        out_package.set(str.substring(start, current));
        start = current + 1;
      }
      current++;
    }
    out_entry.set(str.substring(start, end));

    return !(has_package_separator && out_package.get().isEmpty()) &&
           !(has_type_separator && out_type.get().isEmpty());
  }

}
