package org.robolectric.res.android;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/ResourceTypes.cpp
//   and https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/include/androidfw/ResourceTypes.h

import static org.robolectric.res.android.Errors.BAD_TYPE;
import static org.robolectric.res.android.Errors.NAME_NOT_FOUND;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Errors.NO_INIT;
import static org.robolectric.res.android.ResourceString.decodeString;
import static org.robolectric.res.android.ResourceTypes.validate_chunk;
import static org.robolectric.res.android.Util.ALOGI;
import static org.robolectric.res.android.Util.ALOGW;
import static org.robolectric.res.android.Util.SIZEOF_INT;
import static org.robolectric.res.android.Util.isTruthy;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import org.robolectric.res.android.ResourceString.Type;
import org.robolectric.res.android.ResourceTypes.ResChunk_header;
import org.robolectric.res.android.ResourceTypes.ResStringPool_header;
import org.robolectric.res.android.ResourceTypes.ResStringPool_header.Writer;
import org.robolectric.res.android.ResourceTypes.ResStringPool_ref;
import org.robolectric.res.android.ResourceTypes.ResStringPool_span;
import org.robolectric.res.android.ResourceTypes.WithOffset;

/**
 * Convenience class for accessing data in a ResStringPool resource.
 */
@SuppressWarnings("NewApi")
public class ResStringPool {

  private static boolean kDebugStringPoolNoisy = false;

  private final long myNativePtr;

  private int                    mError;

   byte[]                       mOwnedData;
  //private Object mOwnedData;

  private ResStringPool_header mHeader;
  private int                      mSize;
//    private mutable Mutex               mDecodeLock;
//    const uint32_t*             mEntries;
  private IntArray             mEntries;
//    const uint32_t*             mEntryStyles;
    private IntArray             mEntryStyles;
//    const void*                 mStrings;
    private int                 mStrings;
  //private List<String> mStrings;
  //private String[] mCache;
  //private char16_t mutable**          mCache;
    private int                    mStringPoolSize;    // number of uint16_t
//    const uint32_t*             mStyles;
    private int             mStyles;
    private int                    mStylePoolSize;    // number of int

  public ResStringPool() {
    mError = NO_INIT;
    myNativePtr = Registries.NATIVE_STRING_POOLS.register(new WeakReference<>(this));
  }

  @Override
  protected void finalize() throws Throwable {
    Registries.NATIVE_STRING_POOLS.unregister(myNativePtr);
  }

  public long getNativePtr() {
    return myNativePtr;
  }

  public static ResStringPool getNativeObject(long nativeId) {
    return Registries.NATIVE_STRING_POOLS.getNativeObject(nativeId).get();
  }

  static class IntArray extends WithOffset {
    IntArray(ByteBuffer buf, int offset) {
      super(buf, offset);
    }

    int get(int idx) {
      return myBuf().getInt(myOffset() + idx * SIZEOF_INT);
    }
  }

  void setToEmpty()
  {
    uninit();

    ByteBuffer buf = ByteBuffer.allocate(16 * 1024).order(ByteOrder.LITTLE_ENDIAN);
    Writer resStringPoolWriter = new Writer();
    resStringPoolWriter.write(buf);
    mOwnedData = new byte[buf.position()];
    buf.position();
    buf.get(mOwnedData);

    ResStringPool_header header = new ResStringPool_header(buf, 0);
    mSize = 0;
    mEntries = null;
    mStrings = 0;
    mStringPoolSize = 0;
    mEntryStyles = null;
    mStyles = 0;
    mStylePoolSize = 0;
    mHeader = header;
  }

