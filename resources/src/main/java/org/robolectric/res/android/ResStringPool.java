package org.robolectric.res.android;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/libs/androidfw/ResourceTypes.cpp
//   and https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/include/androidfw/ResourceTypes.h

import static org.robolectric.res.android.Errors.BAD_TYPE;
import static org.robolectric.res.android.Errors.NAME_NOT_FOUND;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Errors.NO_INIT;
import static org.robolectric.res.android.Util.ALOGI;
import static org.robolectric.res.android.Util.ALOGW;

/**
 * Convenience class for accessing data in a ResStringPool resource.
 */
public class ResStringPool {

  private static boolean kDebugStringPoolNoisy = false;

  private int                    mError;

  // TODO(BC): figure out correct type for this
  // void*                       mOwnedData;
  private Object mOwnedData;

  private ResStringPoolHeader mHeader;
  private int                      mSize;
//    private mutable Mutex               mDecodeLock;
  private int[]             mEntries;
//    private final int[]             mEntryStyles;
//    private final void*                 mStrings;
  private String[] mCache;
  //private char16_t mutable**          mCache;
//    private int                    mStringPoolSize;    // number of uint16_t
//    private final int[]             mStyles;
//    private int                    mStylePoolSize;    // number of int

  public ResStringPool() {
    this(null, 0);
  }

  public ResStringPool(final Object data, int size) {
    this(data, size, false);
  }

