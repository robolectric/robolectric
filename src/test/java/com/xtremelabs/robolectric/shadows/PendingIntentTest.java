package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.TestIntentSender;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class PendingIntentTest {
    @Test
    public void shouldGetIntentSender() {
        Intent expectedIntent = new Intent();
        PendingIntent service = PendingIntent.getService(null, 0, expectedIntent, 0);

        IntentSender intentSender = service.getIntentSender();
        assertEquals(expectedIntent, ((TestIntentSender) intentSender).intent);
    }

    @Test
    public void getBroadcast__shouldCreateIntentForBroadcast() throws Exception {
        Intent intent = new Intent();
        Activity context = new Activity();
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 99, intent, 100);
        assertEquals(intent, shadowOf(broadcast).getSavedIntent());
        assertEquals(context, shadowOf(broadcast).getSavedContext());
    }

    @Test
    public void getActivity__shouldCreateIntentForBroadcast() throws Exception {
        Intent intent = new Intent();
        Activity context = new Activity();
        PendingIntent forActivity = PendingIntent.getActivity(context, 99, intent, 100);
        assertEquals(intent, shadowOf(forActivity).getSavedIntent());
        assertEquals(context, shadowOf(forActivity).getSavedContext());
    }
    
    @Test
    public void send__shouldFillInIntentData() throws Exception {
        Intent intent = new Intent();
        Activity context = new Activity();
        PendingIntent forActivity = PendingIntent.getActivity(context, 99, intent, 100);
        
        Activity otherContext = new Activity();
        Intent fillIntent = new Intent();
        fillIntent.putExtra("TEST", 23);
        forActivity.send(otherContext, 0, fillIntent);
        
        Intent i = shadowOf(otherContext).getNextStartedActivity();
        assertThat(i, notNullValue());
        assertThat(i, sameInstance(intent));
        assertThat(i.getIntExtra("TEST", -1), equalTo(23));
    }
}
