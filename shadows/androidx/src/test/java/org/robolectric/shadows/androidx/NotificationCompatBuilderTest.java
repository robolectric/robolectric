package org.robolectric.shadows.androidx;

import static com.google.common.truth.Truth.assertThat;

import android.app.Notification;
import androidx.core.app.NotificationCompat;
import com.android.internal.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.TestRunnerWithManifest;

@RunWith(TestRunnerWithManifest.class)
public class NotificationCompatBuilderTest {
  @Test
  public void addAction__shouldAddActionToNotification() {
    NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_corp_icon, "a title", null).build();
    Notification notification =
        new NotificationCompat.Builder(RuntimeEnvironment.application)
            .addAction(action)
            .build();
    assertThat(notification.actions).asList().hasSize(1);
  }
}
