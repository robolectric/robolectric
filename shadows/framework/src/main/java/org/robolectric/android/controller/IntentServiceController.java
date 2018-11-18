package org.robolectric.android.controller;

import android.app.ActivityThread;
import android.app.Application;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

public class IntentServiceController<T extends IntentService> extends ComponentController<IntentServiceController<T>, T> {

  public static <T extends IntentService> IntentServiceController<T> of(final T service, final Intent intent) {
    final IntentServiceController<T> controller = new IntentServiceController<>(service, intent);
    controller.attach();
    return controller;
  }

  private IntentServiceController(final T service, final Intent intent) {
    super(service, intent);
  }

  private IntentServiceController<T> attach() {
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

  public IntentServiceController<T> bind() {
    invokeWhilePaused("onBind", ClassParameter.from(Intent.class, getIntent()));
    return this;
  }

  @Override public IntentServiceController<T> create() {
    invokeWhilePaused("onCreate");
    return this;
  }

  @Override public IntentServiceController<T> destroy() {
    invokeWhilePaused("onDestroy");
    return this;
  }

  public IntentServiceController<T> rebind() {
    invokeWhilePaused("onRebind", ClassParameter.from(Intent.class, getIntent()));
    return this;
  }

  public IntentServiceController<T> startCommand(final int flags, final int startId) {
    final IntentServiceController<T> intentServiceController = handleIntent();
    get().stopSelf(startId);
    return intentServiceController;
  }

  public IntentServiceController<T> unbind() {
    invokeWhilePaused("onUnbind", ClassParameter.from(Intent.class, getIntent()));
    return this;
  }

  public IntentServiceController<T> handleIntent() {
    invokeWhilePaused("onHandleIntent", ClassParameter.from(Intent.class, getIntent()));
    return this;
  }
}
