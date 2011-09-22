package com.xtremelabs.robolectric.shadows;

import android.app.Notification;
import android.app.NotificationManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(NotificationManager.class)
public class ShadowNotificationManager {

    private Map<Integer, Notification> notifications = new HashMap<Integer, Notification>();
    private Map<String, Integer> idForTag = new HashMap<String, Integer>();

    @Implementation
    public void notify(int id, Notification notification)
    {
        notify(null, id, notification);
    }

    @Implementation
    public void notify(String tag, int id, Notification notification) {
        if (tag != null) {
            idForTag.put(tag, id);
        }
        notifications.put(id, notification);
    }

    @Implementation
    public void cancel(int id)
    {
        cancel(null, id);
    }

    @Implementation
    public void cancel(String tag, int id) {
        // I can't make sense of this method signature. I'm guessing that the id is optional and if it's bogus you are supposed to use the tag instead, but that references to both should be gone. PG
        Integer tagId = idForTag.remove(tag);
        if (notifications.containsKey(id)) {
            notifications.remove(id);
        } else {
            notifications.remove(tagId);
        }
    }

    @Implementation
    public void cancelAll() {
        notifications.clear();
        idForTag.clear();
    }

    public int size() {
        return notifications.size();
    }

    public Notification getNotification(int id) {
        return notifications.get(id);
    }

    public Notification getNotification(String tag) {
        Integer id = idForTag.get(tag);
        return notifications.get(id);
    }

    public List<Notification> getAllNotifications() {
        return new ArrayList<Notification>(notifications.values());
    }
}
