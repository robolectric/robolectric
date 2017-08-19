package org.robolectric.res.android;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/libs/androidfw/ResourceTypes.cpp
//   and https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/include/androidfw/ResourceTypes.h

/**
 * Convenience class for accessing data in a ResStringPool resource.
 */
public class ResStringPool {
  
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
//    inline final char16_t* stringAt(final ResStringPool_ref& ref, Ref<Integer> outLen) final {
//    return stringAt(ref.index, outLen);
//  }
//    final char16_t* stringAt(int idx, Ref<Integer> outLen) final;
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
//    sint indexOfString(final char16_t* str, int strLen) final;
//
//    int size() final;
//    int styleCount() final;
//    int bytes() final;
//
//    boolean isSorted() final;
//    boolean isUTF8() final;
//
//    private Errors                    mError;
//    private Object                       mOwnedData;
//    private final ResStringPool_header* mHeader;
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