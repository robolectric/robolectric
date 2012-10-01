package com.xtremelabs.robolectric.res;


import android.view.MenuItem;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.view.TestMenu;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.DEFAULT_SDK_VERSION;
import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class MenuResourceLoaderTest {
	@Test
    public void shouldInflateComplexMenu() throws Exception {
        ResourceLoader resourceLoader = new ResourceLoader(DEFAULT_SDK_VERSION, R.class, resourceFile("res"), resourceFile("menu"));
        TestMenu testMenu = new TestMenu();
    	resourceLoader.inflateMenu(Robolectric.application, R.menu.test_withchilds, testMenu);
    	assertThat(testMenu.size(), equalTo(4));
    }

	@Test
    public void shouldParseSubItemCorrectly() throws Exception {
        ResourceLoader resourceLoader = new ResourceLoader(DEFAULT_SDK_VERSION, R.class, resourceFile("res"), resourceFile("menu"));
        TestMenu testMenu = new TestMenu();
    	resourceLoader.inflateMenu(Robolectric.application, R.menu.test_withchilds, testMenu);
    	MenuItem mi = testMenu.findItem(R.id.test_submenu_1);
    	assertTrue(mi.hasSubMenu());
    	assertThat(mi.getSubMenu().size(), equalTo(2) );
    	assertThat(mi.getSubMenu().getItem(1).getTitle() + "", equalTo("Test menu item 3") );
    }
}
