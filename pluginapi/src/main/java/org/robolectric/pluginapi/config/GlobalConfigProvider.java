package org.robolectric.pluginapi.config;

import org.robolectric.annotation.Config;

/** Provides the default config for a test. */
public interface GlobalConfigProvider {
  Config get();
}