  //  status_t setTo(const void* data, size_t size, bool copyData=false);
  public int setTo(ByteBuffer buf, int offset, int size, boolean copyData) {
    if (!isTruthy(buf) || !isTruthy(size)) {
      return (mError=BAD_TYPE);
    }

    uninit();

    // The chunk must be at least the size of the string pool header.
    if (size < ResStringPool_header.SIZEOF) {
      ALOGW("Bad string block: data size %zu is too small to be a string block", size);
      return (mError=BAD_TYPE);
    }

    // The data is at least as big as a ResChunk_header, so we can safely validate the other
    // header fields.
    // `data + size` is safe because the source of `size` comes from the kernel/filesystem.
    if (validate_chunk(new ResChunk_header(buf, offset), ResStringPool_header.SIZEOF,
        size,
        "ResStringPool_header") != NO_ERROR) {
      ALOGW("Bad string block: malformed block dimensions");
      return (mError=BAD_TYPE);
    }

//    final boolean notDeviceEndian = htods((short) 0xf0) != 0xf0;
//
//    if (copyData || notDeviceEndian) {
//      mOwnedData = data;
//      if (mOwnedData == null) {
//        return (mError=NO_MEMORY);
//      }
////      memcpy(mOwnedData, data, size);
//      data = mOwnedData;
//    }

    // The size has been checked, so it is safe to read the data in the ResStringPool_header
    // data structure.
    mHeader = new ResStringPool_header(buf, offset);

//    if (notDeviceEndian) {
//      ResStringPool_header h = final_cast<ResStringPool_header*>(mHeader);
//      h.header.headerSize = dtohs(mHeader.header.headerSize);
//      h.header.type = dtohs(mHeader.header.type);
//      h.header.size = dtohl(mHeader.header.size);
//      h.stringCount = dtohl(mHeader.stringCount);
//      h.styleCount = dtohl(mHeader.styleCount);
//      h.flags = dtohl(mHeader.flags);
//      h.stringsStart = dtohl(mHeader.stringsStart);
//      h.stylesStart = dtohl(mHeader.stylesStart);
//    }

    if (mHeader.header.headerSize > mHeader.header.size
        || mHeader.header.size > size) {
      ALOGW("Bad string block: header size %d or total size %d is larger than data size %d\n",
          (int)mHeader.header.headerSize, (int)mHeader.header.size, (int)size);
      return (mError=BAD_TYPE);
    }
    mSize = mHeader.header.size;
    mEntries = new IntArray(mHeader.myBuf(), mHeader.myOffset() + mHeader.header.headerSize);

    if (mHeader.stringCount > 0) {
      if ((mHeader.stringCount*4 /*sizeof(uint32_t)*/ < mHeader.stringCount)  // uint32 overflow?
          || (mHeader.header.headerSize+(mHeader.stringCount*4 /*sizeof(uint32_t)*/))
          > size) {
        ALOGW("Bad string block: entry of %d items extends past data size %d\n",
            (int)(mHeader.header.headerSize+(mHeader.stringCount*4/*sizeof(uint32_t)*/)),
            (int)size);
        return (mError=BAD_TYPE);
      }

      int charSize;
      if (isTruthy(mHeader.flags & ResStringPool_header.UTF8_FLAG)) {
        charSize = 1 /*sizeof(uint8_t)*/;
      } else {
        charSize = 2 /*sizeof(uint16_t)*/;
      }

      // There should be at least space for the smallest string
      // (2 bytes length, null terminator).
      if (mHeader.stringsStart >= (mSize - 2 /*sizeof(uint16_t)*/)) {
        ALOGW("Bad string block: string pool starts at %d, after total size %d\n",
            (int)mHeader.stringsStart, (int)mHeader.header.size);
        return (mError=BAD_TYPE);
      }

      mStrings = mHeader.stringsStart;

      if (mHeader.styleCount == 0) {
        mStringPoolSize = (mSize - mHeader.stringsStart) / charSize;
      } else {
        // check invariant: styles starts before end of data
        if (mHeader.stylesStart >= (mSize - 2 /*sizeof(uint16_t)*/)) {
          ALOGW("Bad style block: style block starts at %d past data size of %d\n",
              (int)mHeader.stylesStart, (int)mHeader.header.size);
          return (mError=BAD_TYPE);
        }
        // check invariant: styles follow the strings
        if (mHeader.stylesStart <= mHeader.stringsStart) {
          ALOGW("Bad style block: style block starts at %d, before strings at %d\n",
              (int)mHeader.stylesStart, (int)mHeader.stringsStart);
          return (mError=BAD_TYPE);
        }
        mStringPoolSize =
            (mHeader.stylesStart-mHeader.stringsStart)/charSize;
      }

      // check invariant: stringCount > 0 requires a string pool to exist
      if (mStringPoolSize == 0) {
        ALOGW("Bad string block: stringCount is %d but pool size is 0\n", (int)mHeader.stringCount);
        return (mError=BAD_TYPE);
      }

      //      if (notDeviceEndian) {
      //        int i;
      //        uint32_t* e = final_cast<uint32_t*>(mEntries);
      //        for (i=0; i<mHeader.stringCount; i++) {
      //          e[i] = dtohl(mEntries[i]);
      //        }
      //        if (!(mHeader.flags&ResStringPool_header::UTF8_FLAG)) {
      //                final uint16_t* strings = (final uint16_t*)mStrings;
      //          uint16_t* s = final_cast<uint16_t*>(strings);
      //          for (i=0; i<mStringPoolSize; i++) {
      //            s[i] = dtohs(strings[i]);
      //          }
      //        }
      //      }

      //      if ((mHeader->flags&ResStringPool_header::UTF8_FLAG &&
      //          ((uint8_t*)mStrings)[mStringPoolSize-1] != 0) ||
      //      (!(mHeader->flags&ResStringPool_header::UTF8_FLAG) &&
      //          ((uint16_t*)mStrings)[mStringPoolSize-1] != 0)) {

      if ((isTruthy(mHeader.flags & ResStringPool_header.UTF8_FLAG)
              && (mHeader.getByte(mStrings + mStringPoolSize - 1) != 0))
          || (!isTruthy(mHeader.flags & ResStringPool_header.UTF8_FLAG)
              && (mHeader.getShort(mStrings + mStringPoolSize * 2 - 2) != 0))) {
        ALOGW("Bad string block: last string is not 0-terminated\n");
        return (mError=BAD_TYPE);
      }
    } else {
      mStrings = -1;
      mStringPoolSize = 0;
    }

    if (mHeader.styleCount > 0) {
      mEntryStyles = new IntArray(mEntries.myBuf(), mEntries.myOffset() + mHeader.stringCount * SIZEOF_INT);
      // invariant: integer overflow in calculating mEntryStyles
      if (mEntryStyles.myOffset() < mEntries.myOffset()) {
        ALOGW("Bad string block: integer overflow finding styles\n");
        return (mError=BAD_TYPE);
      }

//      if (((const uint8_t*)mEntryStyles-(const uint8_t*)mHeader) > (int)size) {
      if ((mEntryStyles.myOffset() - mHeader.myOffset()) > (int)size) {
        ALOGW("Bad string block: entry of %d styles extends past data size %d\n",
            (int)(mEntryStyles.myOffset()),
        (int)size);
        return (mError=BAD_TYPE);
      }
      mStyles = mHeader.stylesStart;
      if (mHeader.stylesStart >= mHeader.header.size) {
        ALOGW("Bad string block: style pool starts %d, after total size %d\n",
            (int)mHeader.stylesStart, (int)mHeader.header.size);
        return (mError=BAD_TYPE);
      }
      mStylePoolSize =
          (mHeader.header.size-mHeader.stylesStart) /* / sizeof(uint32_t)*/;

//      if (notDeviceEndian) {
//        size_t i;
//        uint32_t* e = final_cast<uint32_t*>(mEntryStyles);
//        for (i=0; i<mHeader.styleCount; i++) {
//          e[i] = dtohl(mEntryStyles[i]);
//        }
//        uint32_t* s = final_cast<uint32_t*>(mStyles);
//        for (i=0; i<mStylePoolSize; i++) {
//          s[i] = dtohl(mStyles[i]);
//        }
//      }

//        final ResStringPool_span endSpan = {
//          { htodl(ResStringPool_span.END) },
//          htodl(ResStringPool_span.END), htodl(ResStringPool_span.END)
//      };
//      if (memcmp(&mStyles[mStylePoolSize-(sizeof(endSpan)/sizeof(uint32_t))],
//                   &endSpan, sizeof(endSpan)) != 0) {
      ResStringPool_span endSpan = new ResStringPool_span(buf,
          mHeader.myOffset() + mStyles + (mStylePoolSize - ResStringPool_span.SIZEOF /* / 4 */));
      if (!endSpan.isEnd()) {
        ALOGW("Bad string block: last style is not 0xFFFFFFFF-terminated\n");
        return (mError=BAD_TYPE);
      }
    } else {
      mEntryStyles = null;
      mStyles = 0;
      mStylePoolSize = 0;
    }

    return (mError=NO_ERROR);
  }

//  public void setTo(XmlResStringPool xmlStringPool) {
//    this.mHeader = new ResStringPoolHeader();
//    this.mStrings = new ArrayList<>();
//    Collections.addAll(mStrings, xmlStringPool.strings());
//  }

