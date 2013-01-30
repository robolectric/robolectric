package com.xtremelabs.robolectric.tester.android.view;

import android.app.Activity;
import android.content.Intent;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.shadows.ShadowActivity;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(TestRunners.WithDefaults.class)
public class TestMenuTest {

    @Test
    public void addAndRemoveMenuItems() {
        TestMenu testMenu = new TestMenu(new MyActivity());
        //TestMenuItem testMenuItem = new TestMenuItem(R.id.menu_about);
        testMenu.add(0, 10, 0, com.xtremelabs.robolectric.R.string.ok);

        TestMenuItem testMenuItem = (TestMenuItem) testMenu.findItem(10);

        Assert.assertEquals(10, testMenuItem.getItemId());

        testMenu.removeItem(10);

        testMenuItem = (TestMenuItem) testMenu.findItem(10);
        Assert.assertNull(testMenuItem);

    }

    @Test
    public void clickWithIntent() {
        MyActivity activity = new MyActivity();

        TestMenu testMenu = new TestMenu(activity);
        testMenu.add(0, 10, 0, com.xtremelabs.robolectric.R.string.ok);

        TestMenuItem testMenuItem = (TestMenuItem) testMenu.findItem(10);
        Assert.assertNull(testMenuItem.getIntent());

        Intent intent = new Intent(activity, MyActivity.class);
        testMenuItem.setIntent(intent);
        testMenuItem.click();

        Assert.assertNotNull(testMenuItem);

        ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNotNull(startedIntent);
    }

    private static class MyActivity extends Activity {
        @Override
        protected void onDestroy() {
            super.onDestroy();
        }
    }

}
