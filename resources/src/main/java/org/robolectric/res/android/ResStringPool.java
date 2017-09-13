package org.robolectric.res.android;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/libs/androidfw/ResourceTypes.cpp
//   and https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/include/androidfw/ResourceTypes.h

import static org.robolectric.res.android.Errors.BAD_TYPE;
import static org.robolectric.res.android.Errors.NAME_NOT_FOUND;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Errors.NO_INIT;
import static org.robolectric.res.android.Util.ALOGI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.robolectric.res.android.ResXMLTree.XmlBuffer.XmlResStringPool;
import org.robolectric.util.Strings;

/**
 * Convenience class for accessing data in a ResStringPool resource.
 */
public class ResStringPool {

  private static boolean kDebugStringPoolNoisy = false;

  private int                    mError;

  // void*                       mOwnedData;
  //private Object mOwnedData;

  private ResStringPoolHeader mHeader;
  //private int                      mSize;
//    private mutable Mutex               mDecodeLock;
  //private int[]             mEntries;
//    private final int[]             mEntryStyles;
//    private final void*                 mStrings;
  private List<String> mStrings;
  //private String[] mCache;
  //private char16_t mutable**          mCache;
//    private int                    mStringPoolSize;    // number of uint16_t
//    private final int[]             mStyles;
//    private int                    mStylePoolSize;    // number of int

  public ResStringPool() {
    mError = NO_INIT;
  }

  public ResStringPool(final ResStringPoolHeader header, List<String> strings) {
    setTo(header, strings);
  }

  public int setTo(ResStringPoolHeader header, List<String> strings) {
    if (header == null || strings == null || strings.isEmpty()) {
      return setError(BAD_TYPE);
    }
    this.mHeader = header;
    this.mStrings = strings;
    return setError(NO_ERROR);
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
    mStrings = null;
  }

  public String stringAt(final ResStringPoolRef ref) {
    return stringAt(ref.index);
  }

  public String stringAt(int idx) {
    if (mError == NO_ERROR && idx < mStrings.size()) {
      return mStrings.get(idx);
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

//    final ResStringPool_span* styleAt(final ResStringPool_ref& ref) final;
//    final ResStringPool_span* styleAt(int idx) final;

  public int indexOfString(String str) {
    if (mError != NO_ERROR) {
      return mError;
    }

    if (kDebugStringPoolNoisy) {
      ALOGI("indexOfString : %s", str);
    }

    if ( (mHeader.flags&ResStringPoolHeader.SORTED_FLAG) != 0) {
      // Do a binary search for the string...
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
      for (int i = mStrings.size(); i>=0; i--) {
        String s = stringAt(i);
        if (kDebugStringPoolNoisy) {
          ALOGI("Looking at %s, i=%d\n", s, i);
        }
        if (Strings.equals(s, str)) {
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
      return mStrings.size();
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
