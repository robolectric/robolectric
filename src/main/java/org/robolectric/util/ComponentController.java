package org.robolectric.util;

import static org.fest.reflect.core.Reflection.constructor;
import static org.fest.reflect.core.Reflection.method;
import static org.robolectric.Robolectric.shadowOf_;

import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLooper;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;

abstract class ComponentController<C extends ComponentController<C, T, S>, T, S> {
  protected final C myself;
  protected final T component;
  protected final S shadow;
  protected final ShadowLooper shadowMainLooper;

  protected Application application;
  protected Context baseContext;
  protected Intent intent;

  protected boolean attached;

  public ComponentController(Class<T> componentClass) {
    this(constructor().in(componentClass).newInstance());
  }

  @SuppressWarnings("unchecked")
  public ComponentController(T component) {
    myself = (C)this;
    this.component = component;
    shadow = shadowOf_(component);
    shadowMainLooper = shadowOf_(Looper.getMainLooper());
  }

  public T get() {
    return component;
  }

  public C withApplication(Application application) {
    this.application = application;
    return myself;
  }

  public C withBaseContext(Context baseContext) {
    this.baseContext = baseContext;
    return myself;
  }

  public C withIntent(Intent intent) {
    this.intent = intent;
    return myself;
  }

  public abstract C attach();

  public abstract C create();

  public abstract C destroy();

  public Intent getIntent() {
    Application application = this.application == null ? Robolectric.application : this.application;
    Intent intent = this.intent == null ? new Intent(application, component.getClass()) : this.intent;
    if (intent.getComponent() == null) {
      intent.setClass(application, component.getClass());
    }
    return intent;
  }
  
  protected C invokeWhilePaused(final String methodName) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override public void run() {
        method(methodName).in(component).invoke();
      }
    });
    return myself;
  }

  protected C invokeWhilePaused(final String methodName, final Bundle arg) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override public void run() {
        method(methodName).withParameterTypes(Bundle.class).in(component).invoke(arg);
      }
    });
    return myself;
  }

  protected C invokeWhilePaused(final String methodName, final Intent arg) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override public void run() {
        method(methodName).withParameterTypes(Intent.class).in(component).invoke(arg);
      }
    });
    return myself;
  }
  
  protected C invokeWhilePaused(final String methodName, final Intent arg, final int param1, final int param2) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override public void run() {
        method(methodName).withParameterTypes(Intent.class, int.class, int.class).in(component).invoke(arg, param1, param2);
      }
    });
    return myself;
  }
}
