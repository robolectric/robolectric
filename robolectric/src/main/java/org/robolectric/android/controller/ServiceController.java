package org.robolectric.android.controller;

import android.app.Service;
import android.content.Intent;
import org.robolectric.ShadowsAdapter;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

public class ServiceController<T extends Service> extends org.robolectric.util.ServiceController<T> {

  public static <T extends Service> ServiceController<T> of(ShadowsAdapter shadowsAdapter, T service, Intent intent) {
    return new ServiceController<>(shadowsAdapter, service, intent);
  }

  protected ServiceController(ShadowsAdapter shadowsAdapter, T service, Intent intent) {
    super(shadowsAdapter, service, intent);
  }

  public ServiceController<T> bind() {
    invokeWhilePaused("onBind", from(Intent.class, getIntent()));
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
}
