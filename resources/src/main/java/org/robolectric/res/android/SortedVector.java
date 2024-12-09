package org.robolectric.res.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// roughly transliterated from system/core/libutils/include/utils/SortedVector.h and
// system/core/libutils/VectorImpl.cpp
public class SortedVector<T extends Comparable<T>> {

  // internal storage for the data. Re-sorted on insertion
  private final List<T> mStorage;

  SortedVector(int itemSize) {
    mStorage = new ArrayList<>(itemSize);
  }

  SortedVector() {
    mStorage = new ArrayList<>();
  }

  public void add(T info) {
    mStorage.add(info);
    Collections.sort(mStorage, Comparable::compareTo);
  }

  public int size() {
    return mStorage.size();
  }

  public T itemAt(int contIdx) {
    return mStorage.get(contIdx);
  }

  public int indexOf(T tmpInfo) {
    return mStorage.indexOf(tmpInfo);
  }

  public void removeAt(int matchIdx) {
    mStorage.remove(matchIdx);
  }
}
