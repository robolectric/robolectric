package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.widget.PopupMenu;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(PopupMenu.class)
public class ShadowPopupMenu {

  @RealObject private PopupMenu realPopupMenu;

  private boolean isShowing;
  private PopupMenu.OnMenuItemClickListener onMenuItemClickListener;

  @Implementation
  protected void show() {
    this.isShowing = true;
    setLatestPopupMenu(this);
    reflector(PopupMenuReflector.class, realPopupMenu).show();
  }

  @Implementation
  protected void dismiss() {
    this.isShowing = false;
    reflector(PopupMenuReflector.class, realPopupMenu).dismiss();
  }

  @Implementation
  protected void setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener listener) {
    this.onMenuItemClickListener = listener;
    reflector(PopupMenuReflector.class, realPopupMenu).setOnMenuItemClickListener(listener);
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

  @ForType(PopupMenu.class)
  interface PopupMenuReflector {

    @Direct
    void show();

    @Direct
    void dismiss();

    @Direct
    void setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener listener);
  }
}
