package com.xtremelabs.robolectric.tester.android.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public class TestContextMenu extends TestMenu implements ContextMenu {

	// Constants -----------------------------------------------------

	// Attributes ----------------------------------------------------
	private Drawable headerIcon;
	private CharSequence headerTitle;
	private View headerView;
	// Static --------------------------------------------------------
	private static TestContextMenu lastContextMenu;

	public static TestContextMenu getLastContextMenu() {
		return lastContextMenu;
	}

	// Constructors --------------------------------------------------
	public TestContextMenu() {
		super();
		lastContextMenu = null;
	}

	public TestContextMenu(Context context) {
		super(context);
		lastContextMenu = null;
	}

	// Public --------------------------------------------------------
	@Override
	public SubMenu addSubMenu(int groupId, int itemId, int order,
			CharSequence title) {
		SubMenu sub = super.addSubMenu(groupId, itemId, order, title);
		lastContextMenu = this;
		return sub;
	}

	@Override
	public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
		MenuItem item = super.add(groupId, itemId, order, title);
		lastContextMenu = this;
		return item;
	}

	@Override
	public ContextMenu setHeaderTitle(int titleRes) {
		headerTitle = context.getResources().getText(titleRes);
		return this;
	}

	@Override
	public ContextMenu setHeaderTitle(CharSequence title) {
		headerTitle = title;
		return this;
	}

	@Override
	public ContextMenu setHeaderIcon(int iconRes) {
		headerIcon = context.getResources().getDrawable(iconRes);
		return this;
	}

	@Override
	public ContextMenu setHeaderIcon(Drawable icon) {
		headerIcon = icon;
		return this;
	}

	@Override
	public ContextMenu setHeaderView(View view) {
		headerView = view;
		return this;
	}

	@Override
	public void clearHeader() {
		headerTitle = null;
		headerIcon = null;
		headerView = null;
	}

	public CharSequence getHeaderTitle() {
		return headerTitle;
	}

	public Drawable getHeaderIcon() {
		return headerIcon;
	}

	public View getHeaderView() {
		return headerView;
	}

	// Package protected ---------------------------------------------

	// Protected -----------------------------------------------------

	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
}
