package org.robolectric.util;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ActivityControllerTest {
  private static Transcript transcript;

  @Test public void whenLooperIsNotPaused_shouldCreateTestsWithMainLooperPaused() throws Exception {
    transcript = new Transcript();

    Robolectric.unPauseMainLooper();
    Robolectric.buildActivity(MyActivity.class).create();
    assertThat(shadowOf(Looper.getMainLooper()).isPaused()).isFalse();

    transcript.assertEventsSoFar("finished creating", "looper call");
  }

  @Test public void whenLooperIsAlreadyPaused_shouldCreateTestsWithMainLooperPaused() throws Exception {
    transcript = new Transcript();

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
