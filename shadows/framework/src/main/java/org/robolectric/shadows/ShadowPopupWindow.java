package org.robolectric.shadows;

import static org.robolectric.Shadows.shadowOf;

import android.view.WindowManager;
import android.widget.PopupWindow;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(PopupWindow.class)
public class ShadowPopupWindow {

  @RealObject private PopupWindow realPopupWindow;

  @Filter
  protected void invokePopup(WindowManager.LayoutParams p) {
    shadowOf(RuntimeEnvironment.getApplication()).setLatestPopupWindow(realPopupWindow);
  }
}
