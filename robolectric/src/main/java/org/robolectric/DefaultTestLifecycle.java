package org.robolectric;

import java.lang.reflect.Method;

public class DefaultTestLifecycle implements TestLifecycle {

  /**
   * Called before each test method is run.
   *
   * @param method the test method about to be run
   */
  @Override public void beforeTest(final Method method) {
    if (RuntimeEnvironment.application instanceof TestLifecycleApplication) {
      ((TestLifecycleApplication) RuntimeEnvironment.application).beforeTest(method);
    }
  }

  @Override public void prepareTest(final Object test) {
    if (RuntimeEnvironment.application instanceof TestLifecycleApplication) {
      ((TestLifecycleApplication) RuntimeEnvironment.application).prepareTest(test);
    }
  }

  /**
   * Called after each test method is run.
   *
   * @param method the test method that just ran.
   */
  @Override public void afterTest(final Method method) {
    if (RuntimeEnvironment.application instanceof TestLifecycleApplication) {
      ((TestLifecycleApplication) RuntimeEnvironment.application).afterTest(method);
    }
  }
}
