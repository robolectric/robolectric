package org.robolectric.fakes;

import android.annotation.DrawableRes;
import android.annotation.LayoutRes;
import android.annotation.StringRes;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;

/** Robolectric implementation of {@link MenuItem}. */
public class RoboMenuItem implements MenuItem {
  private int itemId;
  private int groupId;
  @Nullable private CharSequence title;
  @Nullable private CharSequence titleCondensed;
  private boolean enabled = true;
  private boolean checked = false;
  private boolean checkable = false;
  private boolean visible = true;
  private boolean expanded = false;
  @Nullable private OnMenuItemClickListener menuItemClickListener;
  @Nullable public Drawable icon;
  @Nullable private Intent intent;
  @Nullable private SubMenu subMenu;
  @Nullable private View actionView;
  @Nullable private OnActionExpandListener actionExpandListener;
  private int order;
  @Nonnull private final Context context;
  private char numericChar;
  private char alphaChar;
  @Nullable private ActionProvider actionProvider;

  public RoboMenuItem() {
    this(RuntimeEnvironment.getApplication());
  }

  public RoboMenuItem(@Nonnull Context context) {
    this.context = context;
  }

  public RoboMenuItem(int itemId) {
    this();
    this.itemId = itemId;
  }

  public void setItemId(int itemId) {
    this.itemId = itemId;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  @Override
  public int getItemId() {
    return itemId;
  }

  @Override
  public int getGroupId() {
    return groupId;
  }

  @Override
  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  @Override
  @Nonnull
  public MenuItem setTitle(@Nullable CharSequence title) {
    this.title = title;
    return this;
  }

  @Override
  @Nonnull
  public MenuItem setTitle(@StringRes int title) {
    this.title = title == 0 ? null : context.getString(title);
    return this;
  }

  @Override
  @Nullable
  public CharSequence getTitle() {
    return title;
  }

  @Override
  @Nonnull
  public MenuItem setTitleCondensed(@Nullable CharSequence title) {
    this.titleCondensed = title;
    return this;
  }

  @Override
  @Nullable
  public CharSequence getTitleCondensed() {
    return titleCondensed;
  }

  @Override
  @Nonnull
  public MenuItem setIcon(@Nullable Drawable icon) {
    this.icon = icon;
    return this;
  }

  @Override
  @Nonnull
  public MenuItem setIcon(@DrawableRes int iconRes) {
    this.icon = iconRes == 0 ? null : context.getDrawable(iconRes);
    return this;
  }

  @Override
  @Nullable
  public Drawable getIcon() {
    return this.icon;
  }

  @Override
  @Nonnull
  public MenuItem setIntent(@Nullable Intent intent) {
    this.intent = intent;
    return this;
  }

  @Override
  @Nullable
  public Intent getIntent() {
    return this.intent;
  }

  @Override
  @Nonnull
  public MenuItem setShortcut(char numericChar, char alphaChar) {
    return this;
  }

  @Override
  @Nonnull
  public MenuItem setNumericShortcut(char numericChar) {
    this.numericChar = numericChar;
    return this;
  }

  @Override
  public char getNumericShortcut() {
    return numericChar;
  }

  @Override
  @Nonnull
  public MenuItem setAlphabeticShortcut(char alphaChar) {
    this.alphaChar = alphaChar;
    return this;
  }

  @Override
  public char getAlphabeticShortcut() {
    return alphaChar;
  }

  @Override
  @Nonnull
  public MenuItem setCheckable(boolean checkable) {
    this.checkable = checkable;
    return this;
  }

  @Override
  public boolean isCheckable() {
    return checkable;
  }

  @Override
  @Nonnull
  public MenuItem setChecked(boolean checked) {
    this.checked = checked;
    return this;
  }

  @Override
  public boolean isChecked() {
    return checked;
  }

  @Override
  @Nonnull
  public MenuItem setVisible(boolean visible) {
    this.visible = visible;
    return this;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  @Nonnull
  public MenuItem setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean hasSubMenu() {
    return subMenu != null;
  }

  @Override
  @Nullable
  public SubMenu getSubMenu() {
    return subMenu;
  }

  public void setSubMenu(@Nullable SubMenu subMenu) {
    this.subMenu = subMenu;
  }

  @Override
  @Nonnull
  public MenuItem setOnMenuItemClickListener(
      @Nullable OnMenuItemClickListener menuItemClickListener) {
    this.menuItemClickListener = menuItemClickListener;
    return this;
  }

  @Override
  @Nullable
  public ContextMenu.ContextMenuInfo getMenuInfo() {
    return null;
  }

  public void click() {
    if (enabled && menuItemClickListener != null) {
      menuItemClickListener.onMenuItemClick(this);
    } else if (enabled && intent != null) {
      context.startActivity(intent);
    }
  }

  @Override
  public void setShowAsAction(int actionEnum) {}

  @Override
  @Nonnull
  public MenuItem setShowAsActionFlags(int actionEnum) {
    return this;
  }

  @Override
  @Nonnull
  public MenuItem setActionView(@Nullable View view) {
    actionView = view;
    return this;
  }

  @Override
  @Nonnull
  public MenuItem setActionView(@LayoutRes int resId) {
    actionView = LayoutInflater.from(context).inflate(resId, null);
    return this;
  }

  @Override
  @Nullable
  public View getActionView() {
    return actionView;
  }

  @Override
  @Nonnull
  public MenuItem setActionProvider(@Nullable ActionProvider actionProvider) {
    this.actionProvider = actionProvider;
    return this;
  }

  @Override
  @Nullable
  public ActionProvider getActionProvider() {
    return actionProvider;
  }

  @Override
  public boolean expandActionView() {
    if (actionView != null) {
      if (actionExpandListener != null) {
        actionExpandListener.onMenuItemActionExpand(this);
      }

      expanded = true;
      return true;
    }

    return false;
  }

  @Override
  public boolean collapseActionView() {
    if (actionView != null) {
      if (actionExpandListener != null) {
        actionExpandListener.onMenuItemActionCollapse(this);
      }

      expanded = false;
      return true;
    }

    return false;
  }

  @Override
  public boolean isActionViewExpanded() {
    return expanded;
  }

  @Override
  @Nonnull
  public MenuItem setOnActionExpandListener(@Nullable OnActionExpandListener listener) {
    actionExpandListener = listener;
    return this;
  }
}
