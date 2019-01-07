package org.robolectric.internal;

import java.nio.file.Path;
import javax.annotation.Nonnull;
import org.robolectric.internal.dependency.DependencyJar;

/**
 * Represents a unique build of the Android SDK.
 */
public interface Sdk extends Comparable<Sdk> {

  /**
   * Returns the [Android API level](https://source.android.com/setup/start/build-numbers) for this
   * sdk.
   *
   * It must match the version reported by `android.os.Build.VERSION.SDK_INT` provided within.
   */
  int getApiLevel();

  /**
   * Returns the [Android Version](https://source.android.com/setup/start/build-numbers) for this
   * sdk.
   *
   * It should match the version reported by `android.os.Build.VERSION.RELEASE` provided within.
   */
  String getAndroidVersion();

  /**
   * Returns the Android codename for this SDK.
   *
   * It should match the version reported by `android.os.Build.VERSION.CODENAME` provided within.
   */
  String getAndroidCodeName();

  /**
   * @deprecated Use {@link #getJarPath()} instead.
   */
  @Deprecated
  DependencyJar getAndroidSdkDependency();

  Path getJarPath();

  /**
   * Determines if this `Sdk` is known by its provider.
   *
   * Unknown sdks can serve as placeholder objects; they should throw some explanatory exception
   * when {@link #getJarPath()} is invoked.
   */
  boolean isKnown();

  /**
   * Determines if this `Sdk` is supported in the running Robolectric environment.
   *
   * An sdk might be unsupported if e.g. it requires a newer version of the JVM than is currently
   * running.
   *
   * Unsupported sdks should throw some explanatory exception when {@link #getJarPath()} is invoked.
   */
  boolean isSupported();

  /**
   * Instances of `Sdk` are ordered by the API level they implement.
   */
  @Override
  int compareTo(@Nonnull Sdk o);
}
