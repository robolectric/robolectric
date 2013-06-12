package org.robolectric;

import java.lang.reflect.Method;

public class TestLifecycleApplicationImpl implements TestLifecycleApplication {
  @Override public void beforeTest(Method method) {
  }

  @Override public void prepareTest(Object test) {
  }

  @Override public void afterTest(Method method) {
  }
}
