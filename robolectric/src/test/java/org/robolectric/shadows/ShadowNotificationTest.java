package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.RuntimeEnvironment.getApplication;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowNotificationTest {

  @Test
  public void setLatestEventInfo__shouldCaptureContentIntent() {
    PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(), 0, new Intent(), 0);
    Notification notification = new Notification();
    notification.setLatestEventInfo(getApplication(), "title", "content", pendingIntent);
    assertThat(notification.contentIntent).isSameInstanceAs(pendingIntent);
  }
}
