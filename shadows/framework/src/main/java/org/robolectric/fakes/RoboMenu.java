package org.robolectric.fakes;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.robolectric.RuntimeEnvironment;

/**
 * Robolectric implementation of {@link android.view.Menu}.
 */
public class RoboMenu implements Menu {
  private List<MenuItem> menuItems = new ArrayList<>();
  private Context context;

  public RoboMenu() {
    this(RuntimeEnvironment.application);
  }

  public RoboMenu(Context context) {
    this.context = context;
  }

  @Override
  public MenuItem add(CharSequence title) {
    return add(0, 0, 0, title);
  }

  @Override
  public MenuItem add(int titleRes) {
    return add(0, 0, 0, titleRes);
  }

  @Override
  public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
    RoboMenuItem menuItem = new RoboMenuItem(context);
    menuItem.setOrder(order);
    menuItems.add(menuItem);
    menuItem.setGroupId(groupId);
    Collections.sort(menuItems, new CustomMenuItemComparator());
    menuItem.setItemId(itemId);
    menuItem.setTitle(title);
    return menuItem;
  }

  @Override
  public MenuItem add(int groupId, int itemId, int order, int titleRes) {
    return add(groupId, itemId, order, context.getResources().getString(titleRes));
  }

  @Override
  public SubMenu addSubMenu(CharSequence title) {
    RoboSubMenu tsm = new RoboSubMenu(context);
    RoboMenuItem menuItem = new RoboMenuItem(context);
    menuItems.add(menuItem);
    menuItem.setTitle(title);
    menuItem.setSubMenu(tsm);
    return tsm;
  }

  @Override
  public SubMenu addSubMenu(int titleRes) {
    RoboSubMenu tsm = new RoboSubMenu(context);
    RoboMenuItem menuItem = new RoboMenuItem(context);
    menuItems.add(menuItem);
    menuItem.setTitle(titleRes);
    menuItem.setSubMenu(tsm);
    return tsm;
  }

  @Override
  public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
    RoboSubMenu tsm = new RoboSubMenu(context);
    RoboMenuItem menuItem = new RoboMenuItem(context);
    menuItems.add(menuItem);
    menuItem.setGroupId(groupId);
    menuItem.setItemId(itemId);
    menuItem.setTitle(title);
    menuItem.setSubMenu(tsm);
    return tsm;
  }

  @Override
  public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
    RoboSubMenu tsm = new RoboSubMenu(context);
    RoboMenuItem menuItem = new RoboMenuItem(context);
    menuItems.add(menuItem);
    menuItem.setGroupId(groupId);
    menuItem.setItemId(itemId);
    menuItem.setTitle(titleRes);
    menuItem.setSubMenu(tsm);
    return tsm;
  }

  @Override
  public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics,
                              Intent intent, int flags, MenuItem[] outSpecificItems) {
    return 0;
  }

  @Override
  public void removeItem(int id) {
    MenuItem menuItem = findItem(id);
    menuItems.remove(menuItem);
  }

  @Override
  public void removeGroup(int groupId) {
  }

  @Override
  public void clear() {
    menuItems.clear();
  }

  @Override
  public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
  }

  @Override
  public void setGroupVisible(int group, boolean visible) {
  }

  @Override
  public void setGroupEnabled(int group, boolean enabled) {
  }

  @Override
  public boolean hasVisibleItems() {
    return false;
  }

  @Override
  public MenuItem findItem(int id) {
    for (MenuItem item : menuItems) {
      if (item.getItemId() == id) {
        return item;
      }
    }
    return null;
  }

  @Override
  public int size() {
    return menuItems.size();
  }

  @Override
  public MenuItem getItem(int index) {
    return menuItems.get(index);
  }

  @Override
  public void close() {
  }

  @Override
  public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
    return false;
  }

  @Override
  public boolean isShortcutKey(int keyCode, KeyEvent event) {
    return false;
  }

  @Override
  public boolean performIdentifierAction(int id, int flags) {
    return false;
  }

  @Override
  public void setQwertyMode(boolean isQwerty) {
  }

  public RoboMenuItem findMenuItem(CharSequence title) {
    for (int i = 0; i < size(); i++) {
      RoboMenuItem menuItem = (RoboMenuItem) getItem(i);
      if (menuItem.getTitle().equals(title)) {
        return menuItem;
      }
    }
    return null;
  }

  public RoboMenuItem findMenuItemContaining(CharSequence desiredText) {
    for (int i = 0; i < size(); i++) {
      RoboMenuItem menuItem = (RoboMenuItem) getItem(i);
      if (menuItem.getTitle().toString().contains(desiredText)) {
        return menuItem;
      }
    }
    return null;
  }

  private static class CustomMenuItemComparator implements Comparator<MenuItem> {

    @Override
    public int compare(MenuItem a, MenuItem b) {
      if (a.getOrder() == b.getOrder()) {
        return 0;
      } else if (a.getOrder() > b.getOrder()) {
        return 1;
      } else {
        return -1;
      }
    }
  }
}