  private int setError(int error) {
    mError = error;
    return mError;
  }

  void uninit() {
    setError(NO_INIT);
    mHeader = null;
  }

  public String stringAt(int idx) {
    if (mError == NO_ERROR && idx < mHeader.stringCount) {
        final boolean isUTF8 = (mHeader.flags&ResStringPool_header.UTF8_FLAG) != 0;
//        const uint32_t off = mEntries[idx]/(isUTF8?sizeof(uint8_t):sizeof(uint16_t));
      ByteBuffer buf = mHeader.myBuf();
      int bufOffset = mHeader.myOffset();
      // const uint32_t off = mEntries[idx]/(isUTF8?sizeof(uint8_t):sizeof(uint16_t));
      final int off = mEntries.get(idx)
            /(isUTF8?1/*sizeof(uint8_t)*/:2/*sizeof(uint16_t)*/);
      if (off < (mStringPoolSize-1)) {
        if (!isUTF8) {
          final int strings = mStrings;
          final int str = strings+off*2;
          return decodeString(buf, bufOffset + str, Type.UTF16);
//          int u16len = decodeLengthUTF16(buf, bufOffset + str);
//          if ((str+u16len*2-strings) < mStringPoolSize) {
//            // Reject malformed (non null-terminated) strings
//            if (buf.getShort(bufOffset + str + u16len*2) != 0x0000) {
//              ALOGW("Bad string block: string #%d is not null-terminated",
//                  (int)idx);
//              return null;
//            }
//            byte[] bytes = new byte[u16len * 2];
//            buf.position(bufOffset + str);
//            buf.get(bytes);
//               // Reject malformed (non null-terminated) strings
//               if (str[encLen] != 0x00) {
//                   ALOGW("Bad string block: string #%d is not null-terminated",
//                         (int)idx);
//                   return NULL;
//               }
//            return new String(bytes, StandardCharsets.UTF_16);
//          } else {
//            ALOGW("Bad string block: string #%d extends to %d, past end at %d\n",
//                (int)idx, (int)(str+u16len-strings), (int)mStringPoolSize);
//          }
        } else {
          final int strings = mStrings;
          final int u8str = strings+off;
          return decodeString(buf, bufOffset + u8str, Type.UTF8);

//                *u16len = decodeLength(&u8str);
//          size_t u8len = decodeLength(&u8str);
//
//          // encLen must be less than 0x7FFF due to encoding.
//          if ((uint32_t)(u8str+u8len-strings) < mStringPoolSize) {
//            AutoMutex lock(mDecodeLock);
//
//            if (mCache != NULL && mCache[idx] != NULL) {
//              return mCache[idx];
//            }
//
//            // Retrieve the actual length of the utf8 string if the
//            // encoded length was truncated
//            if (stringDecodeAt(idx, u8str, u8len, &u8len) == NULL) {
//                return NULL;
//            }
//
//            // Since AAPT truncated lengths longer than 0x7FFF, check
//            // that the bits that remain after truncation at least match
//            // the bits of the actual length
//            ssize_t actualLen = utf8_to_utf16_length(u8str, u8len);
//            if (actualLen < 0 || ((size_t)actualLen & 0x7FFF) != *u16len) {
//              ALOGW("Bad string block: string #%lld decoded length is not correct "
//                  "%lld vs %llu\n",
//                  (long long)idx, (long long)actualLen, (long long)*u16len);
//              return NULL;
//            }
//
//            utf8_to_utf16(u8str, u8len, u16str, *u16len + 1);
//
//            if (mCache == NULL) {
// #ifndef __ANDROID__
//                if (kDebugStringPoolNoisy) {
//                    ALOGI("CREATING STRING CACHE OF %zu bytes",
//                          mHeader->stringCount*sizeof(char16_t**));
//                }
// #else
//                // We do not want to be in this case when actually running Android.
//                ALOGW("CREATING STRING CACHE OF %zu bytes",
//                        static_cast<size_t>(mHeader->stringCount*sizeof(char16_t**)));
// #endif
//                mCache = (char16_t**)calloc(mHeader->stringCount, sizeof(char16_t*));
//                if (mCache == NULL) {
//                    ALOGW("No memory trying to allocate decode cache table of %d bytes\n",
//                          (int)(mHeader->stringCount*sizeof(char16_t**)));
//                    return NULL;
//                }
//            }
//            *u16len = (size_t) actualLen;
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
//
//            mCache[idx] = u16str;
//            return u16str;
//          } else {
//            ALOGW("Bad string block: string #%lld extends to %lld, past end at %lld\n",
//                (long long)idx, (long long)(u8str+u8len-strings),
//                (long long)mStringPoolSize);
//          }
        }
      } else {
        ALOGW("Bad string block: string #%d entry is at %d, past end at %d\n",
            (int)idx, (int)(off*2/*sizeof(uint16_t)*/),
            (int)(mStringPoolSize*2/*sizeof(uint16_t)*/));
      }
    }
    return null;
  }

