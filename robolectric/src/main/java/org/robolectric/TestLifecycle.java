package org.robolectric;

import java.lang.reflect.Method;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;

public interface TestLifecycle<T> {
  /**
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7
   */
  T createApplication(Method method, AndroidManifest appManifest, Config config);

  /**
   * Called before each test method is run.
   *
   * @param method the test method about to be run
   */
  void beforeTest(Method method);

  /**
   * Called after each test method is run.
   *
   * @param test the instance of the test class that is about to be used
   */
  void prepareTest(Object test);

  /**
   * Called after each test method is run.
   *
   * @param method the test method that was just run
   */
  void afterTest(Method method);
}
