package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.ActivityThread.ActivityClientRecord;
import android.app.Application;
import android.app.IApplicationThread;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.CancellationSignal;
import android.os.IBinder;
import android.os.RemoteCallback;
import android.os.RemoteException;
import com.android.internal.app.IVoiceInteractor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Reflector;
import org.robolectric.util.reflector.WithType;

@Implements(value = ActivityThread.class, isInAndroidSdk = false, looseSignatures = true)
public class ShadowActivityThread {
  private static ApplicationInfo applicationInfo;
  @RealObject protected ActivityThread realActivityThread;

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
              int flags = (Integer) args[1];

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

  /** Update's ActivityThread's list of active Activities */
  IBinder registerActivityLaunch(Intent intent, ActivityInfo activityInfo, Activity activity) {
    IBinder token = new Binder();
    ActivityClientRecord record = new ActivityClientRecord();
    ActivityClientRecordReflector recordReflector =
        reflector(ActivityClientRecordReflector.class, record);
    recordReflector.setToken(token);
    recordReflector.setIntent(intent);
    recordReflector.setActivityInfo(activityInfo);
    recordReflector.setActivity(activity);
    reflector(_ActivityThread_.class, realActivityThread).getActivities().put(token, record);
    return token;
  }

  boolean maybeSetActivityState(IBinder token, int state) {
    if (!reflector(_ActivityThread_.class, realActivityThread).getActivities().containsKey(token)) {
      return false;
    }
    ActivityClientRecord record =
        reflector(_ActivityThread_.class, realActivityThread).getActivities().get(token);
    record.setState(state);
    return true;
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

  void handleRequestDirectActions(IBinder activityToken, RemoteCallback callback)
      throws RemoteException {
    IApplicationThread applicationThread = realActivityThread.getApplicationThread();
    applicationThread.requestDirectActions(
        activityToken, ReflectionHelpers.createDeepProxy(IVoiceInteractor.class), null, callback);
    ShadowLooper.idleMainLooper();
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

    void handleRequestDirectActions(
        IBinder activityToken,
        @WithType("com.android.internal.app.IVoiceInteractor") Object interactor,
        CancellationSignal cancellationSignal,
        RemoteCallback callback);
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
