package org.robolectric.internal;

import org.robolectric.annotation.Config;

/**
 * A factory that detects what build system is in use and provides a ManifestFactory that can
 * create an AndroidManifest for that environment.
 *
 * The following build systems are currently supported:
 *
 * * Maven
 * * Gradle
 * * Buck
 */
public interface ManifestFactory {

  /**
   * Creates a {@link ManifestIdentifier} which represents an Android app, service, or library
   * under test, indicating its manifest file, resources and assets directories, and optionally
   * dependency libraries and an overridden package name.
   *
   * @param config The merged configuration for the running test.
   */
  ManifestIdentifier identify(Config config);

}
