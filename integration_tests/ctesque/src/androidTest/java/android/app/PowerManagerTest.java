package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.os.PowerManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link PowerManager}. */
@RunWith(AndroidJUnit4.class)
public class PowerManagerTest {

  @Test
  public void powerManager_applicationInstance_isNotSameAsActivityInstance() {
    PowerManager applicationPowerManager =
        (PowerManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.POWER_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            PowerManager activityPowerManager =
                (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            assertThat(applicationPowerManager).isNotSameInstanceAs(activityPowerManager);
          });
    }
  }

  @Test
  public void powerManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            PowerManager activityPowerManager =
                (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            PowerManager anotherActivityPowerManager =
                (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            assertThat(anotherActivityPowerManager).isSameInstanceAs(activityPowerManager);
          });
    }
  }

  @Test
  public void powerManager_instance_checkIsInteractive() {
    PowerManager applicationPowerManager =
        (PowerManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.POWER_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            PowerManager activityPowerManager =
                (PowerManager) activity.getSystemService(Context.POWER_SERVICE);

            boolean applicationIsInteractive = applicationPowerManager.isInteractive();
            boolean activityIsInteractive = activityPowerManager.isInteractive();

            assertThat(activityIsInteractive).isEqualTo(applicationIsInteractive);
          });
    }
  }
}
