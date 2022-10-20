package org.robolectric.android.controller;

import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.ViewRootImpl;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowWindowManagerImpl;
import org.robolectric.util.Scheduler;
import org.robolectric.util.TestRunnable;

@RunWith(AndroidJUnit4.class)
public class ActivityControllerTest {
  private static final List<String> transcript = new ArrayList<>();
  private final ComponentName componentName =
      new ComponentName("org.robolectric", MyActivity.class.getName());
  private final ActivityController<MyActivity> controller =
      Robolectric.buildActivity(MyActivity.class);

  @Before
  public void setUp() throws Exception {
    transcript.clear();
  }

  @Test
  public void canCreateActivityNotListedInManifest() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    assertThat(activity).isNotNull();
    assertThat(activity.getThemeResId()).isEqualTo(R.style.Theme_Robolectric);
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
  @LooperMode(LEGACY)
  public void pendingTasks_areRunEagerly_whenActivityIsStarted_andSchedulerUnPaused() {
    final Scheduler s = Robolectric.getForegroundThreadScheduler();
    final long startTime = s.getCurrentTime();
    TestDelayedPostActivity activity = Robolectric.setupActivity(TestDelayedPostActivity.class);
    assertWithMessage("immediate task").that(activity.r1.wasRun).isTrue();
    assertWithMessage("currentTime").that(s.getCurrentTime()).isEqualTo(startTime);
  }

  @Test
  @LooperMode(LEGACY)
  public void delayedTasks_areNotRunEagerly_whenActivityIsStarted_andSchedulerUnPaused() {
    // Regression test for issue #1509
    final Scheduler s = Robolectric.getForegroundThreadScheduler();
    final long startTime = s.getCurrentTime();
    TestDelayedPostActivity activity = Robolectric.setupActivity(TestDelayedPostActivity.class);
    assertWithMessage("before flush").that(activity.r2.wasRun).isFalse();
    assertWithMessage("currentTime before flush").that(s.getCurrentTime()).isEqualTo(startTime);
    s.advanceToLastPostedRunnable();
    assertWithMessage("after flush").that(activity.r2.wasRun).isTrue();
    assertWithMessage("currentTime after flush")
        .that(s.getCurrentTime())
        .isEqualTo(startTime + 60000);
  }

  @Test
  public void shouldSetIntent() throws Exception {
    MyActivity myActivity = controller.create().get();
    assertThat(myActivity.getIntent()).isNotNull();
    assertThat(myActivity.getIntent().getComponent()).isEqualTo(componentName);
  }

  @Test
  public void shouldSetIntentComponentWithCustomIntentWithoutComponentSet() throws Exception {
    MyActivity myActivity =
        Robolectric.buildActivity(MyActivity.class, new Intent(Intent.ACTION_VIEW)).create().get();
    assertThat(myActivity.getIntent().getAction()).isEqualTo(Intent.ACTION_VIEW);
    assertThat(myActivity.getIntent().getComponent()).isEqualTo(componentName);
  }

  @Test
  public void shouldSetIntentForGivenActivityInstance() throws Exception {
    ActivityController<MyActivity> activityController =
        ActivityController.of(new MyActivity()).create();
    assertThat(activityController.get().getIntent()).isNotNull();
  }

  @Test
  @LooperMode(LEGACY)
  public void whenLooperIsNotPaused_shouldCreateWithMainLooperPaused() throws Exception {
    ShadowLooper.unPauseMainLooper();
    controller.create();
    assertThat(shadowOf(Looper.getMainLooper()).isPaused()).isFalse();
    assertThat(transcript).containsAtLeast("finishedOnCreate", "onCreate");
  }

  @Test
  public void whenLooperIsAlreadyPaused_shouldCreateWithMainLooperPaused() throws Exception {
    shadowMainLooper().pause();
    controller.create();
    assertThat(transcript).contains("finishedOnCreate");
    shadowMainLooper().idle();
    assertThat(transcript).contains("onCreate");
  }

  @Test
  public void visible_addsTheDecorViewToTheWindowManager() {
    controller.create().visible();
    assertThat(controller.get().getWindow().getDecorView().getParent().getClass())
        .isEqualTo(ViewRootImpl.class);
  }

  @Test
  public void start_callsPerformStartWhilePaused() {
    controller.create().start();
    assertThat(transcript).containsAtLeast("finishedOnStart", "onStart");
  }

  @Test
  public void stop_callsPerformStopWhilePaused() {
    controller.create().start().stop();
    assertThat(transcript).containsAtLeast("finishedOnStop", "onStop");
  }

  @Test
  public void restart_callsPerformRestartWhilePaused() {
    controller.create().start().stop().restart();
    assertThat(transcript).containsAtLeast("finishedOnRestart", "onRestart");
  }

