package org.robolectric.util;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.widget.LinearLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.internal.runtime.Api19RuntimeAdapter;
import org.robolectric.internal.runtime.RuntimeAdapter;
import org.robolectric.internal.runtime.RuntimeAdapterFactory;
import org.robolectric.shadows.CoreShadowsAdapter;
import org.robolectric.shadows.ShadowLooper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

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
  @Config(manifest = Config.NONE)
  public void canCreateActivityNotListedInManifest() {
    ActivityController<Activity> activityController = Robolectric.buildActivity(Activity.class);
    assertThat(activityController.setup()).isNotNull();
  }

  public static class TestDelayedPostActivity extends Activity {
    TestRunnable r1 = new TestRunnable();
    TestRunnable r2 = new TestRunnable();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Handler h = new Handler();
      h.post(r1);
      h.postDelayed(r2, 60000);
    }
  }

  @Test
  public void pendingTasks_areRunEagerly_whenActivityIsStarted_andSchedulerUnPaused() {
    final Scheduler s = Robolectric.getForegroundThreadScheduler();
    final long startTime = s.getCurrentTime();
    TestDelayedPostActivity activity = Robolectric.setupActivity(TestDelayedPostActivity.class);
    assertThat(activity.r1.wasRun).as("immediate task").isTrue();
    assertThat(s.getCurrentTime()).as("currentTime").isEqualTo(startTime);
  }

  @Test
  public void delayedTasks_areNotRunEagerly_whenActivityIsStarted_andSchedulerUnPaused() {
    // Regression test for issue #1509
    final Scheduler s = Robolectric.getForegroundThreadScheduler();
    final long startTime = s.getCurrentTime();
    TestDelayedPostActivity activity = Robolectric.setupActivity(TestDelayedPostActivity.class);
    assertThat(activity.r2.wasRun).as("before flush").isFalse();
    assertThat(s.getCurrentTime()).as("currentTime before flush").isEqualTo(startTime);
    s.advanceToLastPostedRunnable();
    assertThat(activity.r2.wasRun).as("after flush").isTrue();
    assertThat(s.getCurrentTime()).as("currentTime after flush").isEqualTo(startTime + 60000);
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
    ActivityController<MyActivity> activityController = ActivityController.of(new CoreShadowsAdapter(), new MyActivity()).create();
    assertThat(activityController.get().getIntent()).isNotNull();
  }

  @Test
  public void whenLooperIsNotPaused_shouldCreateWithMainLooperPaused() throws Exception {
    ShadowLooper.unPauseMainLooper();
    controller.create();
    assertThat(shadowOf(Looper.getMainLooper()).isPaused()).isFalse();
    transcript.assertEventsInclude("finishedOnCreate", "onCreate");
  }

  @Test
  public void whenLooperIsAlreadyPaused_shouldCreateWithMainLooperPaused() throws Exception {
    ShadowLooper.pauseMainLooper();
    controller.create();
    assertThat(shadowOf(Looper.getMainLooper()).isPaused()).isTrue();
    transcript.assertEventsInclude("finishedOnCreate");

    ShadowLooper.unPauseMainLooper();
    transcript.assertEventsInclude("onCreate");
  }

  @Test
  public void withApplication_attachesTestApplicationToActivity() {
    Application application = new Application();
    MyActivity activity = controller.withApplication(application).create().get();
    assertThat(activity.getApplication()).isEqualTo(application);
  }

  @Test
  public void withApplication_setsBaseContext() {
    Application application = new Application();
    controller.withApplication(application).create().get();
    assertThat(application.getBaseContext()).isNotNull();
  }

  @Test
  public void withApplication_bindsResourcesAndAssets() {
    Application application = new Application();
    controller.withApplication(application).create().get();
    assertThat(application.getBaseContext().getAssets()).isNotNull();
    assertThat(application.getBaseContext().getResources()).isNotNull();
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

  @Test
  public void setup_callsLifecycleMethodsAndMakesVisible() {
    controller.setup();
    transcript.assertEventsInclude("onCreate", "onStart", "onPostCreate", "onResume", "onPostResume");
    assertEquals(controller.get().getWindow().getDecorView().getParent().getClass().getName(), "android.view.ViewRootImpl");
  }

  @Test
  public void setupWithBundle_callsLifecycleMethodsAndMakesVisible() {
    controller.setup(new Bundle());
    transcript.assertEventsInclude("onCreate", "onStart", "onRestoreInstanceState", "onPostCreate", "onResume", "onPostResume");
    assertEquals(controller.get().getWindow().getDecorView().getParent().getClass().getName(), "android.view.ViewRootImpl");
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.KITKAT)
  public void attach_shouldWorkWithAPI19() {
    MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
    assertThat(activity).isNotNull();
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.KITKAT)
  public void shouldUseCorrectRuntimeAdapter() {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 15);
    MyActivity activity = Robolectric.buildActivity(MyActivity.class).setup().get();
    assertThat(activity).isNotNull();
    RuntimeAdapter adapter = RuntimeAdapterFactory.getInstance();
    assertThat(adapter.getClass().getName()).isEqualTo(Api19RuntimeAdapter.class.getName());
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
      getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
      setContentView(new LinearLayout(RuntimeEnvironment.application));
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
