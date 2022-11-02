package org.robolectric.shadows;

import static android.app.AlarmManager.RTC_WAKEUP;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.AlarmManager.OnAlarmListener;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.WorkSource;
import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.app.AlarmManager}. */
@Implements(AlarmManager.class)
public class ShadowAlarmManager {

  public static final long WINDOW_EXACT = 0;
  public static final long WINDOW_HEURISTIC = -1;

  private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();

  private static boolean canScheduleExactAlarms;
  private static boolean autoSchedule;

  private final Handler schedulingHandler = new Handler(Looper.getMainLooper());

  @GuardedBy("scheduledAlarms")
  private final PriorityQueue<InternalScheduledAlarm> scheduledAlarms = new PriorityQueue<>();

  @RealObject private AlarmManager realObject;

  @Resetter
  public static void reset() {
    TimeZone.setDefault(DEFAULT_TIMEZONE);
    canScheduleExactAlarms = false;
    autoSchedule = false;
  }

  /**
   * When set to true, automatically schedules alarms to fire at the appropriate time (with respect
   * to the main Looper time) when they are set. This means that a test as below could be expected
   * to pass:
   *
   * <pre>{@code
   * shadowOf(alarmManager).setAutoSchedule(true);
   * AlarmManager.OnAlarmListener listener = mock(AlarmManager.OnAlarmListener.class);
   * alarmManager.setExact(
   *   ELAPSED_REALTIME_WAKEUP,
   *   SystemClock.elapsedRealtime() + 10,
   *   "tag",
   *   listener,
   *   new Handler(Looper.getMainLooper()));
   * shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
   * verify(listener).onAlarm();
   * }</pre>
   *
   * <p>Alarms are always scheduled with respect to the trigger/window start time - there is no
   * emulation of alarms being reordered, rescheduled, or delayed, as might happen on a real device.
   * If emulating this is necessary, see {@link #fireAlarm(ScheduledAlarm)}.
   *
   * <p>{@link AlarmManager.OnAlarmListener} alarms will be run on the correct Handler/Executor as
   * specified when the alarm is set.
   */
  public static void setAutoSchedule(boolean autoSchedule) {
    ShadowAlarmManager.autoSchedule = autoSchedule;
  }

