package com.xtremelabs.robolectric.tester.android.view;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import com.xtremelabs.robolectric.shadows.ShadowView;

/**
 * Test for the TestContextMenu
 * 
 * @author Alvaro
 * 
 */
@RunWith(WithTestDefaultsRunner.class)
public class TestContextMenuTest {

	// Constants -----------------------------------------------------

	// Attributes ----------------------------------------------------

	// Static --------------------------------------------------------

	// Constructors --------------------------------------------------

	// Public --------------------------------------------------------

	@Test
	public void shouldRegisterAndShowContextMenu() {
		MyActivity activity = new MyActivity();
		ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
		shadowActivity.callOnCreate(null);
		ShadowView shadowView = Robolectric.shadowOf(activity.view);
		assertNotNull(shadowView.getOnLongClickListener());
		shadowView.performLongClick();
		TestContextMenu contextMenu = TestContextMenu.getLastContextMenu();
		assertNotNull(contextMenu);
		assertEquals("Test menu item 1", contextMenu.getItem(0).getTitle());
		assertEquals("Test menu item 2", contextMenu.getItem(1).getTitle());
		new TestContextMenu();
		assertNull(TestContextMenu.getLastContextMenu());
	}

	// Package protected ---------------------------------------------

	// Protected -----------------------------------------------------

	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------

	private static class MyActivity extends Activity {

		View view;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			ListView listView = new ListView(this);
			view = new TextView(this);
			listView.addView(view);
			registerForContextMenu(view);
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.test, menu);
		}

		@Override
		protected void onDestroy() {
			super.onDestroy();
		}
	}
}
