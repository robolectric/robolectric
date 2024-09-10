package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.telecom.TelecomManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link TelecomManager}. */
@RunWith(AndroidJUnit4.class)
public class TelecomManagerTest {

  @Test
  public void telecomManager_applicationInstance_isNotSameAsActivityInstance() {
    TelecomManager applicationTelecomManager =
        (TelecomManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.TELECOM_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            TelecomManager activityTelecomManager =
                (TelecomManager) activity.getSystemService(Context.TELECOM_SERVICE);
            assertThat(applicationTelecomManager).isNotSameInstanceAs(activityTelecomManager);
          });
    }
  }

  @Test
  public void telecomManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            TelecomManager activityTelecomManager =
                (TelecomManager) activity.getSystemService(Context.TELECOM_SERVICE);
            TelecomManager anotherActivityTelecomManager =
                (TelecomManager) activity.getSystemService(Context.TELECOM_SERVICE);
            assertThat(anotherActivityTelecomManager).isSameInstanceAs(activityTelecomManager);
          });
    }
  }

  @Test
  public void telecomManager_instance_retrievesSameDefaultDialer() {
    TelecomManager applicationTelecomManager =
        (TelecomManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.TELECOM_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            TelecomManager activityTelecomManager =
                (TelecomManager) activity.getSystemService(Context.TELECOM_SERVICE);

            String applicationDefaultDialer = applicationTelecomManager.getDefaultDialerPackage();
            String activityDefaultDialer = activityTelecomManager.getDefaultDialerPackage();

            assertThat(activityDefaultDialer).isEqualTo(applicationDefaultDialer);
          });
    }
  }
}
