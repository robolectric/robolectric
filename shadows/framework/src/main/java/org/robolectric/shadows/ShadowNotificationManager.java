package org.robolectric.shadows;

import static android.app.NotificationManager.INTERRUPTION_FILTER_ALL;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;

import android.app.AutomaticZenRule;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.content.ComponentName;
import android.os.Build;
import android.os.Parcel;
import android.service.notification.StatusBarNotification;
import androidx.annotation.NonNull;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadows for NotificationManager. */
@SuppressWarnings({"UnusedDeclaration", "AndroidConcurrentHashMap"})
@Implements(value = NotificationManager.class, looseSignatures = true)
public class ShadowNotificationManager {
  private static final int MAX_NOTIFICATION_LIMIT = 25;
  private boolean mAreNotificationsEnabled = true;
  private boolean isNotificationPolicyAccessGranted = false;
  private boolean enforceMaxNotificationLimit = false;
  private final Map<Key, PostedNotification> notifications = new ConcurrentHashMap<>();
  private final Map<String, Object> notificationChannels = new ConcurrentHashMap<>();
  private final Map<String, Object> notificationChannelGroups = new ConcurrentHashMap<>();
  private final Map<String, Object> deletedNotificationChannels = new ConcurrentHashMap<>();
  private final Map<String, AutomaticZenRule> automaticZenRules = new ConcurrentHashMap<>();
  private final Map<String, Boolean> listenerAccessGrantedComponents = new ConcurrentHashMap<>();
  private final Set<String> canNotifyOnBehalfPackages = Sets.newConcurrentHashSet();

  private int currentInteruptionFilter = INTERRUPTION_FILTER_ALL;
  private Policy notificationPolicy;
  private String notificationDelegate;
  private int importance;

  @Implementation
  protected void notify(int id, Notification notification) {
    notify(null, id, notification);
  }

