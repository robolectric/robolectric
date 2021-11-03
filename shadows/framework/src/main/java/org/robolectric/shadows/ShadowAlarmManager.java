package org.robolectric.shadows;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.AlarmManager.OnAlarmListener;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AlarmManager.class)
public class ShadowAlarmManager {

  private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();

  private static boolean canScheduleExactAlarms;
  private final List<ScheduledAlarm> scheduledAlarms = new CopyOnWriteArrayList<>();

  @RealObject private AlarmManager realObject;

  @Resetter
  public static void reset() {
    TimeZone.setDefault(DEFAULT_TIMEZONE);
    canScheduleExactAlarms = false;
  }

  @Implementation
  protected void setTimeZone(String timeZone) {
    // Do the real check first
    reflector(AlarmManagerReflector.class, realObject).setTimeZone(timeZone);
    // Then do the right side effect
    TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
  }

  @Implementation
  protected void set(int type, long triggerAtTime, PendingIntent operation) {
    internalSet(type, triggerAtTime, 0L, operation, null);
  }

  @Implementation(minSdk = N)
  protected void set(
      int type, long triggerAtTime, String tag, OnAlarmListener listener, Handler targetHandler) {
    internalSet(type, triggerAtTime, listener, targetHandler);
  }

  @Implementation(minSdk = KITKAT)
  protected void setExact(int type, long triggerAtTime, PendingIntent operation) {
    internalSet(type, triggerAtTime, 0L, operation, null);
  }

  @Implementation(minSdk = N)
  protected void setExact(
      int type, long triggerAtTime, String tag, OnAlarmListener listener, Handler targetHandler) {
    internalSet(type, triggerAtTime, listener, targetHandler);
  }

  @Implementation(minSdk = KITKAT)
  protected void setWindow(
      int type, long windowStartMillis, long windowLengthMillis, PendingIntent operation) {
    internalSet(type, windowStartMillis, 0L, operation, null);
  }

  @Implementation(minSdk = N)
  protected void setWindow(
      int type,
      long windowStartMillis,
      long windowLengthMillis,
      String tag,
      OnAlarmListener listener,
      Handler targetHandler) {
    internalSet(type, windowStartMillis, listener, targetHandler);
  }

  @Implementation(minSdk = M)
  protected void setAndAllowWhileIdle(int type, long triggerAtTime, PendingIntent operation) {
    internalSet(type, triggerAtTime, 0L, operation, null);
  }

  @Implementation(minSdk = M)
  protected void setExactAndAllowWhileIdle(int type, long triggerAtTime, PendingIntent operation) {
    internalSet(type, triggerAtTime, 0L, operation, null);
  }

  @Implementation
  protected void setRepeating(
      int type, long triggerAtTime, long interval, PendingIntent operation) {
    internalSet(type, triggerAtTime, interval, operation, null);
  }

