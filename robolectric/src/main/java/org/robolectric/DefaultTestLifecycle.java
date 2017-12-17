package org.robolectric;

import android.app.Application;
import java.lang.reflect.Method;
import org.robolectric.annotation.Config;
import org.robolectric.internal.DeprecatedMethodMarkerException;
import org.robolectric.manifest.AndroidManifest;

public class DefaultTestLifecycle implements TestLifecycle {

  /**
   * Override this method if you want to provide your own implementation of Application.
   *
   * This method attempts to instantiate an application instance as follows:-
   *
   * 1. If specified loads the application specified in the Config annotation
   * 1. Attempt to load a test application as documented <a href="http://robolectric.blogspot.com/2013/04/the-test-lifecycle-in-20.html">here</a>
   * 1. Use the application as specified in the AndroidManifest.xml
   * 1. Instantiate a standard {@link android.app.Application}
   *
   * @param method The currently-running test method.
   * @param appManifest The application manifest.
   * @param config The configuration annotation from the test if present.
   * @return An instance of the Application class specified by the ApplicationManifest.xml or an instance of
   *         Application if not specified.
   * @deprecated This method is deprecated and will be removed in Robolectric 3.7
   */
  @Deprecated
  @Override public Application createApplication(Method method, AndroidManifest appManifest, Config config) {
    throw new DeprecatedMethodMarkerException();
  }

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
