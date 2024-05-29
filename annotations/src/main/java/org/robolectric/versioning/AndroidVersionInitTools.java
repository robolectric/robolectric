package org.robolectric.versioning;

import java.io.IOException;
import java.util.Properties;
import java.util.jar.JarFile;
import org.robolectric.versioning.AndroidVersions.AndroidRelease;

/**
 * Utility access method to allow robolectric to instantiate AndroidVersions without cluttering code
 * completion for users of AndroidVersions's embedded Types of one per Android Releases.
 */
public final class AndroidVersionInitTools {

  private AndroidVersionInitTools() {}

  public static AndroidRelease computeReleaseVersion(JarFile jarFile) throws IOException {
    return AndroidVersions.computeReleaseVersion(jarFile);
  }

  public static AndroidRelease computeCurrentSdkFromBuildProps(Properties buildProps) {
    return AndroidVersions.computeCurrentSdkFromBuildProps(buildProps);
  }
}
