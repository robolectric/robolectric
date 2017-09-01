package org.robolectric.res.android;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/libs/androidfw/ResourceTypes.cpp
//   and https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/include/androidfw/ResourceTypes.h

import static org.robolectric.res.android.Errors.NAME_NOT_FOUND;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Util.ALOGI;

/**
 * Convenience class for accessing data in a ResStringPool resource.
 */
public class ResStringPool {


  private static boolean kDebugStringPoolNoisy = false;
  
//    public ResStringPool(final Object data, int size, boolean copyData=false);
//
//
//    void setToEmpty();
//    Errors setTo(final Object data, int size, boolean copyData=false);
//
//    Errors getError() final;
//
//    void uninit();
//
//    // Return string entry as UTF16; if the pool is UTF8, the string will
//    // be converted before returning.
  String stringAt(final ResStringPoolRef ref, Ref<Integer> outLen) {
    return stringAt(ref.index, outLen);
  }

  String stringAt(int idx, Ref<Integer> outLen) {
//    if (mError == NO_ERROR && idx < mHeader->stringCount) {
//        const bool isUTF8 = (mHeader->flags&ResStringPool_header::UTF8_FLAG) != 0;
//        const uint32_t off = mEntries[idx]/(isUTF8?sizeof(uint8_t):sizeof(uint16_t));
//      if (off < (mStringPoolSize-1)) {
//        if (!isUTF8) {
//                const uint16_t* strings = (uint16_t*)mStrings;
//                const uint16_t* str = strings+off;
//
//                *u16len = decodeLength(&str);
//          if ((uint32_t)(str+*u16len-strings) < mStringPoolSize) {
//            // Reject malformed (non null-terminated) strings
//            if (str[*u16len] != 0x0000) {
//              ALOGW("Bad string block: string #%d is not null-terminated",
//                  (int)idx);
//              return NULL;
//            }
//            return reinterpret_cast<const char16_t*>(str);
//          } else {
//            ALOGW("Bad string block: string #%d extends to %d, past end at %d\n",
//                (int)idx, (int)(str+*u16len-strings), (int)mStringPoolSize);
//          }
//        } else {
//                const uint8_t* strings = (uint8_t*)mStrings;
//                const uint8_t* u8str = strings+off;
//
//                *u16len = decodeLength(&u8str);
//          size_t u8len = decodeLength(&u8str);
//
//          // encLen must be less than 0x7FFF due to encoding.
//          if ((uint32_t)(u8str+u8len-strings) < mStringPoolSize) {
//            AutoMutex lock(mDecodeLock);
//
//            if (mCache == NULL) {
//#ifndef __ANDROID__
//              if (kDebugStringPoolNoisy) {
//                ALOGI("CREATING STRING CACHE OF %zu bytes",
//                    mHeader->stringCount*sizeof(char16_t**));
//              }
//#else
//              // We do not want to be in this case when actually running Android.
//              ALOGW("CREATING STRING CACHE OF %zu bytes",
//                  static_cast<size_t>(mHeader->stringCount*sizeof(char16_t**)));
//#endif
//                  mCache = (char16_t**)calloc(mHeader->stringCount, sizeof(char16_t**));
//              if (mCache == NULL) {
//                ALOGW("No memory trying to allocate decode cache table of %d bytes\n",
//                    (int)(mHeader->stringCount*sizeof(char16_t**)));
//                return NULL;
//              }
//            }
//
//            if (mCache[idx] != NULL) {
//              return mCache[idx];
//            }
//
//            ssize_t actualLen = utf8_to_utf16_length(u8str, u8len);
//            if (actualLen < 0 || (size_t)actualLen != *u16len) {
//              ALOGW("Bad string block: string #%lld decoded length is not correct "
//                  "%lld vs %llu\n",
//                  (long long)idx, (long long)actualLen, (long long)*u16len);
//              return NULL;
//            }
//
//            // Reject malformed (non null-terminated) strings
//            if (u8str[u8len] != 0x00) {
//              ALOGW("Bad string block: string #%d is not null-terminated",
//                  (int)idx);
//              return NULL;
//            }
//
//            char16_t *u16str = (char16_t *)calloc(*u16len+1, sizeof(char16_t));
//            if (!u16str) {
//              ALOGW("No memory when trying to allocate decode cache for string #%d\n",
//                  (int)idx);
//              return NULL;
//            }
//
//            if (kDebugStringPoolNoisy) {
//              ALOGI("Caching UTF8 string: %s", u8str);
//            }
//            utf8_to_utf16(u8str, u8len, u16str);
//            mCache[idx] = u16str;
//            return u16str;
//          } else {
//            ALOGW("Bad string block: string #%lld extends to %lld, past end at %lld\n",
//                (long long)idx, (long long)(u8str+u8len-strings),
//                (long long)mStringPoolSize);
//          }
//        }
//      } else {
//        ALOGW("Bad string block: string #%d entry is at %d, past end at %d\n",
//            (int)idx, (int)(off*sizeof(uint16_t)),
//            (int)(mStringPoolSize*sizeof(uint16_t)));
//      }
//    }
    return null;
  }

//
//    // Note: returns null if the string pool is not UTF8.
//    final char* string8At(int idx, Ref<Integer> outLen) final;
//
//    // Return string whether the pool is UTF8 or UTF16.  Does not allow you
//    // to distinguish null.
//    final String8 string8ObjectAt(int idx) final;
//
//    final ResStringPool_span* styleAt(final ResStringPool_ref& ref) final;
//    final ResStringPool_span* styleAt(int idx) final;
//

