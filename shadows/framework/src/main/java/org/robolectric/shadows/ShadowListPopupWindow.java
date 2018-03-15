package org.robolectric.shadows;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.widget.ListPopupWindow;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(ListPopupWindow.class)
public class ShadowListPopupWindow {
  
  @RealObject
  private ListPopupWindow realListPopupWindow;

  @Implementation
  public void show() {
    shadowOf(RuntimeEnvironment.application).setLatestListPopupWindow(realListPopupWindow);
    directlyOn(realListPopupWindow, ListPopupWindow.class).show();
  }

  public static ListPopupWindow getLatestListPopupWindow() {
    return shadowOf(RuntimeEnvironment.application).getLatestListPopupWindow();
  }
}
