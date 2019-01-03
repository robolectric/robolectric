package org.robolectric;

/**
 * Represents the contents of a `uses-sdk` element in an Android manifest file.
 */
public interface UsesSdk {
  /**
   * Returns the minimum Android SDK version that this package expects to be runnable on, as
   * specified in the manifest.
   *
   * @return the minimum SDK version
   */
  int getMinSdkVersion();

  /**
   * Returns the Android SDK version that this package prefers to be run on, as specified in the
   * manifest.
   *
   * Note that this value changes the behavior of some Android code (notably {@link
   * android.content.SharedPreferences}) to emulate old bugs.
   *
   * @return the target SDK version
   */
  int getTargetSdkVersion();

  /**
   * Returns the maximum Android SDK version that this package expects to be runnable on, as
   * specified in the manifest.
   *
   * If no maximum version is specified, `null` may be returned.
   *
   * @return the maximum SDK version, or `null`
   */
  Integer getMaxSdkVersion();
}
