package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * A unique id per object registry. Used to emulate android platform behavior of storing a long
 * which represents a pointer to an object.
 */
public class NativeObjRegistry<T> {

  private static final int INITIAL_ID = 1;
  private long ids = INITIAL_ID;
  private BiMap<Long, T> nativeObjToIdMap = HashBiMap.create();

  /**
   * Retrieve the native id for given object. Assigns a new unique id to the object if not
   * previously registered.
   */
  public synchronized long getNativeObjectId(T o) {
    checkNotNull(o);
    Long nativeId = nativeObjToIdMap.inverse().get(o);
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

  /** Retrieve the native object for given id. Throws if object with that id cannot be found */
  public synchronized T getNativeObject(long nativeId) {
    T object = nativeObjToIdMap.get(nativeId);
    return checkNotNull(
        object,
        String.format(
            "Could not find object with nativeId: %d. Currently registered ids: %s",
            nativeId, nativeObjToIdMap.keySet()));
  }

  /**
   * Similar to {@link #getNativeObject(long)} but returns null if object with given id cannot be
   * found.
   */
  public synchronized T peekNativeObject(long nativeId) {
    return nativeObjToIdMap.get(nativeId);
  }

  /** WARNING -- dangerous! Call {@link #unregister(long)} instead! */
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
