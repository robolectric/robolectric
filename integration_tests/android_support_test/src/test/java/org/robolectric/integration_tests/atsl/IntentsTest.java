package org.robolectric.integration_tests.atsl;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static org.junit.Assert.fail;

import android.content.Intent;
import android.support.test.espresso.intent.Intents;
import android.support.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for using ATSL's espresso intents API on Robolectric. */
@RunWith(AndroidJUnit4.class)
public class IntentsTest {

  @Before
  public void setUp() {
    Intents.init();
  }

  @After
  public void tearDown() {
    Intents.release();
  }

  @Test
  public void testNoIntents() {
    Intents.assertNoUnverifiedIntents();
  }

  @Test
  public void testIntendedFailEmpty() {
    try {
      Intents.intended(org.hamcrest.Matchers.any(Intent.class));
    } catch (AssertionError e) {
      // expected
      return;
    }
    fail("AssertionError not thrown");
  }

  @Test
  public void testIntendedSuccess() {
    Intent i = new Intent();
    i.setAction(Intent.ACTION_VIEW);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getTargetContext().startActivity(i);
    Intents.intended(hasAction(Intent.ACTION_VIEW));
  }

  @Test
  public void testIntendedNotMatching() {
    Intent i = new Intent();
    i.setAction(Intent.ACTION_VIEW);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getTargetContext().startActivity(i);
    try {
      Intents.intended(hasAction(Intent.ACTION_AIRPLANE_MODE_CHANGED));
    } catch (AssertionError e) {
      // expected
      return;
    }
    fail("intended did not throw");
  }
}
