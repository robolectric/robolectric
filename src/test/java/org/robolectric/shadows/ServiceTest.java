package org.robolectric.shadows;

import android.app.Service;
import android.appwidget.AppWidgetProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection;
import android.os.IBinder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.robolectric.Robolectric.*;
import static org.fest.assertions.api.Assertions.*;

@RunWith(TestRunners.WithDefaults.class)
public class ServiceTest {
  private final MyService service = new MyService();
  private final ShadowService shadow = shadowOf(service);

  @Test(expected = IllegalStateException.class)
  public void shouldComplainIfServiceIsDestroyedWithRegisteredBroadcastReceivers() throws Exception {
    service.registerReceiver(new AppWidgetProvider(), new IntentFilter());
    service.onDestroy();
  }

  @Test
  public void shouldNotComplainIfServiceIsDestroyedWhileAnotherServiceHasRegisteredBroadcastReceivers() throws Exception {
    MyService service1 = new MyService();
    MyService service2 = new MyService();
    service2.registerReceiver(new AppWidgetProvider(), new IntentFilter());
    service1.onDestroy(); // should not throw exception
  }

  @Test
  public void shouldUnbindServiceSuccessfully() {
    ServiceConnection conn = Robolectric.newInstanceOf(MediaScannerConnection.class);
    service.unbindService(conn);
  }

  @Test (expected=IllegalArgumentException.class)
  public void shouldUnbindServiceWithExceptionWhenRequested() {
    shadow.setUnbindServiceShouldThrowIllegalArgument(true);
    ServiceConnection conn = newInstanceOf(MediaScannerConnection.class);
    service.unbindService(conn);
  }

  @Test
  public void stopForeground() {
    service.stopForeground(true);
    assertThat(shadow.isForegroundStopped()).isTrue();
    assertThat(shadow.getNotificationShouldRemoved()).isTrue();
  }

  @Test
  public void shouldStopSelf() {
    service.stopSelf();
    assertThat(shadow.isStoppedBySelf()).isTrue();
  }

  @Test
  public void shouldStopSelfWithId() {
    service.stopSelf(1);
    assertThat(shadow.isStoppedBySelf()).isTrue();
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
