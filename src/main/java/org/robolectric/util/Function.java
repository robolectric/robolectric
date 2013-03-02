package org.robolectric.util;

public interface Function<R, V> {
    public R call(V value);
}
