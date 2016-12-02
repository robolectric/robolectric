package org.robolectric;

import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;

import java.lang.reflect.Method;

public interface TestLifecycle<T> {
  // This method should return an instance of {@link android.app.Application} or a subclass.
  // Because of class loading issues, this interface can't statically refer to the class, though.
  T createApplication(Method method, AndroidManifest appManifest, Config config);

  void beforeTest(Method method);

  void prepareTest(Object test);

  void afterTest(Method method);
}
