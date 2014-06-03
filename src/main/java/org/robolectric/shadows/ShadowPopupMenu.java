package org.robolectric.shadows;

import android.widget.PopupMenu;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Robolectric.directlyOn;

@Implements(PopupMenu.class)
public class ShadowPopupMenu {

  @RealObject
  private PopupMenu realPopupMenu;

  private boolean isShowing;
  private PopupMenu.OnMenuItemClickListener onMenuItemClickListener;

  @Implementation
  public void show() {
    this.isShowing = true;
    setLatestPopupMenu(this);
    directlyOn(realPopupMenu, PopupMenu.class).show();
  }

  @Implementation
  public void dismiss() {
    this.isShowing = false;
    directlyOn(realPopupMenu, PopupMenu.class).dismiss();
  }

  @Implementation
  public void setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener listener) {
    this.onMenuItemClickListener = listener;
    directlyOn(realPopupMenu, PopupMenu.class).setOnMenuItemClickListener(listener);
  }

  public boolean isShowing() {
    return isShowing;
  }

  public static PopupMenu getLatestPopupMenu() {
    ShadowPopupMenu popupMenu = Robolectric.getShadowApplication().getLatestPopupMenu();
    return popupMenu == null ? null : popupMenu.realPopupMenu;
  }

  public static void setLatestPopupMenu(ShadowPopupMenu latestPopupMenu) {
    ShadowApplication shadowApplication = Robolectric.getShadowApplication();
    if (shadowApplication != null) shadowApplication.setLatestPopupMenu(latestPopupMenu);
  }

  public PopupMenu.OnMenuItemClickListener getOnMenuItemClickListener() {
    return onMenuItemClickListener;
  }
}