  @Test
  public void pause_callsPerformPauseWhilePaused() {
    controller.create().pause();
    assertThat(transcript).containsAtLeast("finishedOnPause", "onPause");
  }

  @Test
  public void resume_callsPerformResumeWhilePaused() {
    controller.create().start().resume();
    assertThat(transcript).containsAtLeast("finishedOnResume", "onResume");
  }

  @Test
  public void destroy_callsPerformDestroyWhilePaused() {
    controller.create().destroy();
    assertThat(transcript).containsAtLeast("finishedOnDestroy", "onDestroy");
  }

  @Test
  @Config(minSdk = VERSION_CODES.JELLY_BEAN_MR1)
  public void destroy_cleansUpWindowManagerState() {
    WindowManager windowManager = controller.get().getWindowManager();
    ShadowWindowManagerImpl shadowWindowManager =
        ((ShadowWindowManagerImpl) shadowOf(windowManager));
    controller.create().start().resume().visible();
    assertThat(shadowWindowManager.getViews())
        .contains(controller.get().getWindow().getDecorView());
    controller.pause().stop().destroy();
    assertThat(shadowWindowManager.getViews())
        .doesNotContain(controller.get().getWindow().getDecorView());
  }

  @Test
  public void postCreate_callsOnPostCreateWhilePaused() {
    controller.create().postCreate(new Bundle());
    assertThat(transcript).containsAtLeast("finishedOnPostCreate", "onPostCreate");
  }

  @Test
  public void postResume_callsOnPostResumeWhilePaused() {
    controller.create().postResume();
    assertThat(transcript).containsAtLeast("finishedOnPostResume", "onPostResume");
  }

  @Test
  public void restoreInstanceState_callsPerformRestoreInstanceStateWhilePaused() {
    controller.create().restoreInstanceState(new Bundle());
    assertThat(transcript)
        .containsAtLeast("finishedOnRestoreInstanceState", "onRestoreInstanceState");
  }

  @Test
  public void newIntent_callsOnNewIntentWhilePaused() {
    controller.create().newIntent(new Intent(Intent.ACTION_VIEW));
    assertThat(transcript).containsAtLeast("finishedOnNewIntent", "onNewIntent");
  }

  @Test
  public void userLeaving_callsPerformUserLeavingWhilePaused() {
    controller.create().userLeaving();
    assertThat(transcript).containsAtLeast("finishedOnUserLeaveHint", "onUserLeaveHint");
  }

  @Test
  public void setup_callsLifecycleMethodsAndMakesVisible() {
    controller.setup();
    assertThat(transcript)
        .containsAtLeast("onCreate", "onStart", "onPostCreate", "onResume", "onPostResume");
    assertThat(controller.get().getWindow().getDecorView().getParent().getClass())
        .isEqualTo(ViewRootImpl.class);
  }

  @Test
  public void setupWithBundle_callsLifecycleMethodsAndMakesVisible() {
    controller.setup(new Bundle());
    assertThat(transcript)
        .containsAtLeast(
            "onCreate",
            "onStart",
            "onRestoreInstanceState",
            "onPostCreate",
            "onResume",
            "onPostResume");
    assertThat(controller.get().getWindow().getDecorView().getParent().getClass())
        .isEqualTo(ViewRootImpl.class);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.KITKAT)
  public void attach_shouldWorkWithAPI19() {
    MyActivity activity = Robolectric.buildActivity(MyActivity.class).create().get();
    assertThat(activity).isNotNull();
  }

  @Test
  public void configurationChange_callsLifecycleMethodsAndAppliesConfig() {
    Configuration config =
        new Configuration(
            ApplicationProvider.getApplicationContext().getResources().getConfiguration());
    final float newFontScale = config.fontScale *= 2;

    controller.setup();
    transcript.clear();
    controller.configurationChange(config);
    assertThat(transcript)
        .containsAtLeast(
            "onPause",
            "onStop",
            "onDestroy",
            "onCreate",
            "onStart",
            "onRestoreInstanceState",
            "onPostCreate",
            "onResume",
            "onPostResume");
    assertThat(controller.get().getResources().getConfiguration().fontScale)
        .isEqualTo(newFontScale);
  }

  @Test
  public void configurationChange_callsOnConfigurationChangedAndAppliesConfigWhenAllManaged() {
    Configuration config =
        new Configuration(
            ApplicationProvider.getApplicationContext().getResources().getConfiguration());
    final float newFontScale = config.fontScale *= 2;

    ActivityController<ConfigAwareActivity> configController =
        Robolectric.buildActivity(ConfigAwareActivity.class).setup();
    transcript.clear();
    configController.configurationChange(config);
    assertThat(transcript).containsAtLeast("onConfigurationChanged", "View.onConfigurationChanged");
    assertThat(configController.get().getResources().getConfiguration().fontScale)
        .isEqualTo(newFontScale);
  }

