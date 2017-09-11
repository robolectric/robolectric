package org.robolectric.shadows;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.assertj.core.util.Preconditions;

/**
 * A unique id per object registry. Used to emulate android platform behavior of storing a long
 * which represents a pointer to an object.
 */
public class NativeObjRegistry<T> {

  private static final int INITIAL_ID = 1;
  private long ids = INITIAL_ID;
  private BiMap<Long, T> nativeObjToIdMap = HashBiMap.create();

  public long getNativeObjectId(T o) {
    Preconditions.checkNotNull(o);
        Long nativeId  = nativeObjToIdMap.inverse().get(o);
    if (nativeId == null) {
      nativeId = ids;
      nativeObjToIdMap.put(nativeId, o);
      ids++;
    }
    return nativeId;
  }

  public void unregister(T removed) {
    nativeObjToIdMap.inverse().remove(removed);
  }

  public T getNativeObject(long nativeId) {
    return nativeObjToIdMap.get(nativeId);
  }

  public void clear() {
    ids = INITIAL_ID;
    nativeObjToIdMap.clear();
  }

  public void unregister(long theme) {
    nativeObjToIdMap.remove(theme);
  }
}
