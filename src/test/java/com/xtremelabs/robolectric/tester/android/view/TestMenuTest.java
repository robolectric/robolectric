package com.xtremelabs.robolectric.tester.android.view;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.view.TestMenuItem;

import android.app.Activity;


@RunWith(WithTestDefaultsRunner.class)
public class TestMenuTest {

    @Test
    public void addAndRemoveMenuItems() {
        TestMenu testMenu = new TestMenu(new MyActivity());
        //TestMenuItem testMenuItem = new TestMenuItem(R.id.menu_about);
        testMenu.add(0, 10,0,0);

        TestMenuItem testMenuItem = (TestMenuItem) testMenu.findItem(10);
        
        Assert.assertEquals(10, testMenuItem.getItemId());
        
        testMenu.removeItem(10);
        
        testMenuItem = (TestMenuItem) testMenu.findItem(10);
        Assert.assertNull(testMenuItem);

    }
    
    private static class MyActivity extends Activity {
        @Override protected void onDestroy() {
            super.onDestroy();
        }
    }

}
