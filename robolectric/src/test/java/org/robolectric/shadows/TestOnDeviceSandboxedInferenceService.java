package org.robolectric.shadows;

import android.app.ondeviceintelligence.Feature;
import android.app.ondeviceintelligence.OnDeviceIntelligenceException;
import android.app.ondeviceintelligence.ProcessingCallback;
import android.app.ondeviceintelligence.ProcessingSignal;
import android.app.ondeviceintelligence.StreamingProcessingCallback;
import android.app.ondeviceintelligence.TokenInfo;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.OutcomeReceiver;
import android.os.PersistableBundle;
import android.service.ondeviceintelligence.OnDeviceSandboxedInferenceService;

/** A test implementation of {@link OnDeviceSandboxedInferenceService}. */
class TestOnDeviceSandboxedInferenceService extends OnDeviceSandboxedInferenceService {
  @Override
  public void onTokenInfoRequest(
      int callerUid,
      Feature feature,
      Bundle request,
      CancellationSignal cancellationSignal,
      OutcomeReceiver<TokenInfo, OnDeviceIntelligenceException> callback) {}

  @Override
  public void onProcessRequestStreaming(
      int callerUid,
      Feature feature,
      Bundle request,
      int requestType,
      CancellationSignal cancellationSignal,
      ProcessingSignal processingSignal,
      StreamingProcessingCallback callback) {}

  @Override
  public void onProcessRequest(
      int callerUid,
      Feature feature,
      Bundle request,
      int requestType,
      CancellationSignal cancellationSignal,
      ProcessingSignal processingSignal,
      ProcessingCallback callback) {}

  @Override
  public void onUpdateProcessingState(
      Bundle processingState,
      OutcomeReceiver<PersistableBundle, OnDeviceIntelligenceException> callback) {}
}
