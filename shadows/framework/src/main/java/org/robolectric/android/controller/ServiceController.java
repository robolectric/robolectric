package org.robolectric.android.controller;

import android.app.ActivityThread;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

public class ServiceController<T extends Service> extends ComponentController<ServiceController<T>, T> {

  public static <T extends Service> ServiceController<T> of(T service, Intent intent) {
    ServiceController<T> controller = new ServiceController<>(service, intent);
    controller.attach();
    return controller;
  }

  private ServiceController(T service, Intent intent) {
    super(service, intent);
  }

  private ServiceController<T> attach() {
    if (attached) {
      return this;
    }

    ReflectionHelpers.callInstanceMethod(Service.class, component, "attach",
        ClassParameter.from(Context.class, RuntimeEnvironment.application.getBaseContext()),
        ClassParameter.from(ActivityThread.class, null),
        ClassParameter.from(String.class, component.getClass().getSimpleName()),
        ClassParameter.from(IBinder.class, null),
        ClassParameter.from(Application.class, RuntimeEnvironment.application),
        ClassParameter.from(Object.class, null));

    attached = true;
    return this;
  }

  public ServiceController<T> bind() {
    invokeWhilePaused("onBind", ClassParameter.from(Intent.class, getIntent()));
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
    invokeWhilePaused("onRebind", ClassParameter.from(Intent.class, getIntent()));
    return this;
  }

  public ServiceController<T> startCommand(int flags, int startId) {
    invokeWhilePaused("onStartCommand", ClassParameter.from(Intent.class, getIntent()), ClassParameter.from(int.class, flags), ClassParameter.from(int.class, startId));
    return this;
  }

  public ServiceController<T> unbind() {
    invokeWhilePaused("onUnbind", ClassParameter.from(Intent.class, getIntent()));
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