  @Implementation
  protected void setInexactRepeating(
      int type, long triggerAtMillis, long intervalMillis, PendingIntent operation) {
    internalSet(type, triggerAtMillis, intervalMillis, operation, null);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void setAlarmClock(AlarmClockInfo info, PendingIntent operation) {
    internalSet(RTC_WAKEUP, info.getTriggerTime(), 0L, operation, info.getShowIntent());
  }

  @Implementation(minSdk = LOLLIPOP)
  protected AlarmClockInfo getNextAlarmClock() {
    for (ScheduledAlarm scheduledAlarm : scheduledAlarms) {
      AlarmClockInfo alarmClockInfo = scheduledAlarm.getAlarmClockInfo();
      if (alarmClockInfo != null) {
        return alarmClockInfo;
      }
    }
    return null;
  }

  private void internalSet(
      int type,
      long triggerAtTime,
      long interval,
      PendingIntent operation,
      PendingIntent showIntent) {
    cancel(operation);
    synchronized (scheduledAlarms) {
      scheduledAlarms.add(new ScheduledAlarm(type, triggerAtTime, interval, operation, showIntent));
      Collections.sort(scheduledAlarms);
    }
  }

  private void internalSet(
      int type, long triggerAtTime, OnAlarmListener listener, Handler handler) {
    cancel(listener);
    synchronized (scheduledAlarms) {
      scheduledAlarms.add(new ScheduledAlarm(type, triggerAtTime, 0L, listener, handler));
      Collections.sort(scheduledAlarms);
    }
  }

  /** @return the next scheduled alarm after consuming it */
  public ScheduledAlarm getNextScheduledAlarm() {
    if (scheduledAlarms.isEmpty()) {
      return null;
    } else {
      return scheduledAlarms.remove(0);
    }
  }

  /** @return the most recently scheduled alarm without consuming it */
  public ScheduledAlarm peekNextScheduledAlarm() {
    if (scheduledAlarms.isEmpty()) {
      return null;
    } else {
      return scheduledAlarms.get(0);
    }
  }

  /** @return all scheduled alarms */
  public List<ScheduledAlarm> getScheduledAlarms() {
    return scheduledAlarms;
  }

  @Implementation
  protected void cancel(PendingIntent operation) {
    ShadowPendingIntent shadowPendingIntent = Shadow.extract(operation);
    final Intent toRemove = shadowPendingIntent.getSavedIntent();
    final int requestCode = shadowPendingIntent.getRequestCode();
    for (ScheduledAlarm scheduledAlarm : scheduledAlarms) {
      if (scheduledAlarm.operation != null) {
        ShadowPendingIntent scheduledShadowPendingIntent = Shadow.extract(scheduledAlarm.operation);
        final Intent scheduledIntent = scheduledShadowPendingIntent.getSavedIntent();
        final int scheduledRequestCode = scheduledShadowPendingIntent.getRequestCode();
        if (scheduledIntent.filterEquals(toRemove) && scheduledRequestCode == requestCode) {
          scheduledAlarms.remove(scheduledAlarm);
          break;
        }
      }
    }
  }

  @Implementation(minSdk = N)
  protected void cancel(OnAlarmListener listener) {
    for (ScheduledAlarm scheduledAlarm : scheduledAlarms) {
      if (scheduledAlarm.onAlarmListener != null) {
        if (scheduledAlarm.onAlarmListener.equals(listener)) {
          scheduledAlarms.remove(scheduledAlarm);
          break;
        }
      }
    }
  }

  /** Returns the schedule exact alarm state set by {@link #setCanScheduleExactAlarms}. */
  @Implementation(minSdk = S)
  protected boolean canScheduleExactAlarms() {
    return canScheduleExactAlarms;
  }

  /**
   * Sets the schedule exact alarm state reported by {@link AlarmManager#canScheduleExactAlarms},
   * but has no effect otherwise.
   */
  public static void setCanScheduleExactAlarms(boolean scheduleExactAlarms) {
    canScheduleExactAlarms = scheduleExactAlarms;
  }

  /** Container object to hold a PendingIntent and parameters describing when to send it. */
  public static class ScheduledAlarm implements Comparable<ScheduledAlarm> {

    public final int type;
    public final long triggerAtTime;
    public final long interval;
    public final PendingIntent operation;

    // A non-null showIntent implies this alarm has a user interface. (i.e. in an alarm clock app)
    public final PendingIntent showIntent;

    public final OnAlarmListener onAlarmListener;
    public final Handler handler;

    public ScheduledAlarm(
        int type, long triggerAtTime, PendingIntent operation, PendingIntent showIntent) {
      this(type, triggerAtTime, 0, operation, showIntent);
    }

    public ScheduledAlarm(
        int type,
        long triggerAtTime,
        long interval,
        PendingIntent operation,
        PendingIntent showIntent) {
      this(type, triggerAtTime, interval, operation, showIntent, null, null);
    }

    private ScheduledAlarm(
        int type,
        long triggerAtTime,
        long interval,
        OnAlarmListener onAlarmListener,
        Handler handler) {
      this(type, triggerAtTime, interval, null, null, onAlarmListener, handler);
    }

    private ScheduledAlarm(
        int type,
        long triggerAtTime,
        long interval,
        PendingIntent operation,
        PendingIntent showIntent,
        OnAlarmListener onAlarmListener,
        Handler handler) {
      this.type = type;
      this.triggerAtTime = triggerAtTime;
      this.operation = operation;
      this.interval = interval;
      this.showIntent = showIntent;
      this.onAlarmListener = onAlarmListener;
      this.handler = handler;
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

  @ForType(AlarmManager.class)
  interface AlarmManagerReflector {

    @Direct
    void setTimeZone(String timeZone);
  }
}
