package com.xtremelabs.robolectric.shadows;

import android.app.AlarmManager;
import android.app.PendingIntent;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

/**
 * Shadows the {@code android.app.AlarmManager} class.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(AlarmManager.class)
public class ShadowAlarmManager {

    private List<ScheduledAlarm> scheduledAlarms = new ArrayList<ScheduledAlarm>();

    @Implementation
    public void set(int type, long triggerAtTime, PendingIntent operation) {
        scheduledAlarms.add(new ScheduledAlarm(type, triggerAtTime, operation));
    }

    /**
     * Non-Android accessor consumes and returns the next scheduled alarm on the
     * AlarmManager's stack.
     *
     * @return the next scheduled alarm, wrapped in a
     *         {@link ShadowAlarmManager.ScheduledAlarm} object
     */
    public ScheduledAlarm getNextScheduledAlarm() {
        if (scheduledAlarms.isEmpty()) {
            return null;
        } else {
            return scheduledAlarms.remove(0);
        }
    }

    /**
     * Non-Android accessor returns the most recent scheduled alarm without
     * consuming it.
     *
     * @return the most recently scheduled alarm, wrapped in a
     *         {@link ShadowAlarmManager.ScheduledAlarm} object
     */
    public ScheduledAlarm peekNextScheduledAlarm() {
        if (scheduledAlarms.isEmpty()) {
            return null;
        } else {
            return scheduledAlarms.get(0);
        }
    }

    /**
     * Container object to hold an PendingIntent, together with the alarm
     * parameters used in a call to {@code AlarmManager}
     */
    public class ScheduledAlarm {
        public int type;
        public long triggerAtTime;
        public long interval;
        public PendingIntent operation;

        public ScheduledAlarm(int type, long triggerAtTime, PendingIntent operation) {
            this(type, triggerAtTime, 0, operation);
        }

        public ScheduledAlarm(int type, long triggerAtTime, long interval, PendingIntent operation) {
            this.type = type;
            this.triggerAtTime = triggerAtTime;
            this.operation = operation;
            this.interval = interval;
        }
    }
}
