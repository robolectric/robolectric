package org.robolectric.android.controller;

import android.content.Intent;
import android.os.Looper;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

public abstract class ComponentController<C extends ComponentController<C, T>, T> {
  protected final C myself;
  protected T component;
  protected final ShadowLooper shadowMainLooper;

  protected Intent intent;

  protected boolean attached;

  @SuppressWarnings("unchecked")
  public ComponentController(T component, Intent intent) {
    this(component);
    this.intent = intent;
  }

  @SuppressWarnings("unchecked")
  public ComponentController(T component) {
    myself = (C) this;
    this.component = component;
    shadowMainLooper = Shadow.extract(Looper.getMainLooper());
  }

  public T get() {
    return component;
  }

  public abstract C create();

  public abstract C destroy();

  public Intent getIntent() {
    Intent intent = this.intent == null ? new Intent(RuntimeEnvironment.application, component.getClass()) : this.intent;
    if (intent.getComponent() == null) {
      intent.setClass(RuntimeEnvironment.application, component.getClass());
    }
    return intent;
  }

  protected C invokeWhilePaused(final String methodName, final ClassParameter<?>... classParameters) {
    shadowMainLooper.runPaused(new Runnable() {
      @Override
      public void run() {
        ReflectionHelpers.callInstanceMethod(component, methodName, classParameters);
      }
    });
    return myself;
  }
}
