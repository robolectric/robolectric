package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

@RunWith(AndroidJUnit4.class)
public class ContextTest {
  @Rule
  public GrantPermissionRule mRuntimePermissionRule =
      GrantPermissionRule.grant(Manifest.permission.MODIFY_AUDIO_SETTINGS);

  @Test
  public void audioManager_applicationInstance_isNotSameAsActivityInstance() {
    AudioManager applicationAudioManager =
        (AudioManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AudioManager activityAudioManager =
                (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            assertThat(applicationAudioManager).isNotSameInstanceAs(activityAudioManager);
          });
    }
  }

  @Test
  public void audioManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AudioManager activityAudioManager =
                (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            AudioManager anotherActivityAudioManager =
                (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            assertThat(anotherActivityAudioManager).isSameInstanceAs(activityAudioManager);
          });
    }
  }

  @Test
  public void audioManager_instance_changesAffectEachOther() {
    AudioManager applicationAudioManager =
        (AudioManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AudioManager activityAudioManager =
                (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

            activityAudioManager.setMode(AudioManager.MODE_RINGTONE);
            assertThat(activityAudioManager.getMode()).isEqualTo(AudioManager.MODE_RINGTONE);
            assertThat(applicationAudioManager.getMode()).isEqualTo(AudioManager.MODE_RINGTONE);

            applicationAudioManager.setMode(AudioManager.MODE_NORMAL);
            assertThat(activityAudioManager.getMode()).isEqualTo(AudioManager.MODE_NORMAL);
            assertThat(applicationAudioManager.getMode()).isEqualTo(AudioManager.MODE_NORMAL);
          });
    }
  }

  @Test
  public void
      activityManager_activityContextEnabled_applicationInstanceIsNotSameAsActivityInstance() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try {
      ActivityManager applicationActivityManager =
          ApplicationProvider.getApplicationContext().getSystemService(ActivityManager.class);
      try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
        scenario.onActivity(
            activity -> {
              ActivityManager activityActivityManager =
                  activity.getSystemService(ActivityManager.class);
              assertThat(applicationActivityManager).isNotSameInstanceAs(activityActivityManager);
            });
      }
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }

  @Test
  public void activityManager_activityContextEnabled_activityInstanceIsSameAsActivityInstance() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            ActivityManager activityActivityManager =
                activity.getSystemService(ActivityManager.class);
            ActivityManager anotherActivityActivityManager =
                activity.getSystemService(ActivityManager.class);
            assertThat(anotherActivityActivityManager).isSameInstanceAs(activityActivityManager);
          });
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }

  @Test
  public void activityManager_activityContextEnabled_differentInstancesChangesAffectEachOther() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try {
      ActivityManager applicationActivityManager =
          ApplicationProvider.getApplicationContext().getSystemService(ActivityManager.class);
      try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
        scenario.onActivity(
            activity -> {
              ActivityManager activityActivityManager =
                  activity.getSystemService(ActivityManager.class);

              activityActivityManager.getMemoryInfo(new ActivityManager.MemoryInfo());
              assertThat(activityActivityManager.isLowRamDevice())
                  .isEqualTo(applicationActivityManager.isLowRamDevice());

              applicationActivityManager.getMemoryInfo(new ActivityManager.MemoryInfo());
              assertThat(activityActivityManager.isLowRamDevice())
                  .isEqualTo(applicationActivityManager.isLowRamDevice());
            });
      }
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
