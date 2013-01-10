package com.xtremelabs.robolectric.res;


import android.view.MenuItem;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.tester.android.view.TestMenu;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.TestUtil.systemResources;
import static com.xtremelabs.robolectric.util.TestUtil.testResources;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(TestRunners.WithDefaults.class)
public class MenuResourceLoaderTest {

    private ResourceLoader resourceLoader;

    @Before
    public void setUp() throws Exception {
        resourceLoader = new PackageResourceLoader(testResources(), systemResources());
    }

    @Test
    public void shouldInflateComplexMenu() throws Exception {
        TestMenu testMenu = new TestMenu();
    	resourceLoader.inflateMenu(Robolectric.application, R.menu.test_withchilds, testMenu);
    	assertThat(testMenu.size(), equalTo(4));
    }

	@Test
    public void shouldParseSubItemCorrectly() throws Exception {
        TestMenu testMenu = new TestMenu();
    	resourceLoader.inflateMenu(Robolectric.application, R.menu.test_withchilds, testMenu);
    	MenuItem mi = testMenu.findItem(R.id.test_submenu_1);
    	assertTrue(mi.hasSubMenu());
    	assertThat(mi.getSubMenu().size(), equalTo(2) );
    	assertThat(mi.getSubMenu().getItem(1).getTitle() + "", equalTo("Test menu item 3") );
    }
}
