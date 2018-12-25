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
  public T get(Class<?> type) {
    if (map.containsKey(type)) {
      return map.get(type);
    }
    T t = computeValue(type);
    synchronized (this) {
      if (!map.containsKey(type)) {
        map.put(type, t);
      } else {
        t = map.get(type);
      }
    }
    return t;
  }
}
