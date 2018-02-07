package org.robolectric.shadows;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.Intent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AlarmManager.class)
public class ShadowAlarmManager {

  private final List<ScheduledAlarm> scheduledAlarms = new ArrayList<>();

  @Implementation
  public void set(int type, long triggerAtTime, PendingIntent operation) {
    internalSet(type, triggerAtTime, 0L, operation, null);
  }

  @Implementation(minSdk = KITKAT)
  public void setExact(int type, long triggerAtTime, PendingIntent operation) {
    internalSet(type, triggerAtTime, 0L, operation, null);
  }

  @Implementation(minSdk = KITKAT)
  public void setWindow(int type, long windowStartMillis, long windowLengthMillis,
      PendingIntent operation) {
    internalSet(type, windowStartMillis, 0L, operation, null);
  }

  @Implementation(minSdk = M)
  public void setAndAllowWhileIdle(int type, long triggerAtTime, PendingIntent operation) {
    internalSet(type, triggerAtTime, 0L, operation, null);
  }

  @Implementation(minSdk = M)
  public void setExactAndAllowWhileIdle(int type, long triggerAtTime, PendingIntent operation) {
    internalSet(type, triggerAtTime, 0L, operation, null);
  }

  @Implementation
  public void setRepeating(int type, long triggerAtTime, long interval, PendingIntent operation) {
    internalSet(type, triggerAtTime, interval, operation, null);
  }

  @Implementation
  public void setInexactRepeating(int type, long triggerAtMillis, long intervalMillis,
      PendingIntent operation) {
    internalSet(type, triggerAtMillis, intervalMillis, operation, null);
  }

  @Implementation(minSdk = LOLLIPOP)
  public void setAlarmClock(AlarmClockInfo info, PendingIntent operation) {
    internalSet(RTC_WAKEUP, info.getTriggerTime(), 0L, operation, info.getShowIntent());
  }

  @Implementation(minSdk = LOLLIPOP)
  public AlarmClockInfo getNextAlarmClock() {
    for (ScheduledAlarm scheduledAlarm : scheduledAlarms) {
      AlarmClockInfo alarmClockInfo = scheduledAlarm.getAlarmClockInfo();
      if (alarmClockInfo != null) {
        return alarmClockInfo;
      }
    }
    return null;
  }

  private void internalSet(int type, long triggerAtTime, long interval, PendingIntent operation,
      PendingIntent showIntent) {
    cancel(operation);
    scheduledAlarms.add(new ScheduledAlarm(type, triggerAtTime, interval, operation, showIntent));
    Collections.sort(scheduledAlarms);
  }

  /**
   * @return the next scheduled alarm after consuming it
   */
  public ScheduledAlarm getNextScheduledAlarm() {
    if (scheduledAlarms.isEmpty()) {
      return null;
    } else {
      return scheduledAlarms.remove(0);
    }
  }

  /**
   * @return the most recently scheduled alarm without consuming it
   */
  public ScheduledAlarm peekNextScheduledAlarm() {
    if (scheduledAlarms.isEmpty()) {
      return null;
    } else {
      return scheduledAlarms.get(0);
    }
  }

  /**
   * @return all scheduled alarms
   */
  public List<ScheduledAlarm> getScheduledAlarms() {
    return scheduledAlarms;
  }

  @Implementation
  public void cancel(PendingIntent operation) {
    ShadowPendingIntent shadowPendingIntent = Shadows.shadowOf(operation);
    final Intent toRemove = shadowPendingIntent.getSavedIntent();
    final int requestCode = shadowPendingIntent.getRequestCode();
    for (ScheduledAlarm scheduledAlarm : scheduledAlarms) {
      ShadowPendingIntent scheduledShadowPendingIntent = Shadows.shadowOf(scheduledAlarm.operation);
      final Intent scheduledIntent = scheduledShadowPendingIntent.getSavedIntent();
      final int scheduledRequestCode = scheduledShadowPendingIntent.getRequestCode();
      if (scheduledIntent.filterEquals(toRemove) && scheduledRequestCode == requestCode) {
        scheduledAlarms.remove(scheduledAlarm);
        break;
      }
    }
  }

  /**
   * Container object to hold a PendingIntent and parameters describing when to send it.
   */
  public static class ScheduledAlarm implements Comparable<ScheduledAlarm> {

    public final int type;
    public final long triggerAtTime;
    public final long interval;
    public final PendingIntent operation;

    // A non-null showIntent implies this alarm has a user interface. (i.e. in an alarm clock app)
    public final PendingIntent showIntent;

    public ScheduledAlarm(int type, long triggerAtTime, PendingIntent operation,
        PendingIntent showIntent) {
      this(type, triggerAtTime, 0, operation, showIntent);
    }

    public ScheduledAlarm(int type, long triggerAtTime, long interval, PendingIntent operation,
        PendingIntent showIntent) {
      this.type = type;
      this.triggerAtTime = triggerAtTime;
      this.operation = operation;
      this.interval = interval;
      this.showIntent = showIntent;
    }

    @TargetApi(LOLLIPOP)
    public AlarmClockInfo getAlarmClockInfo() {
      return showIntent == null ? null : new AlarmClockInfo(triggerAtTime, showIntent);
    }

    @Override
    public int compareTo(ScheduledAlarm scheduledAlarm) {
      return Long.compare(triggerAtTime, scheduledAlarm.triggerAtTime);
    }
  }
}
