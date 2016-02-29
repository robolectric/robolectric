package org.robolectric.shadows.support.v4;

import android.support.v4.widget.DrawerLayout;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Implementation;
import org.robolectric.shadows.ShadowViewGroup;

import java.util.ArrayList;
import java.util.List;

import static org.robolectric.internal.Shadow.directlyOn;

/**
 * Shadow for {@link android.support.v4.widget.DrawerLayout}.
 */
@Implements(DrawerLayout.class)
public class ShadowDrawerLayout extends ShadowViewGroup {
  @RealObject private DrawerLayout realDrawerLayout;
  private final List<DrawerLayout.DrawerListener> drawerListeners = new ArrayList<>();

  @Deprecated
  @Implementation
  public void setDrawerListener(DrawerLayout.DrawerListener drawerListener) {
    this.drawerListeners.clear();
    this.drawerListeners.add(drawerListener);
    directlyOn(realDrawerLayout, DrawerLayout.class).setDrawerListener(drawerListener);
  }

  @Implementation
  public void addDrawerListener(DrawerLayout.DrawerListener drawerListener) {
    this.drawerListeners.add(drawerListener);
    directlyOn(realDrawerLayout, DrawerLayout.class).addDrawerListener(drawerListener);
  }

  @Implementation
  public void removeDrawerListener(DrawerLayout.DrawerListener drawerListener) {
    if (drawerListener != null) {
      this.drawerListeners.remove(drawerListener);
      directlyOn(realDrawerLayout, DrawerLayout.class).removeDrawerListener(drawerListener);
    }
  }

  @Deprecated
  public DrawerLayout.DrawerListener getDrawerListener() {
    return drawerListeners.isEmpty() ? null : drawerListeners.get(0);
  }

  public List<DrawerLayout.DrawerListener> getDrawerListeners() {
    return drawerListeners;
  }
}
