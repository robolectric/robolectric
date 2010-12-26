package com.xtremelabs.robolectric.shadows;

import android.app.Application;
import android.view.MenuInflater;
import com.xtremelabs.robolectric.view.TestMenu;
import com.xtremelabs.robolectric.view.TestMenuItem;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.res.ResourceLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class MenuInflaterTest {
    private MenuInflater MenuInflater;

    @Before
    public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();
        Robolectric.application = ShadowApplication.bind(new Application(), new ResourceLoader(R.class, new File("test/res")));
        MenuInflater = new MenuInflater(Robolectric.application);
    }

	@Test
	public void canRetrieveMenuList()
	{
        TestMenu menu = new TestMenu();
		MenuInflater.inflate(R.menu.test, menu);
		
        TestMenuItem testMenuItem = (TestMenuItem) menu.getItem(0);
        assertEquals("Test menu item 1", testMenuItem.getTitle().toString());
        testMenuItem.click();

		testMenuItem = (TestMenuItem) menu.getItem(1);
        assertEquals("Test menu item 2", testMenuItem.getTitle().toString());
        testMenuItem.click();
	}

}
