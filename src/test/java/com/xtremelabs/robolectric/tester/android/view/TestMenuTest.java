package com.xtremelabs.robolectric.tester.android.view;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.shadows.ShadowActivity;


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
    
    @Test
    public void clickWithIntent() {
    	MyActivity activity = new MyActivity();
    	
    	TestMenu testMenu = new TestMenu(activity);
        testMenu.add(0, 10,0,0);

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
    
    @Test
    public void shouldShowMenuOnMenuButtonClick(){
    	MyActivity activity = new MyActivity();
    	ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
    	
    	shadowActivity.pressMenuKey();
    	TestMenu menu = TestMenu.getLastMenu();
    	
    	assertNotNull(menu);
    	assertEquals("Test menu item 1", menu.getItem(0).getTitle());
		assertEquals("Test menu item 2", menu.getItem(1).getTitle());
		
		assertFalse(activity.option1ActionPerformed);
		menu.clickOn(0);
		assertTrue(activity.option1ActionPerformed);
		assertFalse(activity.option2ActionPerformed);
		menu.clickOn(1);
		assertTrue(activity.option2ActionPerformed);
		
		new TestMenu(activity);
		assertNull(TestMenu.getLastMenu());
    }
    
    private static class MyActivity extends Activity {
    	
    	boolean option1ActionPerformed;
    	boolean option2ActionPerformed;
    	
        @Override protected void onDestroy() {
            super.onDestroy();
        }

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.test, menu);
			return true;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			if(item.getTitle().equals("Test menu item 1")){
				option1ActionPerformed = true;
			}
			if(item.getTitle().equals("Test menu item 2")){
				option2ActionPerformed = true;
			}
			return true;
		}
        
    }
    
}
