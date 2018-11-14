package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.widget.PopupMenu;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@Implements(PopupMenu.class)
public class ShadowPopupMenu {

  @RealObject
  private PopupMenu realPopupMenu;

  private boolean isShowing;
  private PopupMenu.OnMenuItemClickListener onMenuItemClickListener;

  @Implementation
  protected void show() {
    this.isShowing = true;
    setLatestPopupMenu(this);
    directlyOn(realPopupMenu, PopupMenu.class).show();
  }

  @Implementation
  protected void dismiss() {
    this.isShowing = false;
    directlyOn(realPopupMenu, PopupMenu.class).dismiss();
  }

  @Implementation
  protected void setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener listener) {
    this.onMenuItemClickListener = listener;
    directlyOn(realPopupMenu, PopupMenu.class).setOnMenuItemClickListener(listener);
  }

  public boolean isShowing() {
    return isShowing;
  }

  public static PopupMenu getLatestPopupMenu() {
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.application);
    ShadowPopupMenu popupMenu = shadowApplication.getLatestPopupMenu();
    return popupMenu == null ? null : popupMenu.realPopupMenu;
  }

  public static void setLatestPopupMenu(ShadowPopupMenu latestPopupMenu) {
    ShadowApplication shadowApplication = Shadow.extract(RuntimeEnvironment.application);
    if (shadowApplication != null) shadowApplication.setLatestPopupMenu(latestPopupMenu);
  }

  public PopupMenu.OnMenuItemClickListener getOnMenuItemClickListener() {
    return onMenuItemClickListener;
  }
}
