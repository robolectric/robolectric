package com.xtremelabs.robolectric.shadows;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.RemoteViews;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class NotificationBuilderTest {
    private Notification.Builder notificationBuilder;
    private ShadowNotificationBuilder shadowNotificationBuilder;

    @Before
    public void before() {
        notificationBuilder = new Notification.Builder(Robolectric.application);
        shadowNotificationBuilder = Robolectric.shadowOf(notificationBuilder);
    }

    @Test
    public void build() {
        long when = System.currentTimeMillis();
        int smallIcon = 1;
        int smallIconLevel = 2;
        int number = 3;
        RemoteViews contentViews = Robolectric.newInstanceOf(RemoteViews.class);
        CharSequence contentTitle = "title";
        CharSequence contentText = "content";
        CharSequence contentInfo = "info";
        PendingIntent contentIntent = Robolectric.newInstanceOf(PendingIntent.class);
        PendingIntent deleteIntent = Robolectric.newInstanceOf(PendingIntent.class);
        PendingIntent fullScreenIntent = Robolectric.newInstanceOf(PendingIntent.class);
        boolean highPriority = true;
        CharSequence tickerText = "ticker";
        RemoteViews tickerView = Robolectric.newInstanceOf(RemoteViews.class);
        Bitmap largeIcon = Robolectric.newInstanceOf(Bitmap.class);
        Uri soundUri = Uri.EMPTY;
        int streamType = 4;
        long[] vibrate = new long[] { 1, 2, 3 };
        int ledArgb = 5;
        int ledOnMs = 6;
        int ledOffMs = 7;
        boolean autoCancel = true;
        boolean onGoing = true;
        boolean onlyAlertOnce = true;
        int progressMax = 100;
        int progress = 50;
        boolean progressIndeterminate = true;
        Notification notification = notificationBuilder
                .setWhen(when)
                .setSmallIcon(smallIcon, smallIconLevel)
                .setNumber(number)
                .setContent(contentViews)
                .setContentIntent(contentIntent)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentInfo(contentInfo)
                .setDeleteIntent(deleteIntent)
                .setFullScreenIntent(fullScreenIntent, highPriority)
                .setTicker(tickerText, tickerView)
                .setLargeIcon(largeIcon)
                .setSound(soundUri, streamType)
                .setVibrate(vibrate)
                .setLights(ledArgb, ledOnMs, ledOffMs)
                .setAutoCancel(autoCancel)
                .setOngoing(onGoing)
                .setOnlyAlertOnce(onlyAlertOnce)
                .setProgress(progressMax, progress, progressIndeterminate)
                .build();
        assertThat(notification.when, equalTo(when));
        assertThat(notification.icon, equalTo(smallIcon));
        assertThat(notification.iconLevel, equalTo(smallIconLevel));
        assertThat(notification.number, equalTo(number));
        assertThat(notification.contentView, sameInstance(contentViews));
        assertThat(notification.contentIntent, sameInstance(contentIntent));
        assertThat(notification.deleteIntent, sameInstance(deleteIntent));
        assertThat(notification.fullScreenIntent, sameInstance(fullScreenIntent));
        assertThat(notification.tickerText, equalTo(tickerText));
        assertThat(notification.tickerView, sameInstance(tickerView));
        assertThat(notification.largeIcon, sameInstance(largeIcon));
        assertThat(notification.sound, sameInstance(soundUri));
        assertThat(notification.audioStreamType, equalTo(streamType));
        assertThat(notification.vibrate, equalTo(vibrate));
        assertThat(notification.ledARGB, equalTo(ledArgb));
        assertThat(notification.ledOnMS, equalTo(ledOnMs));
        assertThat(notification.ledOffMS, equalTo(ledOffMs));
        ShadowNotification shadowNotification = Robolectric.shadowOf(notification);
        assertThat(shadowNotification.getContentTitle(), equalTo(contentTitle));
        assertThat(shadowNotification.getContentText(), equalTo(contentText));
        assertThat(shadowNotification.isAutoCancel(), equalTo(autoCancel));
        assertThat(shadowNotification.getContentInfo(), equalTo(contentInfo));
        assertThat(shadowNotification.isOngoing(), equalTo(onGoing));
        assertThat(shadowNotification.isOnlyAlertOnce(), equalTo(onlyAlertOnce));
        assertThat(shadowNotification.getProgressMax(), equalTo(progressMax));
        assertThat(shadowNotification.getProgress(), equalTo(progress));
        assertThat(shadowNotification.isProgressIndeterminate(), equalTo(progressIndeterminate));
    }

    @Test
    public void setAutoCancel() {
        assertThat(shadowNotificationBuilder.isAutoCancel(), is(false));
        notificationBuilder.setAutoCancel(true);
        assertThat(shadowNotificationBuilder.isAutoCancel(), is(true));
    }

    @Test
    public void setContent() {
        RemoteViews remoteViews = new RemoteViews(null);
        assertThat(shadowNotificationBuilder.getContentView(), nullValue());
        notificationBuilder.setContent(remoteViews);
        assertThat(shadowNotificationBuilder.getContentView(), sameInstance(remoteViews));
    }

    @Test
    public void setContentInfo() {
        assertThat(shadowNotificationBuilder.getContentInfo(), nullValue());
        notificationBuilder.setContentInfo("foo");
        assertThat(shadowNotificationBuilder.getContentInfo().toString(), equalTo("foo"));
    }

    @Test
    public void setContentIntent() {
        PendingIntent pendingIntent = Robolectric.newInstanceOf(PendingIntent.class);
        assertThat(shadowNotificationBuilder.getContentIntent(), nullValue());
        notificationBuilder.setContentIntent(pendingIntent);
        assertThat(shadowNotificationBuilder.getContentIntent(), sameInstance(pendingIntent));
    }

    @Test
    public void setContentText() {
        assertThat(shadowNotificationBuilder.getContentText(), nullValue());
        notificationBuilder.setContentText("foo");
        assertThat(shadowNotificationBuilder.getContentText().toString(), equalTo("foo"));
    }

    @Test
    public void setContentTitle() {
        assertThat(shadowNotificationBuilder.getContentTitle(), nullValue());
        notificationBuilder.setContentTitle("foo");
        assertThat(shadowNotificationBuilder.getContentTitle().toString(), equalTo("foo"));
    }

    @Test
    public void setDefaults() {
        assertThat(shadowNotificationBuilder.getDefaults(), equalTo(0));
        notificationBuilder.setDefaults(5);
        assertThat(shadowNotificationBuilder.getDefaults(), equalTo(5));
    }

    @Test
    public void setDeleteIntent() {
        PendingIntent pendingIntent = Robolectric.newInstanceOf(PendingIntent.class);
        assertThat(shadowNotificationBuilder.getDeleteIntent(), nullValue());
        notificationBuilder.setDeleteIntent(pendingIntent);
        assertThat(shadowNotificationBuilder.getDeleteIntent(), sameInstance(pendingIntent));
    }

    @Test
    public void setFullScreenIntent() {
        PendingIntent pendingIntent = Robolectric.newInstanceOf(PendingIntent.class);
        assertThat(shadowNotificationBuilder.getFullScreenIntent(), nullValue());
        assertThat(shadowNotificationBuilder.isHighPriority(), equalTo(false));
        notificationBuilder.setFullScreenIntent(pendingIntent, true);
        assertThat(shadowNotificationBuilder.getFullScreenIntent(), sameInstance(pendingIntent));
        assertThat(shadowNotificationBuilder.isHighPriority(), equalTo(true));
    }

    @Test
    public void setLargeIcon() {
        Bitmap bitmap = Robolectric.newInstanceOf(Bitmap.class);
        assertThat(shadowNotificationBuilder.getLargeIcon(), nullValue());
        notificationBuilder.setLargeIcon(bitmap);
        assertThat(shadowNotificationBuilder.getLargeIcon(), sameInstance(bitmap));
    }

    @Test
    public void setLights() {
        assertThat(shadowNotificationBuilder.getLedArgb(), equalTo(0));
        assertThat(shadowNotificationBuilder.getLedOnMs(), equalTo(0));
        assertThat(shadowNotificationBuilder.getLedOffMs(), equalTo(0));
        notificationBuilder.setLights(1, 2, 3);
        assertThat(shadowNotificationBuilder.getLedArgb(), equalTo(1));
        assertThat(shadowNotificationBuilder.getLedOnMs(), equalTo(2));
        assertThat(shadowNotificationBuilder.getLedOffMs(), equalTo(3));
    }

    @Test
    public void setNumber() {
        assertThat(shadowNotificationBuilder.getNumber(), equalTo(0));
        notificationBuilder.setNumber(10);
        assertThat(shadowNotificationBuilder.getNumber(), equalTo(10));
    }

    @Test
    public void setOngoing() {
        assertThat(shadowNotificationBuilder.isOngoing(), equalTo(false));
        notificationBuilder.setOngoing(true);
        assertThat(shadowNotificationBuilder.isOngoing(), equalTo(true));
        assertThat(shadowNotificationBuilder.getFlags(), equalTo(Notification.FLAG_ONGOING_EVENT));
    }

    @Test
    public void setOnlyAlertOnce() {
        assertThat(shadowNotificationBuilder.isOnlyAlertOnce(), equalTo(false));
        notificationBuilder.setOnlyAlertOnce(true);
        assertThat(shadowNotificationBuilder.isOnlyAlertOnce(), equalTo(true));
        assertThat(shadowNotificationBuilder.getFlags(), equalTo(Notification.FLAG_ONLY_ALERT_ONCE));
    }

    @Test
    public void setProgress() {
        assertThat(shadowNotificationBuilder.getProgressMax(), equalTo(0));
        assertThat(shadowNotificationBuilder.getProgress(), equalTo(0));
        assertThat(shadowNotificationBuilder.isProgressIndeterminate(), equalTo(false));
        notificationBuilder.setProgress(1, 2, true);
        assertThat(shadowNotificationBuilder.getProgressMax(), equalTo(1));
        assertThat(shadowNotificationBuilder.getProgress(), equalTo(2));
        assertThat(shadowNotificationBuilder.isProgressIndeterminate(), equalTo(true));
    }

    @Test
    public void setSmallIcon() {
        assertThat(shadowNotificationBuilder.getSmallIcon(), equalTo(0));
        assertThat(shadowNotificationBuilder.getSmallIconLevel(), equalTo(0));
        notificationBuilder.setSmallIcon(1);
        assertThat(shadowNotificationBuilder.getSmallIcon(), equalTo(1));
        assertThat(shadowNotificationBuilder.getSmallIconLevel(), equalTo(0));
    }

    @Test
    public void setSmallIconWithLevel() {
        assertThat(shadowNotificationBuilder.getSmallIcon(), equalTo(0));
        assertThat(shadowNotificationBuilder.getSmallIconLevel(), equalTo(0));
        notificationBuilder.setSmallIcon(1, 2);
        assertThat(shadowNotificationBuilder.getSmallIcon(), equalTo(1));
        assertThat(shadowNotificationBuilder.getSmallIconLevel(), equalTo(2));
    }

    @Test
    public void setSound() {
        assertThat(shadowNotificationBuilder.getSound(), nullValue());
        assertThat(shadowNotificationBuilder.getAudioStreamType(), equalTo(Notification.STREAM_DEFAULT));
        notificationBuilder.setSound(Uri.EMPTY);
        assertThat(shadowNotificationBuilder.getSound(), sameInstance(Uri.EMPTY));
        assertThat(shadowNotificationBuilder.getAudioStreamType(), equalTo(Notification.STREAM_DEFAULT));
    }

    @Test
    public void setSoundWithStreamType() {
        assertThat(shadowNotificationBuilder.getSound(), nullValue());
        assertThat(shadowNotificationBuilder.getAudioStreamType(), equalTo(Notification.STREAM_DEFAULT));
        notificationBuilder.setSound(Uri.EMPTY, 10);
        assertThat(shadowNotificationBuilder.getSound(), sameInstance(Uri.EMPTY));
        assertThat(shadowNotificationBuilder.getAudioStreamType(), equalTo(10));
    }

    @Test
    public void setTicker() {
        assertThat(shadowNotificationBuilder.getTickerText(), nullValue());
        assertThat(shadowNotificationBuilder.getTickerViews(), nullValue());
        notificationBuilder.setTicker("foo");
        assertThat(shadowNotificationBuilder.getTickerText().toString(), equalTo("foo"));
        assertThat(shadowNotificationBuilder.getTickerViews(), nullValue());
    }

    @Test
    public void setTickerWithRemoteViews() {
        RemoteViews remoteViews = Robolectric.newInstanceOf(RemoteViews.class);
        assertThat(shadowNotificationBuilder.getTickerText(), nullValue());
        assertThat(shadowNotificationBuilder.getTickerViews(), nullValue());
        notificationBuilder.setTicker("foo", remoteViews);
        assertThat(shadowNotificationBuilder.getTickerText().toString(), equalTo("foo"));
        assertThat(shadowNotificationBuilder.getTickerViews(), sameInstance(remoteViews));
    }

    @Test
    public void setVibrate() {
        assertThat(shadowNotificationBuilder.getVibrate(), nullValue());
        notificationBuilder.setVibrate(new long[]{ 1, 2, 3 });
        assertThat(shadowNotificationBuilder.getVibrate(), equalTo(new long[]{ 1, 2, 3 }));
    }

    @Test
    public void setWhen() {
        assertThat(shadowNotificationBuilder.getWhen(), notNullValue());
        notificationBuilder.setWhen(123);
        assertThat(shadowNotificationBuilder.getWhen(), equalTo(123L));
    }
}