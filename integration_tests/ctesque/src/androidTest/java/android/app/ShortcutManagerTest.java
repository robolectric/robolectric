package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.pm.ShortcutManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link ShortcutManager}. */
@RunWith(AndroidJUnit4.class)
public class ShortcutManagerTest {

  @Test
  public void shortcutManager_applicationInstance_isNotSameAsActivityInstance() {
    ShortcutManager applicationShortcutManager =
        (ShortcutManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.SHORTCUT_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            ShortcutManager activityShortcutManager =
                (ShortcutManager) activity.getSystemService(Context.SHORTCUT_SERVICE);
            assertThat(applicationShortcutManager).isNotSameInstanceAs(activityShortcutManager);
          });
    }
  }

  @Test
  public void shortcutManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            ShortcutManager activityShortcutManager =
                (ShortcutManager) activity.getSystemService(Context.SHORTCUT_SERVICE);
            ShortcutManager anotherActivityShortcutManager =
                (ShortcutManager) activity.getSystemService(Context.SHORTCUT_SERVICE);
            assertThat(anotherActivityShortcutManager).isSameInstanceAs(activityShortcutManager);
          });
    }
  }

  @Test
  public void shortcutManager_instance_checksRateLimiting() {
    ShortcutManager applicationShortcutManager =
        (ShortcutManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.SHORTCUT_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            ShortcutManager activityShortcutManager =
                (ShortcutManager) activity.getSystemService(Context.SHORTCUT_SERVICE);

            boolean applicationRateLimiting = applicationShortcutManager.isRateLimitingActive();
            boolean activityRateLimiting = activityShortcutManager.isRateLimitingActive();

            assertThat(activityRateLimiting).isEqualTo(applicationRateLimiting);
          });
    }
  }
}
