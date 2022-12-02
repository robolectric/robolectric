package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.AlarmManager.OnAlarmListener;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.os.SystemClock;
import android.os.WorkSource;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.time.Duration;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager.ScheduledAlarm;

@RunWith(AndroidJUnit4.class)
public class ShadowAlarmManagerTest {

  private Context context;
  private AlarmManager alarmManager;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    ShadowAlarmManager.setAutoSchedule(true);
  }

  @Test
  public void setTimeZone_UTC_acceptAlways() {
    alarmManager.setTimeZone("UTC");
    assertThat(TimeZone.getDefault().getID()).isEqualTo("UTC");
  }

  @Test
  public void setTimeZone_OlsonTimeZone_acceptAlways() {
    alarmManager.setTimeZone("America/Sao_Paulo");
    assertThat(TimeZone.getDefault().getID()).isEqualTo("America/Sao_Paulo");
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void setTimeZone_abbreviateTimeZone_ignore() {
    assertThrows(IllegalArgumentException.class, () -> alarmManager.setTimeZone("PST"));
  }

  @Test
  @Config(maxSdk = VERSION_CODES.LOLLIPOP_MR1)
  public void setTimeZone_abbreviateTimezoneId_accept() {
    alarmManager.setTimeZone("PST");
    assertThat(TimeZone.getDefault().getID()).isEqualTo("PST");
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void setTimeZone_invalidTimeZone_ignore() {
    assertThrows(IllegalArgumentException.class, () -> alarmManager.setTimeZone("-07:00"));
  }

  @Test
  @Config(maxSdk = VERSION_CODES.LOLLIPOP_MR1)
  public void setTimeZone_invalidTimeZone_fallbackToGMT() {
    alarmManager.setTimeZone("-07:00");
    assertThat(TimeZone.getDefault().getID()).isEqualTo("GMT");
  }

  @Test
  public void set_pendingIntent() {
    Runnable onFire = mock(Runnable.class);
    try (TestBroadcastListener listener = new TestBroadcastListener(onFire, "action").register()) {
      alarmManager.set(
          AlarmManager.ELAPSED_REALTIME,
          SystemClock.elapsedRealtime() + 10,
          listener.getPendingIntent());

      ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
      verify(onFire).run();
    }
  }

  @Config(minSdk = VERSION_CODES.N)
  @Test
  public void set_alarmListener() {
    OnAlarmListener onFire = mock(OnAlarmListener.class);
    alarmManager.set(
        AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10, "tag", onFire, null);

    ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
    assertThat(alarm).isNotNull();
    assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
    assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
    assertThat(alarm.getTag()).isEqualTo("tag");

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
    verify(onFire).onAlarm();
  }

  @Test
  public void setRepeating_pendingIntent() {
    Runnable onFire = mock(Runnable.class);
    try (TestBroadcastListener listener = new TestBroadcastListener(onFire, "action").register()) {
      alarmManager.setRepeating(
          AlarmManager.ELAPSED_REALTIME,
          SystemClock.elapsedRealtime() + 10,
          20L,
          listener.getPendingIntent());

      ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
      assertThat(alarm.getIntervalMs()).isEqualTo(20);

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
      verify(onFire, times(1)).run();

      alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 20);
      assertThat(alarm.getIntervalMs()).isEqualTo(20);

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(20));
      verify(onFire, times(2)).run();
    }

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(20));
    verify(onFire, times(2)).run();
  }

  @Config(minSdk = VERSION_CODES.KITKAT)
  @Test
  public void setWindow_pendingIntent() {
    Runnable onFire = mock(Runnable.class);
    try (TestBroadcastListener listener = new TestBroadcastListener(onFire, "action").register()) {
      alarmManager.setWindow(
          AlarmManager.ELAPSED_REALTIME,
          SystemClock.elapsedRealtime() + 10,
          20L,
          listener.getPendingIntent());

      ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
      assertThat(alarm.getWindowLengthMs()).isEqualTo(20);

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
      verify(onFire).run();
    }
  }

  @Config(minSdk = VERSION_CODES.N)
  @Test
  public void setWindow_alarmListener() {
    OnAlarmListener onFire = mock(OnAlarmListener.class);
    alarmManager.setWindow(
        AlarmManager.ELAPSED_REALTIME,
        SystemClock.elapsedRealtime() + 10,
        20L,
        "tag",
        onFire,
        null);

    ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
    assertThat(alarm).isNotNull();
    assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
    assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
    assertThat(alarm.getWindowLengthMs()).isEqualTo(20);
    assertThat(alarm.getTag()).isEqualTo("tag");

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
    verify(onFire).onAlarm();
  }

  @Config(minSdk = VERSION_CODES.S)
  @Test
  public void setPrioritized_alarmListener() {
    OnAlarmListener onFire = mock(OnAlarmListener.class);
    alarmManager.setPrioritized(
        AlarmManager.ELAPSED_REALTIME,
        SystemClock.elapsedRealtime() + 10,
        20L,
        "tag",
        Runnable::run,
        onFire);

    ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
    assertThat(alarm).isNotNull();
    assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
    assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
    assertThat(alarm.getWindowLengthMs()).isEqualTo(20);
    assertThat(alarm.getTag()).isEqualTo("tag");

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
    verify(onFire).onAlarm();
  }

  @Config(minSdk = VERSION_CODES.KITKAT)
  @Test
  public void setExact_pendingIntent() {
    Runnable onFire = mock(Runnable.class);
    try (TestBroadcastListener listener = new TestBroadcastListener(onFire, "action").register()) {
      alarmManager.setExact(
          AlarmManager.ELAPSED_REALTIME,
          SystemClock.elapsedRealtime() + 10,
          listener.getPendingIntent());

      ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
      verify(onFire).run();
    }
  }

  @Config(minSdk = VERSION_CODES.N)
  @Test
  public void setExact_alarmListener() {
    OnAlarmListener onFire = mock(OnAlarmListener.class);
    alarmManager.setExact(
        AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10, "tag", onFire, null);

    ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
    assertThat(alarm).isNotNull();
    assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
    assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
    assertThat(alarm.getTag()).isEqualTo("tag");

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
    verify(onFire).onAlarm();
  }

  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  @Test
  public void setAlarmClock_pendingIntent() {
    AlarmClockInfo alarmClockInfo =
        new AlarmClockInfo(
            SystemClock.elapsedRealtime() + 10,
            PendingIntent.getBroadcast(context, 0, new Intent("show"), 0));

    Runnable onFire = mock(Runnable.class);
    try (TestBroadcastListener listener = new TestBroadcastListener(onFire, "action").register()) {
      alarmManager.setAlarmClock(alarmClockInfo, listener.getPendingIntent());

      ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getType()).isEqualTo(AlarmManager.RTC_WAKEUP);
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
      assertThat(alarm.getAlarmClockInfo()).isEqualTo(alarmClockInfo);

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
      verify(onFire).run();
    }
  }

  @Config(minSdk = VERSION_CODES.KITKAT)
  @Test
  public void set_pendingIntent_workSource() {
    Runnable onFire = mock(Runnable.class);
    try (TestBroadcastListener listener = new TestBroadcastListener(onFire, "action").register()) {
      alarmManager.set(
          AlarmManager.ELAPSED_REALTIME,
          SystemClock.elapsedRealtime() + 10,
          20L,
          0L,
          listener.getPendingIntent(),
          new WorkSource());

      ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
      assertThat(alarm.getWindowLengthMs()).isEqualTo(20);
      assertThat(alarm.getIntervalMs()).isEqualTo(0);
      assertThat(alarm.getWorkSource()).isEqualTo(new WorkSource());

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
      verify(onFire).run();
    }
  }

  @Config(minSdk = VERSION_CODES.N)
  @Test
  public void set_alarmListener_workSource() {
    OnAlarmListener onFire = mock(OnAlarmListener.class);
    alarmManager.set(
        AlarmManager.ELAPSED_REALTIME,
        SystemClock.elapsedRealtime() + 10,
        20L,
        0L,
        "tag",
        onFire,
        null,
        new WorkSource());

    ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
    assertThat(alarm).isNotNull();
    assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
    assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
    assertThat(alarm.getWindowLengthMs()).isEqualTo(20);
    assertThat(alarm.getIntervalMs()).isEqualTo(0);
    assertThat(alarm.getTag()).isEqualTo("tag");
    assertThat(alarm.getWorkSource()).isEqualTo(new WorkSource());

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
    verify(onFire).onAlarm();
  }

  @Config(minSdk = VERSION_CODES.N)
  @Test
  public void set_alarmListener_workSource_noTag() {
    OnAlarmListener onFire = mock(OnAlarmListener.class);
    alarmManager.set(
        AlarmManager.ELAPSED_REALTIME,
        SystemClock.elapsedRealtime() + 10,
        20L,
        0L,
        onFire,
        null,
        new WorkSource());

    ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
    assertThat(alarm).isNotNull();
    assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
    assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
    assertThat(alarm.getWindowLengthMs()).isEqualTo(20);
    assertThat(alarm.getIntervalMs()).isEqualTo(0);
    assertThat(alarm.getWorkSource()).isEqualTo(new WorkSource());

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
    verify(onFire).onAlarm();
  }

  @Config(minSdk = VERSION_CODES.S)
  @Test
  public void setExact_alarmListener_workSource() {
    OnAlarmListener onFire = mock(OnAlarmListener.class);
    alarmManager.setExact(
        AlarmManager.ELAPSED_REALTIME,
        SystemClock.elapsedRealtime() + 10,
        "tag",
        Runnable::run,
        new WorkSource(),
        onFire);

    ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
    assertThat(alarm).isNotNull();
    assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
    assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
    assertThat(alarm.getTag()).isEqualTo("tag");
    assertThat(alarm.getWorkSource()).isEqualTo(new WorkSource());

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
    verify(onFire).onAlarm();
  }

  @Test
  public void setInexactRepeating_pendingIntent() {
    Runnable onFire = mock(Runnable.class);
    try (TestBroadcastListener listener = new TestBroadcastListener(onFire, "action").register()) {
      alarmManager.setInexactRepeating(
          AlarmManager.ELAPSED_REALTIME,
          SystemClock.elapsedRealtime() + 10,
          20L,
          listener.getPendingIntent());

      ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
      assertThat(alarm.getIntervalMs()).isEqualTo(20);

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
      verify(onFire, times(1)).run();

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(20));
      verify(onFire, times(2)).run();
    }

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(20));
    verify(onFire, times(2)).run();
  }

  @Config(minSdk = VERSION_CODES.M)
  @Test
  public void setAndAllowWhileIdle_pendingIntent() {
    Runnable onFire = mock(Runnable.class);
    try (TestBroadcastListener listener = new TestBroadcastListener(onFire, "action").register()) {
      alarmManager.setAndAllowWhileIdle(
          AlarmManager.ELAPSED_REALTIME,
          SystemClock.elapsedRealtime() + 10,
          listener.getPendingIntent());

      ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
      assertThat(alarm.isAllowWhileIdle()).isTrue();

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
      verify(onFire, times(1)).run();
    }
  }

  @Config(minSdk = VERSION_CODES.M)
  @Test
  public void setExactAndAllowWhileIdle_pendingIntent() {
    Runnable onFire = mock(Runnable.class);
    try (TestBroadcastListener listener = new TestBroadcastListener(onFire, "action").register()) {
      alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.ELAPSED_REALTIME,
          SystemClock.elapsedRealtime() + 10,
          listener.getPendingIntent());

      ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
      assertThat(alarm.isAllowWhileIdle()).isTrue();

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
      verify(onFire, times(1)).run();
    }
  }

  @Test
  public void cancel_pendingIntent() {
    Runnable onFire1 = mock(Runnable.class);
    Runnable onFire2 = mock(Runnable.class);
    try (TestBroadcastListener listener1 =
            new TestBroadcastListener(onFire1, "action1").register();
        TestBroadcastListener listener2 =
            new TestBroadcastListener(onFire2, "action2").register()) {
      alarmManager.set(
          AlarmManager.ELAPSED_REALTIME,
          SystemClock.elapsedRealtime() + 20,
          listener1.getPendingIntent());
      alarmManager.set(
          AlarmManager.ELAPSED_REALTIME,
          SystemClock.elapsedRealtime() + 10,
          listener2.getPendingIntent());

      assertThat(shadowOf(alarmManager).getScheduledAlarms()).hasSize(2);
      ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);

      alarmManager.cancel(listener2.getPendingIntent());

      assertThat(shadowOf(alarmManager).getScheduledAlarms()).hasSize(1);
      alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 20);

      alarmManager.cancel(listener1.getPendingIntent());

      assertThat(shadowOf(alarmManager).getScheduledAlarms()).isEmpty();
      assertThat(shadowOf(alarmManager).peekNextScheduledAlarm()).isNull();

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(20));
      verify(onFire1, never()).run();
      verify(onFire2, never()).run();
    }
  }

  @Config(minSdk = VERSION_CODES.N)
  @Test
  public void cancel_alarmListener() {
    OnAlarmListener onFire1 = mock(OnAlarmListener.class);
    OnAlarmListener onFire2 = mock(OnAlarmListener.class);
    alarmManager.set(
        AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 20, "tag", onFire1, null);
    alarmManager.set(
        AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10, "tag", onFire2, null);

    assertThat(shadowOf(alarmManager).getScheduledAlarms()).hasSize(2);
    ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
    assertThat(alarm).isNotNull();
    assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);

    alarmManager.cancel(onFire2);

    assertThat(shadowOf(alarmManager).getScheduledAlarms()).hasSize(1);
    alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
    assertThat(alarm).isNotNull();
    assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 20);

    alarmManager.cancel(onFire1);

    assertThat(shadowOf(alarmManager).getScheduledAlarms()).isEmpty();
    assertThat(shadowOf(alarmManager).peekNextScheduledAlarm()).isNull();

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(20));
    verify(onFire1, never()).onAlarm();
    verify(onFire2, never()).onAlarm();
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void canScheduleExactAlarms() {
    assertThat(alarmManager.canScheduleExactAlarms()).isFalse();

    ShadowAlarmManager.setCanScheduleExactAlarms(true);
    assertThat(alarmManager.canScheduleExactAlarms()).isTrue();

    ShadowAlarmManager.setCanScheduleExactAlarms(false);
    assertThat(alarmManager.canScheduleExactAlarms()).isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void getNextAlarmClockInfo() {
    AlarmClockInfo alarmClockInfo1 =
        new AlarmClockInfo(
            SystemClock.elapsedRealtime() + 10,
            PendingIntent.getBroadcast(context, 0, new Intent("show1"), 0));
    AlarmClockInfo alarmClockInfo2 =
        new AlarmClockInfo(
            SystemClock.elapsedRealtime() + 5,
            PendingIntent.getBroadcast(context, 0, new Intent("show2"), 0));

    alarmManager.setAlarmClock(
        alarmClockInfo1, PendingIntent.getBroadcast(context, 0, new Intent("fire1"), 0));
    alarmManager.setAlarmClock(
        alarmClockInfo2, PendingIntent.getBroadcast(context, 0, new Intent("fire2"), 0));
    assertThat(alarmManager.getNextAlarmClock()).isEqualTo(alarmClockInfo2);

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(5));
    assertThat(alarmManager.getNextAlarmClock()).isEqualTo(alarmClockInfo1);

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(5));
    assertThat(alarmManager.getNextAlarmClock()).isNull();
  }

  @Test
  public void replace_pendingIntent() {
    Runnable onFire = mock(Runnable.class);
    try (TestBroadcastListener listener = new TestBroadcastListener(onFire, "action").register()) {
      alarmManager.set(
          AlarmManager.ELAPSED_REALTIME,
          SystemClock.elapsedRealtime() + 10,
          listener.getPendingIntent());
      alarmManager.set(
          AlarmManager.ELAPSED_REALTIME_WAKEUP,
          SystemClock.elapsedRealtime() + 20,
          listener.getPendingIntent());

      ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME_WAKEUP);
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 20);

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
      verify(onFire, never()).run();

      shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
      verify(onFire).run();
    }
  }

  @Config(minSdk = VERSION_CODES.N)
  @Test
  public void replace_alarmListener() {
    OnAlarmListener onFire = mock(OnAlarmListener.class);
    alarmManager.set(
        AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 10, "tag", onFire, null);
    alarmManager.set(
        AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() + 20,
        "tag1",
        onFire,
        null);

    ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
    assertThat(alarm).isNotNull();
    assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME_WAKEUP);
    assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 20);
    assertThat(alarm.getTag()).isEqualTo("tag1");

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
    verify(onFire, never()).onAlarm();

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));
    verify(onFire).onAlarm();
  }

  @Test
  public void pastTime() {
    Runnable onFire = mock(Runnable.class);
    try (TestBroadcastListener listener = new TestBroadcastListener(onFire, "action").register()) {
      alarmManager.set(
          AlarmManager.ELAPSED_REALTIME,
          SystemClock.elapsedRealtime() - 10,
          listener.getPendingIntent());

      ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
      assertThat(alarm).isNotNull();
      assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
      assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() - 10);

      shadowOf(Looper.getMainLooper()).idle();
      verify(onFire).run();

      assertThat(shadowOf(alarmManager).peekNextScheduledAlarm()).isNull();
    }
  }

  @Config(minSdk = VERSION_CODES.N)
  @Test
  public void reentrant() {
    AtomicReference<OnAlarmListener> listenerRef = new AtomicReference<>();
    listenerRef.set(
        () ->
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 10,
                "tag",
                listenerRef.get(),
                null));
    alarmManager.set(
        AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() + 10,
        "tag",
        listenerRef.get(),
        null);

    ScheduledAlarm alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
    assertThat(alarm).isNotNull();
    assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME_WAKEUP);
    assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
    assertThat(alarm.getTag()).isEqualTo("tag");

    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10));

    alarm = shadowOf(alarmManager).peekNextScheduledAlarm();
    assertThat(alarm).isNotNull();
    assertThat(alarm.getType()).isEqualTo(AlarmManager.ELAPSED_REALTIME);
    assertThat(alarm.getTriggerAtMs()).isEqualTo(SystemClock.elapsedRealtime() + 10);
    assertThat(alarm.getTag()).isEqualTo("tag");
  }

  private class TestBroadcastListener extends BroadcastReceiver implements AutoCloseable {

    private final Runnable alarm;
    private final String action;

    @Nullable private PendingIntent pendingIntent;

    TestBroadcastListener(Runnable alarm, String action) {
      this.alarm = alarm;
      this.action = action;
    }

    TestBroadcastListener register() {
      pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(action), 0);
      context.registerReceiver(this, new IntentFilter(action));
      return this;
    }

    PendingIntent getPendingIntent() {
      return Objects.requireNonNull(pendingIntent);
    }

    @Override
    public void close() {
      context.unregisterReceiver(this);
      if (pendingIntent != null) {
        pendingIntent.cancel();
      }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      if (Objects.equals(action, intent.getAction())) {
        alarm.run();
      }
    }
  }
}
