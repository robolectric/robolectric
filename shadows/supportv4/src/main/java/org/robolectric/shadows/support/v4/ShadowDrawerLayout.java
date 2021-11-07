package org.robolectric.shadows.support.v4;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.view.View;
import android.support.v4.widget.DrawerLayout;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowViewGroup;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(DrawerLayout.class)
@Deprecated
public class ShadowDrawerLayout extends ShadowViewGroup {
  @RealObject private DrawerLayout realDrawerLayout;


  public DrawerLayout.DrawerListener getDrawerListener() {
    return reflector(DrawerLayoutReflector.class, realDrawerLayout).getDrawerListener();
  }

  /** Drawer animations are disabled in unit tests. */
  @Implementation
  protected void openDrawer(View drawerView, boolean animate) {
    reflector(DrawerLayoutReflector.class, realDrawerLayout).openDrawer(drawerView, false);
  }

  /** Drawer animations are disabled in unit tests. */
  @Implementation
  protected void closeDrawer(View drawerView, boolean animate) {
    reflector(DrawerLayoutReflector.class, realDrawerLayout).closeDrawer(drawerView, false);
  }

  @ForType(value = DrawerLayout.class, direct = true)
  interface DrawerLayoutReflector {

    @Accessor("mListener")
    DrawerLayout.DrawerListener getDrawerListener();

    void closeDrawer(View drawerView, boolean animate);

    void openDrawer(View drawerView, boolean animate);
  }
}
