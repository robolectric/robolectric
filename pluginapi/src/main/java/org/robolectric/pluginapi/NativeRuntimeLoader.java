package org.robolectric.pluginapi;

import com.google.common.annotations.Beta;

/**
 * Loads the Robolectric native runtime.
 *
 * <p>By default, the native runtime shared library is loaded from Java resources. However, in some
 * environments, there may be a faster and simpler way to load it.
 */
@Beta
public interface NativeRuntimeLoader {
  void ensureLoaded();
}
