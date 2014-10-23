package org.robolectric.shadows;

import android.view.ContextThemeWrapper;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContextThemeWrapper.class)
public class ShadowContextThemeWrapper extends ShadowContextWrapper {
  @RealObject private ContextThemeWrapper realContextThemeWrapper;

  public Integer callGetThemeResId() {
    try {
      Method getThemeResId = ContextThemeWrapper.class.getDeclaredMethod("getThemeResId");
      return (Integer) getThemeResId.invoke(realContextThemeWrapper);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
