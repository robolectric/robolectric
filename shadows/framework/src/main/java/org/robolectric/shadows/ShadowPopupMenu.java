package org.robolectric.shadows;

import android.widget.PopupMenu;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@Implements(PopupMenu.class)
public class ShadowPopupMenu {

  @RealObject private PopupMenu realPopupMenu;

  private boolean isShowing;
  private PopupMenu.OnMenuItemClickListener onMenuItemClickListener;

  @Filter
  protected void show() {
    this.isShowing = true;
    setLatestPopupMenu(this);
  }

  @Filter
  protected void dismiss() {
    this.isShowing = false;
  }

  @Filter
  protected void setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener listener) {
    this.onMenuItemClickListener = listener;
  }

  public boolean isShowing() {
    return isShowing;
  }

  public static PopupMenu getLatestPopupMenu() {
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.getApplication());
    ShadowPopupMenu popupMenu = shadowApplication.getLatestPopupMenu();
    return popupMenu == null ? null : popupMenu.realPopupMenu;
  }

  public static void setLatestPopupMenu(ShadowPopupMenu latestPopupMenu) {
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.getApplication());
    if (shadowApplication != null) shadowApplication.setLatestPopupMenu(latestPopupMenu);
  }

  public PopupMenu.OnMenuItemClickListener getOnMenuItemClickListener() {
    return onMenuItemClickListener;
  }
}
