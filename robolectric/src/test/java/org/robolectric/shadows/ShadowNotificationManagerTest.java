package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowNotificationManagerTest {
  private NotificationManager notificationManager;
  private Notification notification1 = new Notification();
  private Notification notification2 = new Notification();

  @Before public void setUp() {
    notificationManager = (NotificationManager) RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void createNotificationChannel() {
    notificationManager.createNotificationChannel(new NotificationChannel("id", "name", 1));

    assertThat(shadowOf(notificationManager).getNotificationChannels()).hasSize(1);
    NotificationChannel channel = (NotificationChannel)shadowOf(notificationManager)
        .getNotificationChannel("id");
    assertThat(channel.getName()).isEqualTo("name");
    assertThat(channel.getImportance()).isEqualTo(1);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void createNotificationChannelGroup() {
    notificationManager.createNotificationChannelGroup(new NotificationChannelGroup("id", "name"));

    assertThat(shadowOf(notificationManager).getNotificationChannelGroups()).hasSize(1);
    NotificationChannelGroup group = (NotificationChannelGroup)shadowOf(notificationManager)
        .getNotificationChannelGroup("id");
    assertThat(group.getName()).isEqualTo("name");
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void createNotificationChannels() {
    NotificationChannel channel1 = new NotificationChannel("id", "name", 1);
    NotificationChannel channel2 = new NotificationChannel("id2", "name2", 1);

    notificationManager.createNotificationChannels(ImmutableList.of(channel1, channel2));

    assertThat(shadowOf(notificationManager).getNotificationChannels()).hasSize(2);
    NotificationChannel channel =
        (NotificationChannel) shadowOf(notificationManager).getNotificationChannel("id");
    assertThat(channel.getName()).isEqualTo("name");
    assertThat(channel.getImportance()).isEqualTo(1);
    channel = (NotificationChannel) shadowOf(notificationManager).getNotificationChannel("id2");
    assertThat(channel.getName()).isEqualTo("name2");
    assertThat(channel.getImportance()).isEqualTo(1);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void deleteNotificationChannel() {
    final String channelId = "channelId";
    assertThat(shadowOf(notificationManager).isChannelDeleted(channelId)).isFalse();
    notificationManager.createNotificationChannel(new NotificationChannel(channelId, "name", 1));
    assertThat(shadowOf(notificationManager).isChannelDeleted(channelId)).isFalse();
    notificationManager.deleteNotificationChannel(channelId);
    assertThat(shadowOf(notificationManager).isChannelDeleted(channelId)).isTrue();
    assertThat(notificationManager.getNotificationChannel(channelId)).isNull();
    // Per documentation, recreating a deleted channel should have the same settings as the old
    // deleted channel.
    notificationManager.createNotificationChannel(
        new NotificationChannel(channelId, "otherName", 2));
    assertThat(shadowOf(notificationManager).isChannelDeleted(channelId)).isFalse();
    NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
    assertThat(channel.getName()).isEqualTo("name");
    assertThat(channel.getImportance()).isEqualTo(1);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void deleteNotificationChannelGroup() {
    final String channelId = "channelId";
    final String channelGroupId = "channelGroupId";
    notificationManager.createNotificationChannelGroup(
        new NotificationChannelGroup(channelGroupId, "groupName"));
    NotificationChannel channel = new NotificationChannel(channelId, "channelName", 1);
    channel.setGroup(channelGroupId);
    notificationManager.createNotificationChannel(channel);
    assertThat(shadowOf(notificationManager).isChannelDeleted(channelId)).isFalse();
    notificationManager.deleteNotificationChannelGroup(channelGroupId);
    assertThat(shadowOf(notificationManager).getNotificationChannelGroup(channelGroupId)).isNull();
    // Per documentation, deleting a channel group also deletes all associated channels.
    assertThat(shadowOf(notificationManager).isChannelDeleted(channelId)).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void areNotificationsEnabled() {
    shadowOf(notificationManager).setNotificationsEnabled(true);
    assertThat(notificationManager.areNotificationsEnabled()).isTrue();
    shadowOf(notificationManager).setNotificationsEnabled(false);
    assertThat(notificationManager.areNotificationsEnabled()).isFalse();
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
  @Config(minSdk = Build.VERSION_CODES.M)
  public void testGetActiveNotifications() throws Exception {
    notificationManager.notify(1, notification1);
    notificationManager.notify(31, notification2);

    StatusBarNotification[] statusBarNotifications =
        shadowOf(notificationManager).getActiveNotifications();
    assertThat(statusBarNotifications)
        .extractingResultOf("getNotification", Notification.class)
        .containsOnly(notification1, notification2);
  }
}
