package com.xtremelabs.robolectric.tester.android.view;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

public class TestContextMenu extends TestMenu implements ContextMenu {

	// Constants -----------------------------------------------------

	// Attributes ----------------------------------------------------
	private Drawable headerIcon;
	private CharSequence headerTitle;
	private View headerView;

	private View view;
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

	public TestContextMenu(View view, Activity activity) {
		super(activity);
		this.view = view;
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
		TestMenuItem item = (TestMenuItem) super.add(groupId, itemId, order,
				title);
		// get parent of originating view
		ListView parent = (ListView) view.getParent();
		// get position from parent
		int pos = parent.indexOfChild(view);
		// create menu info with empty values and then set
		AdapterContextMenuInfo menuInfo = new AdapterContextMenuInfo(null, 0, 0);
		menuInfo.id = view.getId();
		menuInfo.position = pos;
		menuInfo.targetView = view;
		// set menu info to menu item
		item.setMenuInfo(menuInfo);
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

	@Override
	protected void menuAction(TestMenuItem item) {
		activity.onContextItemSelected(item);
	}

	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
}
