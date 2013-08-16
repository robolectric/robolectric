package org.robolectric.util;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Looper;
import android.view.Window;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ActivityControllerTest {
  private static Transcript transcript;

  @Before
  public void setUp() throws Exception {
    transcript = new Transcript();
  }

  @Test public void shouldSetIntent() throws Exception {
    MyActivity myActivity = Robolectric.buildActivity(MyActivity.class).create().get();
    assertThat(myActivity.getIntent()).isNotNull();
    assertThat(myActivity.getIntent().getComponent())
        .isEqualTo(new ComponentName("org.robolectric", MyActivity.class.getName()));
  }

  @Test public void whenLooperIsNotPaused_shouldCreateTestsWithMainLooperPaused() throws Exception {
    Robolectric.unPauseMainLooper();
    Robolectric.buildActivity(MyActivity.class).create();
    assertThat(shadowOf(Looper.getMainLooper()).isPaused()).isFalse();

    transcript.assertEventsSoFar("finished creating", "looper call");
  }

  @Test public void createsAnActivityThatHasAnActionBar() throws Exception {
    MyActivity myActivity = Robolectric.buildActivity(MyActivity.class).create().get();
    myActivity.getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
    ActionBar actionBar = myActivity.getActionBar();
    assertThat(actionBar).isNotNull();
  }

  @Test public void whenLooperIsAlreadyPaused_shouldCreateTestsWithMainLooperPaused() throws Exception {
    Robolectric.pauseMainLooper();
    Robolectric.buildActivity(MyActivity.class).create();
    assertThat(shadowOf(Looper.getMainLooper()).isPaused()).isTrue();

    transcript.assertEventsSoFar("finished creating");

    Robolectric.unPauseMainLooper();
    transcript.assertEventsSoFar("looper call");
  }

  public static class MyActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
      runOnUiThread(new Runnable() {
        @Override public void run() {
          transcript.add("looper call");
        }
      });
      transcript.add("finished creating");
    }
  }
}
