package org.robolectric.shadows;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowNotificationManagerTest {
  private NotificationManager notificationManager;
  private Notification notification1 = new Notification();
  private Notification notification2 = new Notification();

  @Before public void setUp() {
    notificationManager = (NotificationManager) RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  @Test
  public void testNotify() throws Exception {
    notificationManager.notify(1, notification1);
    assertEquals(1, shadowOf(notificationManager).size());
    assertEquals(notification1, shadowOf(notificationManager).getNotification(null, 1));

    notificationManager.notify(31, notification2);
    assertEquals(2, shadowOf(notificationManager).size());
    assertEquals(notification2, shadowOf(notificationManager).getNotification(null, 31));
  }

  @Test
  public void testNotifyReplaces() throws Exception {
    notificationManager.notify(1, notification1);

    notificationManager.notify(1, notification2);
    assertEquals(1, shadowOf(notificationManager).size());
    assertEquals(notification2, shadowOf(notificationManager).getNotification(null, 1));
  }

  @Test
  public void testNotifyWithTag() throws Exception {
    notificationManager.notify("a tag", 1, notification1);
    assertEquals(1, shadowOf(notificationManager).size());
    assertEquals(notification1, shadowOf(notificationManager).getNotification("a tag", 1));
  }

  @Test
  public void notifyWithTag_shouldReturnNullForNullTag() throws Exception {
    notificationManager.notify("a tag", 1, notification1);
    assertEquals(1, shadowOf(notificationManager).size());
    assertNull(shadowOf(notificationManager).getNotification(null, 1));
  }

  @Test
  public void notifyWithTag_shouldReturnNullForUnknownTag() throws Exception {
    notificationManager.notify("a tag", 1, notification1);
    assertEquals(1, shadowOf(notificationManager).size());
    assertNull(shadowOf(notificationManager).getNotification("unknown tag", 1));
  }

  @Test
  public void testCancel() throws Exception {
    notificationManager.notify(1, notification1);
    notificationManager.cancel(1);

    assertEquals(0, shadowOf(notificationManager).size());
    assertNull(shadowOf(notificationManager).getNotification(null, 1));
  }

  @Test
  public void testCancelWithTag() throws Exception {
    notificationManager.notify("a tag", 1, notification1);
    notificationManager.cancel("a tag", 1);

    assertEquals(0, shadowOf(notificationManager).size());
    assertNull(shadowOf(notificationManager).getNotification(null, 1));
    assertNull(shadowOf(notificationManager).getNotification("a tag", 1));
  }

  @Test
  public void testCancelAll() throws Exception {
    notificationManager.notify(1, notification1);
    notificationManager.notify(31, notification2);
    notificationManager.cancelAll();

    assertEquals(0, shadowOf(notificationManager).size());
    assertNull(shadowOf(notificationManager).getNotification(null, 1));
    assertNull(shadowOf(notificationManager).getNotification(null, 31));
  }

  @Test
  public void testGetActiveNotifications() throws Exception {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      // This API was added in M.
      return;
    }
    notificationManager.notify(1, notification1);
    notificationManager.notify(31, notification2);

    StatusBarNotification[] statusBarNotifications =
        shadowOf(notificationManager).getActiveNotifications();
    assertEquals(2, statusBarNotifications.length);
    boolean hasNotification1 = false;
    boolean hasNotification2 = false;
    for (StatusBarNotification notification : statusBarNotifications) {
      if (notification.getId() == 1) {
        hasNotification1 = true;
        assertEquals(notification1, notification.getNotification());
      } else if (notification.getId() == 31) {
        hasNotification2 = true;
        assertEquals(notification2, notification.getNotification());
      } else {
        fail("Unexpected notification id " + notification.getId());
      }
    }
    assertTrue(hasNotification1 && hasNotification2);
  }
}
