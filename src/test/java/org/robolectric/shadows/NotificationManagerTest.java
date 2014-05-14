package org.robolectric.shadows;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static android.app.Notification.FLAG_FOREGROUND_SERVICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class NotificationManagerTest {
  private NotificationManager notificationManager;
  private ShadowNotificationManager shadowNM;
  private Notification notification1 = new Notification();
  private Notification notification2 = new Notification();

  @Before
  public void setUp() {
    notificationManager = (NotificationManager) Robolectric.application.getSystemService(Context.NOTIFICATION_SERVICE);
    shadowNM = shadowOf(notificationManager);
  }

  @Test
  public void testNotify() throws Exception {
    notificationManager.notify(1, notification1);
    assertEquals(1, shadowNM.size());
    assertEquals(notification1, shadowNM.getNotification(null, 1));

    notificationManager.notify(31, notification2);
    assertEquals(2, shadowNM.size());
    assertEquals(notification2, shadowNM.getNotification(null, 31));
  }

  @Test
  public void testNotifyReplaces() throws Exception {
    notificationManager.notify(1, notification1);

    notificationManager.notify(1, notification2);
    assertEquals(1, shadowNM.size());
    assertEquals(notification2, shadowNM.getNotification(null, 1));
  }

  @Test
  public void testNotifyWithTag() throws Exception {
    notificationManager.notify("a tag", 1, notification1);
    assertEquals(1, shadowNM.size());
    assertEquals(notification1, shadowNM.getNotification("a tag", 1));
  }

  @Test
  public void testNotifyRaw() throws Exception {
    shadowNM.notifyRaw(1, notification1);
    assertEquals(1, shadowNM.size());
    assertEquals(notification1, shadowNM.getNotification(1));
  }

  @Test
  public void testNotifyRawWithTag() throws Exception {
    shadowNM.notifyRaw("a tag", 1, notification1);
    assertEquals(1, shadowNM.size());
    assertEquals(notification1, shadowNM.getNotification("a tag", 1));
  }

  @Test
  public void notifyWithTag_shouldReturnNullForNullTag() throws Exception {
    notificationManager.notify("a tag", 1, notification1);
    assertEquals(1, shadowNM.size());
    assertNull(shadowNM.getNotification(null, 1));
  }

  @Test
  public void notifyWithTag_shouldReturnNullForUnknownTag() throws Exception {
    notificationManager.notify("a tag", 1, notification1);
    assertEquals(1, shadowNM.size());
    assertNull(shadowNM.getNotification("unknown tag", 1));
  }

  @Test
  public void testCancel() throws Exception {
    notificationManager.notify(1, notification1);
    notificationManager.cancel(1);

    assertEquals(0, shadowNM.size());
    assertNull(shadowNM.getNotification(null, 1));
  }

  @Test
  public void testCancelWithTag() throws Exception {
    notificationManager.notify("a tag", 1, notification1);
    notificationManager.cancel("a tag", 1);

    assertEquals(0, shadowNM.size());
    assertNull(shadowNM.getNotification(null, 1));
    assertNull(shadowNM.getNotification("a tag", 1));
  }

  @Test
  public void testCancelAll() throws Exception {
    notificationManager.notify(1, notification1);
    notificationManager.notify(31, notification2);
    notificationManager.cancelAll();

    assertEquals(0, shadowNM.size());
    assertNull(shadowNM.getNotification(null, 1));
    assertNull(shadowNM.getNotification(null, 31));
  }

  @Test
  public void notify_clearsForegroundFlag() {
    notification1.flags |= FLAG_FOREGROUND_SERVICE;
    notification2.flags |= FLAG_FOREGROUND_SERVICE;
    notificationManager.notify(54, notification1);
    assertEquals("Foreground flag should be clear", 0, notification1.flags & FLAG_FOREGROUND_SERVICE);
    notificationManager.notify("tag", 56, notification2);
    assertEquals("Foreground flag should be clear", 0, notification2.flags & FLAG_FOREGROUND_SERVICE);
  }

  @Test
  public void notifyRaw_preservesForegroundFlagIfOn() {
    notification1.flags |= FLAG_FOREGROUND_SERVICE;
    shadowNM.notifyRaw("tag", 57, notification1);
    assertTrue("Foreground flag should be set", (notification1.flags & FLAG_FOREGROUND_SERVICE) != 0);
    shadowNM.notifyRaw(954, notification1);
    assertTrue("Foreground flag should be set", (notification1.flags & FLAG_FOREGROUND_SERVICE) != 0);
  }

  @Test
  public void notify_preservesForegroundFlagIfOn() {
    notification1.flags |= FLAG_FOREGROUND_SERVICE;
    notification2.flags &= ~FLAG_FOREGROUND_SERVICE;
    shadowNM.notifyRaw(1, notification1);
    notificationManager.notify(1, notification2);
    assertTrue((notification2.flags & FLAG_FOREGROUND_SERVICE) != 0);
  }

  @Test
  public void notify_preservesForegroundFlagIfNotOn() {
    notification1.flags &= ~FLAG_FOREGROUND_SERVICE;
    notification2.flags |= FLAG_FOREGROUND_SERVICE;
    notificationManager.notify(1, notification1);
    notificationManager.notify(1, notification2);
    assertEquals(0, (notification2.flags & FLAG_FOREGROUND_SERVICE));
  }
}
