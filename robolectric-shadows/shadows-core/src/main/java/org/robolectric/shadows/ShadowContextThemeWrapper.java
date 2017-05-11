package org.robolectric.shadows;

import android.view.ContextThemeWrapper;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContextThemeWrapper.class)
public class ShadowContextThemeWrapper extends ShadowContextWrapper {
  @RealObject private ContextThemeWrapper realContextThemeWrapper;

  public Integer callGetThemeResId() {
    return ReflectionHelpers.callInstanceMethod(realContextThemeWrapper, "getThemeResId");
  }
}
