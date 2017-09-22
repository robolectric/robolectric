package org.robolectric;

import android.app.Application;
import java.lang.reflect.Method;
import org.robolectric.android.ApplicationTestUtil;
import org.robolectric.android.internal.ClassNameResolver;
import org.robolectric.annotation.Config;
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
   */
  @Override public Application createApplication(Method method, AndroidManifest appManifest, Config config) {

    Application application = null;
    if (config != null && !Config.Builder.isDefaultApplication(config.application())) {
      if (config.application().getCanonicalName() != null) {
        Class<? extends Application> applicationClass;
        try {
          applicationClass = new ClassNameResolver<Application>(null, config.application().getName()).resolve();
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
        application = ApplicationTestUtil.newApplication(applicationClass);
      }
    } else if (appManifest != null && appManifest.getApplicationName() != null) {
      Class<? extends Application> applicationClass = null;
      try {
        applicationClass = new ClassNameResolver<Application>(appManifest.getPackageName(), getTestApplicationName(appManifest.getApplicationName())).resolve();
      } catch (ClassNotFoundException e) {
        // no problem
      }

      if (applicationClass == null) {
        try {
          applicationClass = new ClassNameResolver<Application>(appManifest.getPackageName(), appManifest.getApplicationName()).resolve();
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }

      application = ApplicationTestUtil.newApplication(applicationClass);
    } else {
      application = new Application();
    }

    return application;
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

  public String getTestApplicationName(String applicationName) {
    int lastDot = applicationName.lastIndexOf('.');
    if (lastDot > -1) {
      return applicationName.substring(0, lastDot) + ".Test" + applicationName.substring(lastDot + 1);
    } else {
      return "Test" + applicationName;
    }
  }
}
