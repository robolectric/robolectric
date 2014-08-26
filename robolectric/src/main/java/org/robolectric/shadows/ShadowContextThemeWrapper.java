package org.robolectric.shadows;

import android.view.ContextThemeWrapper;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.fest.reflect.core.Reflection.method;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContextThemeWrapper.class)
public class ShadowContextThemeWrapper extends ShadowContextWrapper {
  @RealObject private ContextThemeWrapper realContextThemeWrapper;

  public Integer callGetThemeResId() {
    return method("getThemeResId").withReturnType(int.class).in(realContextThemeWrapper).invoke();
  }
}
