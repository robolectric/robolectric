package org.robolectric.res.android;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-8.1.0_r22/libs/androidfw/include/androidfw/ResourceUtils.h
class ResourceUtils {
  // Extracts the package, type, and name from a string of the format: [[package:]type/]name
  // Validation must be performed on each extracted piece.
  // Returns false if there was a syntax error.
//  boolean ExtractResourceName(const StringPiece& str, StringPiece* out_package, StringPiece* out_type,
//                           StringPiece* out_entry);

  static int fix_package_id(int resid, int package_id) {
    return resid | (static_cast<int>(package_id) << 24);
  }

  static byte get_package_id(int resid) {
//    return static_cast<int>((resid >> 24) & 0x000000ff);
    return (byte) (resid >>> 24);
  }

  // The type ID is 1-based, so if the returned value is 0 it is invalid.
  static byte get_type_id(int resid) {
//    return static_cast<int>((resid >> 16) & 0x000000ff);
    return (byte) ((resid & 0x00FF0000) >>> 16);
  }

  static short get_entry_id(int resid) {
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

}