  @Test
  @Config(maxSdk = O_MR1)
  public void configurationChange_callsLifecycleMethodsAndAppliesConfigWhenAnyNonManaged_beforeP() {
    configurationChange_callsLifecycleMethodsAndAppliesConfigWhenAnyNonManaged(
        "onSaveInstanceState", "onStop");
  }

  @Test
  @Config(minSdk = P)
  public void configurationChange_callsLifecycleMethodsAndAppliesConfigWhenAnyNonManaged_fromP() {
    configurationChange_callsLifecycleMethodsAndAppliesConfigWhenAnyNonManaged(
        "onStop", "onSaveInstanceState");
  }

  @Test
  @Config(qualifiers = "sw600dp")
  public void noArgsConfigurationChange_appliesChangedSystemConfiguration() {
    ActivityController<ConfigAwareActivity> configController =
        Robolectric.buildActivity(ConfigAwareActivity.class).setup();
    RuntimeEnvironment.setQualifiers("sw800dp");
    configController.configurationChange();
    assertThat(configController.get().newConfig.smallestScreenWidthDp).isEqualTo(800);
  }

  @Test
  @Config(qualifiers = "land")
  public void configurationChange_restoresTheme() {
    Configuration config =
        new Configuration(
            ApplicationProvider.getApplicationContext().getResources().getConfiguration());
    config.orientation = Configuration.ORIENTATION_PORTRAIT;

    controller.get().setTheme(android.R.style.Theme_Black);
    controller.setup();
    transcript.clear();
    controller.configurationChange(config);
    int restoredTheme = shadowOf((ContextThemeWrapper) controller.get()).callGetThemeResId();
    assertThat(restoredTheme).isEqualTo(android.R.style.Theme_Black);
  }

  @Test
  @Config(qualifiers = "land")
  public void configurationChange_reattachesRetainedFragments() {
    Configuration config =
        new Configuration(
            ApplicationProvider.getApplicationContext().getResources().getConfiguration());
    config.orientation = Configuration.ORIENTATION_PORTRAIT;

    ActivityController<NonConfigStateActivity> configController =
        Robolectric.buildActivity(NonConfigStateActivity.class).setup();
    NonConfigStateActivity activity = configController.get();
    Fragment retainedFragment = activity.retainedFragment;
    Fragment otherFragment = activity.nonRetainedFragment;
    configController.configurationChange(config);
    activity = configController.get();

    assertThat(activity.retainedFragment).isNotNull();
    assertThat(activity.retainedFragment).isSameInstanceAs(retainedFragment);
    assertThat(activity.nonRetainedFragment).isNotNull();
    assertThat(activity.nonRetainedFragment).isNotSameInstanceAs(otherFragment);
  }

  @Test
  public void windowFocusChanged() {
    controller.setup();
    assertThat(transcript).doesNotContain("finishedOnWindowFocusChanged");
    assertThat(controller.get().hasWindowFocus()).isFalse();

    transcript.clear();

    controller.windowFocusChanged(true);
    assertThat(transcript).containsExactly("finishedOnWindowFocusChanged");
    assertThat(controller.get().hasWindowFocus()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void onTopActivityResumedCalledWithSetup() {
    controller.setup();
    assertThat(transcript).contains("finishedOnTopResumedActivityChanged");
  }

  @Test
  @Config(maxSdk = VERSION_CODES.P)
  public void onTopActivityResumedNotCalledWithSetupPreQ() {
    controller.setup();
    assertThat(transcript).doesNotContain("finishedOnTopResumedActivityChanged");
  }

  @Test
  public void close_transitionsActivityStateToDestroyed() {
    Robolectric.buildActivity(MyActivity.class).close();
    assertThat(transcript).isEmpty();
    transcript.clear();

    Robolectric.buildActivity(MyActivity.class).create().close();
    assertThat(transcript)
        .containsExactly("onCreate", "finishedOnCreate", "onDestroy", "finishedOnDestroy");
    transcript.clear();

    Robolectric.buildActivity(MyActivity.class).create().start().close();
    assertThat(transcript)
        .containsExactly(
            "onCreate",
            "finishedOnCreate",
            "onStart",
            "finishedOnStart",
            "onStop",
            "finishedOnStop",
            "onDestroy",
            "finishedOnDestroy");
    transcript.clear();

    Robolectric.buildActivity(MyActivity.class).setup().close();
    List<String> expectedStringList = new ArrayList<>();
    expectedStringList.add("onCreate");
    expectedStringList.add("finishedOnCreate");
    expectedStringList.add("onStart");
    expectedStringList.add("finishedOnStart");
    expectedStringList.add("onPostCreate");
    expectedStringList.add("finishedOnPostCreate");
    expectedStringList.add("onResume");
    expectedStringList.add("finishedOnResume");
    expectedStringList.add("onPostResume");
    expectedStringList.add("finishedOnPostResume");
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      expectedStringList.add("finishedOnTopResumedActivityChanged");
    }
    expectedStringList.add("onPause");
    expectedStringList.add("finishedOnPause");
    expectedStringList.add("onStop");
    expectedStringList.add("finishedOnStop");
    expectedStringList.add("onDestroy");
    expectedStringList.add("finishedOnDestroy");
    assertThat(transcript).containsExactly(expectedStringList.toArray());
  }

  @Test
  public void close_tryWithResources_getsDestroyed() {
    try (ActivityController<MyActivity> ignored =
        Robolectric.buildActivity(MyActivity.class).setup()) {
      // no-op
    }
    List<String> expectedStringList = new ArrayList<>();
    expectedStringList.add("onCreate");
    expectedStringList.add("finishedOnCreate");
    expectedStringList.add("onStart");
    expectedStringList.add("finishedOnStart");
    expectedStringList.add("onPostCreate");
    expectedStringList.add("finishedOnPostCreate");
    expectedStringList.add("onResume");
    expectedStringList.add("finishedOnResume");
    expectedStringList.add("onPostResume");
    expectedStringList.add("finishedOnPostResume");
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      expectedStringList.add("finishedOnTopResumedActivityChanged");
    }
    expectedStringList.add("onPause");
    expectedStringList.add("finishedOnPause");
    expectedStringList.add("onStop");
    expectedStringList.add("finishedOnStop");
    expectedStringList.add("onDestroy");
    expectedStringList.add("finishedOnDestroy");
    assertThat(transcript).containsExactly(expectedStringList.toArray());
  }

