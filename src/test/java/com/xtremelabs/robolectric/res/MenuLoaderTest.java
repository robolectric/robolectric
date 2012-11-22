package com.xtremelabs.robolectric.res;


import android.content.ComponentName;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.util.I18nException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.TestUtil.systemResources;
import static com.xtremelabs.robolectric.util.TestUtil.testResources;

@RunWith(TestRunners.WithDefaults.class)
public class MenuLoaderTest {

	private MenuLoader menuLoader;
	
	@Before
	public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor(testResources(), systemResources());

        StringResourceLoader stringResourceLoader = new StringResourceLoader(resourceExtractor);
        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(testResources(), "values");
        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(systemResources(), "values");

        menuLoader = new MenuLoader(resourceExtractor, new AttrResourceLoader(resourceExtractor));
        new DocumentLoader(menuLoader).loadResourceXmlDir(testResources(), "menu");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected=I18nException.class)
	public void shouldThrowI18nExceptionOnMenuWithBareStrings() throws Exception {
		Menu testMenu = new TestMenu();
        menuLoader.setStrictI18n(true);
		menuLoader.inflateMenu(Robolectric.application, R.menu.test, testMenu);
	}
	
	public class TestMenu implements Menu {
		@Override
		public MenuItem add(CharSequence title) {
			return null;
		}

		@Override
		public MenuItem add(int titleRes) {
			return null;
		}

		@Override
		public MenuItem add(int groupId, int itemId, int order,
				CharSequence title) {
			return null;
		}

		@Override
		public MenuItem add(int groupId, int itemId, int order, int titleRes) {
			return null;
		}

		@Override
		public SubMenu addSubMenu(CharSequence title) {
			return null;
		}

		@Override
		public SubMenu addSubMenu(int titleRes) {
			return null;
		}

		@Override
		public SubMenu addSubMenu(int groupId, int itemId, int order,
				CharSequence title) {
			return null;
		}

		@Override
		public SubMenu addSubMenu(int groupId, int itemId, int order,
				int titleRes) {
			return null;
		}

		@Override
		public int addIntentOptions(int groupId, int itemId, int order,
				ComponentName caller, Intent[] specifics, Intent intent,
				int flags, MenuItem[] outSpecificItems) {
			return 0;
		}

		@Override
		public void removeItem(int id) {
			
		}

		@Override
		public void removeGroup(int groupId) {
			
		}

		@Override
		public void clear() {
			
		}

		@Override
		public void setGroupCheckable(int group, boolean checkable,
				boolean exclusive) {
			
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
			return null;
		}

		@Override
		public int size() {
			
			return 0;
		}

		@Override
		public MenuItem getItem(int index) {
			
			return null;
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
	}	
}
