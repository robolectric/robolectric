package org.robolectric.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowLooper;

import static org.robolectric.Shadows.shadowOf_;

abstract class ComponentController<C extends ComponentController<C, T, S>, T, S> {
  protected final C myself;
  protected final T component;
  protected final S shadow;
  protected final ShadowLooper shadowMainLooper;

  protected Application application;
  protected Context baseContext;
  protected Intent intent;

  protected boolean attached;

  public ComponentController(Class<T> componentClass) throws IllegalAccessException, InstantiationException {
    this(componentClass.newInstance());
  }

  @SuppressWarnings("unchecked")
  public ComponentController(T component) {
    myself = (C) this;
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
      @Override
      public void run() {
        ReflectionHelpers.callInstanceMethodReflectively(component, methodName);
      }
    });
    return myself;
  }

  protected C invokeWhilePaused(final String methodName, final Bundle arg) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        ReflectionHelpers.callInstanceMethodReflectively(component, methodName, new ReflectionHelpers.ClassParameter(Bundle.class, arg));
      }
    });
    return myself;
  }

  protected C invokeWhilePaused(final String methodName, final Intent arg) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        ReflectionHelpers.callInstanceMethodReflectively(component, methodName, new ReflectionHelpers.ClassParameter(Intent.class, arg));
      }
    });
    return myself;
  }

  protected C invokeWhilePaused(final String methodName, final Intent arg, final int param1, final int param2) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        ReflectionHelpers.callInstanceMethodReflectively(component, methodName, new ReflectionHelpers.ClassParameter(Intent.class, arg), new ReflectionHelpers.ClassParameter(int.class, param1), new ReflectionHelpers.ClassParameter(int.class, param2));
      }
    });
    return myself;
  }
}
