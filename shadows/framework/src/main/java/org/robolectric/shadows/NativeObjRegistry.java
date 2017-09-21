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

  public synchronized long getNativeObjectId(T o) {
    Preconditions.checkNotNull(o);
    Long nativeId  = nativeObjToIdMap.inverse().get(o);
    if (nativeId == null) {
      nativeId = ids;
      nativeObjToIdMap.put(nativeId, o);
      ids++;
    }
    return nativeId;
  }

  public synchronized void unregister(T removed) {
    nativeObjToIdMap.inverse().remove(removed);
  }

  public synchronized T getNativeObject(long nativeId) {
    return nativeObjToIdMap.get(nativeId);
  }

  public synchronized void clear() {
    ids = INITIAL_ID;
    nativeObjToIdMap.clear();
  }

  public synchronized void unregister(long nativeId) {
    T o = nativeObjToIdMap.remove(nativeId);
    if (o == null) {
      throw new IllegalStateException(
          nativeId + " has already been removed (or was never registered)");
    }
  }
}
