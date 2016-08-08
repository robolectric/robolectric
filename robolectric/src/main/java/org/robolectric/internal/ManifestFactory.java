package org.robolectric.internal;

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
public interface ManifestFactory {
  String DEFAULT_MANIFEST_NAME = "AndroidManifest.xml";

  ManifestIdentifier identify(Config config);

  AndroidManifest create(ManifestIdentifier manifestIdentifier);
}
