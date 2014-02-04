package org.robolectric.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
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
  private static final Transcript transcript = new Transcript();
  private final ComponentName componentName = new ComponentName("org.robolectric", MyActivity.class.getName());
  private final ActivityController<MyActivity> controller = Robolectric.buildActivity(MyActivity.class);

  @Before
  public void setUp() throws Exception {
    transcript.clear();
  }

  @Test
  public void shouldSetIntent() throws Exception {
    MyActivity myActivity = controller.create().get();
    assertThat(myActivity.getIntent()).isNotNull();
    assertThat(myActivity.getIntent().getComponent()).isEqualTo(componentName);
  }

  @Test
  public void shouldSetIntentComponentWithCustomIntentWithoutComponentSet() throws Exception {
    MyActivity myActivity = controller.withIntent(new Intent(Intent.ACTION_VIEW)).create().get();
    assertThat(myActivity.getIntent().getAction()).isEqualTo(Intent.ACTION_VIEW);
    assertThat(myActivity.getIntent().getComponent()).isEqualTo(componentName);
  }

  @Test
  public void shouldSetIntentForGivenActivityInstance() throws Exception {
    ActivityController<MyActivity> activityController = ActivityController.of(new MyActivity()).create();
    assertThat(activityController.get().getIntent()).isNotNull();
  }

  @Test
  public void whenLooperIsNotPaused_shouldCreateWithMainLooperPaused() throws Exception {
    Robolectric.unPauseMainLooper();
    controller.create();
    assertThat(shadowOf(Looper.getMainLooper()).isPaused()).isFalse();
    transcript.assertEventsInclude("finishedOnCreate", "onCreate");
  }

  @Test
  public void whenLooperIsAlreadyPaused_shouldCreateWithMainLooperPaused() throws Exception {
    Robolectric.pauseMainLooper();
    controller.create();
    assertThat(shadowOf(Looper.getMainLooper()).isPaused()).isTrue();
    transcript.assertEventsInclude("finishedOnCreate");

    Robolectric.unPauseMainLooper();
    transcript.assertEventsInclude("onCreate");
  }

  @Test
  public void visible_addsTheDecorViewToTheWindowManager() {
    controller.create().visible();
    assertEquals(controller.get().getWindow().getDecorView().getParent().getClass().getName(), "android.view.ViewRootImpl");
  }

  @Test
  public void start_callsPerformStartWhilePaused() {
    controller.attach().create().start();
    transcript.assertEventsInclude("finishedOnStart", "onStart");
  }

  @Test
  public void stop_callsPerformStopWhilePaused() {
    controller.attach().create().start().stop();
    transcript.assertEventsInclude("finishedOnStop", "onStop");
  }

  @Test
  public void restart_callsPerformRestartWhilePaused() {
    controller.attach().create().start().stop().restart();
    transcript.assertEventsInclude("finishedOnRestart", "onRestart");
  }

  @Test
  public void pause_callsPerformPauseWhilePaused() {
    controller.attach().create().pause();
    transcript.assertEventsInclude("finishedOnPause", "onPause");
  }

  @Test
  public void resume_callsPerformResumeWhilePaused() {
    controller.attach().create().start().resume();
    transcript.assertEventsInclude("finishedOnResume", "onResume");
  }

  @Test
  public void destroy_callsPerformDestroyWhilePaused() {
    controller.attach().create().destroy();
    transcript.assertEventsInclude("finishedOnDestroy", "onDestroy");
  }

  @Test
  public void postCreate_callsOnPostCreateWhilePaused() {
    controller.attach().create().postCreate(new Bundle());
    transcript.assertEventsInclude("finishedOnPostCreate", "onPostCreate");
  }

  @Test
  public void postResume_callsOnPostResumeWhilePaused() {
    controller.attach().create().postResume();
    transcript.assertEventsInclude("finishedOnPostResume", "onPostResume");
  }

  @Test
  public void restoreInstanceState_callsPerformRestoreInstanceStateWhilePaused() {
    controller.attach().create().restoreInstanceState(new Bundle());
    transcript.assertEventsInclude("finishedOnRestoreInstanceState", "onRestoreInstanceState");
  }

  @Test
  public void newIntent_callsOnNewIntentWhilePaused() {
    controller.attach().create().newIntent(new Intent(Intent.ACTION_VIEW));
    transcript.assertEventsInclude("finishedOnNewIntent", "onNewIntent");
  }

  @Test
  public void userLeaving_callsPerformUserLeavingWhilePaused() {
    controller.attach().create().userLeaving();
    transcript.assertEventsInclude("finishedOnUserLeaveHint", "onUserLeaveHint");
  }

  public static class MyActivity extends Activity {
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      transcribeWhilePaused("onRestoreInstanceState");
      transcript.add("finishedOnRestoreInstanceState");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      transcribeWhilePaused("onCreate");
      transcript.add("finishedOnCreate");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
      super.onPostCreate(savedInstanceState);
      transcribeWhilePaused("onPostCreate");
      transcript.add("finishedOnPostCreate");
    }

    @Override
    protected void onPostResume() {
      super.onPostResume();
      transcribeWhilePaused("onPostResume");
      transcript.add("finishedOnPostResume");
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();
      transcribeWhilePaused("onDestroy");
      transcript.add("finishedOnDestroy");
    }

    @Override
    protected void onStart() {
      super.onStart();
      transcribeWhilePaused("onStart");
      transcript.add("finishedOnStart");
    }

    @Override
    protected void onStop() {
      super.onStop();
      transcribeWhilePaused("onStop");
      transcript.add("finishedOnStop");
    }

    @Override
    protected void onResume() {
      super.onResume();
      transcribeWhilePaused("onResume");
      transcript.add("finishedOnResume");
    }

    @Override
    protected void onRestart() {
      super.onRestart();
      transcribeWhilePaused("onRestart");
      transcript.add("finishedOnRestart");
    }

    @Override
    protected void onPause() {
      super.onPause();
      transcribeWhilePaused("onPause");
      transcript.add("finishedOnPause");
    }

    @Override
    protected void onNewIntent(Intent intent) {
      super.onNewIntent(intent);
      transcribeWhilePaused("onNewIntent");
      transcript.add("finishedOnNewIntent");
    }

    @Override
    protected void onUserLeaveHint() {
      super.onUserLeaveHint();
      transcribeWhilePaused("onUserLeaveHint");
      transcript.add("finishedOnUserLeaveHint");
    }

    private void transcribeWhilePaused(final String event) {
      runOnUiThread(new Runnable() {
        @Override public void run() {
          transcript.add(event);
        }
      });
    }
  }
}
