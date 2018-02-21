package org.robolectric.integration_tests.atsl;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ActivityTestRule} on Robolectric. */
@RunWith(AndroidJUnit4.class)
public class ActivityTestRuleTest {

  private static Collection<String> callbacks = new ArrayList<>();

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
    @Override
    public void onCreate(Bundle args) {
      super.onCreate(args);
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
      super.onWindowFocusChanged(hasFocus);
      callbacks.add("onWindowFocusChanged " + hasFocus);
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
            "beforeActivityLaunched", "onCreate", "onStart", "onResume",
            "onWindowFocusChanged true", "afterActivityLaunched");
  }
}
