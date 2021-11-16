package org.robolectric.android.controller;

import android.app.Activity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

/**
 * This test captures an issue where {@link ActivityController#recreate()} would throw an {@link
 * UnsupportedOperationException} if an Activity from a previous test was recreated.
 */
@RunWith(AndroidJUnit4.class)
public class ActivityControllerRecreateTest {
  private static final AtomicReference<ActivityController<Activity>> createdActivity =
      new AtomicReference<>();

  @Before
  public void setUp() {
    createdActivity.compareAndSet(null, Robolectric.buildActivity(Activity.class).create());
  }

  @Test
  public void failsTryingToRecreateActivityFromOtherTest1() {
    createdActivity.get().recreate();
  }

  @Test
  public void failsTryingToRecreateActivityFromOtherTest2() {
    createdActivity.get().recreate();
  }
}
