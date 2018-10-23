package org.robolectric.res.android;

import org.robolectric.res.android.ResourceTypes.ResChunk_header;

/**
 * Definition for a pool of strings.  The data of this chunk is an
 * array of uint32_t providing indices into the pool, relative to
 * stringsStart.  At stringsStart are all of the UTF-16 strings
 * concatenated together; each starts with a uint16_t of the string's
 * length and each ends with a 0x0000 terminator.  If a string is >
 * 32767 characters, the high bit of the length is set meaning to take
 * those 15 bits as a high word and it will be followed by another
 * uint16_t containing the low word.
 *
 * If styleCount is not zero, then immediately following the array of
 * uint32_t indices into the string table is another array of indices
 * into a style table starting at stylesStart.  Each entry in the
 * style table is an array of ResStringPool_span structures.
 */
// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/include/androidfw/ResourceTypes.h#434
public class ResStringPoolHeader {
  public static final int SIZEOF = ResChunk_header.SIZEOF + 20;

  ResChunk_header header;
  // Number of strings in this pool (number of uint32_t indices that follow
  // in the data).
  int stringCount;
  // Number of style span arrays in the pool (number of uint32_t indices
  // follow the string indices).
  int styleCount;

  // Flags.

  // If set, the string index is sorted by the string values (based
  // on strcmp16()).
  public static final int SORTED_FLAG = 1<<0;
  // String pool is encoded in UTF-8
  public static final int UTF8_FLAG = 1<<8;
  int flags;

  // Index from header of the string data.
  int stringsStart;
  // Index from header of the style data.
  int stylesStart;
}
