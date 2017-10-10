package org.robolectric.internal;

import java.util.ArrayList;
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
  ManifestIdentifier identify(Config config);

  default AndroidManifest create(ManifestIdentifier manifestIdentifier) {
    return createLibraryAndroidManifest(manifestIdentifier);
  }

  static AndroidManifest createLibraryAndroidManifest(ManifestIdentifier manifestIdentifier) {
    List<ManifestIdentifier> libraries = manifestIdentifier.getLibraries();
    List<AndroidManifest> libraryManifests = new ArrayList<>();
    if (libraries != null) {
      libraryManifests = libraries.stream()
          .map(ManifestFactory::createLibraryAndroidManifest)
          .collect(Collectors.toList());
    }

    return new AndroidManifest(manifestIdentifier.getManifestFile(), manifestIdentifier.getResDir(),
        manifestIdentifier.getAssetDir(), libraryManifests, manifestIdentifier.getPackageName());
  }
}
