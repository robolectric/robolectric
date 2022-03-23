package org.robolectric.internal.bytecode;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * {@link java.lang.ClassValue} doesn't exist in Android, so provide a trivial impl.
 *
 * Note that if T contains references to Class, this won't really be weak. That's okay.
 */
abstract class ClassValueMap<T> {
  private final Map<Class<?>, T> map = Collections.synchronizedMap(new WeakHashMap<>());

  protected abstract T computeValue(Class<?> type);

  @SuppressWarnings("AndroidJdkLibsChecker")
  public T get(Class<?> type) {
    return map.computeIfAbsent(type, this::computeValue);
  }

  @VisibleForTesting
  void clear() {
    map.clear();
  }
}
