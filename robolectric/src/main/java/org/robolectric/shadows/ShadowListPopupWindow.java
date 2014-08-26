package org.robolectric.shadows;

import android.widget.ListPopupWindow;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Robolectric.directlyOn;

@Implements(ListPopupWindow.class)
public class ShadowListPopupWindow {
  
  @RealObject
  private ListPopupWindow realListPopupWindow;

  @Implementation
  public void show() {
    Robolectric.getShadowApplication().setLatestListPopupWindow(realListPopupWindow);
    directlyOn(realListPopupWindow, ListPopupWindow.class).show();
  }

  public static ListPopupWindow getLatestListPopupWindow() {
    return Robolectric.getShadowApplication().getLatestListPopupWindow();
  }
}
