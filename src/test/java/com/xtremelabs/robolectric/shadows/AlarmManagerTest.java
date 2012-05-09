package com.xtremelabs.robolectric.shadows;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class AlarmManagerTest {

    private MyActivity activity;
    private AlarmManager alarmManager;
    private ShadowAlarmManager shadowAlarmManager;

    @Before
    public void setUp() throws Exception {
        activity = new MyActivity();
        alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        shadowAlarmManager = Robolectric.shadowOf(alarmManager);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldSupportSet() throws Exception {
        assertThat(shadowAlarmManager.getNextScheduledAlarm(), nullValue());
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, 0, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));

        ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
        assertThat(scheduledAlarm, notNullValue());
    }

    @Test
    public void shouldSupportSetRepeating() throws Exception {
        assertThat(shadowAlarmManager.getNextScheduledAlarm(), nullValue());
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, AlarmManager.INTERVAL_HOUR,
                                  PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));

        ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
        assertThat(scheduledAlarm, notNullValue());
    }
    @Test
    public void setShouldReplaceDuplicates() {
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, 0, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, 0, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
        assertEquals(1, shadowAlarmManager.getScheduledAlarms().size());
    }
    @Test
    public void setRepeatingShouldReplaceDuplicates() {
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, AlarmManager.INTERVAL_HOUR, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, AlarmManager.INTERVAL_HOUR, PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0));
        assertEquals(1, shadowAlarmManager.getScheduledAlarms().size());
    }

    @Test
    public void shouldSupportGetNextScheduledAlarm() throws Exception {
        assertThat(shadowAlarmManager.getNextScheduledAlarm(), nullValue());

        long now = new Date().getTime();
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, now, pendingIntent);

        ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
        assertThat(shadowAlarmManager.getNextScheduledAlarm(), nullValue());
        assertScheduledAlarm(now, pendingIntent, scheduledAlarm);
    }

    @Test
    public void shouldSupportGetNextScheduledAlarmForRepeatingAlarms() throws Exception {
        assertThat(shadowAlarmManager.getNextScheduledAlarm(), nullValue());

        long now = new Date().getTime();
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, now, AlarmManager.INTERVAL_HOUR, pendingIntent);

        ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
        assertThat(shadowAlarmManager.getNextScheduledAlarm(), nullValue());
        assertRepeatingScheduledAlarm(now, AlarmManager.INTERVAL_HOUR, pendingIntent, scheduledAlarm);
    }

    @Test
    public void shouldSupportPeekScheduledAlarm() throws Exception {
        assertThat(shadowAlarmManager.getNextScheduledAlarm(), nullValue());

        long now = new Date().getTime();
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, now, pendingIntent);

        ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.peekNextScheduledAlarm();
        assertThat(shadowAlarmManager.peekNextScheduledAlarm(), notNullValue());
        assertScheduledAlarm(now, pendingIntent, scheduledAlarm);
    }

    @Test
    public void cancel_removesMatchingPendingIntents() {
        Intent newIntent = new Intent(Robolectric.application.getApplicationContext(), String.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(Robolectric.application.getApplicationContext(), 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC, 1337, pendingIntent);

        Intent newIntent2 = new Intent(Robolectric.application.getApplicationContext(), Integer.class);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(Robolectric.application.getApplicationContext(), 0, newIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC, 1337, pendingIntent2);

        assertEquals(2, shadowAlarmManager.getScheduledAlarms().size());

        Intent newIntent3 = new Intent(Robolectric.application.getApplicationContext(), String.class);
        PendingIntent newPendingIntent = PendingIntent.getBroadcast(Robolectric.application.getApplicationContext(), 0, newIntent3, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(newPendingIntent);
        assertEquals(1, shadowAlarmManager.getScheduledAlarms().size());
    }

    @Test
    public void cancel_removesMatchingPendingIntentsWithActions() {
        Intent newIntent = new Intent("someAction");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(Robolectric.application.getApplicationContext(), 0, newIntent, 0);
        alarmManager.set(AlarmManager.RTC, 1337, pendingIntent);
        assertEquals(1, shadowAlarmManager.getScheduledAlarms().size());
        alarmManager.cancel(PendingIntent.getBroadcast(Robolectric.application, 0, new Intent("anotherAction"), 0));
        assertEquals(1, shadowAlarmManager.getScheduledAlarms().size());
        alarmManager.cancel(PendingIntent.getBroadcast(Robolectric.application, 0, new Intent("someAction"), 0));
        assertEquals(0, shadowAlarmManager.getScheduledAlarms().size());
    }

    private void assertScheduledAlarm(long now, PendingIntent pendingIntent,
                                      ShadowAlarmManager.ScheduledAlarm scheduledAlarm) {
	    assertRepeatingScheduledAlarm(now, 0L, pendingIntent, scheduledAlarm);
    }

    private void assertRepeatingScheduledAlarm(long now, long interval, PendingIntent pendingIntent,
                                      ShadowAlarmManager.ScheduledAlarm scheduledAlarm) {
        assertThat(scheduledAlarm, notNullValue());
        assertThat(scheduledAlarm.operation, notNullValue());
        assertThat(scheduledAlarm.operation, sameInstance(pendingIntent));
        assertThat(scheduledAlarm.type, equalTo(AlarmManager.ELAPSED_REALTIME));
        assertThat(scheduledAlarm.triggerAtTime, equalTo(now));
        assertThat(scheduledAlarm.interval, equalTo(interval));
    }

    private static class MyActivity extends Activity {
        @Override
        protected void onDestroy() {
            super.onDestroy();
        }
    }

}
