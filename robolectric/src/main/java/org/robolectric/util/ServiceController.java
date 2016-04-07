package org.robolectric.util;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.IBinder;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.ShadowsAdapter;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

public class ServiceController<T extends Service> extends ComponentController<ServiceController<T>, T>{

  private String shadowActivityThreadClassName;

  public static <T extends Service> ServiceController<T> of(ShadowsAdapter shadowsAdapter, Class<T> serviceClass) {
    return of(shadowsAdapter, ReflectionHelpers.callConstructor(serviceClass));
  }

  public static <T extends Service> ServiceController<T> of(ShadowsAdapter shadowsAdapter, T service) {
    ServiceController<T> controller = new ServiceController<>(shadowsAdapter, service);
    controller.doAttach();
    return controller;
  }

  private ServiceController(ShadowsAdapter shadowsAdapter, T service) {
    super(shadowsAdapter, service);
    shadowActivityThreadClassName = shadowsAdapter.getShadowActivityThreadClassName();
  }

  private void doAttach() {
    Context baseContext = RuntimeEnvironment.application.getBaseContext();

    ClassLoader cl = baseContext.getClassLoader();
    Class<?> activityThreadClass;
    try {
      activityThreadClass = cl.loadClass(shadowActivityThreadClassName);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    ReflectionHelpers.callInstanceMethod(Service.class, component, "attach",
        ClassParameter.from(Context.class, baseContext),
        ClassParameter.from(activityThreadClass, null),
        ClassParameter.from(String.class, component.getClass().getSimpleName()),
        ClassParameter.from(IBinder.class, null),
        ClassParameter.from(Application.class, RuntimeEnvironment.application),
        ClassParameter.from(Object.class, null));
  }

  /**
   * @deprecated The service is automatically attached. There is no need to call this method.
   */
  @Deprecated
  public ServiceController<T> attach() {
    return this;
  }

  public ServiceController<T> bind() {
    invokeWhilePaused("onBind", getIntent());
    return this;
  }

  public ServiceController<T> create() {
    invokeWhilePaused("onCreate");
    return this;
  }

  public ServiceController<T> destroy() {
    invokeWhilePaused("onDestroy");
    return this;
  }

  public ServiceController<T> rebind() {
    invokeWhilePaused("onRebind", getIntent());
    return this;
  }

  public ServiceController<T> startCommand(int flags, int startId) {
    invokeWhilePaused("onStartCommand", getIntent(), flags, startId);
    return this;
  }

  public ServiceController<T> unbind() {
    invokeWhilePaused("onUnbind", getIntent());
    return this;
  }
}
