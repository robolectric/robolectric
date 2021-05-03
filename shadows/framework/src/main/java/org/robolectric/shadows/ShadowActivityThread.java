package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.ActivityThread;
import android.app.Application;
import android.app.Instrumentation;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import androidx.test.platform.app.InstrumentationRegistry;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import javax.annotation.Nonnull;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Reflector;

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

  @Implementation
  protected Instrumentation getInstrumentation() {
    // TODO: delete this shadow method once Instrumentation is not lazy loaded.
    // Prefer the stored instrumentation from the real Activity Thread.
    Instrumentation instrumentation =
        Reflector.reflector(_ActivityThread_.class, realActivityThread).getInstrumentation();
    if (instrumentation == null) {
      return InstrumentationRegistry.getInstrumentation();
    } else {
      return instrumentation;
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
    reflector(_ActivityThread_.class, realActivityThread)
        .setCompatConfiguration(androidConfiguration);
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

    /** internal use only. Tests should use {@link ActivityThread.getInstrumentation} */
    @Accessor("mInstrumentation")
    Instrumentation getInstrumentation();
  }

  /** Accessor interface for {@link ActivityThread.AppBindData}'s internals. */
  @ForType(className = "android.app.ActivityThread$AppBindData")
  public interface _AppBindData_ {

    @Accessor("appInfo")
    void setAppInfo(ApplicationInfo applicationInfo);

    @Accessor("processName")
    void setProcessName(String name);
  }
}
