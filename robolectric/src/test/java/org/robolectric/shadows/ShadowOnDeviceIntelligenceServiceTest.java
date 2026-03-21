package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.shadow.api.Shadow.extract;

import android.app.ondeviceintelligence.OnDeviceIntelligenceException;
import android.os.Bundle;
import android.os.OutcomeReceiver;
import android.os.PersistableBundle;
import android.service.ondeviceintelligence.OnDeviceIntelligenceService;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowOnDeviceIntelligenceService.UpdateProcessingStateRequest;

/** Test for {@link ShadowOnDeviceIntelligenceService}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VANILLA_ICE_CREAM)
public class ShadowOnDeviceIntelligenceServiceTest {

  private Executor executor;

  @Before
  public void setUp() {
    executor = directExecutor();
  }

  @Test
  public void updateProcessingState_callsCallbackWithSetValue() {
    OnDeviceIntelligenceService service =
        Robolectric.buildService(TestOnDeviceIntelligenceService.class).create().get();
    ShadowOnDeviceIntelligenceService shadowService =
        (ShadowOnDeviceIntelligenceService) extract(service);

    PersistableBundle expectedResult = new PersistableBundle();
    expectedResult.putString("testKey", "testValue");
    shadowService.setUpdateProcessingStateResult(expectedResult);

    Bundle state = new Bundle();
    OutcomeReceiver<PersistableBundle, OnDeviceIntelligenceException> mockReceiver =
        mockOutcomeReceiver();

    service.updateProcessingState(state, executor, mockReceiver);

    verify(mockReceiver).onResult(expectedResult);
  }

  @Test
  public void triggerUpdateProcessingState_callsCallbackWithSetValue() {
    OnDeviceIntelligenceService service =
        Robolectric.buildService(TestOnDeviceIntelligenceService.class).create().get();
    ShadowOnDeviceIntelligenceService shadowService =
        (ShadowOnDeviceIntelligenceService) extract(service);

    PersistableBundle expectedResult = new PersistableBundle();
    expectedResult.putString("testKey", "testValue");
    shadowService.setUpdateProcessingStateResult(expectedResult);

    Bundle state = new Bundle();
    OutcomeReceiver<PersistableBundle, OnDeviceIntelligenceException> mockReceiver =
        mockOutcomeReceiver();

    shadowService.triggerUpdateProcessingState(state, executor, mockReceiver);

    verify(mockReceiver).onResult(expectedResult);
  }

  @Test
  public void triggerUpdateProcessingState_callsCallbackWithError() {
    OnDeviceIntelligenceService service =
        Robolectric.buildService(TestOnDeviceIntelligenceService.class).create().get();
    ShadowOnDeviceIntelligenceService shadowService =
        (ShadowOnDeviceIntelligenceService) extract(service);

    OnDeviceIntelligenceException expectedException =
        new OnDeviceIntelligenceException(1, "Test error", new PersistableBundle());
    shadowService.setUpdateProcessingStateError(expectedException);

    Bundle state = new Bundle();
    OutcomeReceiver<PersistableBundle, OnDeviceIntelligenceException> mockReceiver =
        mockOutcomeReceiver();

    shadowService.triggerUpdateProcessingState(state, executor, mockReceiver);

    verify(mockReceiver).onError(expectedException);
  }

  @Test
  public void updateProcessingState_autoTriggerFalse_stashesRequest() {
    OnDeviceIntelligenceService service =
        Robolectric.buildService(TestOnDeviceIntelligenceService.class).create().get();
    ShadowOnDeviceIntelligenceService shadowService =
        (ShadowOnDeviceIntelligenceService) extract(service);
    shadowService.setAutoTriggerCallbacks(false);

    PersistableBundle expectedResult = new PersistableBundle();
    shadowService.setUpdateProcessingStateResult(expectedResult);

    Bundle state = new Bundle();
    OutcomeReceiver<PersistableBundle, OnDeviceIntelligenceException> mockReceiver =
        mockOutcomeReceiver();

    service.updateProcessingState(state, executor, mockReceiver);

    assertThat(shadowService.getUpdateProcessingStateRequests()).hasSize(1);
    UpdateProcessingStateRequest request = shadowService.getLastUpdateProcessingStateRequest();
    assertThat(request.processingState).isEqualTo(state);
    assertThat(request.callbackExecutor).isEqualTo(executor);
    assertThat(request.statusReceiver).isEqualTo(mockReceiver);
  }

  @SuppressWarnings("unchecked") // Casting from mock() is safe.
  private <R, E extends Throwable> OutcomeReceiver<R, E> mockOutcomeReceiver() {
    return mock(OutcomeReceiver.class);
  }
}
