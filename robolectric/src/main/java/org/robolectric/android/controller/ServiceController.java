package org.robolectric.android.controller;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.ShadowsAdapter;
import org.robolectric.util.ReflectionHelpers;

public class ServiceController<T extends Service> extends ComponentController<ServiceController<T>, T> {

  private String shadowActivityThreadClassName;

  public static <T extends Service> ServiceController<T> of(ShadowsAdapter shadowsAdapter, T service, Intent intent) {
    ServiceController<T> controller = new ServiceController<>(shadowsAdapter, service, intent);
    controller.attach();
    return controller;
  }

  protected ServiceController(ShadowsAdapter shadowsAdapter, T service, Intent intent) {
    super(shadowsAdapter, service, intent);
    shadowActivityThreadClassName = shadowsAdapter.getShadowActivityThreadClassName();
  }

  private ServiceController<T> attach() {
    if (attached) {
      return this;
    }

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
        from(Context.class, baseContext),
        from(activityThreadClass, null),
        from(String.class, component.getClass().getSimpleName()),
        from(IBinder.class, null),
        from(Application.class, RuntimeEnvironment.application),
        from(Object.class, null));

    attached = true;
    return this;
  }

  public ServiceController<T> bind() {
    invokeWhilePaused("onBind", from(Intent.class, getIntent()));
    return this;
  }

  @Override public ServiceController<T> create() {
    invokeWhilePaused("onCreate");
    return this;
  }

  @Override public ServiceController<T> destroy() {
    invokeWhilePaused("onDestroy");
    return this;
  }

  public ServiceController<T> rebind() {
    invokeWhilePaused("onRebind", from(Intent.class, getIntent()));
    return this;
  }

  public ServiceController<T> startCommand(int flags, int startId) {
    invokeWhilePaused("onStartCommand", from(Intent.class, getIntent()), from(int.class, flags), from(int.class, startId));
    return this;
  }

  public ServiceController<T> unbind() {
    invokeWhilePaused("onUnbind", from(Intent.class, getIntent()));
    return this;
  }

  /**
   * @deprecated Use the appropriate builder in {@link org.robolectric.Robolectric} instead.
   *
   * This method will be removed in Robolectric 3.6.
   */
  @Deprecated
  public ServiceController<T> withIntent(Intent intent) {
    this.intent = intent;
    return this;
  }
}
