package org.robolectric.integrationtests.axt;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.app.Activity;
import android.app.AppComponentFactory;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle.State;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Integration tests for {@link ActivityScenario} that verify it behaves consistently on device and
 * Robolectric.
 */
@RunWith(AndroidJUnit4.class)
public class ActivityScenarioTest {

  private static final List<String> callbacks = new ArrayList<>();

  public static class TranscriptActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      callbacks.add("onCreate");
    }

    @Override
    public void onStart() {
      super.onStart();
      callbacks.add("onStart");
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
      super.onPostCreate(savedInstanceState);
      callbacks.add("onPostCreate");
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
      callbacks.add("onStop " + isChangingConfigurations());
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

  public static class LifecycleOwnerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
      setTheme(R.style.Theme_AppCompat);
      super.onCreate(bundle);
    }
  }

  @Before
  public void setUp() {
    callbacks.clear();
  }

  public static class ActivityWithCustomConstructor extends Activity {
    private final int intValue;

    public ActivityWithCustomConstructor(int intValue) {
      this.intValue = intValue;
    }

    public int getIntValue() {
      return intValue;
    }
  }

  public static class CustomAppComponentFactory extends AppComponentFactory {

    @NonNull
    @Override
    public Activity instantiateActivity(
        @NonNull ClassLoader cl, @NonNull String className, @Nullable Intent intent)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException {
      if (className.contains(ActivityWithCustomConstructor.class.getName())) {
        return new ActivityWithCustomConstructor(100);
      }
      return super.instantiateActivity(cl, className, intent);
    }
  }

  @Test
  public void launch_callbackSequence() {
    try (ActivityScenario<TranscriptActivity> activityScenario =
        ActivityScenario.launch(TranscriptActivity.class)) {
      assertThat(activityScenario).isNotNull();
      assertThat(callbacks)
          .containsExactly(
              "onCreate", "onStart", "onPostCreate", "onResume", "onWindowFocusChanged true");
    }
  }

  @Test
  public void launch_pauseAndResume_callbackSequence() {
    try (ActivityScenario<TranscriptActivity> activityScenario =
        ActivityScenario.launch(TranscriptActivity.class)) {
      assertThat(activityScenario).isNotNull();
      activityScenario.moveToState(State.STARTED);
      activityScenario.moveToState(State.RESUMED);
      assertThat(callbacks)
          .containsExactly(
              "onCreate",
              "onStart",
              "onPostCreate",
              "onResume",
              "onWindowFocusChanged true",
              "onPause",
              "onResume");
    }
  }

  @Test
  public void launch_stopAndResume_callbackSequence() {
    try (ActivityScenario<TranscriptActivity> activityScenario =
        ActivityScenario.launch(TranscriptActivity.class)) {
      assertThat(activityScenario).isNotNull();
      activityScenario.moveToState(State.CREATED);
      activityScenario.moveToState(State.RESUMED);
      assertThat(callbacks)
          .containsExactly(
              "onCreate",
              "onStart",
              "onPostCreate",
              "onResume",
              "onWindowFocusChanged true",
              "onPause",
              "onStop false",
              "onRestart",
              "onStart",
              "onResume");
    }
  }

  @Test
  public void launchAlias_createTargetAndCallbackSequence() {
    Context context = ApplicationProvider.getApplicationContext();
    try (ActivityScenario<Activity> activityScenario =
        ActivityScenario.launch(
            new Intent()
                .setClassName(
                    context, "org.robolectric.integrationtests.axt.ActivityScenarioTestAlias"))) {

      assertThat(activityScenario).isNotNull();
      activityScenario.onActivity(
          activity -> assertThat(activity).isInstanceOf(TranscriptActivity.class));
      assertThat(callbacks)
          .containsExactly(
              "onCreate", "onStart", "onPostCreate", "onResume", "onWindowFocusChanged true");
    }
  }

  @Test
  public void launch_lifecycleOwnerActivity() {
    try (ActivityScenario<LifecycleOwnerActivity> activityScenario =
        ActivityScenario.launch(LifecycleOwnerActivity.class)) {
      assertThat(activityScenario).isNotNull();
      activityScenario.onActivity(
          activity ->
              assertThat(activity.getLifecycle().getCurrentState()).isEqualTo(State.RESUMED));
      activityScenario.moveToState(State.STARTED);
      activityScenario.onActivity(
          activity ->
              assertThat(activity.getLifecycle().getCurrentState()).isEqualTo(State.STARTED));
      activityScenario.moveToState(State.CREATED);
      activityScenario.onActivity(
          activity ->
              assertThat(activity.getLifecycle().getCurrentState()).isEqualTo(State.CREATED));
    }
  }

  @Test
  public void recreate_retainFragmentHostingActivity() {
    Fragment fragment = new Fragment();
    fragment.setRetainInstance(true);
    try (ActivityScenario<LifecycleOwnerActivity> activityScenario =
        ActivityScenario.launch(LifecycleOwnerActivity.class)) {
      assertThat(activityScenario).isNotNull();
      activityScenario.onActivity(
          activity -> {
            activity
                .getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, fragment)
                .commitNow();
            assertThat(activity.getSupportFragmentManager().findFragmentById(android.R.id.content))
                .isSameInstanceAs(fragment);
          });
      activityScenario.recreate();
      activityScenario.onActivity(
          activity ->
              assertThat(
                      activity.getSupportFragmentManager().findFragmentById(android.R.id.content))
                  .isSameInstanceAs(fragment));
    }
  }

  @Test
  public void recreate_nonRetainFragmentHostingActivity() {
    Fragment fragment = new Fragment();
    fragment.setRetainInstance(false);
    try (ActivityScenario<LifecycleOwnerActivity> activityScenario =
        ActivityScenario.launch(LifecycleOwnerActivity.class)) {
      assertThat(activityScenario).isNotNull();
      activityScenario.onActivity(
          activity -> {
            activity
                .getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, fragment)
                .commitNow();
            assertThat(activity.getSupportFragmentManager().findFragmentById(android.R.id.content))
                .isSameInstanceAs(fragment);
          });
      activityScenario.recreate();
      activityScenario.onActivity(
          activity ->
              assertThat(
                      activity.getSupportFragmentManager().findFragmentById(android.R.id.content))
                  .isNotSameInstanceAs(fragment));
    }
  }

  @Test
  public void recreate_isChangingConfigurations() {
    try (ActivityScenario<TranscriptActivity> activityScenario =
        ActivityScenario.launch(TranscriptActivity.class)) {
      activityScenario.recreate();

      assertThat(callbacks).contains("onStop true");
    }
  }

  @Test
  public void setRotation_recreatesActivity() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    try (ActivityScenario<TranscriptActivity> activityScenario =
        ActivityScenario.launch(TranscriptActivity.class)) {
      AtomicReference<Activity> originalActivity = new AtomicReference<>();
      activityScenario.onActivity(originalActivity::set);

      uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

      activityScenario.onActivity(
          activity -> {
            assertThat(activity.getResources().getConfiguration().orientation)
                .isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
            assertThat(activity).isNotSameInstanceAs(originalActivity);
          });
    }
  }

  @Test
  public void onActivityExceptionPropagated() {
    try (ActivityScenario<TranscriptActivity> activityScenario =
        ActivityScenario.launch(TranscriptActivity.class)) {
      assertThrows(
          IllegalStateException.class,
          () ->
              activityScenario.onActivity(
                  activity -> {
                    throw new IllegalStateException("test");
                  }));
    }
  }

  @Test
  public void onActivity_runsOnMainLooperThread() {
    try (ActivityScenario<TranscriptActivity> activityScenario =
        ActivityScenario.launch(TranscriptActivity.class)) {
      activityScenario.onActivity(
          activity -> {
            assertThat(Looper.getMainLooper().getThread()).isEqualTo(Thread.currentThread());
          });
    }
  }

  @Test
  public void getCallingActivity_empty() {
    try (ActivityScenario<TranscriptActivity> activityScenario =
        ActivityScenario.launch(TranscriptActivity.class)) {
      activityScenario.onActivity(
          activity -> {
            assertThat(activity.getCallingActivity()).isNull();
          });
    }
  }

  @Test
  public void getCallingActivity_isSet() {
    try (ActivityScenario<TranscriptActivity> activityScenario =
        ActivityScenario.launchActivityForResult(TranscriptActivity.class)) {
      activityScenario.onActivity(
          activity -> {
            assertThat(activity.getCallingActivity().getPackageName())
                .isEqualTo("org.robolectric.integrationtests.axt");
          });
    }
  }

  @Test
  @Config(minSdk = P)
  public void launchActivityWithCustomConstructor() {
    try (ActivityScenario<ActivityWithCustomConstructor> activityScenario =
        ActivityScenario.launch(ActivityWithCustomConstructor.class)) {
      assertThat(activityScenario.getState()).isEqualTo(State.RESUMED);
      activityScenario.onActivity(
          activity -> {
            assertThat(activity.getIntValue()).isEqualTo(100);
          });
    }
  }
}
