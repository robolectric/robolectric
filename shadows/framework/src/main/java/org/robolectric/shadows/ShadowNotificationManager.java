package org.robolectric.shadows;

import static android.app.NotificationManager.INTERRUPTION_FILTER_ALL;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;

import android.app.AutomaticZenRule;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.os.Build;
import android.os.Parcel;
import android.service.notification.StatusBarNotification;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = NotificationManager.class, looseSignatures = true)
public class ShadowNotificationManager {
  private boolean mAreNotificationsEnabled = true;
  private boolean isNotificationPolicyAccessGranted = false;
  private Map<Key, Notification> notifications = new HashMap<>();
  private final Map<String, Object> notificationChannels = new HashMap<>();
  private final Map<String, Object> notificationChannelGroups = new HashMap<>();
  private final Map<String, Object> deletedNotificationChannels = new HashMap<>();
  private final Map<String, AutomaticZenRule> automaticZenRules = new HashMap<>();
  private int currentInteruptionFilter = INTERRUPTION_FILTER_ALL;
  private Policy notificationPolicy;

  @Implementation
  protected void notify(int id, Notification notification) {
    notify(null, id, notification);
  }

  @Implementation
  protected void notify(String tag, int id, Notification notification) {
    notifications.put(new Key(tag, id), notification);
  }

  @Implementation
  protected void cancel(int id) {
    cancel(null, id);
  }

  @Implementation
  protected void cancel(String tag, int id) {
    Key key = new Key(tag, id);
    if (notifications.containsKey(key)) {
      notifications.remove(key);
    }
  }

  @Implementation
  protected void cancelAll() {
    notifications.clear();
  }

  @Implementation(minSdk = Build.VERSION_CODES.N)
  protected boolean areNotificationsEnabled() {
    return mAreNotificationsEnabled;
  }

  public void setNotificationsEnabled(boolean areNotificationsEnabled) {
    mAreNotificationsEnabled = areNotificationsEnabled;
  }

