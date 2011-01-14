package com.xtremelabs.robolectric.shadows;

import android.app.Service;
import android.appwidget.AppWidgetProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WithTestDefaultsRunner.class)
public class ServiceTest {

    @Test(expected = IllegalStateException.class)
    public void shouldComplainIfServiceIsDestroyedWithRegisteredBroadcastReceivers() throws Exception {
        MyService service = new MyService();
        service.registerReceiver(new AppWidgetProvider(), new IntentFilter());
        service.onDestroy();
    }

    @Test
    public void shouldNotComplainIfServiceIsDestroyedWhileAnotherServiceHasRegisteredBroadcastReceivers() throws Exception {
        MyService service = new MyService();

        MyService service2 = new MyService();
        service2.registerReceiver(new AppWidgetProvider(), new IntentFilter());

        service.onDestroy(); // should not throw exception
    }

    private static class MyService extends Service {
        @Override public void onDestroy() {
            super.onDestroy();
        }

        @Override public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
