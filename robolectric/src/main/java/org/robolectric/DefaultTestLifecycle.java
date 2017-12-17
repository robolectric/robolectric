package org.robolectric;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import java.lang.reflect.Method;
import org.robolectric.android.internal.ClassNameResolver;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

public class DefaultTestLifecycle implements TestLifecycle<Application> {

  /**
   * Override this method if you want to provide your own implementation of Application.
   *
   * This method attempts to instantiate an application instance as follows:
   *
   * 1. Loads the application specified in the Config annotation, if present
   * 1. Attempts to load a test application as documented <a href="http://robolectric.blogspot.com/2013/04/the-test-lifecycle-in-20.html">here</a>
   * 1. Uses the application as specified in the `AndroidManifest.xml`
   * 1. Instantiates a standard {@link android.app.Application}
   *
   * @param method the test method that's being run (in the sandbox classloader)
   * @param applicationInfo application info for the package
   * @param config the merged config for this test method
   * @return An instance of the Application class specified by the application info, or an instance of
   *         Application if not specified.
   */
  @Override public Application createApplication(Method method, ApplicationInfo applicationInfo, Config config) {
    Application application = null;
    if (config != null && !Config.Builder.isDefaultApplication(config.application())) {
      if (config.application().getCanonicalName() != null) {
        Class<? extends Application> applicationClass;
        try {
          applicationClass = ClassNameResolver.resolve(null, config.application().getName());
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
        application = ReflectionHelpers.callConstructor((applicationClass));
      }
    } else if (applicationInfo != null && applicationInfo.className != null) {
      Class<? extends Application> applicationClass = null;
      try {
        applicationClass = ClassNameResolver.resolve(applicationInfo.packageName, getTestApplicationName(applicationInfo.className));
      } catch (ClassNotFoundException e) {
        // no problem
      }

      if (applicationClass == null) {
        try {
          applicationClass = ClassNameResolver.resolve(applicationInfo.packageName, applicationInfo.className);
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }

      application = ReflectionHelpers.callConstructor(applicationClass);
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
