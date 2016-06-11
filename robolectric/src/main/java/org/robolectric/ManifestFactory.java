package org.robolectric;

import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;

/**
 * A factory that detects what build system is in use and provides a ManifestFactory that can
 * create an AndroidManifest for that environment.
 *
 * <p>The following build systems are currently supported:
 * <ul>
 *   <li>Maven</li>
 *   <li>Gradle</li>
 * </ul>
 */
public abstract class ManifestFactory {
  protected static final String DEFAULT_MANIFEST_NAME = "AndroidManifest.xml";

  protected ManifestFactory() {}

  /**
   * Detects what build system is in use and returns the appropriate ManifestFactory implementation.
   * @param config Specification of the SDK version, manifest file, package name, etc.
   */
  public static ManifestFactory newManifestFactory(Config config) {
    if (config.constants() != null && config.constants() != Void.class) {
      return new GradleManifestFactory(config);
    } else {
      return new MavenManifestFactory(config);
    }
  }

  /**
   * @return A new AndroidManifest including the location of libraries, assets, resources, etc.
   */
  public abstract AndroidManifest create();


}
