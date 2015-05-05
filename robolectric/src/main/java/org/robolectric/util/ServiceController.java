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
    try {
      return new ServiceController<>(shadowsAdapter, serviceClass);
    } catch (IllegalAccessException | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T extends Service> ServiceController<T> of(ShadowsAdapter shadowsAdapter, T service) {
    return new ServiceController<>(shadowsAdapter, service);
  }

  public ServiceController(ShadowsAdapter shadowsAdapter, Class<T> serviceClass) throws IllegalAccessException, InstantiationException {
    this(shadowsAdapter, serviceClass.newInstance());
  }

  public ServiceController(ShadowsAdapter shadowsAdapter, T service) {
    super(shadowsAdapter, service);
    shadowActivityThreadClassName = shadowsAdapter.getShadowActivityThreadClassName();
  }

  public ServiceController<T> attach() {
    Application application = this.application == null ? RuntimeEnvironment.application : this.application;
    Context baseContext = this.baseContext == null ? application : this.baseContext;

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
        ClassParameter.from(Application.class, application),
        ClassParameter.from(Object.class, null));

    attached = true;
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
