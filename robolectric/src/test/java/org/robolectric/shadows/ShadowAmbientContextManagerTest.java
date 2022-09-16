package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.PendingIntent;
import android.app.ambientcontext.AmbientContextEvent;
import android.app.ambientcontext.AmbientContextEventRequest;
import android.app.ambientcontext.AmbientContextManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowAmbientContextManager}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.TIRAMISU)
public class ShadowAmbientContextManagerTest {
  private Context context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void default_shouldNotStoreAnyRequest() throws Exception {
    assertThat(
            ((ShadowAmbientContextManager)
                    Shadow.extract(context.getSystemService(AmbientContextManager.class)))
                .getLastRegisterObserverRequest())
        .isNull();
  }

  @Test
  public void registerObserver_shouldStoreLastRequest() throws Exception {
    AmbientContextManager ambientContextManager =
        context.getSystemService(AmbientContextManager.class);
    AmbientContextEventRequest request =
        new AmbientContextEventRequest.Builder()
            .addEventType(AmbientContextEvent.EVENT_COUGH)
            .addEventType(AmbientContextEvent.EVENT_SNORE)
            .build();
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(
            context, /* requestCode= */ 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);

    ambientContextManager.registerObserver(
        request,
        pendingIntent,
        MoreExecutors.directExecutor(),
        (Integer status) -> {
          /* Do nothing. */
        });

    assertThat(
            ((ShadowAmbientContextManager) Shadow.extract(ambientContextManager))
                .getLastRegisterObserverRequest())
        .isEqualTo(request);
  }

  @Test
  public void registerObserver_thenUnregister_shouldClearLastRequest() throws Exception {
    AmbientContextManager ambientContextManager =
        context.getSystemService(AmbientContextManager.class);
    AmbientContextEventRequest request =
        new AmbientContextEventRequest.Builder()
            .addEventType(AmbientContextEvent.EVENT_COUGH)
            .addEventType(AmbientContextEvent.EVENT_SNORE)
            .build();
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(
            context, /* requestCode= */ 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);
    ambientContextManager.registerObserver(
        request,
        pendingIntent,
        MoreExecutors.directExecutor(),
        (Integer status) -> {
          /* Do nothing. */
        });

    ambientContextManager.unregisterObserver();

    assertThat(
            ((ShadowAmbientContextManager) Shadow.extract(ambientContextManager))
                .getLastRegisterObserverRequest())
        .isNull();
  }

  @Test
  public void registerObserver_statusSetToSuccess_shouldNotifyConsumerWithStoredStatus()
      throws Exception {
    AmbientContextManager ambientContextManager =
        context.getSystemService(AmbientContextManager.class);
    ((ShadowAmbientContextManager) Shadow.extract(ambientContextManager))
        .setAmbientContextServiceStatus(AmbientContextManager.STATUS_SUCCESS);
    AmbientContextEventRequest request =
        new AmbientContextEventRequest.Builder()
            .addEventType(AmbientContextEvent.EVENT_COUGH)
            .addEventType(AmbientContextEvent.EVENT_SNORE)
            .build();
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(
            context, /* requestCode= */ 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);

    SettableFuture<Integer> statusFuture = SettableFuture.create();
    ambientContextManager.registerObserver(
        request, pendingIntent, MoreExecutors.directExecutor(), statusFuture::set);

    assertThat(statusFuture.get()).isEqualTo(AmbientContextManager.STATUS_SUCCESS);
  }

  @Test
  public void queryAmbientContextServiceStatus_statusSetToSuccess_shouldReturnSuccess()
      throws Exception {
    AmbientContextManager ambientContextManager =
        context.getSystemService(AmbientContextManager.class);
    ((ShadowAmbientContextManager) Shadow.extract(ambientContextManager))
        .setAmbientContextServiceStatus(AmbientContextManager.STATUS_SUCCESS);

    SettableFuture<Integer> statusFuture = SettableFuture.create();
    ambientContextManager.queryAmbientContextServiceStatus(
        ImmutableSet.of(AmbientContextEvent.EVENT_SNORE),
        MoreExecutors.directExecutor(),
        statusFuture::set);

    assertThat(statusFuture.get()).isEqualTo(AmbientContextManager.STATUS_SUCCESS);
  }

  @Test
  public void queryAmbientContextServiceStatus_statusSetToNotSupported_shouldReturnNotSupported()
      throws Exception {
    AmbientContextManager ambientContextManager =
        context.getSystemService(AmbientContextManager.class);
    ((ShadowAmbientContextManager) Shadow.extract(ambientContextManager))
        .setAmbientContextServiceStatus(AmbientContextManager.STATUS_NOT_SUPPORTED);

    SettableFuture<Integer> statusFuture = SettableFuture.create();
    ambientContextManager.queryAmbientContextServiceStatus(
        ImmutableSet.of(AmbientContextEvent.EVENT_SNORE),
        MoreExecutors.directExecutor(),
        statusFuture::set);

    assertThat(statusFuture.get()).isEqualTo(AmbientContextManager.STATUS_NOT_SUPPORTED);
  }

  @Test
  public void
      getLastRequestedEventCodesForConsentActivity_consentActivityNeverStarted_shouldReturnNull()
          throws Exception {
    Set<Integer> lastRequestedEventCodes =
        ((ShadowAmbientContextManager)
                Shadow.extract(context.getSystemService(AmbientContextManager.class)))
            .getLastRequestedEventCodesForConsentActivity();

    assertThat(lastRequestedEventCodes).isNull();
  }

  @Test
  public void
      getLastRequestedEventCodesForConsentActivity_consentActivityStarted_shouldReturnRequestedEventCodes()
          throws Exception {
    AmbientContextManager ambientContextManager =
        context.getSystemService(AmbientContextManager.class);
    ImmutableSet<Integer> requestedEventCodes =
        ImmutableSet.of(AmbientContextEvent.EVENT_SNORE, AmbientContextEvent.EVENT_COUGH);
    ambientContextManager.startConsentActivity(requestedEventCodes);

    Set<Integer> lastRequestedEventCodes =
        ((ShadowAmbientContextManager) Shadow.extract(ambientContextManager))
            .getLastRequestedEventCodesForConsentActivity();

    assertThat(lastRequestedEventCodes).containsExactlyElementsIn(requestedEventCodes);
  }
}
