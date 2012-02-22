package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Service;
import android.appwidget.AppWidgetProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection;
import android.os.IBinder;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

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
    
    @Test
    public void shouldUnbindServiceSuccessfully() {
        MyService service = new MyService();
        ServiceConnection conn = Robolectric.newInstanceOf(MediaScannerConnection.class);
        service.unbindService(conn);
    }

    @Test (expected=IllegalArgumentException.class)
    public void shouldUnbindServiceWithExceptionWhenRequested() {
        MyService service = new MyService();
        shadowOf(service).setUnbindServiceShouldThrowIllegalArgument(true);
        
        ServiceConnection conn = newInstanceOf(MediaScannerConnection.class);
        service.unbindService(conn);
    }
    
    @Test
    public void stopForeground() {
        MyService service = new MyService();
        service.stopForeground(true);
        
        ShadowService shadowService = shadowOf(service);
        assertThat(shadowService.isForegroundStopped(), is(true));
        assertThat(shadowService.getNotificationShouldRemoved(), is(true));
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
