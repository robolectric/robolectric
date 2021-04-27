package org.robolectric;

import java.lang.reflect.Method;

/**
 * The default {@link TestLifecycle} used by Robolectric.
 *
 * <p>Owing to tradeoffs, this class is not guaranteed to work with {@link
 * org.robolectric.annotation.experimental.LazyApplication} enabled on tests where the application
 * is inferred from the apk (instead of explicitly specified in AndroidManifest.xml).
 */
public class DefaultTestLifecycle implements TestLifecycle {

  /**
   * Called before each test method is run.
   *
   * @param method the test method about to be run
   */
  @Override
  public void beforeTest(final Method method) {
    if (isTestLifecycleApplicationClass(RuntimeEnvironment.getConfiguredApplicationClass())) {
      ((TestLifecycleApplication) RuntimeEnvironment.getApplication()).beforeTest(method);
    }
  }

  @Override
  public void prepareTest(final Object test) {
    if (isTestLifecycleApplicationClass(RuntimeEnvironment.getConfiguredApplicationClass())) {
      ((TestLifecycleApplication) RuntimeEnvironment.getApplication()).prepareTest(test);
    }
  }

  /**
   * Called after each test method is run.
   *
   * @param method the test method that just ran.
   */
  @Override
  public void afterTest(final Method method) {
    if (isTestLifecycleApplicationClass(RuntimeEnvironment.getConfiguredApplicationClass())) {
      ((TestLifecycleApplication) RuntimeEnvironment.getApplication()).afterTest(method);
    }
  }

  private boolean isTestLifecycleApplicationClass(Class<?> applicationClass) {
    return applicationClass != null
        && TestLifecycleApplication.class.isAssignableFrom(applicationClass);
  }
}
