package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection;
import android.os.IBinder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
public class ShadowServiceTest {
  private MyService service ;
  private ShadowService shadow;
  private Notification.Builder notBuilder;

  private final ShadowNotificationManager nm = shadowOf((NotificationManager) RuntimeEnvironment.application
      .getSystemService(Context.NOTIFICATION_SERVICE));

  @Before
  public void setup() {
    service = Robolectric.setupService(MyService.class);
    shadow = shadowOf(service);
    notBuilder = new Notification.Builder(service)
        .setSmallIcon(1)
        .setContentTitle("Test")
        .setContentText("Hi there");
  }

  @Test
  public void shouldUnbindServiceAtShadowApplication() {
    ShadowApplication shadowApplication = shadowOf(RuntimeEnvironment.application);
    ServiceConnection conn = Shadow.newInstanceOf(MediaScannerConnection.class);
    service.bindService(new Intent("dummy"), conn, 0);
    assertThat(shadowApplication.getUnboundServiceConnections()).isEmpty();
    service.unbindService(conn);
    assertThat(shadowApplication.getUnboundServiceConnections()).hasSize(1);
  }

  @Test
  public void shouldUnbindServiceSuccessfully() {
    ServiceConnection conn = Shadow.newInstanceOf(MediaScannerConnection.class);
    service.unbindService(conn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldUnbindServiceWithExceptionWhenRequested() {
    ShadowApplication.getInstance().setUnbindServiceShouldThrowIllegalArgument(true);
    ServiceConnection conn = Shadow.newInstanceOf(MediaScannerConnection.class);
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

  public static class MyService extends Service {
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
