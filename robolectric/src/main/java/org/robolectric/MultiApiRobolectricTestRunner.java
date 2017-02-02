package org.robolectric;

import org.robolectric.annotation.Config;

/**
 * A test runner for Robolectric that will run a test against multiple API versions.
 *
 * @deprecated Use {@link RobolectricTestRunner} instead.
 */
@Deprecated
public class MultiApiRobolectricTestRunner extends RobolectricTestRunner {
  public MultiApiRobolectricTestRunner(Class<?> klass) throws Throwable {
    super(klass);
  }

  @Override
  protected Config buildGlobalConfig() {
    return new Config.Builder(super.buildGlobalConfig())
        .setSdk(Config.ALL_SDKS)
        .build();
  }
}
