package org.robolectric.pluginapi;

import java.util.Collection;

/**
 * A provider of known instances of {@link Sdk}. Implement this interface if you need to provide
 * SDKs in a special way for your environment.
 *
 * This is an extension point for Robolectric; see {@link org.robolectric.pluginapi} for details.
 */
@ExtensionPoint
public interface SdkProvider {

  /**
   * Returns the set of SDKs available to run tests against.
   *
   * It's okay for the implementation to block briefly while building the list; the results will be
   * cached.
   */
  Collection<Sdk> getSdks();
}
