package org.robolectric.pluginapi;

import java.nio.file.Path;
import javax.annotation.Nonnull;

/**
 * Represents a unique build of the Android SDK.
 */
@SuppressWarnings("NewApi")
public abstract class Sdk implements Comparable<Sdk> {

  private final int apiLevel;

  protected Sdk(int apiLevel) {
    this.apiLevel = apiLevel;
  }

  /**
   * Returns the Android API level for this SDK.
   *
   * <p>It must match the version reported by {@code android.os.Build.VERSION.SDK_INT} provided
   * within.
   *
   * @see <a href="https://source.android.com/setup/start/build-numbers">Android build numbers</a>
   */
  public final int getApiLevel() {
    return apiLevel;
  }

  /**
   * Returns the Android Version for this SDK.
   *
   * <p>It should match the version reported by {@code android.os.Build.VERSION.RELEASE} provided
   * within.
   *
   * <p>If this is an expensive operation, the implementation should cache the return value.
   *
   * @see <a href="https://source.android.com/setup/start/build-numbers">Android build numbers</a>
   */
  public abstract String getAndroidVersion();

  /**
   * Returns the Android codename for this SDK.
   *
   * <p>It should match the version reported by {@code android.os.Build.VERSION.CODENAME} provided
   * within.
   *
   * <p>If this is an expensive operation, the implementation should cache the return value.
   */
  public abstract String getAndroidCodeName();

  /**
   * Returns the path to jar for this SDK.
   */
  public abstract Path getJarPath();

  /**
   * Determines if this SDK is supported in the running Robolectric environment.
   *
   * An SDK might be unsupported if e.g. it requires a newer version of the JVM than is currently
   * running.
   *
   * Unsupported SDKs should throw some explanatory exception when {@link #getJarPath()} is invoked.
   *
   * If this is an expensive operation, the implementation should cache the return value.
   */
  public abstract boolean isSupported();

  /**
   * Returns a human-readable message explaining why this SDK isn't supported.
   *
   * If this is an expensive operation, the implementation should cache the return value.
   */
  public abstract String getUnsupportedMessage();

  /**
   * Determines if this SDK is known by its provider.
   *
   * Unknown SDKs can serve as placeholder objects; they should throw some explanatory exception
   * when {@link #getJarPath()} is invoked.
   */
  public boolean isKnown() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Sdk)) {
      return false;
    }
    Sdk sdk = (Sdk) o;
    return apiLevel == sdk.apiLevel;
  }

  @Override
  public int hashCode() {
    return apiLevel;
  }

  @Override
  public String toString() {
    return "SDK " + apiLevel;
  }

  /** Instances of {@link Sdk} are ordered by the API level they implement. */
  @Override
  public int compareTo(@Nonnull Sdk o) {
    return apiLevel - o.apiLevel;
  }

  /**
   * Verify that the SDK is supported.
   *
   * <p>Implementations should throw an exception if SDK is unsupported. They can choose to either
   * throw org.junit.AssumptionViolatedException to just skip execution of tests on the SDK, with a
   * warning, or throw a RuntimeException to fail the test.
   *
   */
  public abstract void verifySupportedSdk(String testClassName);
}
