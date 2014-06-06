package org.robolectric.shadows;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
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
  private final Notification.Builder notBuilder = new Notification.Builder(
      service).setSmallIcon(1).setContentTitle("Test")
      .setContentText("Hi there");
  private final ShadowNotificationManager nm = shadowOf((NotificationManager) Robolectric.application
      .getSystemService(Context.NOTIFICATION_SERVICE));

  @Test(expected = IllegalStateException.class)
  public void shouldComplainIfServiceIsDestroyedWithRegisteredBroadcastReceivers() throws Exception {
    service.registerReceiver(new AppWidgetProvider(), new IntentFilter());
    service.onDestroy();
  }

  @Test
  public void shouldNotComplainIfServiceIsDestroyedWhileAnotherServiceHasRegisteredBroadcastReceivers()
    throws Exception {

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

  @Test(expected = IllegalArgumentException.class)
  public void shouldUnbindServiceWithExceptionWhenRequested() {
    shadow.setUnbindServiceShouldThrowIllegalArgument(true);
    ServiceConnection conn = newInstanceOf(MediaScannerConnection.class);
    service.unbindService(conn);
  }

  @Test
  public void startForeground() {
    Notification n = notBuilder.build();
    service.startForeground(23, n);
    assertThat(shadow.getLastForegroundNotification()).isSameAs(n);
    assertThat(shadow.getLastForegroundNotificationId()).isEqualTo(23);
    assertThat(nm.getNotification(23)).isSameAs(n);
    assertThat(n.flags & Notification.FLAG_FOREGROUND_SERVICE).isNotZero();
  }

  @Test
  public void stopForeground() {
    service.stopForeground(true);
    assertThat(shadow.isForegroundStopped()).isTrue();
    assertThat(shadow.getNotificationShouldRemoved()).isTrue();
  }

  @Test
  public void stopForegroundRemovesNotificationIfAsked() {
    service.startForeground(21, notBuilder.build());
    service.stopForeground(true);
    assertThat(nm.getNotification(21)).isNull();
  }

  /**
   * According to spec, if the foreground notification is not removed earlier,
   * then it will be removed when the service is destroyed.
   */
  @Test
  public void stopForegroundDoesntRemoveNotificationUnlessAsked() {
    Notification n = notBuilder.build();
    service.startForeground(21, n);
    service.stopForeground(false);
    assertThat(nm.getNotification(21)).isSameAs(n);
  }

  /**
   * According to spec, if the foreground notification is not removed earlier,
   * then it will be removed when the service is destroyed.
   */
  @Test
  public void onDestroyRemovesNotification() {
    Notification n = notBuilder.build();
    service.startForeground(21, n);
    service.onDestroy();
    assertThat(nm.getNotification(21)).isNull();
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
    @Override
    public void onDestroy() {
      super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }
  }
}
