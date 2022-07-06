package org.robolectric.util;

import java.lang.ref.SoftReference;

/**
 * Soft reference to a {@code java.lang.ThreadLocal}.
 *
 * @param <T> The referent to track.
 */
public abstract class SoftThreadLocal<T> {

  private final ThreadLocal<SoftReference<T>> threadLocal = new ThreadLocal<SoftReference<T>>() {
    @Override protected SoftReference<T> initialValue() {
      return new SoftReference<>(create());
    }
  };

  synchronized public T get() {
    T item = threadLocal.get().get();
    if (item == null) {
      item = create();
      threadLocal.set(new SoftReference<>(item));
    }
    return item;
  }

  public void set(T item) {
    threadLocal.set(new SoftReference<>(item));
  }

  abstract protected T create();
}
