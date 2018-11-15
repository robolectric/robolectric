package org.robolectric.res.android;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/include/androidfw/ByteBucketArray.h
/**
 * Stores a sparsely populated array. Has a fixed size of 256
 * (number of entries that a byte can represent).
 */
public abstract class ByteBucketArray<T> {
  public ByteBucketArray(T mDefault) {
    this.mDefault = mDefault;
  }

  final int size() {
    return NUM_BUCKETS * BUCKET_SIZE;
  }

//  inline const T& get(size_t index) const {
//    return (*this)[index];
//  }

  final T get(int index) {
    if (index >= size()) {
      return mDefault;
    }

//    byte bucketIndex = static_cast<byte>(index) >> 4;
    byte bucketIndex = (byte) (index >> 4);
    T[] bucket = (T[]) mBuckets[bucketIndex];
    if (bucket == null) {
      return mDefault;
    }
    T t = bucket[0x0f & ((byte) index)];
    return t == null ? mDefault : t;
  }

  T editItemAt(int index) {
//    ALOG_ASSERT(index < size(), "ByteBucketArray.getOrCreate(index=%u) with size=%u",
//        (uint32_t) index, (uint32_t) size());

//    uint8_t bucketIndex = static_cast<uint8_t>(index) >> 4;
    byte bucketIndex = (byte) (((byte) index) >> 4);
    Object[] bucket = mBuckets[bucketIndex];
    if (bucket == null) {
      bucket = mBuckets[bucketIndex] = new Object[BUCKET_SIZE];
    }
//    return bucket[0x0f & static_cast<uint8_t>(index)];
    T t = (T) bucket[0x0f & ((byte) index)];
    if (t == null) {
      t = newInstance();
      bucket[0x0f & ((byte) index)] = t;
    }
    return t;
  }

  abstract T newInstance();

  boolean set(int index, final T value) {
    if (index >= size()) {
      return false;
    }

//    editItemAt(index) = value;
    byte bucketIndex = (byte) (((byte) index) >> 4);
    Object[] bucket = mBuckets[bucketIndex];
    if (bucket == null) {
      bucket = mBuckets[bucketIndex] = new Object[BUCKET_SIZE];
    }
    bucket[0x0f & ((byte) index)] = value;

    return true;
  }

  private static final int NUM_BUCKETS = 16, BUCKET_SIZE = 16;

  Object[][] mBuckets = new Object[NUM_BUCKETS][];
  T mDefault;

}
