package org.robolectric.shadows.androidx;

import android.view.View;

import androidx.drawerlayout.widget.DrawerLayout;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowViewGroup;

import static org.robolectric.shadow.api.Shadow.directlyOn;

@Implements(DrawerLayout.class)
public class ShadowDrawerLayout extends ShadowViewGroup {
  @RealObject private DrawerLayout realDrawerLayout;
  private DrawerLayout.DrawerListener drawerListener;

  @Implementation
  protected void setDrawerListener(DrawerLayout.DrawerListener drawerListener) {
    this.drawerListener = drawerListener;
    directlyOn(realDrawerLayout, DrawerLayout.class).setDrawerListener(drawerListener);
  }

  public DrawerLayout.DrawerListener getDrawerListener() {
    return drawerListener;
  }

  /** Drawer animations are disabled in unit tests. */
  @Implementation
  protected void openDrawer(View drawerView, boolean animate) {
    directlyOn(realDrawerLayout, DrawerLayout.class).openDrawer(drawerView, false);
  }

  /** Drawer animations are disabled in unit tests. */
  @Implementation
  protected void closeDrawer(View drawerView, boolean animate) {
    directlyOn(realDrawerLayout, DrawerLayout.class).closeDrawer(drawerView, false);
  }
}
