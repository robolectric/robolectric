package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.M;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AlarmManager.class)
public class ShadowAlarmManager {

  private List<ScheduledAlarm> scheduledAlarms = new ArrayList<ScheduledAlarm>();

  @Implementation
  public void set(int type, long triggerAtTime, PendingIntent operation) {
    internalSet(type, triggerAtTime, 0L, operation);
  }

  @Implementation(minSdk = KITKAT)
  public void setExact(int type, long triggerAtTime, PendingIntent operation) {
    internalSet(type, triggerAtTime, 0L, operation);
  }

  @Implementation(minSdk = KITKAT)
  public void setWindow(
      int type, long windowStartMillis, long windowLengthMillis, PendingIntent operation) {
    internalSet(type, windowStartMillis, 0L, operation);
  }

  @Implementation(minSdk = M)
  public void setAndAllowWhileIdle(int type, long triggerAtTime, PendingIntent operation) {
    internalSet(type, triggerAtTime, 0L, operation);
  }

  @Implementation(minSdk = M)
  public void setExactAndAllowWhileIdle(int type, long triggerAtTime, PendingIntent operation) {
    internalSet(type, triggerAtTime, 0L, operation);
  }

  @Implementation
  public void setRepeating(int type, long triggerAtTime, long interval, PendingIntent operation) {
    internalSet(type, triggerAtTime, interval, operation);
  }

  @Implementation
  public void setInexactRepeating(int type, long triggerAtMillis, long intervalMillis, PendingIntent operation) {
    internalSet(type, triggerAtMillis, intervalMillis, operation);
  }

  private void internalSet(int type, long triggerAtTime, long interval, PendingIntent operation) {
    ShadowPendingIntent shadowPendingIntent = Shadows.shadowOf(operation);
    Intent intent = shadowPendingIntent.getSavedIntent();
    int requestCode = shadowPendingIntent.getRequestCode();
    for (ScheduledAlarm scheduledAlarm : scheduledAlarms) {
      ShadowPendingIntent shadowScheduledPendingIntent = Shadows.shadowOf(scheduledAlarm.operation);
      Intent scheduledIntent = shadowScheduledPendingIntent.getSavedIntent();
      int scheduledRequestCode = shadowScheduledPendingIntent.getRequestCode();
      if (scheduledIntent.filterEquals(intent) && requestCode == scheduledRequestCode) {
        scheduledAlarms.remove(scheduledAlarm);
        break;
      }
    }
    scheduledAlarms.add(new ScheduledAlarm(type, triggerAtTime, interval, operation));
  }

  /**
   * Consumes and returns the next scheduled alarm on the
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
   * Returns the most recent scheduled alarm without consuming it.
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

  public List<ScheduledAlarm> getScheduledAlarms() {
    return scheduledAlarms;
  }

  @Implementation
  public void cancel(PendingIntent pendingIntent) {
    ShadowPendingIntent shadowPendingIntent = Shadows.shadowOf(pendingIntent);
    final Intent intentTypeToRemove = shadowPendingIntent.getSavedIntent();
    final int requestCode = shadowPendingIntent.getRequestCode();
    for (ScheduledAlarm scheduledAlarm : new ArrayList<ScheduledAlarm>(scheduledAlarms)) {
      ShadowPendingIntent scheduledShadowPendingIntent = Shadows.shadowOf(scheduledAlarm.operation);
      final Intent alarmIntent = scheduledShadowPendingIntent.getSavedIntent();
      final int alarmRequestCode = scheduledShadowPendingIntent.getRequestCode();
      if (intentTypeToRemove.filterEquals(alarmIntent) && requestCode == alarmRequestCode) {
        scheduledAlarms.remove(scheduledAlarm);
      }
    }
  }

  /**
   * Container object to hold an PendingIntent, together with the alarm
   * parameters used in a call to {@code AlarmManager}
   */
  public static class ScheduledAlarm {
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
