package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection;
import android.os.IBinder;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowServiceTest {
  private MyService service ;
  private Notification.Builder notBuilder;

  NotificationManager nm2 =
      (NotificationManager)
          ApplicationProvider.getApplicationContext()
              .getSystemService(Context.NOTIFICATION_SERVICE);

  @Before
  public void setup() {
    service = Robolectric.setupService(MyService.class);
    notBuilder =
        new Notification.Builder(service)
            .setSmallIcon(1)
            .setContentTitle("Test")
            .setContentText("Hi there");
  }

  @Test
  public void shouldUnbindServiceAtShadowApplication() {
    Application application = ApplicationProvider.getApplicationContext();
    ServiceConnection conn = Shadow.newInstanceOf(MediaScannerConnection.class);
    service.bindService(new Intent("dummy").setPackage("dummy.package"), conn, 0);
    assertThat(shadowOf(application).getUnboundServiceConnections()).isEmpty();
    service.unbindService(conn);
    assertThat(shadowOf(application).getUnboundServiceConnections()).hasSize(1);
  }

  @Test
  public void shouldUnbindServiceSuccessfully() {
    ServiceConnection conn = Shadow.newInstanceOf(MediaScannerConnection.class);
    service.unbindService(conn);
  }

  @Test
  public void shouldUnbindServiceWithExceptionWhenRequested() {
    shadowOf(RuntimeEnvironment.getApplication()).setUnbindServiceShouldThrowIllegalArgument(true);
    ServiceConnection conn = Shadow.newInstanceOf(MediaScannerConnection.class);
    try {
      service.unbindService(conn);
      fail("Should throw");
    } catch (IllegalArgumentException e) {
      // Expected.
    }
  }

  @Test
  public void startForeground() {
    Notification n = notBuilder.build();
    service.startForeground(23, n);
    assertThat(shadowOf(service).getLastForegroundNotification()).isSameInstanceAs(n);
    assertThat(shadowOf(service).getLastForegroundNotificationId()).isEqualTo(23);
    assertThat(shadowOf(nm2).getNotification(23)).isSameInstanceAs(n);
    assertThat(n.flags & Notification.FLAG_FOREGROUND_SERVICE).isNotEqualTo(0);
  }

  @Test
  @Config(minSdk = Q)
  public void startForegroundWithForegroundServiceType() {
    Notification n = notBuilder.build();
    service.startForeground(23, n, 64);
    assertThat(shadowOf(service).getLastForegroundNotification()).isSameInstanceAs(n);
    assertThat(shadowOf(service).getLastForegroundNotificationId()).isEqualTo(23);
    assertThat(shadowOf(nm2).getNotification(23)).isSameInstanceAs(n);
    assertThat(n.flags & Notification.FLAG_FOREGROUND_SERVICE).isNotEqualTo(0);
    assertThat(service.getForegroundServiceType()).isEqualTo(64);
  }

  @Test
  public void stopForeground() {
    service.stopForeground(true);
    assertThat(shadowOf(service).isForegroundStopped()).isTrue();
    assertThat(shadowOf(service).getNotificationShouldRemoved()).isTrue();
  }

  @Test
  public void stopForegroundRemovesNotificationIfAsked() {
    service.startForeground(21, notBuilder.build());
    service.stopForeground(true);
    assertThat(shadowOf(nm2).getNotification(21)).isNull();
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
    assertThat(shadowOf(nm2).getNotification(21)).isSameInstanceAs(n);
  }

  @Test
  public void stopForegroundDoesntDetachNotificationUnlessAsked() {
    service.startForeground(21, notBuilder.build());
    service.stopForeground(false);
    assertThat(shadowOf(service).isLastForegroundNotificationAttached()).isTrue();
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
    assertThat(shadowOf(nm2).getNotification(21)).isNull();
  }

  /**
   * Since Nougat, it's been possible to detach the foreground notification from the service,
   * allowing it to remain after the service dies.
   */
  @Test
  @Config(minSdk = N)
  public void onDestroyDoesntRemoveDetachedNotification() {
    Notification n = notBuilder.build();
    service.startForeground(21, n);
    service.stopForeground(Service.STOP_FOREGROUND_DETACH);
    service.onDestroy();
    assertThat(shadowOf(nm2).getNotification(21)).isSameInstanceAs(n);
  }

  @Test
  public void shouldStopSelf() {
    service.stopSelf();
    assertThat(shadowOf(service).isStoppedBySelf()).isTrue();
  }

  @Test
  public void shouldStopSelfWithId() {
    service.stopSelf(1);
    assertThat(shadowOf(service).isStoppedBySelf()).isTrue();
    assertThat(shadowOf(service).getStopSelfId()).isEqualTo(1);
  }

  @Test
  public void shouldStopSelfResultWithId() {
    service.stopSelfResult(1);
    assertThat(shadowOf(service).isStoppedBySelf()).isTrue();
    assertThat(shadowOf(service).getStopSelfResultId()).isEqualTo(1);
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