  @Implementation
  protected void notify(String tag, int id, Notification notification) {
    if (!enforceMaxNotificationLimit || notifications.size() < MAX_NOTIFICATION_LIMIT) {
      notifications.put(
          new Key(tag, id), new PostedNotification(notification, ShadowSystem.currentTimeMillis()));
    }
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

  @Implementation(minSdk = Build.VERSION_CODES.N)
  protected int getImportance() {
    return importance;
  }

  public void setImportance(int importance) {
    this.importance = importance;
  }

  @Implementation(minSdk = M)
  public StatusBarNotification[] getActiveNotifications() {
    // Must make a copy because otherwise the size of the map may change after we have allocated
    // the array:
    ImmutableMap<Key, PostedNotification> notifsCopy = ImmutableMap.copyOf(notifications);
    StatusBarNotification[] statusBarNotifications = new StatusBarNotification[notifsCopy.size()];
    int i = 0;
    for (Map.Entry<Key, PostedNotification> entry : notifsCopy.entrySet()) {
      statusBarNotifications[i++] =
          new StatusBarNotification(
              RuntimeEnvironment.getApplication().getPackageName(),
              null /* opPkg */,
              entry.getKey().id,
              entry.getKey().tag,
              android.os.Process.myUid() /* uid */,
              android.os.Process.myPid() /* initialPid */,
              0 /* score */,
              entry.getValue().notification,
              android.os.Process.myUserHandle(),
              entry.getValue().postedTimeMillis /* postTime */);
    }
    return statusBarNotifications;
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected Object /*NotificationChannel*/ getNotificationChannel(String channelId) {
    return notificationChannels.get(channelId);
  }

  /** Returns a NotificationChannel that has the given parent and conversation ID. */
  @Implementation(minSdk = R)
  protected NotificationChannel getNotificationChannel(String channelId, String conversationId) {
    for (Object object : getNotificationChannels()) {
      NotificationChannel notificationChannel = (NotificationChannel) object;
      if (conversationId.equals(notificationChannel.getConversationId())
          && channelId.equals(notificationChannel.getParentChannelId())) {
        return notificationChannel;
      }
    }
    return null;
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected void createNotificationChannelGroup(Object /*NotificationChannelGroup*/ group) {
    String id = ReflectionHelpers.callInstanceMethod(group, "getId");
    notificationChannelGroups.put(id, group);
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected void createNotificationChannelGroups(
      List<Object /*NotificationChannelGroup*/> groupList) {
    for (Object group : groupList) {
      createNotificationChannelGroup(group);
    }
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
    }
    NotificationChannel existingChannel = (NotificationChannel) notificationChannels.get(id);
    // Per documentation, recreating a channel can change name and description, lower importance or
    // set a group if no group set. Other settings remain unchanged. See
    // https://developer.android.com/reference/android/app/NotificationManager#createNotificationChannel%28android.app.NotificationChannel@29
    // for more info.
    if (existingChannel != null) {
      NotificationChannel newChannel = (NotificationChannel) channel;
      existingChannel.setName(newChannel.getName());
      existingChannel.setDescription(newChannel.getDescription());
      if (newChannel.getImportance() < existingChannel.getImportance()) {
        existingChannel.setImportance(newChannel.getImportance());
      }
      if (Strings.isNullOrEmpty(existingChannel.getGroup())) {
        existingChannel.setGroup(newChannel.getGroup());
      }
      return;
    }
    notificationChannels.put(id, channel);
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
   *     via {@link #setInterruptionFilter(int)}
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

  /** @return the value specified via {@link #setNotificationPolicy(Policy)} */
  @Implementation(minSdk = M)
  protected final Policy getNotificationPolicy() {
    return notificationPolicy;
  }

  /** @return the value specified via {@link #setNotificationPolicyAccessGranted(boolean)} */
  @Implementation(minSdk = M)
  protected final boolean isNotificationPolicyAccessGranted() {
    return isNotificationPolicyAccessGranted;
  }

  /**
   * @return the value specified for the given {@link ComponentName} via {@link
   *     #setNotificationListenerAccessGranted(ComponentName, boolean)} or false if unset.
   */
  @Implementation(minSdk = O_MR1)
  protected final boolean isNotificationListenerAccessGranted(ComponentName componentName) {
    return listenerAccessGrantedComponents.getOrDefault(componentName.flattenToString(), false);
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

  /**
   * Sets the value returned by {@link
   * NotificationManager#isNotificationListenerAccessGranted(ComponentName)} for the provided {@link
   * ComponentName}.
   */
  @Implementation(minSdk = O_MR1)
  public void setNotificationListenerAccessGranted(ComponentName componentName, boolean granted) {
    listenerAccessGrantedComponents.put(componentName.flattenToString(), granted);
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
    Preconditions.checkState(
        automaticZenRule.getOwner() != null || automaticZenRule.getConfigurationActivity() != null,
        "owner/configurationActivity cannot be null at the same time");

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
    Preconditions.checkState(
        automaticZenRule.getOwner() != null || automaticZenRule.getConfigurationActivity() != null,
        "owner/configurationActivity cannot be null at the same time");
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

  @Implementation(minSdk = Q)
  protected String getNotificationDelegate() {
    return notificationDelegate;
  }

  @Implementation(minSdk = Q)
  protected boolean canNotifyAsPackage(@NonNull String pkg) {
    // TODO: This doesn't work correctly with notification delegates because
    // ShadowNotificationManager doesn't respect the associated context, it just uses the global
    // RuntimeEnvironment.getApplication() context.

    // So for the sake of testing, we will compare with values set using
    // setCanNotifyAsPackage()
    return canNotifyOnBehalfPackages.contains(pkg);
  }

  /**
   * Sets notification delegate for the package provided.
   *
   * <p>{@link #canNotifyAsPackage(String)} will be returned based on this value.
   *
   * @param otherPackage the package for which the current package can notify on behalf
   * @param canNotify whether the current package is set as notification delegate for 'otherPackage'
   */
  public void setCanNotifyAsPackage(@NonNull String otherPackage, boolean canNotify) {
    if (canNotify) {
      canNotifyOnBehalfPackages.add(otherPackage);
    } else {
      canNotifyOnBehalfPackages.remove(otherPackage);
    }
  }

  @Implementation(minSdk = Q)
  protected void setNotificationDelegate(String delegate) {
    notificationDelegate = delegate;
  }

  /**
   * Ensures a notification limit is applied before posting the notification.
   *
   * <p>When set to true a maximum notification limit of 25 is applied. Notifications past this
   * limit are dropped and are not posted or enqueued.
   *
   * <p>When set to false no limit is applied and all notifications are posted or enqueued. This is
   * the default behavior.
   */
  public void setEnforceMaxNotificationLimit(boolean enforceMaxNotificationLimit) {
    this.enforceMaxNotificationLimit = enforceMaxNotificationLimit;
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

  @Implementation(minSdk = P)
  public Object /*NotificationChannelGroup*/ getNotificationChannelGroup(String id) {
    return notificationChannelGroups.get(id);
  }

  public int size() {
    return notifications.size();
  }

  public Notification getNotification(int id) {
    PostedNotification postedNotification = notifications.get(new Key(null, id));
    return postedNotification == null ? null : postedNotification.notification;
  }

  public Notification getNotification(String tag, int id) {
    PostedNotification postedNotification = notifications.get(new Key(tag, id));
    return postedNotification == null ? null : postedNotification.notification;
  }

  public List<Notification> getAllNotifications() {
    List<Notification> result = new ArrayList<>(notifications.size());
    for (PostedNotification postedNotification : notifications.values()) {
      result.add(postedNotification.notification);
    }
    return result;
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
      return (this.tag == null ? other.tag == null : this.tag.equals(other.tag))
          && this.id == other.id;
    }
  }

  private static final class PostedNotification {
    private final Notification notification;
    private final long postedTimeMillis;

    private PostedNotification(Notification notification, long postedTimeMillis) {
      this.notification = notification;
      this.postedTimeMillis = postedTimeMillis;
    }
  }
}
