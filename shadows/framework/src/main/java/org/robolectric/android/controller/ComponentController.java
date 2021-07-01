package org.robolectric.android.controller;

import static com.google.common.base.Preconditions.checkState;

import android.content.Intent;
import android.os.Looper;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.LooperMode;
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
    Intent intent =
        this.intent == null
            ? new Intent(RuntimeEnvironment.getApplication(), component.getClass())
            : this.intent;
    if (intent.getComponent() == null) {
      intent.setClass(RuntimeEnvironment.getApplication(), component.getClass());
    }
    return intent;
  }

  protected C invokeWhilePaused(final String methodName, final ClassParameter<?>... classParameters) {
    return invokeWhilePaused(
        () -> ReflectionHelpers.callInstanceMethod(component, methodName, classParameters));
  }

  protected C invokeWhilePaused(Runnable runnable) {
    if (ShadowLooper.looperMode() == LooperMode.Mode.PAUSED) {
      checkState(Looper.myLooper() == Looper.getMainLooper(), "Expecting to be on main thread!");
    }
    shadowMainLooper.runPaused(runnable);
    return myself;
  }
}
