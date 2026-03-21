package org.robolectric.shadows;

import android.app.ondeviceintelligence.DownloadCallback;
import android.app.ondeviceintelligence.Feature;
import android.app.ondeviceintelligence.FeatureDetails;
import android.app.ondeviceintelligence.OnDeviceIntelligenceException;
import android.os.CancellationSignal;
import android.os.OutcomeReceiver;
import android.os.ParcelFileDescriptor;
import android.service.ondeviceintelligence.OnDeviceIntelligenceService;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/** A test implementation of {@link OnDeviceIntelligenceService}. */
class TestOnDeviceIntelligenceService extends OnDeviceIntelligenceService {
  @Override
  public void onInferenceServiceConnected() {}

  @Override
  public void onInferenceServiceDisconnected() {}

  @Override
  public void onGetReadOnlyFeatureFileDescriptorMap(
      Feature feature, Consumer<Map<String, ParcelFileDescriptor>> fileDescriptorMapConsumer) {}

  @Override
  public void onDownloadFeature(
      int callerUid,
      Feature feature,
      CancellationSignal cancellationSignal,
      DownloadCallback downloadCallback) {}

  @Override
  public void onGetFeatureDetails(
      int callerUid,
      Feature feature,
      OutcomeReceiver<FeatureDetails, OnDeviceIntelligenceException> featureDetailsCallback) {}

  @Override
  public void onGetFeature(
      int callerUid,
      int featureId,
      OutcomeReceiver<Feature, OnDeviceIntelligenceException> featureCallback) {}

  @Override
  public void onListFeatures(
      int callerUid,
      OutcomeReceiver<List<Feature>, OnDeviceIntelligenceException> listFeaturesCallback) {}

  @Override
  public void onGetVersion(LongConsumer versionConsumer) {}
}
