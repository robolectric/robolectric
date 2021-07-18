package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.widget.ListPopupWindow;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(ListPopupWindow.class)
public class ShadowListPopupWindow {

  @RealObject private ListPopupWindow realListPopupWindow;

  @Implementation
  protected void show() {
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.getApplication());
    shadowApplication.setLatestListPopupWindow(realListPopupWindow);
    reflector(ListPopupWindowReflector.class, realListPopupWindow).show();
  }

  public static ListPopupWindow getLatestListPopupWindow() {
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.getApplication());
    return shadowApplication.getLatestListPopupWindow();
  }

  @ForType(ListPopupWindow.class)
  interface ListPopupWindowReflector {

    @Direct
    void show();
  }
}
