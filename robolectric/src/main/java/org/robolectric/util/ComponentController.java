package org.robolectric.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.ShadowsAdapter;
import org.robolectric.ShadowsAdapter.ShadowLooperAdapter;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

public abstract class ComponentController<C extends ComponentController<C, T>, T> {
  protected final C myself;
  protected final T component;
  protected final ShadowLooperAdapter shadowMainLooper;

  protected Application application;
  protected Context baseContext;
  protected Intent intent;

  protected boolean attached;

  public ComponentController(ShadowsAdapter shadowsAdapter, Class<T> componentClass) throws IllegalAccessException, InstantiationException {
    this(shadowsAdapter, componentClass.newInstance());
  }

  @SuppressWarnings("unchecked")
  public ComponentController(ShadowsAdapter shadowsAdapter, T component) {
    myself = (C) this;
    this.component = component;
    shadowMainLooper = shadowsAdapter.getMainLooper();
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
    Application application = this.application == null ? RuntimeEnvironment.application : this.application;
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
        ReflectionHelpers.callInstanceMethod(component, methodName);
      }
    });
    return myself;
  }

  protected C invokeWhilePaused(final String methodName, final Bundle arg) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        ReflectionHelpers.callInstanceMethod(component, methodName, ClassParameter.from(Bundle.class, arg));
      }
    });
    return myself;
  }

  protected C invokeWhilePaused(final String methodName, final Intent arg) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        ReflectionHelpers.callInstanceMethod(component, methodName, ClassParameter.from(Intent.class, arg));
      }
    });
    return myself;
  }

  protected C invokeWhilePaused(final String methodName, final Intent arg, final int param1, final int param2) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        ReflectionHelpers.callInstanceMethod(component, methodName, ClassParameter.from(Intent.class, arg), ClassParameter.from(int.class, param1), ClassParameter.from(int.class, param2));
      }
    });
    return myself;
  }
}