  @Implementation
  protected void set(int type, long triggerAtMs, PendingIntent operation) {
    setImpl(type, triggerAtMs, WINDOW_HEURISTIC, 0L, operation, null, null, false);
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected void set(
      int type,
      long triggerAtMs,
      @Nullable String tag,
      OnAlarmListener listener,
      @Nullable Handler handler) {
    setImpl(
        type,
        triggerAtMs,
        WINDOW_HEURISTIC,
        0L,
        tag,
        listener,
        new HandlerExecutor(handler),
        null,
        false);
  }

  @Implementation
  protected void setRepeating(
      int type, long triggerAtMs, long intervalMs, PendingIntent operation) {
    setImpl(type, triggerAtMs, WINDOW_HEURISTIC, intervalMs, operation, null, null, false);
  }

  @Implementation(minSdk = VERSION_CODES.KITKAT)
  protected void setWindow(
      int type, long windowStartMs, long windowLengthMs, PendingIntent operation) {
    setImpl(type, windowStartMs, windowLengthMs, 0L, operation, null, null, false);
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected void setWindow(
      int type,
      long windowStartMs,
      long windowLengthMs,
      @Nullable String tag,
      OnAlarmListener listener,
      @Nullable Handler handler) {
    setImpl(
        type,
        windowStartMs,
        windowLengthMs,
        0L,
        tag,
        listener,
        new HandlerExecutor(handler),
        null,
        false);
  }

  @Implementation(minSdk = 34)
  protected void setWindow(
      int type,
      long windowStartMs,
      long windowLengthMs,
      @Nullable String tag,
      Executor executor,
      OnAlarmListener listener) {
    setImpl(type, windowStartMs, windowLengthMs, 0L, tag, listener, executor, null, false);
  }

  @Implementation(minSdk = 34)
  protected void setWindow(
      int type,
      long windowStartMs,
      long windowLengthMs,
      @Nullable String tag,
      Executor executor,
      WorkSource workSource,
      OnAlarmListener listener) {
    setImpl(type, windowStartMs, windowLengthMs, 0L, tag, listener, executor, workSource, false);
  }

  @Implementation(minSdk = VERSION_CODES.S)
  protected void setPrioritized(
      int type,
      long windowStartMs,
      long windowLengthMs,
      @Nullable String tag,
      Executor executor,
      OnAlarmListener listener) {
    Objects.requireNonNull(executor);
    Objects.requireNonNull(listener);
    setImpl(type, windowStartMs, windowLengthMs, 0L, tag, listener, executor, null, true);
  }

  @Implementation(minSdk = VERSION_CODES.KITKAT)
  protected void setExact(int type, long triggerAtMs, PendingIntent operation) {
    setImpl(type, triggerAtMs, WINDOW_EXACT, 0L, operation, null, null, false);
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected void setExact(
      int type,
      long triggerAtTime,
      @Nullable String tag,
      OnAlarmListener listener,
      @Nullable Handler targetHandler) {
    setImpl(
        type,
        triggerAtTime,
        WINDOW_EXACT,
        0L,
        tag,
        listener,
        new HandlerExecutor(targetHandler),
        null,
        false);
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Implementation(minSdk = VERSION_CODES.LOLLIPOP)
  protected void setAlarmClock(AlarmClockInfo info, PendingIntent operation) {
    setImpl(RTC_WAKEUP, info.getTriggerTime(), WINDOW_EXACT, 0L, operation, null, info, true);
  }

  @Implementation(minSdk = VERSION_CODES.KITKAT)
  protected void set(
      int type,
      long triggerAtMs,
      long windowLengthMs,
      long intervalMs,
      PendingIntent operation,
      @Nullable WorkSource workSource) {
    setImpl(type, triggerAtMs, windowLengthMs, intervalMs, operation, workSource, null, false);
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected void set(
      int type,
      long triggerAtMs,
      long windowLengthMs,
      long intervalMs,
      @Nullable String tag,
      OnAlarmListener listener,
      @Nullable Handler targetHandler,
      @Nullable WorkSource workSource) {
    setImpl(
        type,
        triggerAtMs,
        windowLengthMs,
        intervalMs,
        tag,
        listener,
        new HandlerExecutor(targetHandler),
        workSource,
        false);
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected void set(
      int type,
      long triggerAtMs,
      long windowLengthMs,
      long intervalMs,
      OnAlarmListener listener,
      @Nullable Handler targetHandler,
      @Nullable WorkSource workSource) {
    setImpl(
        type,
        triggerAtMs,
        windowLengthMs,
        intervalMs,
        null,
        listener,
        new HandlerExecutor(targetHandler),
        workSource,
        false);
  }

  @Implementation(minSdk = VERSION_CODES.S)
  protected void setExact(
      int type,
      long triggerAtMs,
      @Nullable String tag,
      Executor executor,
      WorkSource workSource,
      OnAlarmListener listener) {
    Objects.requireNonNull(workSource);
    setImpl(type, triggerAtMs, WINDOW_EXACT, 0L, tag, listener, executor, workSource, false);
  }

  @Implementation
  protected void setInexactRepeating(
      int type, long triggerAtMs, long intervalMillis, PendingIntent operation) {
    setImpl(type, triggerAtMs, WINDOW_HEURISTIC, intervalMillis, operation, null, null, false);
  }

  @Implementation(minSdk = VERSION_CODES.M)
  protected void setAndAllowWhileIdle(int type, long triggerAtMs, PendingIntent operation) {
    setImpl(type, triggerAtMs, WINDOW_HEURISTIC, 0L, operation, null, null, true);
  }

  @Implementation(minSdk = VERSION_CODES.M)
  protected void setExactAndAllowWhileIdle(int type, long triggerAtMs, PendingIntent operation) {
    setImpl(type, triggerAtMs, WINDOW_EXACT, 0L, operation, null, null, true);
  }

  @Implementation(minSdk = 34)
  protected void setExactAndAllowWhileIdle(
      int type,
      long triggerAtMs,
      @Nullable String tag,
      Executor executor,
      @Nullable WorkSource workSource,
      OnAlarmListener listener) {
    setImpl(type, triggerAtMs, WINDOW_EXACT, 0L, tag, listener, executor, workSource, true);
  }

  @Implementation
  protected void cancel(PendingIntent operation) {
    synchronized (scheduledAlarms) {
      Iterables.removeIf(
          scheduledAlarms,
          alarm -> {
            if (operation.equals(alarm.operation)) {
              alarm.deschedule();
              return true;
            }
            return false;
          });
    }
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected void cancel(OnAlarmListener listener) {
    synchronized (scheduledAlarms) {
      Iterables.removeIf(
          scheduledAlarms,
          alarm -> {
            if (listener.equals(alarm.onAlarmListener)) {
              alarm.deschedule();
              return true;
            }
            return false;
          });
    }
  }

  @Implementation(minSdk = 34)
  protected void cancelAll() {
    synchronized (scheduledAlarms) {
      for (InternalScheduledAlarm alarm : scheduledAlarms) {
        alarm.deschedule();
      }
      scheduledAlarms.clear();
    }
  }

  @Implementation
  protected void setTimeZone(String timeZone) {
    // Do the real check first
    reflector(AlarmManagerReflector.class, realObject).setTimeZone(timeZone);
    // Then do the right side effect
    TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
  }

  @Implementation(minSdk = VERSION_CODES.S)
  protected boolean canScheduleExactAlarms() {
    return canScheduleExactAlarms;
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Implementation(minSdk = VERSION_CODES.LOLLIPOP)
  @Nullable
  protected AlarmClockInfo getNextAlarmClock() {
    synchronized (scheduledAlarms) {
      for (ScheduledAlarm scheduledAlarm : scheduledAlarms) {
        AlarmClockInfo alarmClockInfo = scheduledAlarm.getAlarmClockInfo();
        if (alarmClockInfo != null) {
          return alarmClockInfo;
        }
      }
      return null;
    }
  }

  private void setImpl(
      int type,
      long triggerAtMs,
      long windowLengthMs,
      long intervalMs,
      PendingIntent operation,
      @Nullable WorkSource workSource,
      @Nullable Object alarmClockInfo,
      boolean allowWhileIdle) {
    synchronized (scheduledAlarms) {
      cancel(operation);
      scheduledAlarms.add(
          new InternalScheduledAlarm(
                  type,
                  triggerAtMs,
                  windowLengthMs,
                  intervalMs,
                  operation,
                  workSource,
                  alarmClockInfo,
                  allowWhileIdle)
              .schedule());
    }
  }

  private void setImpl(
      int type,
      long triggerAtMs,
      long windowLengthMs,
      long intervalMs,
      @Nullable String tag,
      OnAlarmListener listener,
      Executor executor,
      @Nullable WorkSource workSource,
      boolean allowWhileIdle) {
    synchronized (scheduledAlarms) {
      cancel(listener);
      scheduledAlarms.add(
          new InternalScheduledAlarm(
                  type,
                  triggerAtMs,
                  windowLengthMs,
                  intervalMs,
                  tag,
                  listener,
                  executor,
                  workSource,
                  null,
                  allowWhileIdle)
              .schedule());
    }
  }

  /**
   * Returns the earliest scheduled alarm and removes it from the list of scheduled alarms.
   *
   * @deprecated Prefer to use {@link ShadowAlarmManager#setAutoSchedule(boolean)} in combination
   *     with incrementing time to actually run alarms and test their side-effects.
   */
  @Deprecated
  @Nullable
  public ScheduledAlarm getNextScheduledAlarm() {
    synchronized (scheduledAlarms) {
      InternalScheduledAlarm alarm = scheduledAlarms.poll();
      if (alarm != null) {
        alarm.deschedule();
      }
      return alarm;
    }
  }

  /** Returns the earliest scheduled alarm. */
  @Nullable
  public ScheduledAlarm peekNextScheduledAlarm() {
    synchronized (scheduledAlarms) {
      return scheduledAlarms.peek();
    }
  }

  /** Returns a list of all scheduled alarms, ordered from earliest time to latest time. */
  public List<ScheduledAlarm> getScheduledAlarms() {
    synchronized (scheduledAlarms) {
      return new ArrayList<>(scheduledAlarms);
    }
  }

  /**
   * Immediately removes the given alarm from the list of scheduled alarms (and then reschedules it
   * in the case of a repeating alarm) and fires it. The given alarm must on the list of scheduled
   * alarms prior to being fired.
   *
   * <p>Generally prefer to use {@link ShadowAlarmManager#setAutoSchedule(boolean)} in combination
   * with advancing time on the main Looper in order to test alarms - however this method can be
   * useful to emulate rescheduled, reordered, or delayed alarms, as may happen on a real device.
   */
  public void fireAlarm(ScheduledAlarm alarm) {
    synchronized (scheduledAlarms) {
      if (!scheduledAlarms.contains(alarm)) {
        throw new IllegalArgumentException();
      }

      ((InternalScheduledAlarm) alarm).deschedule();
      ((InternalScheduledAlarm) alarm).run();
    }
  }

  /**
   * Sets the schedule exact alarm state reported by {@link AlarmManager#canScheduleExactAlarms()},
   * but has no effect otherwise.
   */
  public static void setCanScheduleExactAlarms(boolean scheduleExactAlarms) {
    canScheduleExactAlarms = scheduleExactAlarms;
  }

  /** Represents a set alarm. */
  public static class ScheduledAlarm implements Comparable<ScheduledAlarm> {

    @Deprecated public final int type;
    @Deprecated public final long triggerAtTime;
    private final long windowLengthMs;
    @Deprecated public final long interval;
    @Nullable private final String tag;
    @Deprecated @Nullable public final PendingIntent operation;
    @Deprecated @Nullable public final OnAlarmListener onAlarmListener;
    @Deprecated @Nullable public final Executor executor;
    @Nullable private final WorkSource workSource;
    @Nullable private final Object alarmClockInfo;
    @Deprecated public final boolean allowWhileIdle;

    @Deprecated @Nullable public final PendingIntent showIntent;
    @Deprecated @Nullable public final Handler handler;

    @Deprecated
    public ScheduledAlarm(
        int type, long triggerAtMs, PendingIntent operation, PendingIntent showIntent) {
      this(type, triggerAtMs, 0, operation, showIntent);
    }

    @Deprecated
    public ScheduledAlarm(
        int type,
        long triggerAtMs,
        long intervalMs,
        PendingIntent operation,
        PendingIntent showIntent) {
      this(type, triggerAtMs, intervalMs, operation, showIntent, false);
    }

    @Deprecated
    public ScheduledAlarm(
        int type,
        long triggerAtMs,
        long intervalMs,
        PendingIntent operation,
        PendingIntent showIntent,
        boolean allowWhileIdle) {
      this(
          type,
          triggerAtMs,
          intervalMs,
          WINDOW_HEURISTIC,
          operation,
          null,
          VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && showIntent != null
              ? new AlarmClockInfo(triggerAtMs, showIntent)
              : null,
          allowWhileIdle);
    }

    protected ScheduledAlarm(
        int type,
        long triggerAtMs,
        long windowLengthMs,
        long intervalMs,
        PendingIntent operation,
        @Nullable WorkSource workSource,
        @Nullable Object alarmClockInfo,
        boolean allowWhileIdle) {
      this.type = type;
      this.triggerAtTime = triggerAtMs;
      this.windowLengthMs = windowLengthMs;
      this.interval = intervalMs;
      this.tag = null;
      this.operation = Objects.requireNonNull(operation);
      this.onAlarmListener = null;
      this.executor = null;
      this.workSource = workSource;
      this.alarmClockInfo = alarmClockInfo;
      this.allowWhileIdle = allowWhileIdle;

      this.handler = null;
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && alarmClockInfo != null) {
        this.showIntent = ((AlarmClockInfo) alarmClockInfo).getShowIntent();
      } else {
        this.showIntent = null;
      }
    }

    protected ScheduledAlarm(
        int type,
        long triggerAtMs,
        long windowLengthMs,
        long intervalMs,
        @Nullable String tag,
        OnAlarmListener listener,
        Executor executor,
        @Nullable WorkSource workSource,
        @Nullable Object alarmClockInfo,
        boolean allowWhileIdle) {
      this.type = type;
      this.triggerAtTime = triggerAtMs;
      this.windowLengthMs = windowLengthMs;
      this.interval = intervalMs;
      this.tag = tag;
      this.operation = null;
      this.onAlarmListener = Objects.requireNonNull(listener);
      this.executor = Objects.requireNonNull(executor);
      this.workSource = workSource;
      this.alarmClockInfo = alarmClockInfo;
      this.allowWhileIdle = allowWhileIdle;

      if (executor instanceof HandlerExecutor) {
        this.handler = ((HandlerExecutor) executor).handler;
      } else {
        this.handler = null;
      }
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && alarmClockInfo != null) {
        this.showIntent = ((AlarmClockInfo) alarmClockInfo).getShowIntent();
      } else {
        this.showIntent = null;
      }
    }

    protected ScheduledAlarm(long triggerAtMs, ScheduledAlarm alarm) {
      this.type = alarm.type;
      this.triggerAtTime = triggerAtMs;
      this.windowLengthMs = alarm.windowLengthMs;
      this.interval = alarm.interval;
      this.tag = alarm.tag;
      this.operation = alarm.operation;
      this.onAlarmListener = alarm.onAlarmListener;
      this.executor = alarm.executor;
      this.workSource = alarm.workSource;
      this.alarmClockInfo = alarm.alarmClockInfo;
      this.allowWhileIdle = alarm.allowWhileIdle;

      this.handler = alarm.handler;
      this.showIntent = alarm.showIntent;
    }

    public int getType() {
      return type;
    }

    public long getTriggerAtMs() {
      return triggerAtTime;
    }

    public long getWindowLengthMs() {
      return windowLengthMs;
    }

    public long getIntervalMs() {
      return interval;
    }

    @Nullable
    public String getTag() {
      return tag;
    }

    @Nullable
    public WorkSource getWorkSource() {
      return workSource;
    }

    @RequiresApi(VERSION_CODES.LOLLIPOP)
    @Nullable
    public AlarmClockInfo getAlarmClockInfo() {
      return (AlarmClockInfo) alarmClockInfo;
    }

    public boolean isAllowWhileIdle() {
      return allowWhileIdle;
    }

    @Override
    public int compareTo(ScheduledAlarm scheduledAlarm) {
      return Long.compare(triggerAtTime, scheduledAlarm.triggerAtTime);
    }
  }

  // wrapper class created because we can't modify ScheduledAlarm without breaking compatibility
  private class InternalScheduledAlarm extends ScheduledAlarm implements Runnable {

    InternalScheduledAlarm(
        int type,
        long triggerAtMs,
        long windowLengthMs,
        long intervalMs,
        PendingIntent operation,
        @Nullable WorkSource workSource,
        @Nullable Object alarmClockInfo,
        boolean allowWhileIdle) {
      super(
          type,
          triggerAtMs,
          windowLengthMs,
          intervalMs,
          operation,
          workSource,
          alarmClockInfo,
          allowWhileIdle);
    }

    InternalScheduledAlarm(
        int type,
        long triggerAtMs,
        long windowLengthMs,
        long intervalMs,
        @Nullable String tag,
        OnAlarmListener listener,
        Executor executor,
        @Nullable WorkSource workSource,
        @Nullable Object alarmClockInfo,
        boolean allowWhileIdle) {
      super(
          type,
          triggerAtMs,
          windowLengthMs,
          intervalMs,
          tag,
          listener,
          executor,
          workSource,
          alarmClockInfo,
          allowWhileIdle);
    }

    InternalScheduledAlarm(long triggerAtMs, InternalScheduledAlarm alarm) {
      super(triggerAtMs, alarm);
    }

    InternalScheduledAlarm schedule() {
      if (autoSchedule) {
        schedulingHandler.postDelayed(this, triggerAtTime - SystemClock.elapsedRealtime());
      }
      return this;
    }

    void deschedule() {
      schedulingHandler.removeCallbacks(this);
    }

    @Override
    public void run() {
      Executor executor;
      if (operation != null) {
        executor = Runnable::run;
      } else {
        executor = Objects.requireNonNull(this.executor);
      }

      executor.execute(
          () -> {
            synchronized (scheduledAlarms) {
              if (!scheduledAlarms.remove(this)) {
                return;
              }
              if (interval > 0) {
                scheduledAlarms.add(
                    new InternalScheduledAlarm(triggerAtTime + interval, this).schedule());
              }
            }
            if (operation != null) {
              try {
                operation.send();
              } catch (CanceledException e) {
                // only necessary in case this is a repeated alarm and we've already rescheduled
                cancel(operation);
              }
            } else if (VERSION.SDK_INT >= VERSION_CODES.N) {
              Objects.requireNonNull(onAlarmListener).onAlarm();
            } else {
              throw new IllegalStateException();
            }
          });
    }
  }

  private static final class HandlerExecutor implements Executor {
    private final Handler handler;

    HandlerExecutor(@Nullable Handler handler) {
      this.handler = handler != null ? handler : new Handler(Looper.getMainLooper());
    }

    @Override
    public void execute(Runnable command) {
      if (!handler.post(command)) {
        throw new RejectedExecutionException(handler + " is shutting down");
      }
    }
  }

  @ForType(AlarmManager.class)
  interface AlarmManagerReflector {

    @Direct
    void setTimeZone(String timeZone);
  }
}
