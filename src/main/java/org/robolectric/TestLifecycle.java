package org.robolectric;

import java.lang.reflect.Method;

public interface TestLifecycle<T> {
  T createApplication(Method method, AndroidManifest appManifest);

  void beforeTest(Method method);

  void prepareTest(Object test);

  void afterTest(Method method);
}