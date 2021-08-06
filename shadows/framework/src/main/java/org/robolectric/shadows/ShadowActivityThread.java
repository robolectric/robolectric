package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.ActivityThread.ActivityClientRecord;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;
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
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

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
    Application currentApplication = activityThreadReflector.getInitialApplication();
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
    activityThreadReflector.getActivities().put(token, record);
    return token;
  }

  void removeActivity(IBinder token) {
    activityThreadReflector.getActivities().remove(token);
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

  /**
   * internal, do not use
   *
   * @param androidConfiguration
   */
  public void setCompatConfiguration(Configuration androidConfiguration) {
    activityThreadReflector.setCompatConfiguration(androidConfiguration);
  }

  /** Reflector interface for {@link ActivityThread}'s internals. */
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

  /** Reflector interface for {@link ActivityThread.AppBindData}'s internals. */
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
