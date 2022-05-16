package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.ActivityThread.ActivityClientRecord;
import android.app.Application;
import android.app.Instrumentation;
import android.app.ResultInfo;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.IBinder;
import com.android.internal.content.ReferrerIntent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Reflector;

@Implements(value = ActivityThread.class, isInAndroidSdk = false, looseSignatures = true)
public class ShadowActivityThread {
  private static ApplicationInfo applicationInfo;
  @RealObject protected ActivityThread realActivityThread;
  @ReflectorObject protected _ActivityThread_ activityThreadReflector;

  @Implementation
  public static Object getPackageManager() {
    ClassLoader classLoader = ShadowActivityThread.class.getClassLoader();
    Class<?> iPackageManagerClass;
    try {
      iPackageManagerClass = classLoader.loadClass("android.content.pm.IPackageManager");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return Proxy.newProxyInstance(
        classLoader,
        new Class[] {iPackageManagerClass},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, @Nonnull Method method, Object[] args)
              throws Exception {
            if (method.getName().equals("getApplicationInfo")) {
              String packageName = (String) args[0];
              int flags = ((Number) args[1]).intValue();
              if (packageName.equals(ShadowActivityThread.applicationInfo.packageName)) {
                return ShadowActivityThread.applicationInfo;
              }

              try {
                return RuntimeEnvironment.getApplication()
                    .getPackageManager()
                    .getApplicationInfo(packageName, flags);
              } catch (PackageManager.NameNotFoundException e) {
                return null;
              }
            } else if (method.getName().equals("notifyPackageUse")) {
              return null;
            } else if (method.getName().equals("getPackageInstaller")) {
              return null;
            } else if (method.getName().equals("hasSystemFeature")) {
              String featureName = (String) args[0];
              return RuntimeEnvironment.getApplication()
                  .getPackageManager()
                  .hasSystemFeature(featureName);
            }
            throw new UnsupportedOperationException("sorry, not supporting " + method + " yet!");
          }
        });
  }

  @Implementation
  public static Object currentActivityThread() {
    return RuntimeEnvironment.getActivityThread();
  }

  @Implementation
  protected static Application currentApplication() {
    return ((ActivityThread) currentActivityThread()).getApplication();
  }

  @Implementation
  protected Application getApplication() {
    // Prefer the stored application from the real Activity Thread.
    Application currentApplication =
        Reflector.reflector(_ActivityThread_.class, realActivityThread).getInitialApplication();
    if (currentApplication == null) {
      return RuntimeEnvironment.getApplication();
    } else {
      return currentApplication;
    }
  }

  @Implementation(minSdk = R)
  public static Object getPermissionManager() {
    ClassLoader classLoader = ShadowActivityThread.class.getClassLoader();
    Class<?> iPermissionManagerClass;
    try {
      iPermissionManagerClass = classLoader.loadClass("android.permission.IPermissionManager");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return Proxy.newProxyInstance(
        classLoader,
        new Class<?>[] {iPermissionManagerClass},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, @Nonnull Method method, Object[] args)
              throws Exception {
            if (method.getName().equals("getSplitPermissions")) {
              return Collections.emptyList();
            }
            return method.getDefaultValue();
          }
        });
  }

  // Override this method as it's used directly by reflection by androidx ActivityRecreator.
  @Implementation(minSdk = N, maxSdk = O_MR1)
  protected void requestRelaunchActivity(
      IBinder token,
      List<ResultInfo> pendingResults,
      List<ReferrerIntent> pendingNewIntents,
      int configChanges,
      boolean notResumed,
      Configuration config,
      Configuration overrideConfig,
      boolean fromServer,
      boolean preserveWindow) {
    ActivityClientRecord record = activityThreadReflector.getActivities().get(token);
    if (record != null) {
      reflector(ActivityClientRecordReflector.class, record).getActivity().recreate();
    }
  }

  /** Update's ActivityThread's list of active Activities */
  void registerActivityLaunch(
      Intent intent, ActivityInfo activityInfo, Activity activity, IBinder token) {
    ActivityClientRecord record = new ActivityClientRecord();
    ActivityClientRecordReflector recordReflector =
        reflector(ActivityClientRecordReflector.class, record);
    recordReflector.setToken(token);
    recordReflector.setIntent(intent);
    recordReflector.setActivityInfo(activityInfo);
    recordReflector.setActivity(activity);
    reflector(_ActivityThread_.class, realActivityThread).getActivities().put(token, record);
  }

  void removeActivity(IBinder token) {
    reflector(_ActivityThread_.class, realActivityThread).getActivities().remove(token);
  }

  /**
   * Internal use only.
   *
   * @deprecated do not use
   */
  @Deprecated
  public static void setApplicationInfo(ApplicationInfo applicationInfo) {
    ShadowActivityThread.applicationInfo = applicationInfo;
  }

  static ApplicationInfo getApplicationInfo() {
    return applicationInfo;
  }

  /**
   * internal, do not use
   *
   * @param androidConfiguration
   */
  public void setCompatConfiguration(Configuration androidConfiguration) {
    if (RuntimeEnvironment.getApiLevel() >= S) {
      // Setting compat configuration was refactored in android S
      // use reflection to create package private classes
      Class<?> activityThreadInternalClass =
          ReflectionHelpers.loadClass(
              getClass().getClassLoader(), "android.app.ActivityThreadInternal");
      Class<?> configurationControllerClass =
          ReflectionHelpers.loadClass(
              getClass().getClassLoader(), "android.app.ConfigurationController");
      Object configController =
          ReflectionHelpers.callConstructor(
              configurationControllerClass, from(activityThreadInternalClass, realActivityThread));
      ReflectionHelpers.callInstanceMethod(
          configController,
          "setCompatConfiguration",
          from(Configuration.class, androidConfiguration));
      androidConfiguration =
          ReflectionHelpers.callInstanceMethod(configController, "getCompatConfiguration");
      ReflectionHelpers.setField(realActivityThread, "mConfigurationController", configController);
    } else {
      reflector(_ActivityThread_.class, realActivityThread)
          .setCompatConfiguration(androidConfiguration);
    }
  }

  /** Accessor interface for {@link ActivityThread}'s internals. */
  @ForType(ActivityThread.class)
  public interface _ActivityThread_ {

    @Accessor("mBoundApplication")
    void setBoundApplication(Object data);

    @Accessor("mBoundApplication")
    Object getBoundApplication();

    @Accessor("mCompatConfiguration")
    void setCompatConfiguration(Configuration configuration);

    @Accessor("mInitialApplication")
    void setInitialApplication(Application application);

    /** internal use only. Tests should use {@link ActivityThread.getApplication} */
    @Accessor("mInitialApplication")
    Application getInitialApplication();

    @Accessor("mInstrumentation")
    void setInstrumentation(Instrumentation instrumentation);

    @Accessor("mActivities")
    Map<IBinder, ActivityClientRecord> getActivities();
  }

  /** Accessor interface for {@link ActivityThread.AppBindData}'s internals. */
  @ForType(className = "android.app.ActivityThread$AppBindData")
  public interface _AppBindData_ {

    @Accessor("appInfo")
    void setAppInfo(ApplicationInfo applicationInfo);

    @Accessor("processName")
    void setProcessName(String name);
  }

  @ForType(ActivityClientRecord.class)
  private interface ActivityClientRecordReflector {
    @Accessor("activity")
    void setActivity(Activity activity);

    @Accessor("activity")
    Activity getActivity();

    @Accessor("token")
    void setToken(IBinder token);

    @Accessor("intent")
    void setIntent(Intent intent);

    @Accessor("activityInfo")
    void setActivityInfo(ActivityInfo activityInfo);
  }

  @Resetter
  public static void reset() {
    reflector(_ActivityThread_.class, RuntimeEnvironment.getActivityThread())
        .getActivities()
        .clear();
  }
}
