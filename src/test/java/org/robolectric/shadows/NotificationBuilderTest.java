package org.robolectric.shadows;

import android.app.Activity;
import android.app.Notification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class NotificationBuilderTest {
  Notification.Builder builder;

  @Before
  public void setup() {
    builder = new Notification.Builder(Robolectric.buildActivity(Activity.class).get());
  }

  @Test
  public void build_setsContentTitleOnNotification() throws Exception {
    builder.setContentTitle("Hello");
    Notification notification = builder.build();
    assertEquals("Hello", shadowOf(notification).getContentTitle());
  }

  @Test
  public void build_setsContentTextOnNotification() throws Exception {
    builder.setContentText("Hello Text");
    Notification notification = builder.build();
    assertEquals("Hello Text", shadowOf(notification).getContentText());
  }

  @Test
  public void build_setsIconOnNotification() throws Exception {
    builder.setSmallIcon(9001);
    Notification notification = builder.build();;
    assertEquals(9001, shadowOf(notification).getSmallIcon());
  }

  @Test
  public void build_setsWhenOnNotification() throws Exception {
    builder.setWhen(11L);
    Notification notification = builder.build();
    assertEquals(11L, shadowOf(notification).getWhen());
  }
}
