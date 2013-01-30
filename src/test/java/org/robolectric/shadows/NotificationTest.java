package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;

@RunWith(TestRunners.WithDefaults.class)
public class NotificationTest {
    @Test
    public void setLatestEventInfo__shouldCaptureContentIntent() throws Exception {
        PendingIntent pendingIntent = PendingIntent.getActivity(new Activity(), 0, new Intent(), 0);
        Notification notification = new Notification();
        notification.setLatestEventInfo(new Activity(), "title", "content", pendingIntent);
        assertSame(pendingIntent, notification.contentIntent);
    }
}
