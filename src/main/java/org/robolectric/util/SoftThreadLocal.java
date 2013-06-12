package org.robolectric.util;

import java.lang.ref.SoftReference;

public abstract class SoftThreadLocal<T> {
  private final ThreadLocal<SoftReference<T>> threadLocal = new ThreadLocal<SoftReference<T>>() {
    protected SoftReference<T> initialValue() {
      return new SoftReference<T>(create());
    }
  };

  synchronized public T get() {
    T item = threadLocal.get().get();
    if (item == null) {
      item = create();
      threadLocal.set(new SoftReference<T>(item));
    }
    return item;
  }

  public void set(T item) {
    threadLocal.set(new SoftReference<T>(item));
  }

  abstract protected T create();
}
