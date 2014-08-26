package org.robolectric.shadows;

import android.support.v4.widget.DrawerLayout;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Robolectric.directlyOn;

@Implements(DrawerLayout.class)
public class ShadowDrawerLayout extends ShadowViewGroup {
  private DrawerLayout.DrawerListener drawerListener;

  @RealObject private DrawerLayout realDrawerLayout;

  @Implementation
  public void setDrawerListener(DrawerLayout.DrawerListener drawerListener) {
    this.drawerListener = drawerListener;
    directlyOn(realDrawerLayout, DrawerLayout.class).setDrawerListener(drawerListener);
  }

  public DrawerLayout.DrawerListener getDrawerListener() {
    return drawerListener;
  }
}