  private void configurationChange_callsLifecycleMethodsAndAppliesConfigWhenAnyNonManaged(
      String secondExpected, String thirdExpected) {
    Configuration config =
        new Configuration(
            ApplicationProvider.getApplicationContext().getResources().getConfiguration());
    final float newFontScale = config.fontScale *= 2;
    final int newOrientation = config.orientation = (config.orientation + 1) % 3;

    ActivityController<ConfigAwareActivity> configController =
        Robolectric.buildActivity(ConfigAwareActivity.class).setup();
    transcript.clear();
    configController.configurationChange(config);
    assertThat(transcript)
        .containsAtLeast(
            "onPause",
            secondExpected,
            thirdExpected,
            "onDestroy",
            "onCreate",
            "onStart",
            "onResume")
        .inOrder();
    assertThat(configController.get().getResources().getConfiguration().fontScale)
        .isEqualTo(newFontScale);
    assertThat(configController.get().getResources().getConfiguration().orientation)
        .isEqualTo(newOrientation);
  }

  public static class MyActivity extends Activity {
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      transcribeWhilePaused("onRestoreInstanceState");
      transcript.add("finishedOnRestoreInstanceState");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      transcribeWhilePaused("onSaveInstanceState");
      transcript.add("finishedOnSaveInstanceState");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
      setContentView(
          new LinearLayout(this) {
            @Override
            protected void onConfigurationChanged(Configuration configuration) {
              super.onConfigurationChanged(configuration);
              transcribeWhilePaused("View.onConfigurationChanged");
            }
          });
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      transcribeWhilePaused("onConfigurationChanged");
      transcript.add("finishedOnConfigurationChanged");
    }

    @Override
    public void onWindowFocusChanged(boolean newFocus) {
      super.onWindowFocusChanged(newFocus);
      transcript.add("finishedOnWindowFocusChanged");
    }

    @Override
    public void onTopResumedActivityChanged(boolean isTopResumedActivity) {
      super.onTopResumedActivityChanged(isTopResumedActivity);
      transcript.add("finishedOnTopResumedActivityChanged");
    }

    private void transcribeWhilePaused(final String event) {
      runOnUiThread(() -> transcript.add(event));
    }
  }

  public static class ConfigAwareActivity extends MyActivity {

    Configuration newConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (savedInstanceState != null) {
        assertThat(savedInstanceState.getSerializable("test")).isNotNull();
      }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putSerializable("test", new Exception());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      this.newConfig = new Configuration(newConfig);
      super.onConfigurationChanged(newConfig);
    }
  }

  public static final class NonConfigStateActivity extends Activity {
    Fragment retainedFragment;
    Fragment nonRetainedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (savedInstanceState == null) {
        retainedFragment = new Fragment();
        retainedFragment.setRetainInstance(true);
        nonRetainedFragment = new Fragment();
        getFragmentManager()
            .beginTransaction()
            .add(android.R.id.content, retainedFragment, "retained")
            .add(android.R.id.content, nonRetainedFragment, "non-retained")
            .commit();
      } else {
        retainedFragment = getFragmentManager().findFragmentByTag("retained");
        nonRetainedFragment = getFragmentManager().findFragmentByTag("non-retained");
      }
    }
  }
}
