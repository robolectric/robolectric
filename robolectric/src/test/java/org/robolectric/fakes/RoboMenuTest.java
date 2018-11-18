package org.robolectric.fakes;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AndroidJUnit4.class)
public class RoboMenuTest {

  @Test
  public void addAndRemoveMenuItems() {
    RoboMenu menu = new RoboMenu(ApplicationProvider.getApplicationContext());
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
    RoboMenu menu = new RoboMenu(ApplicationProvider.getApplicationContext());
    menu.addSubMenu(9, 10, 0, org.robolectric.R.string.ok);

    RoboMenuItem item = (RoboMenuItem) menu.findItem(10);

    assertThat(item.getGroupId()).isEqualTo(9);
    assertThat(item.getItemId()).isEqualTo(10);
  }

  @Test
  public void clickWithIntent() {
    Activity a = Robolectric.buildActivity(Activity.class).get();
    RoboMenu menu = new RoboMenu(a);
    menu.add(0, 10, 0, org.robolectric.R.string.ok);

    RoboMenuItem item = (RoboMenuItem) menu.findItem(10);
    Assert.assertNull(item.getIntent());

    Intent intent = new Intent(a, Activity.class);
    item.setIntent(intent);
    item.click();

    Assert.assertNotNull(item);

    Intent startedIntent = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(startedIntent);
  }

  @Test
  public void add_AddsItemsInOrder() {
    RoboMenu menu = new RoboMenu(ApplicationProvider.getApplicationContext());
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
}
