package org.robolectric.shadows;

import static android.app.AlarmManager.INTERVAL_HOUR;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowAlarmManagerTest {

  private Context context;
  private Activity activity;
  private AlarmManager alarmManager;
  private ShadowAlarmManager shadowAlarmManager;

  @Before
  public void setUp() {
    context = RuntimeEnvironment.application;
    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    shadowAlarmManager = shadowOf(alarmManager);
    activity = Robolectric.setupActivity(Activity.class);
  }

  @Test
  public void set_shouldRegisterAlarm() {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, 0,
        PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNotNull();
  }

  @Test
  @Config(minSdk = M)
  public void setAndAllowWhileIdle_shouldRegisterAlarm() {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
    alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, 0,
        PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNotNull();
  }

  @Test
  @Config(minSdk = M)
  public void setExactAndAllowWhileIdle_shouldRegisterAlarm() {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, 0,
        PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNotNull();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void setExact_shouldRegisterAlarm_forApi19() {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME, 0,
        PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNotNull();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void setWindow_shouldRegisterAlarm_forApi19() {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
    alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME, 0, 1,
        PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNotNull();
  }

  @Test
  public void setRepeating_shouldRegisterAlarm() {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, INTERVAL_HOUR,
        PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNotNull();
  }

  @Test
  public void set_shouldReplaceAlarmsWithSameIntentReceiver() {
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, 500,
        PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, 1000,
        PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(1);
  }

  @Test
  public void set_shouldReplaceDuplicates() {
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, 0,
        PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, 0,
        PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(1);
  }

  @Test
  public void setRepeating_shouldReplaceDuplicates() {
    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, INTERVAL_HOUR,
        PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, INTERVAL_HOUR,
        PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(1);
  }

  @Test
  public void shouldSupportGetNextScheduledAlarm() {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();

    long now = new Date().getTime();
    Intent intent = new Intent(activity, activity.getClass());
    PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, now, pendingIntent);

    ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
    assertScheduledAlarm(now, pendingIntent, scheduledAlarm);
  }

  @Test
  public void getNextScheduledAlarm_shouldReturnRepeatingAlarms() {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();

    long now = new Date().getTime();
    Intent intent = new Intent(activity, activity.getClass());
    PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, now, INTERVAL_HOUR, pendingIntent);

    ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
    assertRepeatingScheduledAlarm(now, INTERVAL_HOUR, pendingIntent, scheduledAlarm);
  }

  @Test
  public void peekNextScheduledAlarm_shouldReturnNextAlarm() {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();

    long now = new Date().getTime();
    Intent intent = new Intent(activity, activity.getClass());
    PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, now, pendingIntent);

    ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.peekNextScheduledAlarm();
    assertThat(shadowAlarmManager.peekNextScheduledAlarm()).isNotNull();
    assertScheduledAlarm(now, pendingIntent, scheduledAlarm);
  }

  @Test
  public void cancel_removesMatchingPendingIntents() {
    Intent intent = new Intent(context, String.class);
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(context, 0, intent, FLAG_UPDATE_CURRENT);
    alarmManager.set(AlarmManager.RTC, 1337, pendingIntent);

    Intent intent2 = new Intent(context, Integer.class);
    PendingIntent pendingIntent2 =
        PendingIntent.getBroadcast(context, 0, intent2, FLAG_UPDATE_CURRENT);
    alarmManager.set(AlarmManager.RTC, 1337, pendingIntent2);

    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(2);

    Intent intent3 = new Intent(context, String.class);
    PendingIntent pendingIntent3 =
        PendingIntent.getBroadcast(context, 0, intent3, FLAG_UPDATE_CURRENT);
    alarmManager.cancel(pendingIntent3);

    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(1);
  }

  @Test
  public void cancel_removesMatchingPendingIntentsWithActions() {
    Intent newIntent = new Intent("someAction");
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, newIntent, 0);

    alarmManager.set(AlarmManager.RTC, 1337, pendingIntent);
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(1);

    alarmManager.cancel(PendingIntent.getBroadcast(context, 0, new Intent("anotherAction"), 0));
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(1);

    alarmManager.cancel(PendingIntent.getBroadcast(context, 0, new Intent("someAction"), 0));
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(0);
  }
  
    @Test
  public void schedule_useRequestCodeToMatchExistingPendingIntents() throws Exception {
    Intent intent = new Intent("ACTION!");
    PendingIntent pI = PendingIntent.getService(RuntimeEnvironment.application, 1, intent, 0);
    AlarmManager alarmManager =
        (AlarmManager) RuntimeEnvironment.application.getSystemService(Context.ALARM_SERVICE);
    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 10, pI);

    PendingIntent pI2 = PendingIntent.getService(RuntimeEnvironment.application, 2, intent, 0);
    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 10, pI2);

    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(2);
  }

  @Test
  public void cancel_useRequestCodeToMatchExistingPendingIntents() throws Exception {
    Intent intent = new Intent("ACTION!");
    PendingIntent pI = PendingIntent.getService(RuntimeEnvironment.application, 1, intent, 0);
    AlarmManager alarmManager =
        (AlarmManager) RuntimeEnvironment.application.getSystemService(Context.ALARM_SERVICE);
    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 10, pI);

    PendingIntent pI2 = PendingIntent.getService(RuntimeEnvironment.application, 2, intent, 0);
    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 10, pI2);

    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(2);

    alarmManager.cancel(pI);
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(1);
    assertThat(shadowAlarmManager.getNextScheduledAlarm().operation).isEqualTo(pI2);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getNextAlarmClockInfo() {
    assertThat(alarmManager.getNextAlarmClock()).isNull();
    assertThat(shadowAlarmManager.peekNextScheduledAlarm()).isNull();

    // Schedule an alarm.
    PendingIntent show = PendingIntent.getBroadcast(context, 0, new Intent("showAction"), 0);
    PendingIntent operation = PendingIntent.getBroadcast(context, 0, new Intent("opAction"), 0);
    AlarmClockInfo info = new AlarmClockInfo(1000, show);
    alarmManager.setAlarmClock(info, operation);

    AlarmClockInfo next = alarmManager.getNextAlarmClock();
    assertThat(next).isNotNull();
    assertThat(next.getTriggerTime()).isEqualTo(1000);
    assertThat(next.getShowIntent()).isSameAs(show);
    assertThat(shadowAlarmManager.peekNextScheduledAlarm().operation).isSameAs(operation);

    // Schedule another alarm sooner.
    PendingIntent show2 = PendingIntent.getBroadcast(context, 0, new Intent("showAction2"), 0);
    PendingIntent operation2 = PendingIntent.getBroadcast(context, 0, new Intent("opAction2"), 0);
    AlarmClockInfo info2 = new AlarmClockInfo(500, show2);
    alarmManager.setAlarmClock(info2, operation2);

    next = alarmManager.getNextAlarmClock();
    assertThat(next).isNotNull();
    assertThat(next.getTriggerTime()).isEqualTo(500);
    assertThat(next.getShowIntent()).isSameAs(show2);
    assertThat(shadowAlarmManager.peekNextScheduledAlarm().operation).isSameAs(operation2);

    // Remove the soonest alarm.
    alarmManager.cancel(operation2);

    next = alarmManager.getNextAlarmClock();
    assertThat(next).isNotNull();
    assertThat(next.getTriggerTime()).isEqualTo(1000);
    assertThat(next.getShowIntent()).isSameAs(show);
    assertThat(shadowAlarmManager.peekNextScheduledAlarm().operation).isSameAs(operation);

    // Remove the sole alarm.
    alarmManager.cancel(operation);

    assertThat(alarmManager.getNextAlarmClock()).isNull();
    assertThat(shadowAlarmManager.peekNextScheduledAlarm()).isNull();
  }

  private void assertScheduledAlarm(long now, PendingIntent pendingIntent,
      ShadowAlarmManager.ScheduledAlarm scheduledAlarm) {
    assertRepeatingScheduledAlarm(now, 0L, pendingIntent, scheduledAlarm);
  }

  private void assertRepeatingScheduledAlarm(long now, long interval, PendingIntent pendingIntent,
      ShadowAlarmManager.ScheduledAlarm scheduledAlarm) {
    assertThat(scheduledAlarm).isNotNull();
    assertThat(scheduledAlarm.operation).isNotNull();
    assertThat(scheduledAlarm.operation).isSameAs(pendingIntent);
    assertThat(scheduledAlarm.type).isEqualTo(AlarmManager.ELAPSED_REALTIME);
    assertThat(scheduledAlarm.triggerAtTime).isEqualTo(now);
    assertThat(scheduledAlarm.interval).isEqualTo(interval);
  }
}
