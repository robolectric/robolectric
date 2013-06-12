package org.robolectric.shadows;

import android.app.Notification;
import android.app.NotificationManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(NotificationManager.class)
public class ShadowNotificationManager {

  private Map<Key, Notification> notifications = new HashMap<Key, Notification>();

  @Implementation
  public void notify(int id, Notification notification)
  {
    notify(null, id, notification);
  }

  @Implementation
  public void notify(String tag, int id, Notification notification) {
    notifications.put(new Key(tag, id), notification);
  }

  @Implementation
  public void cancel(int id)
  {
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

  public int size() {
    return notifications.size();
  }

  public Notification getNotification(String tag, int id) {
    return notifications.get(new Key(tag, id));
  }

  public List<Notification> getAllNotifications() {
    return new ArrayList<Notification>(notifications.values());
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
}
