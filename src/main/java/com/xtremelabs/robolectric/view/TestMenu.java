package com.xtremelabs.robolectric.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestMenu implements Menu {
    private int menuCounter = 0;
    private Context context;
    private Map<Integer, MenuItem> menuItems = new LinkedHashMap<Integer, MenuItem>();

    public TestMenu() {
        this(null);
    }

    public TestMenu(Context context) {
        this.context = context;
    }

    @Override public MenuItem add(CharSequence id) {
        int resourceId = 0;
        TestMenuItem menuItem = null;

        if (context != null) {
            try {
                Class<?> c = Class.forName(context.getPackageName() + ".R$id");
                Field idField = c.getDeclaredField(id.toString().split("/")[1]);
                resourceId = idField.getInt(idField);
            } catch (Exception e) {
                resourceId = menuCounter++;
            }
        } else {
            resourceId = menuCounter++;
        }
        menuItem = new TestMenuItem(resourceId);
        menuItems.put(new Integer(resourceId), menuItem);
        return menuItem;

    }

    @Override public MenuItem add(int titleRes) {
        return null;
    }

    @Override public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        return null;
    }

    @Override public MenuItem add(int groupId, int itemId, int order, int titleRes) {
        return null;
    }

    @Override public SubMenu addSubMenu(CharSequence title) {
        return null;
    }

    @Override public SubMenu addSubMenu(int titleRes) {
        return null;
    }

    @Override public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
        return null;
    }

    @Override public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
        return null;
    }

    @Override
    public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
        return 0;
    }

    @Override public void removeItem(int id) {
    }

    @Override public void removeGroup(int groupId) {
    }

    @Override public void clear() {
        menuItems.clear();
    }

    @Override public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
    }

    @Override public void setGroupVisible(int group, boolean visible) {
    }

    @Override public void setGroupEnabled(int group, boolean enabled) {
    }

    @Override public boolean hasVisibleItems() {
        return false;
    }

    @Override public MenuItem findItem(int id) {
		return menuItems.get(new Integer(id));
    }

    @Override public int size() {
        return menuItems.size();
    }

    @Override public MenuItem getItem(int index) {
		return menuItems.values().toArray(new MenuItem[menuItems.size()])[index];
    }

    @Override public void close() {
    }

    @Override public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        return false;
    }

    @Override public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return false;
    }

    @Override public boolean performIdentifierAction(int id, int flags) {
        return false;
    }

    @Override public void setQwertyMode(boolean isQwerty) {
    }

    public TestMenuItem findMenuItem(CharSequence title) {
        for (int i = 0; i < size(); i++) {
            TestMenuItem menuItem = (TestMenuItem) getItem(i);
            if (menuItem.getTitle().equals(title)) {
                return menuItem;
            }
        }
        return null;
    }
}