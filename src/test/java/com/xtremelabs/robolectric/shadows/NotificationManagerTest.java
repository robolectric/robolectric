package com.xtremelabs.robolectric.shadows;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(WithTestDefaultsRunner.class)
public class NotificationManagerTest {
    private NotificationManager notificationManager;
    private Notification notification1 = new Notification();
    private Notification notification2 = new Notification();

    @Before public void setUp() {
        notificationManager = (NotificationManager) Robolectric.application.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Test
    public void testNotify() throws Exception {
        notificationManager.notify(1, notification1);
        assertEquals(1, shadowOf(notificationManager).size());
        assertEquals(notification1, shadowOf(notificationManager).getNotification(1));

        notificationManager.notify(31, notification2);
        assertEquals(2, shadowOf(notificationManager).size());
        assertEquals(notification2, shadowOf(notificationManager).getNotification(31));
    }

    @Test
    public void testNotifyReplaces() throws Exception {
        notificationManager.notify(1, notification1);

        notificationManager.notify(1, notification2);
        assertEquals(1, shadowOf(notificationManager).size());
        assertEquals(notification2, shadowOf(notificationManager).getNotification(1));
    }

    @Test
    public void testNotifyWithTag() throws Exception {
        notificationManager.notify("a tag", 1, notification1);
        assertEquals(1, shadowOf(notificationManager).size());
        assertEquals(notification1, shadowOf(notificationManager).getNotification("a tag"));
    }

    @Test
    public void notifyWithTag_shouldReturnNullForNullTag() throws Exception {
        notificationManager.notify("a tag", 1, notification1);
        assertEquals(1, shadowOf(notificationManager).size());
        assertNull(shadowOf(notificationManager).getNotification(null));
    }

    @Test
    public void notifyWithTag_shouldReturnNullForUnknownTag() throws Exception {
        notificationManager.notify("a tag", 1, notification1);
        assertEquals(1, shadowOf(notificationManager).size());
        assertNull(shadowOf(notificationManager).getNotification("unknown tag"));
    }

    @Test
    public void testCancel() throws Exception {
        notificationManager.notify(1, notification1);
        notificationManager.cancel(1);
        
        assertEquals(0, shadowOf(notificationManager).size());
        assertNull(shadowOf(notificationManager).getNotification(1));
    }

    @Test
    public void testCancelWithTag() throws Exception {
        notificationManager.notify("a tag", 1, notification1);
        notificationManager.cancel("a tag", 1);

        assertEquals(0, shadowOf(notificationManager).size());
        assertNull(shadowOf(notificationManager).getNotification(1));
        assertNull(shadowOf(notificationManager).getNotification("a tag"));
    }

    @Test
    public void testCancelAll() throws Exception {
        notificationManager.notify(1, notification1);
        notificationManager.notify(31, notification2);
        notificationManager.cancelAll();

        assertEquals(0, shadowOf(notificationManager).size());
        assertNull(shadowOf(notificationManager).getNotification(1));
        assertNull(shadowOf(notificationManager).getNotification(31));
    }
}
