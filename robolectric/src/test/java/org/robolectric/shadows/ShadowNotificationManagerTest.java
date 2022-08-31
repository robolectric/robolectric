package org.robolectric.shadows;

import static android.app.NotificationManager.INTERRUPTION_FILTER_ALL;
import static android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

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
import android.os.Build.VERSION_CODES;
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenPolicy;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class ShadowNotificationManagerTest {
  private NotificationManager notificationManager;
  private Notification notification1 = new Notification();
  private Notification notification2 = new Notification();

  @Before
  public void setUp() {
    notificationManager =
        (NotificationManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.M)
  public void getCurrentInterruptionFilter() {
    // Sensible default
    assertThat(notificationManager.getCurrentInterruptionFilter())
        .isEqualTo(INTERRUPTION_FILTER_ALL);

    notificationManager.setInterruptionFilter(INTERRUPTION_FILTER_PRIORITY);
    assertThat(notificationManager.getCurrentInterruptionFilter())
        .isEqualTo(INTERRUPTION_FILTER_PRIORITY);
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
    NotificationChannel channel =
        (NotificationChannel) shadowOf(notificationManager).getNotificationChannel("id");
    assertThat(channel.getName().toString()).isEqualTo("name");
    assertThat(channel.getImportance()).isEqualTo(1);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void createNotificationChannel_updateChannel() {
    NotificationChannel channel = new NotificationChannel("id", "name", 1);
    channel.setDescription("description");
    channel.setGroup("channelGroupId");
    channel.setLightColor(7);

    NotificationChannel channelUpdate = new NotificationChannel("id", "newName", 2);
    channelUpdate.setDescription("newDescription");
    channelUpdate.setGroup("newChannelGroupId");
    channelUpdate.setLightColor(15);

    notificationManager.createNotificationChannel(channel);
    notificationManager.createNotificationChannel(channelUpdate);

    assertThat(shadowOf(notificationManager).getNotificationChannels()).hasSize(1);
    NotificationChannel resultChannel =
        (NotificationChannel) shadowOf(notificationManager).getNotificationChannel("id");
    assertThat(resultChannel.getName().toString()).isEqualTo("newName");
    assertThat(resultChannel.getDescription()).isEqualTo("newDescription");
    // No importance upgrade.
    assertThat(resultChannel.getImportance()).isEqualTo(1);
    // No group resultChannel.
    assertThat(resultChannel.getGroup()).isEqualTo("channelGroupId");
    // Other settings are unchanged as well.
    assertThat(resultChannel.getLightColor()).isEqualTo(7);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void createNotificationChannel_downGradeImportanceSetGroup() {
    NotificationChannel channel = new NotificationChannel("id", "name", 1);
    channel.setDescription("description");

    NotificationChannel channelUpdate = new NotificationChannel("id", "newName", 0);
    channelUpdate.setDescription("newDescription");
    channelUpdate.setGroup("newChannelGroupId");

    notificationManager.createNotificationChannel(channel);
    notificationManager.createNotificationChannel(channelUpdate);

    assertThat(shadowOf(notificationManager).getNotificationChannels()).hasSize(1);
    NotificationChannel resultChannel =
        (NotificationChannel) shadowOf(notificationManager).getNotificationChannel("id");
    assertThat(resultChannel.getName().toString()).isEqualTo("newName");
    assertThat(resultChannel.getDescription()).isEqualTo("newDescription");
    assertThat(resultChannel.getImportance()).isEqualTo(0);
    assertThat(resultChannel.getGroup()).isEqualTo("newChannelGroupId");
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void createNotificationChannelGroup() {
    notificationManager.createNotificationChannelGroup(new NotificationChannelGroup("id", "name"));

    assertThat(shadowOf(notificationManager).getNotificationChannelGroups()).hasSize(1);
    NotificationChannelGroup group =
        (NotificationChannelGroup) shadowOf(notificationManager).getNotificationChannelGroup("id");
    assertThat(group.getName().toString()).isEqualTo("name");
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
    assertThat(channel.getName().toString()).isEqualTo("name");
    assertThat(channel.getImportance()).isEqualTo(1);
    channel = (NotificationChannel) shadowOf(notificationManager).getNotificationChannel("id2");
    assertThat(channel.getName().toString()).isEqualTo("name2");
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
    // deleted channel except of name, description, importance downgrade or setting a group if group
    // was not previously set.
    notificationManager.createNotificationChannel(
        new NotificationChannel(channelId, "otherName", 2));
    assertThat(shadowOf(notificationManager).isChannelDeleted(channelId)).isFalse();
    NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
    // Name is changed.
    assertThat(channel.getName().toString()).isEqualTo("otherName");
    // Original importance.
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
  @Config(minSdk = Build.VERSION_CODES.N)
  public void setAndGetImportance() {
    shadowOf(notificationManager).setImportance(NotificationManager.IMPORTANCE_DEFAULT);
    assertThat(notificationManager.getImportance())
        .isEqualTo(NotificationManager.IMPORTANCE_DEFAULT);

    shadowOf(notificationManager).setImportance(NotificationManager.IMPORTANCE_NONE);
    assertThat(notificationManager.getImportance()).isEqualTo(NotificationManager.IMPORTANCE_NONE);
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
  @Config(minSdk = VERSION_CODES.O_MR1)
  public void isNotificationListenerAccessGranted() {
    ComponentName componentName = new ComponentName("pkg", "cls");
    shadowOf(notificationManager).setNotificationListenerAccessGranted(componentName, true);
    assertThat(notificationManager.isNotificationListenerAccessGranted(componentName)).isTrue();
    shadowOf(notificationManager).setNotificationListenerAccessGranted(componentName, false);
    assertThat(notificationManager.isNotificationListenerAccessGranted(componentName)).isFalse();
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
  @Config(minSdk = VERSION_CODES.Q)
  public void addAutomaticZenRule_oneRuleWithConfigurationActivity_shouldAddRuleAndReturnId() {
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(true);

    AutomaticZenRule rule =
        new AutomaticZenRule(
            "name",
            /* owner= */ null,
            new ComponentName("pkg", "cls"),
            Uri.parse("condition://id"),
            new ZenPolicy.Builder().build(),
            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            /* enabled= */ true);
    String id = notificationManager.addAutomaticZenRule(rule);

    assertThat(id).isNotEmpty();
    assertThat(notificationManager.getAutomaticZenRule(id)).isEqualTo(rule);
    assertThat(notificationManager.getAutomaticZenRules()).containsExactly(id, rule);
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
      assertThat(notificationManager.updateAutomaticZenRule(nonexistentId, updatedRule)).isTrue();
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
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void updateAutomaticZenRule_nullOwnerWithConfigurationActivity_updateRuleAndReturnTrue() {
    shadowOf(notificationManager).setNotificationPolicyAccessGranted(true);
    AutomaticZenRule rule1 =
        new AutomaticZenRule(
            "name1",
            /* owner= */ null,
            new ComponentName("pkg1", "cls1"),
            Uri.parse("condition://id1"),
            new ZenPolicy.Builder().build(),
            NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            /* enabled= */ true);
    AutomaticZenRule rule2 =
        new AutomaticZenRule(
            "name2",
            /* owner= */ null,
            new ComponentName("pkg2", "cls2"),
            Uri.parse("condition://id2"),
            new ZenPolicy.Builder().build(),
            NotificationManager.INTERRUPTION_FILTER_ALARMS,
            /* enabled= */ false);
    String id1 = notificationManager.addAutomaticZenRule(rule1);
    String id2 = notificationManager.addAutomaticZenRule(rule2);

    AutomaticZenRule updatedRule =
        new AutomaticZenRule(
            "updated_name",
            /* owner= */ null,
            new ComponentName("updated_pkg", "updated_cls"),
            Uri.parse("condition://updated_id"),
            new ZenPolicy.Builder().build(),
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
  public void testNotify() {
    notificationManager.notify(1, notification1);
    assertEquals(1, shadowOf(notificationManager).size());
    assertEquals(notification1, shadowOf(notificationManager).getNotification(null, 1));

    notificationManager.notify(31, notification2);
    assertEquals(2, shadowOf(notificationManager).size());
    assertEquals(notification2, shadowOf(notificationManager).getNotification(null, 31));
  }

  @Test
  public void testNotifyReplaces() {
    notificationManager.notify(1, notification1);

    notificationManager.notify(1, notification2);
    assertEquals(1, shadowOf(notificationManager).size());
    assertEquals(notification2, shadowOf(notificationManager).getNotification(null, 1));
  }

  @Test
  public void testNotifyWithTag() {
    notificationManager.notify("a tag", 1, notification1);
    assertEquals(1, shadowOf(notificationManager).size());
    assertEquals(notification1, shadowOf(notificationManager).getNotification("a tag", 1));
  }

  @Test
  public void notifyWithTag_shouldReturnNullForNullTag() {
    notificationManager.notify("a tag", 1, notification1);
    assertEquals(1, shadowOf(notificationManager).size());
    assertNull(shadowOf(notificationManager).getNotification(null, 1));
  }

  @Test
  public void notifyWithTag_shouldReturnNullForUnknownTag() {
    notificationManager.notify("a tag", 1, notification1);
    assertEquals(1, shadowOf(notificationManager).size());
    assertNull(shadowOf(notificationManager).getNotification("unknown tag", 1));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.M)
  public void testNotify_setsPostTime() {
    long startTimeMillis = ShadowSystem.currentTimeMillis();

    ShadowSystemClock.advanceBy(Duration.ofSeconds(1)); // Now startTimeMillis + 1000.
    notificationManager.notify(1, notification1);
    ShadowSystemClock.advanceBy(Duration.ofSeconds(1)); // Now startTimeMillis + 2000.
    notificationManager.notify(2, notification2);

    assertThat(getStatusBarNotification(1).getPostTime()).isEqualTo(startTimeMillis + 1000);
    assertThat(getStatusBarNotification(2).getPostTime()).isEqualTo(startTimeMillis + 2000);
  }

  @Test
  public void testNotify_withLimitEnforced() {
    shadowOf(notificationManager).setEnforceMaxNotificationLimit(true);

    for (int i = 0; i < 25; i++) {
      Notification notification = new Notification();
      notificationManager.notify(i, notification);
    }
    assertEquals(25, shadowOf(notificationManager).size());
    notificationManager.notify("26tag", 26, notification1);
    assertEquals(25, shadowOf(notificationManager).size());
    assertNull(shadowOf(notificationManager).getNotification("26tag", 26));

    shadowOf(notificationManager).setEnforceMaxNotificationLimit(false);
  }

  @Test
  public void testNotify_withLimitNotEnforced() {
    for (int i = 0; i < 25; i++) {
      Notification notification = new Notification();
      notificationManager.notify(i, notification);
    }
    assertEquals(25, shadowOf(notificationManager).size());
    notificationManager.notify("26tag", 26, notification1);
    assertEquals(26, shadowOf(notificationManager).size());
    assertEquals(notification1, shadowOf(notificationManager).getNotification("26tag", 26));
  }

  @Test
  public void testCancel() {
    notificationManager.notify(1, notification1);
    notificationManager.cancel(1);

    assertEquals(0, shadowOf(notificationManager).size());
    assertNull(shadowOf(notificationManager).getNotification(null, 1));
  }

  @Test
  public void testCancelWithTag() {
    notificationManager.notify("a tag", 1, notification1);
    notificationManager.cancel("a tag", 1);

    assertEquals(0, shadowOf(notificationManager).size());
    assertNull(shadowOf(notificationManager).getNotification(null, 1));
    assertNull(shadowOf(notificationManager).getNotification("a tag", 1));
  }

  @Test
  public void testCancelAll() {
    notificationManager.notify(1, notification1);
    notificationManager.notify(31, notification2);
    notificationManager.cancelAll();

    assertEquals(0, shadowOf(notificationManager).size());
    assertNull(shadowOf(notificationManager).getNotification(null, 1));
    assertNull(shadowOf(notificationManager).getNotification(null, 31));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.M)
  public void testGetActiveNotifications() {
    notificationManager.notify(1, notification1);
    notificationManager.notify(31, notification2);

    StatusBarNotification[] statusBarNotifications =
        shadowOf(notificationManager).getActiveNotifications();

    assertThat(asNotificationList(statusBarNotifications))
        .containsExactly(notification1, notification2);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testSetNotificationDelegate() {
    notificationManager.setNotificationDelegate("com.example.myapp");

    assertThat(notificationManager.getNotificationDelegate()).isEqualTo("com.example.myapp");
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testSetNotificationDelegate_null() {
    notificationManager.setNotificationDelegate("com.example.myapp");
    notificationManager.setNotificationDelegate(null);

    assertThat(notificationManager.getNotificationDelegate()).isNull();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testCanNotifyAsPackage_isFalseWhenNoDelegateIsSet() {
    assertThat(notificationManager.canNotifyAsPackage("some.package")).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testCanNotifyAsPackage_isTrueWhenDelegateIsSet() {
    String pkg = "some.package";
    shadowOf(notificationManager).setCanNotifyAsPackage(pkg, true);
    assertThat(notificationManager.canNotifyAsPackage(pkg)).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testCanNotifyAsPackage_isFalseWhenDelegateIsUnset() {
    String pkg = "some.package";
    shadowOf(notificationManager).setCanNotifyAsPackage(pkg, true);
    shadowOf(notificationManager).setCanNotifyAsPackage(pkg, false);
    assertThat(notificationManager.canNotifyAsPackage(pkg)).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testCanNotifyAsPackage_isFalseWhenOtherDelegateIsSet() {
    shadowOf(notificationManager).setCanNotifyAsPackage("other.package", true);
    assertThat(notificationManager.canNotifyAsPackage("some.package")).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void testCanNotifyAsPackage_workAsExpectedWhenMultipleDelegatesSetAndUnset() {
    String pkg1 = "some.package";
    String pkg2 = "another.package";
    // When pkg1 and pkg2 where set for delegation
    shadowOf(notificationManager).setCanNotifyAsPackage(pkg1, true);
    shadowOf(notificationManager).setCanNotifyAsPackage(pkg2, true);
    assertThat(notificationManager.canNotifyAsPackage(pkg1)).isTrue();
    assertThat(notificationManager.canNotifyAsPackage(pkg2)).isTrue();
    // When pkg1 unset
    shadowOf(notificationManager).setCanNotifyAsPackage(pkg1, false);
    assertThat(notificationManager.canNotifyAsPackage(pkg1)).isFalse();
    assertThat(notificationManager.canNotifyAsPackage(pkg2)).isTrue();
    // When pkg2 unset
    shadowOf(notificationManager).setCanNotifyAsPackage(pkg2, false);
    assertThat(notificationManager.canNotifyAsPackage(pkg1)).isFalse();
    assertThat(notificationManager.canNotifyAsPackage(pkg2)).isFalse();
  }

  @Config(minSdk = R)
  @Test
  public void getNotificationChannel() {
    NotificationChannel notificationChannel = new NotificationChannel("id", "name", 1);
    String conversationId = "conversation_id";
    String parentChannelId = "parent_channel_id";
    notificationChannel.setConversationId(parentChannelId, conversationId);
    notificationManager.createNotificationChannel(notificationChannel);

    assertThat(notificationManager.getNotificationChannels()).hasSize(1);
    NotificationChannel channel =
        notificationManager.getNotificationChannel(parentChannelId, conversationId);
    assertThat(channel.getName().toString()).isEqualTo("name");
    assertThat(channel.getImportance()).isEqualTo(1);
  }

  private static List<Notification> asNotificationList(
      StatusBarNotification[] statusBarNotifications) {
    List<Notification> notificationList = new ArrayList<>(statusBarNotifications.length);
    for (StatusBarNotification statusBarNotification : statusBarNotifications) {
      notificationList.add(statusBarNotification.getNotification());
    }
    return notificationList;
  }

  private StatusBarNotification getStatusBarNotification(int id) {
    for (StatusBarNotification statusBarNotification :
        shadowOf(notificationManager).getActiveNotifications()) {
      if (statusBarNotification.getTag() == null && statusBarNotification.getId() == id) {
        return statusBarNotification;
      }
    }
    return null;
  }
}
