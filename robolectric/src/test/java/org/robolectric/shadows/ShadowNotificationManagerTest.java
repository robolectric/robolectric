package org.robolectric.shadows;

import static android.app.NotificationManager.INTERRUPTION_FILTER_ALL;
import static android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.AutomaticZenRule;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowNotificationManagerTest {
  private NotificationManager notificationManager;
  private Notification notification1 = new Notification();
  private Notification notification2 = new Notification();

  @Before public void setUp() {
    notificationManager =
        (NotificationManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.M)
  public void getCurrentInterruptionFilter() {
    // Sensible default
    assertThat(notificationManager.getCurrentInterruptionFilter()).isEqualTo(INTERRUPTION_FILTER_ALL);

    notificationManager.setInterruptionFilter(INTERRUPTION_FILTER_PRIORITY);
    assertThat(notificationManager.getCurrentInterruptionFilter()).isEqualTo(INTERRUPTION_FILTER_PRIORITY);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.M)
  public void getNotificationPolicy() {
    assertThat(notificationManager.getNotificationPolicy()).isNull();

    final Policy policy = new Policy(0, 0, 0);
    notificationManager.setNotificationPolicy(policy);
    assertThat(notificationManager.getNotificationPolicy()).isEqualTo(policy);
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
  @Config(minSdk = Build.VERSION_CODES.M)
  public void isNotificationPolicyAccessGranted() {
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(true);
    assertThat(notificationManager.isNotificationPolicyAccessGranted()).isTrue();
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(false);
    assertThat(notificationManager.isNotificationPolicyAccessGranted()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void
      setNotificationPolicyAccessGranted_temporarilyDenyAccess_shouldClearAutomaticZenRules() {
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(true);
    AutomaticZenRule rule =
        new AutomaticZenRule(
            "name",
            new ComponentName("pkg", "cls"),
            Uri.parse("condition://id"),
            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            /* enabled= */ true);
    String id = notificationManager.addAutomaticZenRule(rule);

    shadowOf(notificationManager).setNotificationPolicyAccessGranted(false);
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(true);

    assertThat(notificationManager.getAutomaticZenRule(id)).isNull();
    assertThat(notificationManager.getAutomaticZenRules()).isEmpty();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void getAutomaticZenRule_notificationAccessDenied_shouldThrowSecurityException() {
    try {
      notificationManager.getAutomaticZenRule("some_id");
      fail("Should have thrown SecurityException");
    } catch (SecurityException expected) {
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void getAutomaticZenRule_nonexistentId_shouldReturnNull() {
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(true);
    AutomaticZenRule rule =
        new AutomaticZenRule(
            "name",
            new ComponentName("pkg", "cls"),
            Uri.parse("condition://id"),
            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            /* enabled= */ true);
    String id = notificationManager.addAutomaticZenRule(rule);

    String nonexistentId = "id_different_from_" + id;
    assertThat(notificationManager.getAutomaticZenRule(nonexistentId)).isNull();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void getAutomaticZenRules_notificationAccessDenied_shouldThrowSecurityException() {
    try {
      notificationManager.getAutomaticZenRules();
      fail("Should have thrown SecurityException");
    } catch (SecurityException expected) {
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void getAutomaticZenRules_noRulesAdded_shouldReturnEmptyMap() {
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(true);
    assertThat(notificationManager.getAutomaticZenRules()).isEmpty();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void addAutomaticZenRule_notificationAccessDenied_shouldThrowSecurityException() {
    AutomaticZenRule rule =
        new AutomaticZenRule(
            "name",
            new ComponentName("pkg", "cls"),
            Uri.parse("condition://id"),
            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            /* enabled= */ true);
    try {
      notificationManager.addAutomaticZenRule(rule);
      fail("Should have thrown SecurityException");
    } catch (SecurityException expected) {
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void addAutomaticZenRule_oneRule_shouldAddRuleAndReturnId() {
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(true);

    AutomaticZenRule rule =
        new AutomaticZenRule(
            "name",
            new ComponentName("pkg", "cls"),
            Uri.parse("condition://id"),
            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            /* enabled= */ true);
    String id = notificationManager.addAutomaticZenRule(rule);

    assertThat(id).isNotEmpty();
    assertThat(notificationManager.getAutomaticZenRule(id)).isEqualTo(rule);
    assertThat(notificationManager.getAutomaticZenRules()).containsExactly(id, rule);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void addAutomaticZenRule_twoRules_shouldAddBothRulesAndReturnDifferentIds() {
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(true);

    AutomaticZenRule rule1 =
        new AutomaticZenRule(
            "name1",
            new ComponentName("pkg1", "cls1"),
            Uri.parse("condition://id1"),
            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            /* enabled= */ true);
    AutomaticZenRule rule2 =
        new AutomaticZenRule(
            "name2",
            new ComponentName("pkg2", "cls2"),
            Uri.parse("condition://id2"),
            NotificationManager.INTERRUPTION_FILTER_ALARMS,
            /* enabled= */ false);
    String id1 = notificationManager.addAutomaticZenRule(rule1);
    String id2 = notificationManager.addAutomaticZenRule(rule2);

    assertThat(id2).isNotEqualTo(id1);
    assertThat(notificationManager.getAutomaticZenRule(id1)).isEqualTo(rule1);
    assertThat(notificationManager.getAutomaticZenRule(id2)).isEqualTo(rule2);
    assertThat(notificationManager.getAutomaticZenRules()).containsExactly(id1, rule1, id2, rule2);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void updateAutomaticZenRule_notificationAccessDenied_shouldThrowSecurityException() {
    AutomaticZenRule rule =
        new AutomaticZenRule(
            "name",
            new ComponentName("pkg", "cls"),
            Uri.parse("condition://id"),
            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            /* enabled= */ true);
    try {
      notificationManager.updateAutomaticZenRule("some_id", rule);
      fail("Should have thrown SecurityException");
    } catch (SecurityException expected) {
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void updateAutomaticZenRule_nonexistentId_shouldThrowSecurityException() {
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(true);
    AutomaticZenRule rule =
        new AutomaticZenRule(
            "name",
            new ComponentName("pkg", "cls"),
            Uri.parse("condition://id"),
            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            /* enabled= */ true);
    String id = notificationManager.addAutomaticZenRule(rule);

    String nonexistentId = "id_different_from_" + id;
    AutomaticZenRule updatedRule =
        new AutomaticZenRule(
            "updated_name",
            new ComponentName("updated_pkg", "updated_cls"),
            Uri.parse("condition://updated_id"),
            NotificationManager.INTERRUPTION_FILTER_ALL,
            /* enabled= */ false);
    try {
      assertThat(notificationManager.updateAutomaticZenRule(nonexistentId, updatedRule));
      fail("Should have thrown SecurityException");
    } catch (SecurityException expected) {
    }

    assertThat(notificationManager.getAutomaticZenRule(id)).isEqualTo(rule);
    assertThat(notificationManager.getAutomaticZenRule(nonexistentId)).isNull();
    assertThat(notificationManager.getAutomaticZenRules()).containsExactly(id, rule);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void updateAutomaticZenRule_existingId_shouldUpdateRuleAndReturnTrue() {
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(true);
    AutomaticZenRule rule1 =
        new AutomaticZenRule(
            "name1",
            new ComponentName("pkg1", "cls1"),
            Uri.parse("condition://id1"),
            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            /* enabled= */ true);
    AutomaticZenRule rule2 =
        new AutomaticZenRule(
            "name2",
            new ComponentName("pkg2", "cls2"),
            Uri.parse("condition://id2"),
            NotificationManager.INTERRUPTION_FILTER_ALARMS,
            /* enabled= */ false);
    String id1 = notificationManager.addAutomaticZenRule(rule1);
    String id2 = notificationManager.addAutomaticZenRule(rule2);

    AutomaticZenRule updatedRule =
        new AutomaticZenRule(
            "updated_name",
            new ComponentName("updated_pkg", "updated_cls"),
            Uri.parse("condition://updated_id"),
            NotificationManager.INTERRUPTION_FILTER_ALL,
            /* enabled= */ false);
    assertThat(notificationManager.updateAutomaticZenRule(id2, updatedRule)).isTrue();

    assertThat(notificationManager.getAutomaticZenRule(id1)).isEqualTo(rule1);
    assertThat(notificationManager.getAutomaticZenRule(id2)).isEqualTo(updatedRule);
    assertThat(notificationManager.getAutomaticZenRules())
        .containsExactly(id1, rule1, id2, updatedRule);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void removeAutomaticZenRule_notificationAccessDenied_shouldThrowSecurityException() {
    try {
      notificationManager.removeAutomaticZenRule("some_id");
      fail("Should have thrown SecurityException");
    } catch (SecurityException expected) {
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void removeAutomaticZenRule_nonexistentId_shouldAndReturnFalse() {
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(true);
    AutomaticZenRule rule =
        new AutomaticZenRule(
            "name",
            new ComponentName("pkg", "cls"),
            Uri.parse("condition://id"),
            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            /* enabled= */ true);
    String id = notificationManager.addAutomaticZenRule(rule);

    String nonexistentId = "id_different_from_" + id;
    assertThat(notificationManager.removeAutomaticZenRule(nonexistentId)).isFalse();
    // The rules stored in NotificationManager should remain unchanged.
    assertThat(notificationManager.getAutomaticZenRules()).containsExactly(id, rule);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.N)
  public void removeAutomaticZenRule_existingId_shouldRemoveRuleAndReturnTrue() {
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(true);
    AutomaticZenRule rule1 =
        new AutomaticZenRule(
            "name1",
            new ComponentName("pkg1", "cls1"),
            Uri.parse("condition://id1"),
            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            /* enabled= */ true);
    AutomaticZenRule rule2 =
        new AutomaticZenRule(
            "name2",
            new ComponentName("pkg2", "cls2"),
            Uri.parse("condition://id2"),
            NotificationManager.INTERRUPTION_FILTER_ALARMS,
            /* enabled= */ false);
    String id1 = notificationManager.addAutomaticZenRule(rule1);
    String id2 = notificationManager.addAutomaticZenRule(rule2);

    assertThat(notificationManager.removeAutomaticZenRule(id1)).isTrue();

    assertThat(notificationManager.getAutomaticZenRule(id1)).isNull();
    assertThat(notificationManager.getAutomaticZenRule(id2)).isEqualTo(rule2);
    assertThat(notificationManager.getAutomaticZenRules()).containsExactly(id2, rule2);
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

    assertThat(asNotificationList(statusBarNotifications))
        .containsExactly(notification1, notification2);
  }

  private static List<Notification> asNotificationList(
      StatusBarNotification[] statusBarNotifications) {
    List<Notification> notificationList = new ArrayList<>(statusBarNotifications.length);
    for (StatusBarNotification statusBarNotification : statusBarNotifications) {
      notificationList.add(statusBarNotification.getNotification());
    }
    return notificationList;
  }
}
