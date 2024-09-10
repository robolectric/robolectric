package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link NotificationManager}. */
@RunWith(AndroidJUnit4.class)
public class NotificationManagerTest {

  @Test
  public void notificationManager_applicationInstance_isNotSameAsActivityInstance() {
    NotificationManager applicationNotificationManager =
        (NotificationManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            NotificationManager activityNotificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            assertThat(applicationNotificationManager)
                .isNotSameInstanceAs(activityNotificationManager);
          });
    }
  }

  @Test
  public void notificationManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            NotificationManager activityNotificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationManager anotherActivityNotificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            assertThat(anotherActivityNotificationManager)
                .isSameInstanceAs(activityNotificationManager);
          });
    }
  }

  @Test
  public void notificationManager_createAndRetrieveChannel() {
    NotificationManager applicationNotificationManager =
        (NotificationManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

    NotificationChannel testChannel =
        new NotificationChannel(
            "test_channel_id", "Test Channel", NotificationManager.IMPORTANCE_DEFAULT);
    applicationNotificationManager.createNotificationChannel(testChannel);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            NotificationManager activityNotificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel applicationChannel =
                applicationNotificationManager.getNotificationChannel("test_channel_id");
            NotificationChannel activityChannel =
                activityNotificationManager.getNotificationChannel("test_channel_id");

            assertThat(activityChannel).isEqualTo(applicationChannel);
          });
    }
  }
}
