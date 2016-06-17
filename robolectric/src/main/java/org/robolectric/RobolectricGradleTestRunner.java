package org.robolectric;

import org.junit.runners.model.InitializationError;

/**
 * Test runner customized for running unit tests either through the Gradle CLI or
 * Android Studio. The runner uses the build type and build flavor to compute the
 * resource, asset, and AndroidManifest paths.
 *
 * This test runner requires that you set the 'constants' field on the @Config
 * annotation (or the org.robolectric.Config.properties file) for your tests.
 *
 * @deprecated Please use {@link RobolectricTestRunner directly since this now supports Gradle}
 */
@Deprecated
public class RobolectricGradleTestRunner extends RobolectricTestRunner {
  public RobolectricGradleTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }
}
