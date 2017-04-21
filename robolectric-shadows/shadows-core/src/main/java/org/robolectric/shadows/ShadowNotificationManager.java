package org.robolectric.shadows;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import com.google.common.collect.ImmutableList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(NotificationManager.class)
public class ShadowNotificationManager {
  private Map<Key, Notification> notifications = new HashMap<>();
  private final Map<String, NotificationChannel> notificationChannels = new HashMap<>();
  private final Map<String, NotificationChannelGroup> notificationChannelGroups = new HashMap<>();

  @Implementation
  public void notify(int id, Notification notification) {
    notify(null, id, notification);
  }

  @Implementation
  public void notify(String tag, int id, Notification notification) {
    notifications.put(new Key(tag, id), notification);
  }

  @Implementation
  public void cancel(int id) {
    cancel(null, id);
  }

  @Implementation
  public void cancel(String tag, int id) {
    Key key = new Key(tag, id);
    if (notifications.containsKey(key)) {
      notifications.remove(key);
    }
  }

  @Implementation
  public void cancelAll() {
    notifications.clear();
  }

  @Implementation(minSdk = Build.VERSION_CODES.M)
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
  public NotificationChannel getNotificationChannel(String channelId) {
    return notificationChannels.get(channelId);
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  public void createNotificationChannelGroup(NotificationChannelGroup group) {
    notificationChannelGroups.put(group.getId(), group);
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  public List<NotificationChannelGroup> getNotificationChannelGroups() {
    return ImmutableList.copyOf(notificationChannelGroups.values());
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  public void createNotificationChannel(NotificationChannel channel) {
    notificationChannels.put(channel.getId(), channel);
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  public List<NotificationChannel> getNotificationChannels() {
    return ImmutableList.copyOf(notificationChannels.values());
  }

  public NotificationChannelGroup getNotificationChannelGroup(String id) {
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
