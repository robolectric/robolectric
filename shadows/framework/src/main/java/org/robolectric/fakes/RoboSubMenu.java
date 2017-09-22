package org.robolectric.fakes;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import org.robolectric.RuntimeEnvironment;

/**
 * Robolectric implementation of {@link android.view.SubMenu}.
 */
public class RoboSubMenu extends RoboMenu implements SubMenu {

  public RoboSubMenu() {
    this(RuntimeEnvironment.application);
  }

  public RoboSubMenu(Context context) {
    super(context);
  }

  @Override
  public SubMenu setHeaderTitle(int titleRes) {
    return this;
  }

  @Override
  public SubMenu setHeaderTitle(CharSequence title) {
    return this;
  }

  @Override
  public SubMenu setHeaderIcon(int iconRes) {
    return this;
  }

  @Override
  public SubMenu setHeaderIcon(Drawable icon) {
    return this;
  }

  @Override
  public SubMenu setHeaderView(View view) {
    return this;
  }

  @Override
  public void clearHeader() {
  }

  @Override
  public SubMenu setIcon(int iconRes) {
    return this;
  }

  @Override
  public SubMenu setIcon(Drawable icon) {
    return this;
  }

  @Override
  public MenuItem getItem() {
    return null;
  }
}

