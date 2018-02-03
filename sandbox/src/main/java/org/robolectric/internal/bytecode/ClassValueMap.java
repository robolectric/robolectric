package org.robolectric.internal.bytecode;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * {@link java.lang.ClassValue} doesn't exist in Android, so provide a trivial impl.
 *
 * Note that if T contains references to Class, this won't really be weak. That's okay.
 */
abstract class ClassValueMap<T> {
  private final Map<Class<?>, T> map = new WeakHashMap<>();

  protected abstract T computeValue(Class<?> type);

  @SuppressWarnings("Java8MapApi")
  public synchronized T get(Class<?> type) {
    T t = map.get(type);
    if (t == null) {
      if (!map.containsKey(type)) {
        t = computeValue(type);
        map.put(type, t);
      }
    }
    return t;
  }
}
