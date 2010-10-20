package com.xtremelabs.robolectric.fakes;

import android.app.Activity;
import android.app.Application;
import android.appwidget.AppWidgetProvider;
import android.content.IntentFilter;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DogfoodRobolectricTestRunner.class)
public class ActivityTest {
    @Before
    public void setUp() throws Exception {
        DogfoodRobolectricTestRunner.addGenericProxies();

        Robolectric.application = new Application();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldComplainIfActivityIsDestroyedWithRegisteredBroadcastReceivers() throws Exception {
        MyActivity activity = new MyActivity();
        activity.registerReceiver(new AppWidgetProvider(), new IntentFilter());
        activity.onDestroy();
    }

    @Test
    public void shouldNotComplainIfActivityIsDestroyedWhileAnotherActivityHasRegisteredBroadcastReceivers() throws Exception {
        MyActivity activity = new MyActivity();

        MyActivity activity2 = new MyActivity();
        activity2.registerReceiver(new AppWidgetProvider(), new IntentFilter());

        activity.onDestroy(); // should not throw exception
    }

    private static class MyActivity extends Activity {
        @Override protected void onDestroy() {
            super.onDestroy();
        }
    }
}