  public ResStringPool(final Object data, int size, boolean copyData) {
    mError = NO_INIT;
    mOwnedData = null;
    mHeader = null;
    mCache = null;
//    if (data != null) {
//      setTo(data, size, copyData);
//    }

  }

//
//
//    void setToEmpty();
    int setTo(final Object data, int size, boolean copyData) {
      if (data  == null || size == 0) {
        return setError(BAD_TYPE);
      }

      uninit();

      if (copyData) {
        mOwnedData = data;
      }

      mHeader = (ResStringPoolHeader)data;

//      if (notDeviceEndian) {
//        ResStringPool_header* h = const_cast<ResStringPool_header*>(mHeader);
//        h.header.headerSize = dtohs(mHeader.header.headerSize);
//        h.header.type = dtohs(mHeader.header.type);
//        h.header.size = dtohl(mHeader.header.size);
//        h.stringCount = dtohl(mHeader.stringCount);
//        h.styleCount = dtohl(mHeader.styleCount);
//        h.flags = dtohl(mHeader.flags);
//        h.stringsStart = dtohl(mHeader.stringsStart);
//        h.stylesStart = dtohl(mHeader.stylesStart);
//      }

      if (mHeader.header.headerSize > mHeader.header.size
          || mHeader.header.size > size) {
        ALOGW("Bad string block: header size %d or total size %d is larger than data size %d\n",
            (int)mHeader.header.headerSize, (int)mHeader.header.size, (int)size);
        return setError(BAD_TYPE);
      }
      mSize = mHeader.header.size;
//      mEntries = (const uint32_t*)
//      (((const uint8_t*)data)+mHeader.header.headerSize);
//
//      if (mHeader.stringCount > 0) {
//        if ((mHeader.stringCount*sizeof(uint32_t) < mHeader.stringCount)  // uint32 overflow?
//            || (mHeader.header.headerSize+(mHeader.stringCount*sizeof(uint32_t)))
//            > size) {
//          ALOGW("Bad string block: entry of %d items extends past data size %d\n",
//              (int)(mHeader.header.headerSize+(mHeader.stringCount*sizeof(uint32_t))),
//              (int)size);
//          return (mError=BAD_TYPE);
//        }
//
//        size_t charSize;
//        if (mHeader.flags&ResStringPool_header::UTF8_FLAG) {
//          charSize = sizeof(uint8_t);
//        } else {
//          charSize = sizeof(uint16_t);
//        }
//
//        // There should be at least space for the smallest string
//        // (2 bytes length, null terminator).
//        if (mHeader.stringsStart >= (mSize - sizeof(uint16_t))) {
//          ALOGW("Bad string block: string pool starts at %d, after total size %d\n",
//              (int)mHeader.stringsStart, (int)mHeader.header.size);
//          return (mError=BAD_TYPE);
//        }
//
//        mStrings = (const void*)
//        (((const uint8_t*)data) + mHeader.stringsStart);
//
//        if (mHeader.styleCount == 0) {
//          mStringPoolSize = (mSize - mHeader.stringsStart) / charSize;
//        } else {
//          // check invariant: styles starts before end of data
//          if (mHeader.stylesStart >= (mSize - sizeof(uint16_t))) {
//            ALOGW("Bad style block: style block starts at %d past data size of %d\n",
//                (int)mHeader.stylesStart, (int)mHeader.header.size);
//            return (mError=BAD_TYPE);
//          }
//          // check invariant: styles follow the strings
//          if (mHeader.stylesStart <= mHeader.stringsStart) {
//            ALOGW("Bad style block: style block starts at %d, before strings at %d\n",
//                (int)mHeader.stylesStart, (int)mHeader.stringsStart);
//            return (mError=BAD_TYPE);
//          }
//          mStringPoolSize =
//              (mHeader.stylesStart-mHeader.stringsStart)/charSize;
//        }
//
//        // check invariant: stringCount > 0 requires a string pool to exist
//        if (mStringPoolSize == 0) {
//          ALOGW("Bad string block: stringCount is %d but pool size is 0\n", (int)mHeader.stringCount);
//          return (mError=BAD_TYPE);
//        }
//
//        if (notDeviceEndian) {
//          size_t i;
//          uint32_t* e = const_cast<uint32_t*>(mEntries);
//          for (i=0; i<mHeader.stringCount; i++) {
//            e[i] = dtohl(mEntries[i]);
//          }
//          if (!(mHeader.flags&ResStringPool_header::UTF8_FLAG)) {
//                const uint16_t* strings = (const uint16_t*)mStrings;
//            uint16_t* s = const_cast<uint16_t*>(strings);
//            for (i=0; i<mStringPoolSize; i++) {
//              s[i] = dtohs(strings[i]);
//            }
//          }
//        }
//
//        if ((mHeader.flags&ResStringPool_header::UTF8_FLAG &&
//            ((uint8_t*)mStrings)[mStringPoolSize-1] != 0) ||
//        (!(mHeader.flags&ResStringPool_header::UTF8_FLAG) &&
//            ((uint16_t*)mStrings)[mStringPoolSize-1] != 0)) {
//          ALOGW("Bad string block: last string is not 0-terminated\n");
//          return (mError=BAD_TYPE);
//        }
//      } else {
//        mStrings = null;
//        mStringPoolSize = 0;
//      }
//
//      if (mHeader.styleCount > 0) {
//        mEntryStyles = mEntries + mHeader.stringCount;
//        // invariant: integer overflow in calculating mEntryStyles
//        if (mEntryStyles < mEntries) {
//          ALOGW("Bad string block: integer overflow finding styles\n");
//          return (mError=BAD_TYPE);
//        }
//
//        if (((const uint8_t*)mEntryStyles-(const uint8_t*)mHeader) > (int)size) {
//          ALOGW("Bad string block: entry of %d styles extends past data size %d\n",
//              (int)((const uint8_t*)mEntryStyles-(const uint8_t*)mHeader),
//          (int)size);
//          return (mError=BAD_TYPE);
//        }
//        mStyles = (const uint32_t*)
//        (((const uint8_t*)data)+mHeader.stylesStart);
//        if (mHeader.stylesStart >= mHeader.header.size) {
//          ALOGW("Bad string block: style pool starts %d, after total size %d\n",
//              (int)mHeader.stylesStart, (int)mHeader.header.size);
//          return (mError=BAD_TYPE);
//        }
//        mStylePoolSize =
//            (mHeader.header.size-mHeader.stylesStart)/sizeof(uint32_t);
//
//        if (notDeviceEndian) {
//          size_t i;
//          uint32_t* e = const_cast<uint32_t*>(mEntryStyles);
//          for (i=0; i<mHeader.styleCount; i++) {
//            e[i] = dtohl(mEntryStyles[i]);
//          }
//          uint32_t* s = const_cast<uint32_t*>(mStyles);
//          for (i=0; i<mStylePoolSize; i++) {
//            s[i] = dtohl(mStyles[i]);
//          }
//        }
//
//        const ResStringPool_span endSpan = {
//            { htodl(ResStringPool_span::END) },
//            htodl(ResStringPool_span::END), htodl(ResStringPool_span::END)
//        };
//        if (memcmp(&mStyles[mStylePoolSize-(sizeof(endSpan)/sizeof(uint32_t))],
//                   &endSpan, sizeof(endSpan)) != 0) {
//          ALOGW("Bad string block: last style is not 0xFFFFFFFF-terminated\n");
//          return (mError=BAD_TYPE);
//        }
//      } else {
//        mEntryStyles = null;
//        mStyles = null;
//        mStylePoolSize = 0;
//      }

      return setError(NO_ERROR);

    }

