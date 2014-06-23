package org.robolectric;

import org.robolectric.annotation.Config;

import java.lang.reflect.Method;

public interface TestLifecycle<T> {
  T createApplication(Method method, AndroidManifest appManifest, Config config);

  void beforeTest(Method method);

  void prepareTest(Object test);

  void afterTest(Method method);
}