  String stringAt(int idx, Ref<Integer> outLen) {
    String s = stringAt(idx);
    if (s != null && outLen != null) {
      outLen.set(s.length());
    }
    return s;
  }

  public String string8At(int id, Ref<Integer> outLen) {
    return stringAt(id, outLen);
  }

  final ResStringPool_span styleAt(final ResStringPool_ref ref) {
    return styleAt(ref.index);
  }

  public final ResStringPool_span styleAt(int idx) {
    if (mError == NO_ERROR && idx < mHeader.styleCount) {
      // const uint32_t off = (mEntryStyles[idx]/sizeof(uint32_t));
      final int off = mEntryStyles.get(idx) / SIZEOF_INT;
      if (off < mStylePoolSize) {
        // return (const ResStringPool_span*)(mStyles+off);
        return new ResStringPool_span(
            mHeader.myBuf(), mHeader.myOffset() + mStyles + off * SIZEOF_INT);
      } else {
        ALOGW("Bad string block: style #%d entry is at %d, past end at %d\n",
            (int)idx, (int)(off*SIZEOF_INT),
            (int)(mStylePoolSize*SIZEOF_INT));
      }
    }
    return null;
  }

  public int indexOfString(String str) {
    if (mError != NO_ERROR) {
      return mError;
    }

    if (kDebugStringPoolNoisy) {
      ALOGI("indexOfString : %s", str);
    }

    if ( (mHeader.flags&ResStringPoolHeader.SORTED_FLAG) != 0) {
      // Do a binary search for the string...  this is a little tricky,
      // because the strings are sorted with strzcmp16().  So to match
      // the ordering, we need to convert strings in the pool to UTF-16.
      // But we don't want to hit the cache, so instead we will have a
      // local temporary allocation for the conversions.
      int l = 0;
      int h = mHeader.stringCount-1;

      int mid;
      while (l <= h) {
        mid = l + (h - l)/2;
        String s = stringAt(mid);
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
      for (int i = mHeader.stringCount; i>=0; i--) {
        String s = stringAt(i);
        if (kDebugStringPoolNoisy) {
          ALOGI("Looking at %s, i=%d\n", s, i);
        }
        if (Objects.equals(s, str)) {
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
    public int size() {
      return mError == NO_ERROR ? mHeader.stringCount : 0;
    }

    int styleCount() {
      return mError == NO_ERROR ? mHeader.styleCount : 0;
    }

    int bytes() {
      return mError == NO_ERROR ? mHeader.header.size : 0;
    }

  public boolean isUTF8() {
    return true;
  }

  public int getError() {
    return mError;
  }

//    int styleCount() final;
//    int bytes() final;
//
//    boolean isSorted() final;
//    boolean isUTF8() final;
//

}
