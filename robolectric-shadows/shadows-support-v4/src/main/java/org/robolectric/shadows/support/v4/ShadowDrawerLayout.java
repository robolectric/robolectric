package org.robolectric.shadows.support.v4;

import android.support.v4.widget.DrawerLayout;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Implementation;
import org.robolectric.shadows.ShadowViewGroup;

import static org.robolectric.internal.Shadow.directlyOn;

/**
 * Shadow for {@link android.support.v4.widget.DrawerLayout}.
 */
@Implements(DrawerLayout.class)
public class ShadowDrawerLayout extends ShadowViewGroup {
  @RealObject private DrawerLayout realDrawerLayout;
  private DrawerLayout.DrawerListener drawerListener;

  @Implementation
  public void setDrawerListener(DrawerLayout.DrawerListener drawerListener) {
    this.drawerListener = drawerListener;
    directlyOn(realDrawerLayout, DrawerLayout.class).setDrawerListener(drawerListener);
  }

  public DrawerLayout.DrawerListener getDrawerListener() {
    return drawerListener;
  }
}
