package org.robolectric.integrationtests.axt;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Integration tests for {@link ActivityTestRule} that verify it behaves consistently on device and
 * Robolectric. */
@RunWith(AndroidJUnit4.class)
public class ActivityTestRuleTest {

  private static final List<String> callbacks = new ArrayList<>();

  @Rule
  public ActivityTestRule<TranscriptActivity> rule =
      new ActivityTestRule<TranscriptActivity>(TranscriptActivity.class, false, false) {
        @Override
        protected void beforeActivityLaunched() {
          super.beforeActivityLaunched();
          callbacks.add("beforeActivityLaunched");
        }

        @Override
        protected void afterActivityLaunched() {
          callbacks.add("afterActivityLaunched");
          super.afterActivityLaunched();
        }

        @Override
        protected void afterActivityFinished() {
          callbacks.add("afterActivityFinished");
          super.afterActivityFinished();
        }
      };

  public static class TranscriptActivity extends Activity {
    Bundle receivedBundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      receivedBundle = savedInstanceState;
      callbacks.add("onCreate");
    }

    @Override
    public void onStart() {
      super.onStart();
     callbacks.add("onStart");
    }

    @Override
    public void onResume() {
      super.onResume();
      callbacks.add("onResume");
    }

    @Override
    public void onPause() {
      super.onPause();
      callbacks.add("onPause");
    }

    @Override
    public void onStop() {
      super.onStop();
      callbacks.add("onStop");
    }

    @Override
    public void onRestart() {
      super.onRestart();
      callbacks.add("onRestart");
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      callbacks.add("onDestroy");
    }
  }

  @Before
  public void setUp() {
    callbacks.clear();
  }

  @Test
  public void launchActivity_callbackSequence() {
    TranscriptActivity activity = rule.launchActivity(null);
    assertThat(activity).isNotNull();
    assertThat(callbacks)
        .containsExactly(
            "beforeActivityLaunched", "onCreate", "onStart", "onResume", "afterActivityLaunched");
  }

  /**
   * Starting an activity with options is currently not supported, so check that received bundle is
   * always null in both modes.
   */
  @Test
  public void launchActivity_bundle() {
    TranscriptActivity activity = rule.launchActivity(null);
    assertThat(activity.receivedBundle).isNull();
  }

  @Test public void launchActivity_intentExtras() {
    Intent intent = new Intent();
    intent.putExtra("Key", "Value");

    TranscriptActivity activity = rule.launchActivity(intent);

    Intent activityIntent = activity.getIntent();
    assertThat(activityIntent.getExtras()).isNotNull();
    assertThat(activityIntent.getStringExtra("Key")).isEqualTo("Value");
  }

  @Test
  public void finishActivity() {
    rule.launchActivity(null);
    callbacks.clear();
    rule.finishActivity();

    assertThat(callbacks).contains("afterActivityFinished");
    // TODO: On-device this will also invoke onPause windowFocusChanged false
    // need to track activity state and respond accordingly in robolectric
  }

  @Test
  @Ignore // javadoc for ActivityTestRule#finishActivity is incorrect
  public void finishActivity_notLaunched() {
    try {
      rule.finishActivity();
      fail("exception not thrown");
    } catch (IllegalStateException e) {
      // expected
    }
  }
}
