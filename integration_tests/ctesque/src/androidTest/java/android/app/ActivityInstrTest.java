package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Button;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.testapp.ActivityWithAnotherTheme;
import org.robolectric.testapp.ActivityWithoutTheme;
import org.robolectric.testapp.R;
import org.robolectric.testapp.TestActivity;

@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class ActivityInstrTest {

  @Before
  public void setUp() {
    ActivityWithAnotherTheme.setThemeBeforeContentView = null;
  }

  @Test
  public void whenSetOnActivityInManifest_activityGetsThemeFromActivityInManifest() {
    try (ActivityScenario<ActivityWithAnotherTheme> scenario =
        ActivityScenario.launch(ActivityWithAnotherTheme.class)) {
      scenario.onActivity(
          activity -> {
            Button theButton = activity.findViewById(R.id.button);
            ColorDrawable background = (ColorDrawable) theButton.getBackground();
            assertThat(background.getColor()).isEqualTo(0xffff0000);
          });
    }
  }

  @Test
  public void
      whenExplicitlySetOnActivity_afterSetContentView_activityGetsThemeFromActivityInManifest() {
    try (ActivityScenario<ActivityWithAnotherTheme> scenario =
        ActivityScenario.launch(ActivityWithAnotherTheme.class)) {
      scenario.onActivity(
          activity -> {
            activity.setTheme(R.style.Theme_Robolectric);
            Button theButton = activity.findViewById(R.id.button);
            ColorDrawable background = (ColorDrawable) theButton.getBackground();
            assertThat(background.getColor()).isEqualTo(0xffff0000);
          });
    }
  }

  @Test
  public void whenExplicitlySetOnActivity_beforeSetContentView_activityUsesNewTheme() {
    ActivityWithAnotherTheme.setThemeBeforeContentView = R.style.Theme_Robolectric;
    try (ActivityScenario<ActivityWithAnotherTheme> scenario =
        ActivityScenario.launch(ActivityWithAnotherTheme.class)) {
      scenario.onActivity(
          activity -> {
            Button theButton = activity.findViewById(R.id.button);
            ColorDrawable background = (ColorDrawable) theButton.getBackground();
            assertThat(background.getColor()).isEqualTo(0xff00ff00);
          });
    }
  }

  @Test
  public void whenNotSetOnActivityInManifest_activityGetsThemeFromApplicationInManifest() {
    try (ActivityScenario<ActivityWithoutTheme> scenario =
        ActivityScenario.launch(ActivityWithoutTheme.class)) {
      scenario.onActivity(
          activity -> {
            Button theButton = activity.findViewById(R.id.button);
            ColorDrawable background = (ColorDrawable) theButton.getBackground();
            assertThat(background.getColor()).isEqualTo(0xff00ff00);
          });
    }
  }

  @Test
  public void audioManager_activityContextEnabled() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try {
      AudioManager applicationAudioManager =
          (AudioManager)
              ApplicationProvider.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
      Log.d("Test", "Initial applicationAudioManager mode: " + applicationAudioManager.getMode());

      try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
        scenario.onActivity(
            activity -> {
              AudioManager activityAudioManager =
                  (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
              assertThat(applicationAudioManager).isNotSameInstanceAs(activityAudioManager);
              Log.d("TestActivity", "Setting mode to RINGTONE on activityAudioManager");
              activityAudioManager.setMode(AudioManager.MODE_RINGTONE);
              Log.d("TestActivity", "activityAudioManager mode: " + activityAudioManager.getMode());
              Log.d(
                  "TestActivity",
                  "applicationAudioManager mode: " + applicationAudioManager.getMode());
              assertThat(activityAudioManager.getMode()).isEqualTo(AudioManager.MODE_RINGTONE);
              assertThat(applicationAudioManager.getMode()).isEqualTo(AudioManager.MODE_RINGTONE);
            });
      }
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
