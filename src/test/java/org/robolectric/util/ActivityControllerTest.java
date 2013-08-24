package org.robolectric.util;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Looper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
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

  @Test public void whenLooperIsAlreadyPaused_shouldCreateTestsWithMainLooperPaused() throws Exception {
    Robolectric.pauseMainLooper();
    Robolectric.buildActivity(MyActivity.class).create();
    assertThat(shadowOf(Looper.getMainLooper()).isPaused()).isTrue();

    transcript.assertEventsSoFar("finished creating");

    Robolectric.unPauseMainLooper();
    transcript.assertEventsSoFar("looper call");
  }

  @Test public void visible_addsTheDecorViewToTheWindowManager() {
    ActivityController controller = Robolectric.buildActivity(MyActivity.class).create();
    controller.visible();

    assertEquals(controller.get().getWindow().getDecorView().getParent().getClass().getName(), "android.view.ViewRootImpl");
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
