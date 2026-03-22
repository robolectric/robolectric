package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;

import android.app.ondeviceintelligence.OnDeviceIntelligenceException;
import android.os.Bundle;
import android.os.OutcomeReceiver;
import android.os.PersistableBundle;
import android.service.ondeviceintelligence.OnDeviceIntelligenceService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import org.jspecify.annotations.NullMarked;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link OnDeviceIntelligenceService}. */
@NullMarked
@Implements(
    value = OnDeviceIntelligenceService.class,
    minSdk = VANILLA_ICE_CREAM,
    isInAndroidSdk = false)
@SuppressWarnings("NonFinalStaticField") // Need shadow members to be non-final to allow resetting.
public class ShadowOnDeviceIntelligenceService extends ShadowService {

  public static final PersistableBundle DUMMY_BUNDLE = new PersistableBundle();

  private static PersistableBundle updateProcessingStateResult = DUMMY_BUNDLE;
  @Nullable private static OnDeviceIntelligenceException updateProcessingStateError = null;

  private static boolean autoTriggerCallbacks = true;

  private static final List<UpdateProcessingStateRequest> updateProcessingStateRequests =
      new ArrayList<>();

  @Implementation
  protected void updateProcessingState(
      Bundle processingState,
      Executor callbackExecutor,
      OutcomeReceiver<PersistableBundle, OnDeviceIntelligenceException> statusReceiver) {
    if (autoTriggerCallbacks) {
      triggerUpdateProcessingState(processingState, callbackExecutor, statusReceiver);
    } else {
      updateProcessingStateRequests.add(
          new UpdateProcessingStateRequest(processingState, callbackExecutor, statusReceiver));
    }
  }

  public void setUpdateProcessingStateResult(PersistableBundle result) {
    ShadowOnDeviceIntelligenceService.updateProcessingStateResult = result;
  }

  public void setUpdateProcessingStateError(@Nullable OnDeviceIntelligenceException error) {
    ShadowOnDeviceIntelligenceService.updateProcessingStateError = error;
  }

  public void triggerUpdateProcessingState(
      Bundle processingState,
      Executor callbackExecutor,
      OutcomeReceiver<PersistableBundle, OnDeviceIntelligenceException> statusReceiver) {
    if (updateProcessingStateError != null) {
      callbackExecutor.execute(() -> statusReceiver.onError(updateProcessingStateError));
    } else {
      callbackExecutor.execute(() -> statusReceiver.onResult(updateProcessingStateResult));
    }
  }

  public void setAutoTriggerCallbacks(boolean autoTriggerCallbacks) {
    ShadowOnDeviceIntelligenceService.autoTriggerCallbacks = autoTriggerCallbacks;
  }

  public List<UpdateProcessingStateRequest> getUpdateProcessingStateRequests() {
    return ImmutableList.copyOf(updateProcessingStateRequests);
  }

  public UpdateProcessingStateRequest getLastUpdateProcessingStateRequest() {
    return Iterables.getLast(updateProcessingStateRequests);
  }

  @Resetter
  public static void reset() {
    updateProcessingStateRequests.clear();
    autoTriggerCallbacks = true;
    updateProcessingStateResult = DUMMY_BUNDLE;
    updateProcessingStateError = null;
  }

  /** Stashed request for {@link #updateProcessingState}. */
  public static final class UpdateProcessingStateRequest {
    public final Bundle processingState;
    public final Executor callbackExecutor;
    public final OutcomeReceiver<PersistableBundle, OnDeviceIntelligenceException> statusReceiver;

    public UpdateProcessingStateRequest(
        Bundle processingState,
        Executor callbackExecutor,
        OutcomeReceiver<PersistableBundle, OnDeviceIntelligenceException> statusReceiver) {
      this.processingState = processingState;
      this.callbackExecutor = callbackExecutor;
      this.statusReceiver = statusReceiver;
    }
  }
}
