package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.widget.ListPopupWindow;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@Implements(ListPopupWindow.class)
public class ShadowListPopupWindow {
  
  @RealObject
  private ListPopupWindow realListPopupWindow;

  @Implementation
  protected void show() {
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.application);
    shadowApplication.setLatestListPopupWindow(realListPopupWindow);
    directlyOn(realListPopupWindow, ListPopupWindow.class).show();
  }

  public static ListPopupWindow getLatestListPopupWindow() {
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.application);
    return shadowApplication.getLatestListPopupWindow();
  }
}
