package org.robolectric;

import android.content.pm.ApplicationInfo;
import java.lang.reflect.Method;
import org.robolectric.annotation.Config;
import org.robolectric.internal.DeprecatedMethodMarkerException;
import org.robolectric.manifest.AndroidManifest;

public interface TestLifecycle<T> {
  /**
   * Implement this method if you want to provide your own implementation of Application.
   *
   * @param method the test method that's being run (in the sandbox classloader)
   * @param applicationInfo application info for the package
   * @param config the merged config for this test method
   * @return the application to use for this test
   */
  T createApplication(Method method, ApplicationInfo applicationInfo, Config config);

  /**
   * Implement this method if you want to provide your own implementation of Application.
   *
   * @param method the test method that's being run (in the sandbox classloader)
   * @param appManifest the test's manifest
   * @param config the merged config for this test method
   * @return the application to use for this test
   * @deprecated Implement {@link #createApplication(Method, AndroidManifest, Config)} instead.
   */
  @Deprecated
  default T createApplication(Method method, AndroidManifest appManifest, Config config) {
    throw new DeprecatedMethodMarkerException();
  }

  void beforeTest(Method method);

  void prepareTest(Object test);

  void afterTest(Method method);

}
