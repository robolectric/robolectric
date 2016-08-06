package org.robolectric;

import org.robolectric.annotation.Config;
import org.robolectric.internal.ManifestIdentifier;
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

  public abstract ManifestIdentifier identify(Config config);

  public abstract AndroidManifest create(ManifestIdentifier manifestIdentifier);
}
