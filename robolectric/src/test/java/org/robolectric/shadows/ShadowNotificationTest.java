package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowNotificationTest {

  @Test
  public void setLatestEventInfo__shouldCaptureContentIntent() throws Exception {
    PendingIntent pendingIntent = PendingIntent.getActivity(application, 0, new Intent(), 0);
    Notification notification = new Notification();
    notification.setLatestEventInfo(application, "title", "content", pendingIntent);
    assertThat(notification.contentIntent).isSameAs(pendingIntent);
  }
}
