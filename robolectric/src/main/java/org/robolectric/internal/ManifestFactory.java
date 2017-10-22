package org.robolectric.internal;

import java.util.List;
import java.util.stream.Collectors;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;

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

  /**
   * @deprecated This method should no longer be overridden as of Robolectric 3.5. Instead,
   *             {@link #identify(Config)} should return a fully-specified
   *             {@link ManifestIdentifier}.
   */
  @Deprecated
  default AndroidManifest create(ManifestIdentifier manifestIdentifier) {
    return createLibraryAndroidManifest(manifestIdentifier);
  }

  static AndroidManifest createLibraryAndroidManifest(ManifestIdentifier manifestIdentifier) {
    List<ManifestIdentifier> libraries = manifestIdentifier.getLibraries();
    List<AndroidManifest> libraryManifests = libraries.stream()
        .map(ManifestFactory::createLibraryAndroidManifest)
        .collect(Collectors.toList());

    return new AndroidManifest(manifestIdentifier.getManifestFile(), manifestIdentifier.getResDir(),
        manifestIdentifier.getAssetDir(), libraryManifests, manifestIdentifier.getPackageName());
  }
}
