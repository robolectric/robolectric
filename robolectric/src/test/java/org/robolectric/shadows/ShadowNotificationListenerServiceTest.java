package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

/** Test for ShadowNotificationListenerService. */
@Config(minSdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(AndroidJUnit4.class)
public final class ShadowNotificationListenerServiceTest {

  /** NotificationListenerService needs to be extended to function. */
  private static final class TestNotificationListenerService extends NotificationListenerService {
    TestNotificationListenerService() {}
  }

  private static final String DEFAULT_PACKAGE = "com.google.android.apps.example";
  private static final int DEFAULT_ID = 0;

  private final Context context = ApplicationProvider.getApplicationContext();

  private NotificationListenerService service;
  private ShadowNotificationListenerService shadowService;

  @Before
  public void setUp() {
    service = Robolectric.buildService(TestNotificationListenerService.class).get();
    shadowService = shadowOf(service);
    ShadowNotificationListenerService.reset();
  }

  @Test
  public void getActiveNotification_noActiveNotification_returnsEmptyResult() {
    StatusBarNotification[] activeNotifications = service.getActiveNotifications();

    assertThat(activeNotifications).isEmpty();
  }

  @Test
  public void getActiveNotification_oneActiveNotification_returnsOneNotification() {
    Notification dummyNotification = createDummyNotification();
    shadowService.addActiveNotification(DEFAULT_PACKAGE, DEFAULT_ID, dummyNotification);

    StatusBarNotification[] activeNotifications = service.getActiveNotifications();

    assertThat(activeNotifications).hasLength(1);
    assertThat(activeNotifications[0].getPackageName()).isEqualTo(DEFAULT_PACKAGE);
    assertThat(activeNotifications[0].getNotification()).isEqualTo(dummyNotification);
  }

  @Test
  public void getActiveNotification_multipleActiveNotifications_returnsAllNotifications() {
    ImmutableList<Notification> dummyNotifications =
        ImmutableList.of(
            createDummyNotification(), createDummyNotification(), createDummyNotification());
    dummyNotifications.stream()
        .forEach(
            notification ->
                shadowService.addActiveNotification(DEFAULT_PACKAGE, DEFAULT_ID, notification));

    StatusBarNotification[] activeNotifications = service.getActiveNotifications();

    assertThat(activeNotifications).hasLength(dummyNotifications.size());
    for (int i = 0; i < dummyNotifications.size(); i++) {
      assertThat(activeNotifications[i].getPackageName()).isEqualTo(DEFAULT_PACKAGE);
      assertThat(activeNotifications[i].getNotification()).isEqualTo(dummyNotifications.get(i));
    }
  }

  @Test
  public void
      getActiveNotifications_activeNotificationsFromMultiplePackages_returnsAllNotifications() {
    Notification dummyNotification1 = createDummyNotification();
    Notification dummyNotification2 = createDummyNotification();
    String package1 = "com.google.android.app.first";
    String package2 = "com.google.android.app.second";
    shadowService.addActiveNotification(package1, DEFAULT_ID, dummyNotification1);
    shadowService.addActiveNotification(package2, DEFAULT_ID, dummyNotification2);

    StatusBarNotification[] activeNotifications = service.getActiveNotifications();

    assertThat(activeNotifications).hasLength(2);
    assertThat(activeNotifications[0].getPackageName()).isEqualTo(package1);
    assertThat(activeNotifications[0].getNotification()).isEqualTo(dummyNotification1);
    assertThat(activeNotifications[1].getPackageName()).isEqualTo(package2);
    assertThat(activeNotifications[1].getNotification()).isEqualTo(dummyNotification2);
  }

  @Test
  public void getActiveNotifications_statusBarNotificationObjects_returnsAllNotifications() {
    StatusBarNotification sbn1 = mock(StatusBarNotification.class);
    StatusBarNotification sbn2 = mock(StatusBarNotification.class);
    shadowService.addActiveNotification(sbn1);
    shadowService.addActiveNotification(sbn2);

    StatusBarNotification[] activeNotifications = service.getActiveNotifications();

    assertThat(activeNotifications).hasLength(2);
    assertThat(activeNotifications[0]).isSameInstanceAs(sbn1);
    assertThat(activeNotifications[1]).isSameInstanceAs(sbn2);
  }

  @Test
  public void getActiveNotification_filterByKeys_returnsAllMatchedNotifications() {
    ImmutableList<Notification> dummyNotifications =
        ImmutableList.of(
            createDummyNotification(),
            createDummyNotification(),
            createDummyNotification(),
            createDummyNotification());
    String[] keys = new String[dummyNotifications.size()];
    for (int i = 0; i < dummyNotifications.size(); i++) {
      keys[i] = shadowService.addActiveNotification(DEFAULT_PACKAGE, i, dummyNotifications.get(i));
    }

    StatusBarNotification[] activeNotifications =
        service.getActiveNotifications(new String[] {keys[0], keys[2]});

    assertThat(activeNotifications).hasLength(2);
    assertThat(activeNotifications[0].getNotification()).isEqualTo(dummyNotifications.get(0));
    assertThat(activeNotifications[1].getNotification()).isEqualTo(dummyNotifications.get(2));
  }

  @Test
  public void cancelNotification_keyFound_removesActiveNotification() {
    String key =
        shadowService.addActiveNotification(DEFAULT_PACKAGE, DEFAULT_ID, createDummyNotification());

    service.cancelNotification(key);

    assertThat(service.getActiveNotifications()).isEmpty();
  }

  @Test
  public void cancelNotification_keyNotFound_noOp() {
    shadowService.addActiveNotification(DEFAULT_PACKAGE, DEFAULT_ID, createDummyNotification());

    service.cancelNotification("made_up_key");

    assertThat(service.getActiveNotifications()).hasLength(1);
  }

  @Test
  public void cancelAllNotifications_removesAllActiveNotifications() {
    ImmutableList<Notification> dummyNotifications =
        ImmutableList.of(
            createDummyNotification(), createDummyNotification(), createDummyNotification());
    dummyNotifications.stream()
        .forEach(
            notification ->
                shadowService.addActiveNotification(DEFAULT_PACKAGE, DEFAULT_ID, notification));

    service.cancelAllNotifications();

    assertThat(service.getActiveNotifications()).isEmpty();
  }

  @Test
  public void requestInterruptionFilter_updatesInterruptionFilter() {
    service.requestInterruptionFilter(NotificationListenerService.INTERRUPTION_FILTER_ALARMS);

    assertThat(service.getCurrentInterruptionFilter())
        .isEqualTo(NotificationListenerService.INTERRUPTION_FILTER_ALARMS);
  }

  @Test
  public void requestListenerHints_updatesListenerHints() {
    service.requestListenerHints(NotificationListenerService.HINT_HOST_DISABLE_CALL_EFFECTS);

    assertThat(service.getCurrentListenerHints())
        .isEqualTo(NotificationListenerService.HINT_HOST_DISABLE_CALL_EFFECTS);
  }

  @Test
  @Config(minSdk = VERSION_CODES.N)
  public void requestRebind_incrementsCounter() {
    TestNotificationListenerService.requestRebind(
        ComponentName.createRelative(DEFAULT_PACKAGE, "Test"));
    TestNotificationListenerService.requestRebind(
        ComponentName.createRelative(DEFAULT_PACKAGE, "Test"));

    assertThat(ShadowNotificationListenerService.getRebindRequestCount()).isEqualTo(2);
  }

  @Test
  @Config(minSdk = VERSION_CODES.N)
  public void requestUnbind_incrementsCounter() {
    service.requestUnbind();
    service.requestUnbind();

    assertThat(shadowService.getUnbindRequestCount()).isEqualTo(2);
  }

  private Notification createDummyNotification() {
    return new Notification.Builder(context).build();
  }
}