  private int setError(int error) {
    mError = error;
    return mError;
  }

  //
//    Errors getError() final;
//
void uninit()
  {
    mError = NO_INIT;
    if (mHeader != null && mCache != null) {
      for (int x = 0; x < mHeader.stringCount; x++) {
        if (mCache[x] != null) {
          mCache[x] = null;
        }
      }
      mCache = null;
    }
    if (mOwnedData != null) {
      mOwnedData = null;
    }
  }
//
//    // Return string entry as UTF16; if the pool is UTF8, the string will
//    // be converted before returning.
  String stringAt(final ResStringPoolRef ref, Ref<Integer> outLen) {
    return stringAt(ref.index, outLen);
  }

  String stringAt(int idx, Ref<Integer> outLen) {
    return null;
  }
//    if (mError == NO_ERROR && idx < mHeader.stringCount) {
//        final boolean isUTF8 = (mHeader.flags&ResStringPoolHeader.UTF8_FLAG) != 0;
//        final int off = mEntries[idx]/(isUTF8?sizeof(uint8_t):sizeof(uint16_t));
//        if (off < (mStringPoolSize-1)) {
//        if (!isUTF8) {
//                final uint16_t* strings = (uint16_t*)mStrings;
//                final uint16_t* str = strings+off;
//
//                *u16len = decodeLength(&str);
//          if ((uint32_t)(str+*u16len-strings) < mStringPoolSize) {
//            // Reject malformed (non null-terminated) strings
//            if (str[*u16len] != 0x0000) {
//              ALOGW("Bad string block: string #%d is not null-terminated",
//                  (int)idx);
//              return null;
//            }
//            return reinterpret_cast<final char16_t*>(str);
//          } else {
//            ALOGW("Bad string block: string #%d extends to %d, past end at %d\n",
//                (int)idx, (int)(str+*u16len-strings), (int)mStringPoolSize);
//          }
//        } else {
//                final uint8_t* strings = (uint8_t*)mStrings;
//                final uint8_t* u8str = strings+off;
//
//                *u16len = decodeLength(&u8str);
//          size_t u8len = decodeLength(&u8str);
//
//          // encLen must be less than 0x7FFF due to encoding.
//          if ((uint32_t)(u8str+u8len-strings) < mStringPoolSize) {
//            AutoMutex lock(mDecodeLock);
//
//            if (mCache == null) {
//#ifndef __ANDROID__
//              if (kDebugStringPoolNoisy) {
//                ALOGI("CREATING STRING CACHE OF %zu bytes",
//                    mHeader.stringCount*sizeof(char16_t**));
//              }
//#else
//              // We do not want to be in this case when actually running Android.
//              ALOGW("CREATING STRING CACHE OF %zu bytes",
//                  static_cast<size_t>(mHeader.stringCount*sizeof(char16_t**)));
//#endif
//                  mCache = (char16_t**)calloc(mHeader.stringCount, sizeof(char16_t**));
//              if (mCache == null) {
//                ALOGW("No memory trying to allocate decode cache table of %d bytes\n",
//                    (int)(mHeader.stringCount*sizeof(char16_t**)));
//                return null;
//              }
//            }
//
//            if (mCache[idx] != null) {
//              return mCache[idx];
//            }
//
//            ssize_t actualLen = utf8_to_utf16_length(u8str, u8len);
//            if (actualLen < 0 || (size_t)actualLen != *u16len) {
//              ALOGW("Bad string block: string #%lld decoded length is not correct "
//                  "%lld vs %llu\n",
//                  (long long)idx, (long long)actualLen, (long long)*u16len);
//              return null;
//            }
//
//            // Reject malformed (non null-terminated) strings
//            if (u8str[u8len] != 0x00) {
//              ALOGW("Bad string block: string #%d is not null-terminated",
//                  (int)idx);
//              return null;
//            }
//
//            char16_t *u16str = (char16_t *)calloc(*u16len+1, sizeof(char16_t));
//            if (!u16str) {
//              ALOGW("No memory when trying to allocate decode cache for string #%d\n",
//                  (int)idx);
//              return null;
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
//     }
//    }
//    return null;
//  }

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
//        ssize_t h = mHeader.stringCount-1;
//        ssize_t mid;
//        while (l <= h) {
//          mid = l + (h - l)/2;
//                const uint8_t* s = (const uint8_t*)string8At(mid, &len);
//          int c;
//          if (s != null) {
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
//        for (int i=mHeader.stringCount-1; i>=0; i--) {
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

  };