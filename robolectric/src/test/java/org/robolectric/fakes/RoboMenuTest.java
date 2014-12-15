package org.robolectric.fakes;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class RoboMenuTest {

  @Test
  public void addAndRemoveMenuItems() {
    RoboMenu menu = new RoboMenu(new MyActivity());
    menu.add(9, 10, 0, org.robolectric.R.string.ok);

    RoboMenuItem item = (RoboMenuItem) menu.findItem(10);

    assertThat(item.getGroupId()).isEqualTo(9);
    assertThat(item.getItemId()).isEqualTo(10);

    menu.removeItem(10);

    item = (RoboMenuItem) menu.findItem(10);
    Assert.assertNull(item);
  }

  @Test
  public void addSubMenu() {
    RoboMenu menu = new RoboMenu(new MyActivity());
    menu.addSubMenu(9, 10, 0, org.robolectric.R.string.ok);

    RoboMenuItem item = (RoboMenuItem) menu.findItem(10);

    assertThat(item.getGroupId()).isEqualTo(9);
    assertThat(item.getItemId()).isEqualTo(10);
  }

  @Test
  public void clickWithIntent() {
    MyActivity activity = new MyActivity();

    RoboMenu menu = new RoboMenu(activity);
    menu.add(0, 10, 0, org.robolectric.R.string.ok);

    RoboMenuItem item = (RoboMenuItem) menu.findItem(10);
    Assert.assertNull(item.getIntent());

    Intent intent = new Intent(activity, MyActivity.class);
    item.setIntent(intent);
    item.click();

    Assert.assertNotNull(item);

    ShadowActivity shadowActivity = Shadows.shadowOf(activity);
    Intent startedIntent = shadowActivity.getNextStartedActivity();
    assertNotNull(startedIntent);
  }

  @Test
  public void add_AddsItemsInOrder() {
    MyActivity activity = new MyActivity();
    RoboMenu menu = new RoboMenu(activity);
    menu.add(0, 0, 1, "greeting");
    menu.add(0, 0, 0, "hell0");
    menu.add(0, 0, 0, "hello");

    MenuItem item = menu.getItem(0);
    assertEquals("hell0", item.getTitle());
    item = menu.getItem(1);
    assertEquals("hello", item.getTitle());
    item = menu.getItem(2);
    assertEquals("greeting", item.getTitle());
  }

  private static class MyActivity extends Activity {

    @Override
    protected void onDestroy() {
      super.onDestroy();
    }
  }
}
