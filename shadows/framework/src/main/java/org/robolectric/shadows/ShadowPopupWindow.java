package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.view.WindowManager;
import android.widget.PopupWindow;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(PopupWindow.class)
public class ShadowPopupWindow {

  @RealObject private PopupWindow realPopupWindow;

  @Implementation
  protected void invokePopup(WindowManager.LayoutParams p) {
    ShadowApplication.getInstance().setLatestPopupWindow(realPopupWindow);
    reflector(PopupWindowReflector.class, realPopupWindow).invokePopup(p);
  }

  @ForType(PopupWindow.class)
  interface PopupWindowReflector {

    @Direct
    void invokePopup(WindowManager.LayoutParams p);
  }
}
