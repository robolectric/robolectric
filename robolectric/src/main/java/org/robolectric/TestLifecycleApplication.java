package org.robolectric;

import java.lang.reflect.Method;

public interface TestLifecycleApplication {
  void beforeTest(Method method);

  void prepareTest(Object test);

  void afterTest(Method method);
}