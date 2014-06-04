package org.robolectric.shadows;

import android.app.PendingIntent;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.app.Notification;
import org.robolectric.TestRunners;
import static org.robolectric.Robolectric.*;
import static org.fest.assertions.api.Assertions.*;

@RunWith(TestRunners.WithDefaults.class)
public class NotificationBuilderTest {
  private final Notification.Builder builder = new Notification.Builder(application);

  @Test
  public void build_setsContentTitleOnNotification() throws Exception {
    Notification notification = builder.setContentTitle("Hello").build();
    assertThat("Hello").isEqualTo(shadowOf(notification).getContentTitle().toString());
  }

  @Test
  public void build_setsContentTextOnNotification() throws Exception {
    Notification notification = builder.setContentText("Hello Text").build();
    assertThat("Hello Text").isEqualTo(shadowOf(notification).getContentText().toString());
  }

  @Test
  public void build_setsTickerOnNotification() throws Exception {
    Notification notification = builder.setTicker("My ticker").build();
    assertThat("My ticker").isEqualTo(shadowOf(notification).getTicker().toString());
  }

  @Test
  public void build_setsContentInfoOnNotification() throws Exception {
    Notification notification = builder.setContentInfo("11").build();
    assertThat("11").isEqualTo(shadowOf(notification).getContentInfo().toString());
  }

  @Test
  public void build_setsIconOnNotification() throws Exception {
    Notification notification = builder.setSmallIcon(9001).build();
    assertThat(9001).isEqualTo(shadowOf(notification).getSmallIcon());
  }

  @Test
  public void build_setsWhenOnNotification() throws Exception {
    Notification notification = builder.setWhen(11L).build();
    assertThat(11L).isEqualTo(shadowOf(notification).getWhen());
  }

  @Test
  public void build_handlesNullContentTitle() {
    Notification notification = builder.setContentTitle(null).build();
    assertThat(shadowOf(notification).getContentTitle()).isNull();
  }

  @Test
  public void build_handlesNullContentText() {
    Notification notification = builder.setContentText(null).build();
    assertThat(shadowOf(notification).getContentText()).isNull();
  }

  @Test
  public void build_handlesNullTicker() {
    Notification notification = builder.setTicker(null).build();
    assertThat(shadowOf(notification).getTicker()).isNull();
  }

  @Test
  public void build_handlesNullContentInfo() {
    Notification notification = builder.setContentInfo(null).build();
    assertThat(shadowOf(notification).getContentInfo()).isNull();
  }

  @Test
  public void build_addsActionToNotification() throws Exception {
    PendingIntent action = PendingIntent.getBroadcast(application, 0, null, 0);
    Notification notification = builder.addAction(0, "Action", action).build();
    assertThat(shadowOf(notification).getActions().get(0).actionIntent)
        .isEqualsToByComparingFields(action);
  }
}
