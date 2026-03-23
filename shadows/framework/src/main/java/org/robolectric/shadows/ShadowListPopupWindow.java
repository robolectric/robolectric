package org.robolectric.shadows;

import android.widget.ListPopupWindow;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@Implements(ListPopupWindow.class)
public class ShadowListPopupWindow {

  @RealObject private ListPopupWindow realListPopupWindow;

  @Filter
  protected void show() {
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.getApplication());
    shadowApplication.setLatestListPopupWindow(realListPopupWindow);
  }

  public static ListPopupWindow getLatestListPopupWindow() {
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.getApplication());
    return shadowApplication.getLatestListPopupWindow();
  }
}
