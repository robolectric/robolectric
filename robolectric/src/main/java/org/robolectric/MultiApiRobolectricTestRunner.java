package org.robolectric;

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
}
