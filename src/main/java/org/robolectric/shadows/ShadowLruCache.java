package org.robolectric.shadows;

import android.util.LruCache;
import org.robolectric.annotation.Implements;

@Implements(value = LruCache.class, callThroughByDefault = true)
public class ShadowLruCache {
}
