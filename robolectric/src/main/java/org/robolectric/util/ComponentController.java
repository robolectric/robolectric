package org.robolectric.util;

import android.app.Application;
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

  protected Intent intent;

  protected boolean attached;

  @SuppressWarnings("unchecked")
  public ComponentController(ShadowsAdapter shadowsAdapter, T component, Intent intent) {
    this(shadowsAdapter, component);
    this.intent = intent;
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

  /**
   * @deprecated Prefer passing the Intent through the constructor instead, it is safer as this
   * method is broken for ActivityController where it is called after attach(). This method will be
   * removed in a forthcoming release.
   */
  @Deprecated
  public C withIntent(Intent intent) {
    this.intent = intent;
    return myself;
  }

  /**
   * @deprecated The component is automatically attached. There is no need to call this method.
   *
   * TODO(jongerrish): Make this method private so that it can only be called internally, should not
   * be part of the API.
   */
  @Deprecated
  public abstract C attach();

  public abstract C create();

  public abstract C destroy();

  public Intent getIntent() {
    Intent intent = this.intent == null ? new Intent(RuntimeEnvironment.application, component.getClass()) : this.intent;
    if (intent.getComponent() == null) {
      intent.setClass(RuntimeEnvironment.application, component.getClass());
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
