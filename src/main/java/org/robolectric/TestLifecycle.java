package org.robolectric;

import java.lang.reflect.Method;

public interface TestLifecycle<T> {
  T createApplication(AndroidManifest appManifest);

  void beforeTest(Method method);

  void prepareTest(Object test);

  void afterTest(Method method);
}