package org.robolectric.res.android;

/**
 * transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/include/androidfw/ResourceTypes.h:541
 * Wrapper class that allows the caller to retrieve a string from a string pool without knowing
 * which string pool to look.
 */
class StringPoolRef {

  private final ResStringPool mPool;
  private int mIndex;

  StringPoolRef(final ResStringPool pool, int index) {
    this.mPool = pool;
    this.mIndex = index;
  }

  final String string() {
    return mPool.stringAt(mIndex);
  }

//  final byte[] string8(Ref<Integer> outLen) {
//    return null; //----------------------------------------------------------------------------------------
//  }
//
//  final char[] string16(Ref<Integer> outLen) {
//    return null; //----------------------------------------------------------------------------------------
//  }
};