  @Implementation(minSdk = M)
  public StatusBarNotification[] getActiveNotifications() {
    StatusBarNotification[] statusBarNotifications =
        new StatusBarNotification[notifications.size()];
    int i = 0;
    for (Map.Entry<Key, Notification> entry : notifications.entrySet()) {
      statusBarNotifications[i++] = new StatusBarNotification(
	  RuntimeEnvironment.application.getPackageName(),
	  null /* opPkg */,
	  entry.getKey().id,
	  entry.getKey().tag,
	  android.os.Process.myUid() /* uid */,
	  android.os.Process.myPid() /* initialPid */,
	  0 /* score */,
	  entry.getValue(),
	  android.os.Process.myUserHandle(),
	  0 /* postTime */);
    }
    return statusBarNotifications;
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected Object /*NotificationChannel*/ getNotificationChannel(String channelId) {
    return notificationChannels.get(channelId);
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected void createNotificationChannelGroup(Object /*NotificationChannelGroup*/ group) {
    String id = ReflectionHelpers.callInstanceMethod(group, "getId");
    notificationChannelGroups.put(id, group);
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected List<Object /*NotificationChannelGroup*/> getNotificationChannelGroups() {
    return ImmutableList.copyOf(notificationChannelGroups.values());
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected void createNotificationChannel(Object /*NotificationChannel*/ channel) {
    String id = ReflectionHelpers.callInstanceMethod(channel, "getId");
    // Per documentation, recreating a deleted channel should have the same settings as the old
    // deleted channel. See
    // https://developer.android.com/reference/android/app/NotificationManager.html#deleteNotificationChannel%28java.lang.String%29
    // for more info.
    if (deletedNotificationChannels.containsKey(id)) {
      notificationChannels.put(id, deletedNotificationChannels.remove(id));
    } else {
      notificationChannels.put(id, channel);
    }
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected void createNotificationChannels(List<Object /*NotificationChannel*/> channelList) {
    for (Object channel : channelList) {
      createNotificationChannel(channel);
    }
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  public List<Object /*NotificationChannel*/> getNotificationChannels() {
    return ImmutableList.copyOf(notificationChannels.values());
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected void deleteNotificationChannel(String channelId) {
    if (getNotificationChannel(channelId) != null) {
      Object /*NotificationChannel*/ channel = notificationChannels.remove(channelId);
      deletedNotificationChannels.put(channelId, channel);
    }
  }

  /**
   * Delete a notification channel group and all notification channels associated with the group.
   * This method will not notify any NotificationListenerService of resulting changes to
   * notification channel groups nor to notification channels.
   */
  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected void deleteNotificationChannelGroup(String channelGroupId) {
    if (getNotificationChannelGroup(channelGroupId) != null) {
      // Deleting a channel group also deleted all associated channels. See
      // https://developer.android.com/reference/android/app/NotificationManager.html#deleteNotificationChannelGroup%28java.lang.String%29
      // for more info.
      for (/* NotificationChannel */ Object channel : getNotificationChannels()) {
        String groupId = ReflectionHelpers.callInstanceMethod(channel, "getGroup");
        if (channelGroupId.equals(groupId)) {
          String channelId = ReflectionHelpers.callInstanceMethod(channel, "getId");
          deleteNotificationChannel(channelId);
        }
      }
      notificationChannelGroups.remove(channelGroupId);
    }
  }

  /**
   * @return {@link NotificationManager#INTERRUPTION_FILTER_ALL} by default, or the value specified
   *         via {@link #setInterruptionFilter(int)}
   */
  @Implementation(minSdk = M)
  protected final int getCurrentInterruptionFilter() {
    return currentInteruptionFilter;
  }

  /**
   * Currently does not support checking for granted policy access.
   *
   * @see NotificationManager#getCurrentInterruptionFilter()
   */
  @Implementation(minSdk = M)
  protected final void setInterruptionFilter(int interruptionFilter) {
    currentInteruptionFilter = interruptionFilter;
  }

  /**
   * @return the value specified via {@link #setNotificationPolicy(Policy)}
   */
  @Implementation(minSdk = M)
  protected final Policy getNotificationPolicy() {
    return notificationPolicy;
  }

  /**
   * @return the value specified via {@link #setNotificationPolicyAccessGranted(boolean)}
   */
  @Implementation(minSdk = M)
  protected final boolean isNotificationPolicyAccessGranted() {
    return isNotificationPolicyAccessGranted;
  }

  /**
   * Currently does not support checking for granted policy access.
   *
   * @see NotificationManager#getNotificationPolicy()
   */
  @Implementation(minSdk = M)
  protected final void setNotificationPolicy(Policy policy) {
    notificationPolicy = policy;
  }

  /**
   * Sets the value returned by {@link NotificationManager#isNotificationPolicyAccessGranted()}. If
   * {@code granted} is false, this also deletes all {@link AutomaticZenRule}s.
   *
   * @see NotificationManager#isNotificationPolicyAccessGranted()
   */
  public void setNotificationPolicyAccessGranted(boolean granted) {
    isNotificationPolicyAccessGranted = granted;
    if (!granted) {
      automaticZenRules.clear();
    }
  }

  @Implementation(minSdk = N)
  protected AutomaticZenRule getAutomaticZenRule(String id) {
    Preconditions.checkNotNull(id);
    enforcePolicyAccess();

    return automaticZenRules.get(id);
  }

  @Implementation(minSdk = N)
  protected Map<String, AutomaticZenRule> getAutomaticZenRules() {
    enforcePolicyAccess();

    ImmutableMap.Builder<String, AutomaticZenRule> rules = new ImmutableMap.Builder();
    for (Map.Entry<String, AutomaticZenRule> entry : automaticZenRules.entrySet()) {
      rules.put(entry.getKey(), copyAutomaticZenRule(entry.getValue()));
    }
    return rules.build();
  }

  @Implementation(minSdk = N)
  protected String addAutomaticZenRule(AutomaticZenRule automaticZenRule) {
    Preconditions.checkNotNull(automaticZenRule);
    Preconditions.checkNotNull(automaticZenRule.getName());
    Preconditions.checkNotNull(automaticZenRule.getOwner());
    Preconditions.checkNotNull(automaticZenRule.getConditionId());
    enforcePolicyAccess();

    String id = UUID.randomUUID().toString().replace("-", "");
    automaticZenRules.put(id, copyAutomaticZenRule(automaticZenRule));
    return id;
  }

  @Implementation(minSdk = N)
  protected boolean updateAutomaticZenRule(String id, AutomaticZenRule automaticZenRule) {
    // NotificationManagerService doesn't check that id is non-null.
    Preconditions.checkNotNull(automaticZenRule);
    Preconditions.checkNotNull(automaticZenRule.getName());
    Preconditions.checkNotNull(automaticZenRule.getOwner());
    Preconditions.checkNotNull(automaticZenRule.getConditionId());
    enforcePolicyAccess();

    // ZenModeHelper throws slightly cryptic exceptions.
    if (id == null) {
      throw new IllegalArgumentException("Rule doesn't exist");
    } else if (!automaticZenRules.containsKey(id)) {
      throw new SecurityException("Cannot update rules not owned by your condition provider");
    }

    automaticZenRules.put(id, copyAutomaticZenRule(automaticZenRule));
    return true;
  }

  @Implementation(minSdk = N)
  protected boolean removeAutomaticZenRule(String id) {
    Preconditions.checkNotNull(id);
    enforcePolicyAccess();
    return automaticZenRules.remove(id) != null;
  }

  /**
   * Enforces that the caller has notification policy access.
   *
   * @see NotificationManager#isNotificationPolicyAccessGranted()
   * @throws SecurityException if the caller doesn't have notification policy access
   */
  private void enforcePolicyAccess() {
    if (!isNotificationPolicyAccessGranted) {
      throw new SecurityException("Notification policy access denied");
    }
  }

  /** Returns a copy of {@code automaticZenRule}. */
  private AutomaticZenRule copyAutomaticZenRule(AutomaticZenRule automaticZenRule) {
    Parcel parcel = Parcel.obtain();
    try {
      automaticZenRule.writeToParcel(parcel, /* flags= */ 0);
      parcel.setDataPosition(0);
      return new AutomaticZenRule(parcel);
    } finally {
      parcel.recycle();
    }
  }

  /**
   * Checks whether a channel is considered a "deleted" channel by Android. This is a channel that
   * was created but later deleted. If a channel is created that was deleted before, it recreates
   * the channel with the old settings.
   */
  public boolean isChannelDeleted(String channelId) {
    return deletedNotificationChannels.containsKey(channelId);
  }

  public Object /*NotificationChannelGroup*/ getNotificationChannelGroup(String id) {
    return notificationChannelGroups.get(id);
  }

  public int size() {
    return notifications.size();
  }

  public Notification getNotification(int id) {
    return notifications.get(new Key(null, id));
  }

  public Notification getNotification(String tag, int id) {
    return notifications.get(new Key(tag, id));
  }

  public List<Notification> getAllNotifications() {
    return new ArrayList<>(notifications.values());
  }

  private static final class Key {
    public final String tag;
    public final int id;

    private Key(String tag, int id) {
      this.tag = tag;
      this.id = id;
    }

    @Override
    public int hashCode() {
      int hashCode = 17;
      hashCode = 37 * hashCode + (tag == null ? 0 : tag.hashCode());
      hashCode = 37 * hashCode + id;
      return hashCode;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Key)) return false;
      Key other = (Key) o;
      return (this.tag == null ? other.tag == null : this.tag.equals(other.tag)) && this.id == other.id;
    }
  }
}
