package org.robolectric.shadows;

import android.util.LruCache;
import org.robolectric.internal.Implements;

@Implements(value = LruCache.class, callThroughByDefault = true)
public class ShadowLruCache {
}
