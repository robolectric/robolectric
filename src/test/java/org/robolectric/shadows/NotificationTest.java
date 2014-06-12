package org.robolectric.shadows;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;
import android.app.Activity;
import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowNotification.ShadowBigTextStyle;

@Config(emulateSdk = 16, reportSdk = 16)
@RunWith(TestRunners.WithDefaults.class)
public class NotificationTest {

  private Context context;
  private Notification notification;
  private ShadowNotification shadow;
  
  @Before
  public void setUp() {
    context = new Activity();
    setNotification(new Notification());
  }
  @Test
  public void setLatestEventInfo__shouldCaptureContentIntent() throws Exception {
    PendingIntent pendingIntent = PendingIntent.getActivity(new Activity(), 0, new Intent(), 0);
    notification.setLatestEventInfo(context, "title", "content", pendingIntent);
    assertThat(notification.contentIntent).isSameAs(pendingIntent);
  }
  
  @Test
  public void setProgress() {
    setNotification(new Builder(context).setProgress(57, 65, true).build());
    ShadowNotification.Progress progress = shadow.getProgress();
    assertThat(progress.indeterminate).isTrue();
    assertThat(progress.max).as("max").isEqualTo(57);
    assertThat(progress.progress).as("progress").isEqualTo(65);

    setNotification(new Builder(context).setProgress(52, 68, false).build());
    progress = shadow.getProgress();
    assertThat(progress.indeterminate).isFalse();
    assertThat(progress.max).as("max").isEqualTo(52);
    assertThat(progress.progress).as("progress").isEqualTo(68);
  }
  
  @Test
  public void setBigTextStyle() {
    final String BIG_TEXT = "This is the big text";
    final String BIG_TITLE = "Big Title";
    final String SUMMARY = "summary";
    BigTextStyle style = new BigTextStyle(new Builder(context))
      .bigText(BIG_TEXT)
      .setBigContentTitle(BIG_TITLE)
      .setSummaryText(SUMMARY);

    setNotification(style.build());
    assertThat(shadow.getStyle()).isSameAs(style);
    ShadowNotification.ShadowBigTextStyle sStyle = shadowOf((BigTextStyle)shadow.getStyle());
    
    assertThat(sStyle.getBigText()).as("bigText").isEqualTo(BIG_TEXT);
    assertThat(sStyle.getBigContentTitle()).as("bigContentTitle").isEqualTo(BIG_TITLE);
    assertThat(sStyle.getSummaryText()).as("summary").isEqualTo(SUMMARY);
  }
  
  @Test
  public void setUsesChronometer() {
    setNotification(new Builder(context).setUsesChronometer(true).build());
    assertThat(shadow.usesChronometer()).isTrue();
    setNotification(new Builder(context).setUsesChronometer(false).build());
    assertThat(shadow.usesChronometer()).isFalse();
  }
  
  private void setNotification(Notification notification) {
    this.notification = notification;
    shadow = shadowOf(notification);
  }
}
