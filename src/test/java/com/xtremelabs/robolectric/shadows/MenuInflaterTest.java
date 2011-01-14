package com.xtremelabs.robolectric.shadows;

import android.view.MenuInflater;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.view.TestMenu;
import com.xtremelabs.robolectric.view.TestMenuItem;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class MenuInflaterTest {
    @Test
    public void canRetrieveMenuList() {
        TestMenu menu = new TestMenu();
        new MenuInflater(Robolectric.application).inflate(R.menu.test, menu);

        TestMenuItem testMenuItem = (TestMenuItem) menu.getItem(0);
        assertEquals("Test menu item 1", testMenuItem.getTitle().toString());
        testMenuItem.click();

        testMenuItem = (TestMenuItem) menu.getItem(1);
        assertEquals("Test menu item 2", testMenuItem.getTitle().toString());
        testMenuItem.click();
    }

}
