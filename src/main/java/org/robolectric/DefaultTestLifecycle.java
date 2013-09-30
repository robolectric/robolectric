package org.robolectric;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import org.robolectric.internal.ClassNameResolver;
import org.robolectric.res.ActivityData;
import org.robolectric.res.builder.RobolectricPackageManager;

import java.lang.reflect.Method;
import java.util.Map;

public class DefaultTestLifecycle implements TestLifecycle {
  /**
   * Override this method if you want to provide your own implementation of Application.
   * <p/>
   * This method attempts to instantiate an application instance as specified by the AndroidManifest.xml.
   *
   * @param method The currently-running test method.
   * @param appManifest The application manifest.
   * @return An instance of the Application class specified by the ApplicationManifest.xml or an instance of
   *         Application if not specified.
   */
  public Application createApplication(Method method, AndroidManifest appManifest) {
    String applicationName = appManifest == null ? Application.class.getName() : appManifest.getApplicationName();
    Application application;
    if (applicationName != null) {
      Class<? extends Application> applicationClass = null;

      String packageName = appManifest == null ? null : appManifest.getPackageName();
      try {
        applicationClass = new ClassNameResolver<Application>(packageName, getTestApplicationName(applicationName)).resolve();
      } catch (ClassNotFoundException e) {
        // no problem
      }

      if (applicationClass == null) {
        try {
          applicationClass = new ClassNameResolver<Application>(packageName, applicationName).resolve();
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }

      try {
        application = applicationClass.newInstance();
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    } else {
      application = new Application();
    }

    addManifestActivitiesToPackageManager(appManifest, application);

    return application;
  }

  private void addManifestActivitiesToPackageManager(AndroidManifest appManifest, Application application) {
    if (appManifest != null) {
      Map<String,ActivityData> activityDatas = appManifest.getActivityDatas();

      RobolectricPackageManager packageManager = (RobolectricPackageManager) application.getPackageManager();

      for (ActivityData data : activityDatas.values()) {
        String name = data.getName();
        String activityName = name.startsWith(".") ? appManifest.getPackageName() + name : name;
        packageManager.addResolveInfoForIntent(new Intent(activityName), new ResolveInfo());
      }
    }
  }

  /**
   * Called before each test method is run.
   *
   * @param method the test method about to be run
   */
  public void beforeTest(final Method method) {
    if (Robolectric.application instanceof TestLifecycleApplication) {
      ((TestLifecycleApplication) Robolectric.application).beforeTest(method);
    }
  }

  public void prepareTest(final Object test) {
    if (Robolectric.application instanceof TestLifecycleApplication) {
      ((TestLifecycleApplication) Robolectric.application).prepareTest(test);
    }
  }

  /**
   * Called after each test method is run.
   *
   * @param method the test method that just ran.
   */
  public void afterTest(final Method method) {
    if (Robolectric.application instanceof TestLifecycleApplication) {
      ((TestLifecycleApplication) Robolectric.application).afterTest(method);
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
