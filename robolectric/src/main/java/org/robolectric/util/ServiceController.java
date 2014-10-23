package org.robolectric.util;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.IBinder;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivityThread;
import org.robolectric.shadows.ShadowService;

import java.lang.reflect.InvocationTargetException;

public class ServiceController<T extends Service> extends ComponentController<ServiceController<T>, T, ShadowService>{

  public static <T extends Service> ServiceController<T> of(Class<T> serviceClass) {
    try {
      return new ServiceController<T>(serviceClass);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T extends Service> ServiceController<T> of(T service) {
    return new ServiceController<T>(service);
  }

  public ServiceController(Class<T> serviceClass) throws IllegalAccessException, InstantiationException {
    this(serviceClass.newInstance());
  }

  public ServiceController(T service) {
    super(service);
  }

  public ServiceController<T> attach() {
    Application application = this.application == null ? Robolectric.application : this.application;
    Context baseContext = this.baseContext == null ? application : this.baseContext;

    ClassLoader cl = baseContext.getClassLoader();
    Class<?> activityThreadClass;
    try {
      activityThreadClass = cl.loadClass(ShadowActivityThread.CLASS_NAME);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    try {
      component.getClass().getMethod("attach",
          Context.class /* context */,
          activityThreadClass /* aThread */,
          String.class /* className */,
          IBinder.class /* token */,
          Application.class /* application */,
          Object.class /* activityManager */).invoke(component, baseContext,
          null /* aThread */,
          component.getClass().getSimpleName(), /* className */
          null /* token */,
          application,
          null /* activityManager */);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

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