  int indexOfString(String str) {
    if (mError != NO_ERROR) {
      return mError;
    }
    // note: the C++ variant branches here based on whether strings are utf-8 or utf-16
    // luckily in java land we don't care, so logic is simplified


//    if ((mHeader.flags&ResStringPoolHeader.UTF8_FLAG) != 0) {
//      if (kDebugStringPoolNoisy) {
//        ALOGI("indexOfString UTF-8: %s", str);
//      }
//      // The string pool contains UTF 8 strings; we don't want to cause
//      // temporary UTF-16 strings to be created as we search.
//      if ( (mHeader.flags&ResStringPoolHeader.SORTED_FLAG) != 0) {
//        // Do a binary search for the string...  this is a little tricky,
//        // because the strings are sorted with strzcmp16().  So to match
//        // the ordering, we need to convert strings in the pool to UTF-16.
//        // But we don't want to hit the cache, so instead we will have a
//        // local temporary allocation for the conversions.
//        char16_t* convBuffer = (char16_t*)malloc(strLen+4);
//        ssize_t l = 0;
//        ssize_t h = mHeader->stringCount-1;
//        ssize_t mid;
//        while (l <= h) {
//          mid = l + (h - l)/2;
//                const uint8_t* s = (const uint8_t*)string8At(mid, &len);
//          int c;
//          if (s != NULL) {
//            char16_t* end = utf8_to_utf16_n(s, len, convBuffer, strLen+3);
//                    *end = 0;
//            c = strzcmp16(convBuffer, end-convBuffer, str, strLen);
//          } else {
//            c = -1;
//          }
//          if (kDebugStringPoolNoisy) {
//            ALOGI("Looking at %s, cmp=%d, l/mid/h=%d/%d/%d\n",
//                (const char*)s, c, (int)l, (int)mid, (int)h);
//          }
//          if (c == 0) {
//            if (kDebugStringPoolNoisy) {
//              ALOGI("MATCH!");
//            }
//            free(convBuffer);
//            return mid;
//          } else if (c < 0) {
//            l = mid + 1;
//          } else {
//            h = mid - 1;
//          }
//        }
//        free(convBuffer);
//      } else {
//        // It is unusual to get the ID from an unsorted string block...
//        // most often this happens because we want to get IDs for style
//        // span tags; since those always appear at the end of the string
//        // block, start searching at the back.
//        String8 str8(str, strLen);
//            const size_t str8Len = str8.size();
//        for (int i=mHeader->stringCount-1; i>=0; i--) {
//                const char* s = string8At(i, &len);
//          if (kDebugStringPoolNoisy) {
//            ALOGI("Looking at %s, i=%d\n", String8(s).string(), i);
//          }
//          if (s && str8Len == len && memcmp(s, str8.string(), str8Len) == 0) {
//            if (kDebugStringPoolNoisy) {
//              ALOGI("MATCH!");
//            }
//            return i;
//          }
//        }
//      }
//    } else {
      if (kDebugStringPoolNoisy) {
        ALOGI("indexOfString : %s", str);
      }
      Ref<Integer> len = new Ref<>(-1);
      if ( (mHeader.flags&ResStringPoolHeader.SORTED_FLAG) != 0) {
        // Do a binary search for the string...
        int l = 0;
        int h = mHeader.stringCount-1;
        int mid;
        while (l <= h) {
          mid = l + (h - l)/2;
          String s = stringAt(mid, len);
          int c = s != null ? s.compareTo(str) : -1;
          if (kDebugStringPoolNoisy) {
            ALOGI("Looking at %s, cmp=%d, l/mid/h=%d/%d/%d\n",
                s, c, (int)l, (int)mid, (int)h);
          }
          if (c == 0) {
            if (kDebugStringPoolNoisy) {
              ALOGI("MATCH!");
            }
            return mid;
          } else if (c < 0) {
            l = mid + 1;
          } else {
            h = mid - 1;
          }
        }
      } else {
        // It is unusual to get the ID from an unsorted string block...
        // most often this happens because we want to get IDs for style
        // span tags; since those always appear at the end of the string
        // block, start searching at the back.
        for (int i=mHeader.stringCount-1; i>=0; i--) {
          String s = stringAt(i, len);
          if (kDebugStringPoolNoisy) {
            ALOGI("Looking at %s, i=%d\n", s, i);
          }
          if (s != null && str.length() == len.get() && s.compareTo(str) == 0) {
            if (kDebugStringPoolNoisy) {
              ALOGI("MATCH!");
            }
            return i;
          }
        }
      }

    return NAME_NOT_FOUND;
  }
//
//    int size() final;
//    int styleCount() final;
//    int bytes() final;
//
//    boolean isSorted() final;
//    boolean isUTF8() final;
//
    private int                    mError;
//    private Object                       mOwnedData;
    private final ResStringPoolHeader mHeader = new ResStringPoolHeader();
//    private int                      mSize;
//    private mutable Mutex               mDecodeLock;
//    private final int[]             mEntries;
//    private final int[]             mEntryStyles;
//    private final void*                 mStrings;
//    private char16_t mutable**          mCache;
//    private int                    mStringPoolSize;    // number of uint16_t
//    private final int[]             mStyles;
//    private int                    mStylePoolSize;    // number of int
  };