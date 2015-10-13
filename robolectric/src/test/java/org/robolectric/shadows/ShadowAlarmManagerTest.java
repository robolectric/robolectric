package org.robolectric.shadows;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowAlarmManagerTest {

  private final Activity activity = new Activity();
  private final AlarmManager alarmManager = (AlarmManager) RuntimeEnvironment.application.getSystemService(Context.ALARM_SERVICE);
  private final ShadowAlarmManager shadowAlarmManager = Shadows.shadowOf(alarmManager);

  @Test
  public void set_shouldRegisterAlarm() throws Exception {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, 0, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNotNull();
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.KITKAT)
  public void setExact_shouldRegisterAlarm_forApi19() throws Exception {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME, 0, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNotNull();
  }

  @Test
  public void setRepeating_shouldRegisterAlarm() throws Exception {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, AlarmManager.INTERVAL_HOUR, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNotNull();
  }
  
  @Test
  public void set_shouldReplaceAlarmsWithSameIntentReceiver() {
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, 500, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, 1000, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(1);
  }
  
  @Test
  public void set_shouldReplaceDuplicates() {
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, 0, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, 0, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(1);
  }

  @Test
  public void setRepeating_shouldReplaceDuplicates() {
    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, AlarmManager.INTERVAL_HOUR, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, AlarmManager.INTERVAL_HOUR, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(1);
  }

  @Test
  public void shouldSupportGetNextScheduledAlarm() throws Exception {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();

    long now = new Date().getTime();
    PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0);
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, now, pendingIntent);

    ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
    assertScheduledAlarm(now, pendingIntent, scheduledAlarm);
  }

  @Test
  public void getNextScheduledAlarm_shouldReturnRepeatingAlarms() throws Exception {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();

    long now = new Date().getTime();
    PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0);
    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, now, AlarmManager.INTERVAL_HOUR, pendingIntent);

    ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();
    assertRepeatingScheduledAlarm(now, AlarmManager.INTERVAL_HOUR, pendingIntent, scheduledAlarm);
  }

  @Test
  public void peekNextScheduledAlarm_shouldReturnNextAlarm() throws Exception {
    assertThat(shadowAlarmManager.getNextScheduledAlarm()).isNull();

    long now = new Date().getTime();
    PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0);
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, now, pendingIntent);

    ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.peekNextScheduledAlarm();
    assertThat(shadowAlarmManager.peekNextScheduledAlarm()).isNotNull();
    assertScheduledAlarm(now, pendingIntent, scheduledAlarm);
  }

  @Test
  public void cancel_removesMatchingPendingIntents() {
    Intent newIntent = new Intent(RuntimeEnvironment.application.getApplicationContext(), String.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(RuntimeEnvironment.application.getApplicationContext(), 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.set(AlarmManager.RTC, 1337, pendingIntent);

    Intent newIntent2 = new Intent(RuntimeEnvironment.application.getApplicationContext(), Integer.class);
    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(RuntimeEnvironment.application.getApplicationContext(), 0, newIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.set(AlarmManager.RTC, 1337, pendingIntent2);

    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(2);

    Intent newIntent3 = new Intent(RuntimeEnvironment.application.getApplicationContext(), String.class);
    PendingIntent newPendingIntent = PendingIntent.getBroadcast(RuntimeEnvironment.application.getApplicationContext(), 0, newIntent3, PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(newPendingIntent);
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(1);
  }

  @Test
  public void cancel_removesMatchingPendingIntentsWithActions() {
    Intent newIntent = new Intent("someAction");
    PendingIntent pendingIntent = PendingIntent.getBroadcast(RuntimeEnvironment.application.getApplicationContext(), 0, newIntent, 0);

    alarmManager.set(AlarmManager.RTC, 1337, pendingIntent);
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(1);

    alarmManager.cancel(PendingIntent.getBroadcast(RuntimeEnvironment.application, 0, new Intent("anotherAction"), 0));
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(1);

    alarmManager.cancel(PendingIntent.getBroadcast(RuntimeEnvironment.application, 0, new Intent("someAction"), 0));
    assertThat(shadowAlarmManager.getScheduledAlarms()).hasSize(0);
  }

  private void assertScheduledAlarm(long now, PendingIntent pendingIntent, ShadowAlarmManager.ScheduledAlarm scheduledAlarm) {
    assertRepeatingScheduledAlarm(now, 0L, pendingIntent, scheduledAlarm);
  }

  private void assertRepeatingScheduledAlarm(long now, long interval, PendingIntent pendingIntent, ShadowAlarmManager.ScheduledAlarm scheduledAlarm) {
    assertThat(scheduledAlarm).isNotNull();
    assertThat(scheduledAlarm.operation).isNotNull();
    assertThat(scheduledAlarm.operation).isSameAs(pendingIntent);
    assertThat(scheduledAlarm.type).isEqualTo(AlarmManager.ELAPSED_REALTIME);
    assertThat(scheduledAlarm.triggerAtTime).isEqualTo(now);
    assertThat(scheduledAlarm.interval).isEqualTo(interval);
  }
}
