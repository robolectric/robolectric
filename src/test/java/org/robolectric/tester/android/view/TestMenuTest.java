package org.robolectric.tester.android.view;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.shadows.ShadowActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class TestMenuTest {

  @Test
  public void addAndRemoveMenuItems() {
    TestMenu testMenu = new TestMenu(new MyActivity());
    //TestMenuItem testMenuItem = new TestMenuItem(R.id.menu_about);
    testMenu.add(9, 10, 0, org.robolectric.R.string.ok);

    TestMenuItem testMenuItem = (TestMenuItem) testMenu.findItem(10);

    assertThat(testMenuItem.getGroupId()).isEqualTo(9);
    assertThat(testMenuItem.getItemId()).isEqualTo(10);

    testMenu.removeItem(10);

    testMenuItem = (TestMenuItem) testMenu.findItem(10);
    Assert.assertNull(testMenuItem);
  }

  @Test
  public void addSubMenu() {
    TestMenu testMenu = new TestMenu(new MyActivity());
    testMenu.addSubMenu(9, 10, 0, org.robolectric.R.string.ok);

    TestMenuItem testMenuItem = (TestMenuItem) testMenu.findItem(10);

    assertThat(testMenuItem.getGroupId()).isEqualTo(9);
    assertThat(testMenuItem.getItemId()).isEqualTo(10);
  }

  @Test
  public void clickWithIntent() {
    MyActivity activity = new MyActivity();

    TestMenu testMenu = new TestMenu(activity);
    testMenu.add(0, 10, 0, org.robolectric.R.string.ok);

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
  public void add_AddsItemsInOrder()
  {
    MyActivity activity = new MyActivity();

    TestMenu testMenu = new TestMenu(activity);
    testMenu.add(0, 0, 1, "greeting");
    testMenu.add(0, 0, 0, "hell0");
    testMenu.add(0, 0, 0, "hello");

    MenuItem item = testMenu.getItem(0);
    assertEquals("hell0", item.getTitle());
    item = testMenu.getItem(1);
    assertEquals("hello", item.getTitle());
    item = testMenu.getItem(2);
    assertEquals("greeting", item.getTitle());
  }

  private static class MyActivity extends Activity {
    @Override
    protected void onDestroy() {
      super.onDestroy();
    }
  }
}
