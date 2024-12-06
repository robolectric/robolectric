package org.robolectric.integrationtests.axt;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.intent.Intents.getIntents;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.ext.truth.content.IntentCorrespondences.action;
import static androidx.test.ext.truth.content.IntentCorrespondences.all;
import static androidx.test.ext.truth.content.IntentCorrespondences.data;
import static androidx.test.ext.truth.content.IntentSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.Iterables;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for using androidx.test's espresso intents API on Robolectric. */
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
    assertThrows(
        AssertionError.class, () -> Intents.intended(org.hamcrest.Matchers.any(Intent.class)));
  }

  @Test
  public void testIntendedSuccess() {
    Intent i = new Intent();
    i.setAction(Intent.ACTION_VIEW);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getApplicationContext().startActivity(i);
    Intents.intended(hasAction(Intent.ACTION_VIEW));
  }

  @Test
  public void testIntendedNotMatching() {
    Intent i = new Intent();
    i.setAction(Intent.ACTION_VIEW);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getApplicationContext().startActivity(i);
    assertThrows(
        AssertionError.class,
        () -> Intents.intended(hasAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)));
  }

  /**
   * Variant of testIntendedSuccess that uses truth APIs.
   *
   * <p>In this form the test verifies that only a single intent was sent.
   */
  @Test
  public void testIntendedSuccess_truth() {
    Intent i = new Intent();
    i.setAction(Intent.ACTION_VIEW);
    i.putExtra("ignoreextra", "");
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getApplicationContext().startActivity(i);
    assertThat(Iterables.getOnlyElement(getIntents())).hasAction(Intent.ACTION_VIEW);
  }

  /**
   * Variant of testIntendedSuccess that uses truth APIs.
   *
   * <p>This is a more flexible/lenient variant of {@link #testIntendedSuccess_truth} that handles
   * cases where other intents might have been sent.
   */
  @Test
  public void testIntendedSuccess_truthCorrespondence() {
    Intent i = new Intent();
    i.setAction(Intent.ACTION_VIEW);
    i.putExtra("ignoreextra", "");
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getApplicationContext().startActivity(i);
    Intent alsoSentIntentButDontCare = new Intent(Intent.ACTION_MAIN);
    alsoSentIntentButDontCare.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    getApplicationContext().startActivity(alsoSentIntentButDontCare);
    assertThat(getIntents())
        .comparingElementsUsing(action())
        .contains(new Intent(Intent.ACTION_VIEW));
  }

  /** Variant of testIntendedSuccess_truthCorrespondence that uses chained Correspondences. */
  @Test
  public void testIntendedSuccess_truthChainedCorrespondence() {
    Intent i = new Intent();
    i.setAction(Intent.ACTION_VIEW);
    i.putExtra("ignoreextra", "");
    i.setData(Uri.parse("http://robolectric.org"));
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getApplicationContext().startActivity(i);
    Intent alsoSentIntentButNotMatching = new Intent(Intent.ACTION_VIEW);
    alsoSentIntentButNotMatching.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getApplicationContext().startActivity(alsoSentIntentButNotMatching);

    Intent expectedIntent =
        new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://robolectric.org"));
    assertThat(getIntents()).comparingElementsUsing(all(action(), data())).contains(expectedIntent);
  }

  /** Activity that captures calls to {#onActivityResult() } */
  public static class ResultCapturingActivity extends Activity {

    private ActivityResult activityResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
      super.onStart();
    }

    @Override
    protected void onResume() {
      super.onResume();
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();
    }

    @Override
    protected void onPause() {
      super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      activityResult = new ActivityResult(resultCode, data);
    }
  }

  /** Dummy activity whose calls we intent to we're stubbing out. */
  public static class DummyActivity extends Activity {}

  @Test
  public void intending_callsOnActivityResult() {
    intending(hasComponent(hasClassName(DummyActivity.class.getName())))
        .respondWith(new ActivityResult(Activity.RESULT_OK, new Intent().putExtra("key", 123)));

    ActivityScenario<ResultCapturingActivity> activityScenario =
        ActivityScenario.launch(ResultCapturingActivity.class);

    activityScenario.onActivity(
        activity -> activity.startActivityForResult(new Intent(activity, DummyActivity.class), 0));

    activityScenario.onActivity(
        activity -> {
          assertThat(activity.activityResult.getResultCode()).isEqualTo(Activity.RESULT_OK);
          assertThat(activity.activityResult.getResultData()).extras().containsKey("key");
        });
  }

  @Test
  public void browserIntentNotResolved() {
    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
    browserIntent.setData(Uri.parse("http://www.robolectric.org"));
    browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    Intents.intending(hasAction(Intent.ACTION_VIEW))
        .respondWithFunction(
            intent -> {
              throw new ActivityNotFoundException();
            });

    assertThrows(
        ActivityNotFoundException.class,
        () -> getApplicationContext().startActivity(browserIntent));
  }
}
