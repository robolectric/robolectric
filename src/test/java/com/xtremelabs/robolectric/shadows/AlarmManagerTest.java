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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
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
    public void shouldSupportPeekScheduledAlarm() throws Exception {
        assertThat(shadowAlarmManager.getNextScheduledAlarm(), nullValue());

        long now = new Date().getTime();
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()), 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, now, pendingIntent);

        ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.peekNextScheduledAlarm();
        assertThat(shadowAlarmManager.peekNextScheduledAlarm(), notNullValue());
        assertScheduledAlarm(now, pendingIntent, scheduledAlarm);
    }

    private void assertScheduledAlarm(long now, PendingIntent pendingIntent,
                                      ShadowAlarmManager.ScheduledAlarm scheduledAlarm) {
        assertThat(scheduledAlarm, notNullValue());
        assertThat(scheduledAlarm.operation, notNullValue());
        assertThat(scheduledAlarm.operation, sameInstance(pendingIntent));
        assertThat(scheduledAlarm.type, equalTo(AlarmManager.ELAPSED_REALTIME));
        assertThat(scheduledAlarm.triggerAtTime, equalTo(now));
        assertThat(scheduledAlarm.interval, equalTo(0L));
    }

    private static class MyActivity extends Activity {
        @Override protected void onDestroy() {
            super.onDestroy();
        }
    }

}
