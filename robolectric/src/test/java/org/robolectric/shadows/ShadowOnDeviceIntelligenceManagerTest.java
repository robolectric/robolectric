package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.shadow.api.Shadow.extract;

import android.app.ondeviceintelligence.DownloadCallback;
import android.app.ondeviceintelligence.Feature;
import android.app.ondeviceintelligence.FeatureDetails;
import android.app.ondeviceintelligence.OnDeviceIntelligenceException;
import android.app.ondeviceintelligence.OnDeviceIntelligenceManager;
import android.app.ondeviceintelligence.ProcessingCallback;
import android.app.ondeviceintelligence.ProcessingSignal;
import android.app.ondeviceintelligence.StreamingProcessingCallback;
import android.app.ondeviceintelligence.TokenInfo;
import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.OutcomeReceiver;
import android.os.PersistableBundle;
import android.os.ServiceManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.LongConsumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

/** Test for {@link ShadowOnDeviceIntelligenceManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VANILLA_ICE_CREAM)
public class ShadowOnDeviceIntelligenceManagerTest {

  private Executor executor;

  @Before
  public void setUp() {
    executor = MoreExecutors.directExecutor();
  }

  @Test
  public void setVersion_getVersionReturnsSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    long expectedVersion = 12345L;
    shadowManager.setVersion(expectedVersion);
    LongConsumer mockConsumer = mock(LongConsumer.class);
    manager.getVersion(executor, mockConsumer);
    verify(mockConsumer).accept(expectedVersion);
  }

  @Test
  public void triggerGetVersion_callsConsumerWithSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    long expectedVersion = 12345L;
    shadowManager.setVersion(expectedVersion);
    LongConsumer mockConsumer = mock(LongConsumer.class);
    shadowManager.triggerGetVersion(executor, mockConsumer);
    verify(mockConsumer).accept(expectedVersion);
  }

  @Test
  public void setRemoteServicePackageName_getRemoteServicePackageNameReturnsSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    String expectedPackageName = "com.example.test";
    shadowManager.setRemoteServicePackageName(expectedPackageName);
    assertThat(manager.getRemoteServicePackageName()).isEqualTo(expectedPackageName);
  }

  @Test
  public void setFeature_getFeatureReturnsSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    Feature expectedFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    shadowManager.setFeature(expectedFeature);
    OutcomeReceiver<Feature, OnDeviceIntelligenceException> mockReceiver = mockOutcomeReceiver();
    manager.getFeature(123, executor, mockReceiver);
    verify(mockReceiver).onResult(expectedFeature);
  }

  @Test
  public void triggerGetFeature_callsReceiverWithSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    Feature expectedFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    shadowManager.setFeature(expectedFeature);
    OutcomeReceiver<Feature, OnDeviceIntelligenceException> mockReceiver = mockOutcomeReceiver();
    shadowManager.triggerGetFeature(123, executor, mockReceiver);
    verify(mockReceiver).onResult(expectedFeature);
  }

  @Test
  public void triggerGetFeature_callsReceiverWithError() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    OnDeviceIntelligenceException expectedException =
        new OnDeviceIntelligenceException(1, "Test error", new PersistableBundle());
    shadowManager.setGetFeatureError(expectedException);
    OutcomeReceiver<Feature, OnDeviceIntelligenceException> mockReceiver = mockOutcomeReceiver();
    shadowManager.triggerGetFeature(123, executor, mockReceiver);
    verify(mockReceiver).onError(expectedException);
  }

  @Test
  public void setFeatures_listFeaturesReturnsSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    ImmutableList<Feature> expectedFeatures =
        ImmutableList.of(ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE);
    shadowManager.setFeatures(expectedFeatures);
    OutcomeReceiver<List<Feature>, OnDeviceIntelligenceException> mockReceiver =
        mockOutcomeReceiver();
    manager.listFeatures(executor, mockReceiver);
    verify(mockReceiver).onResult(expectedFeatures);
  }

  @Test
  public void triggerListFeatures_callsReceiverWithSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    ImmutableList<Feature> expectedFeatures =
        ImmutableList.of(ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE);
    shadowManager.setFeatures(expectedFeatures);
    OutcomeReceiver<List<Feature>, OnDeviceIntelligenceException> mockReceiver =
        mockOutcomeReceiver();
    shadowManager.triggerListFeatures(executor, mockReceiver);
    verify(mockReceiver).onResult(expectedFeatures);
  }

  @Test
  public void triggerListFeatures_callsReceiverWithError() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    OnDeviceIntelligenceException expectedException =
        new OnDeviceIntelligenceException(1, "Test error", new PersistableBundle());
    shadowManager.setListFeaturesError(expectedException);
    OutcomeReceiver<List<Feature>, OnDeviceIntelligenceException> mockReceiver =
        mockOutcomeReceiver();
    shadowManager.triggerListFeatures(executor, mockReceiver);
    verify(mockReceiver).onError(expectedException);
  }

  @Test
  public void setFeatureDetails_getFeatureDetailsReturnsSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    FeatureDetails expectedFeatureDetails = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE_DETAILS;
    shadowManager.setFeatureDetails(expectedFeatureDetails);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    OutcomeReceiver<FeatureDetails, OnDeviceIntelligenceException> mockReceiver =
        mockOutcomeReceiver();
    manager.getFeatureDetails(mockFeature, executor, mockReceiver);
    verify(mockReceiver).onResult(expectedFeatureDetails);
  }

  @Test
  public void triggerGetFeatureDetails_callsReceiverWithSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    FeatureDetails expectedFeatureDetails = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE_DETAILS;
    shadowManager.setFeatureDetails(expectedFeatureDetails);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    OutcomeReceiver<FeatureDetails, OnDeviceIntelligenceException> mockReceiver =
        mockOutcomeReceiver();
    shadowManager.triggerGetFeatureDetails(mockFeature, executor, mockReceiver);
    verify(mockReceiver).onResult(expectedFeatureDetails);
  }

  @Test
  public void triggerGetFeatureDetails_callsReceiverWithError() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    OnDeviceIntelligenceException expectedException =
        new OnDeviceIntelligenceException(1, "Test error", new PersistableBundle());
    shadowManager.setGetFeatureDetailsError(expectedException);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    OutcomeReceiver<FeatureDetails, OnDeviceIntelligenceException> mockReceiver =
        mockOutcomeReceiver();
    shadowManager.triggerGetFeatureDetails(mockFeature, executor, mockReceiver);
    verify(mockReceiver).onError(expectedException);
  }

  @Test
  public void requestFeatureDownload_callsCallbackOnDownloadCompleted() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    DownloadCallback mockCallback = mock(DownloadCallback.class);
    CancellationSignal cancellationSignal = new CancellationSignal();
    manager.requestFeatureDownload(mockFeature, cancellationSignal, executor, mockCallback);
    verify(mockCallback).onDownloadCompleted(any());
  }

  @Test
  public void setTokenInfo_requestTokenInfoReturnsSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    TokenInfo expectedTokenInfo = ShadowOnDeviceIntelligenceManager.DUMMY_TOKEN_INFO;
    shadowManager.setTokenInfo(expectedTokenInfo);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    Bundle request = new Bundle();
    CancellationSignal cancellationSignal = new CancellationSignal();
    OutcomeReceiver<TokenInfo, OnDeviceIntelligenceException> mockReceiver = mockOutcomeReceiver();
    manager.requestTokenInfo(mockFeature, request, cancellationSignal, executor, mockReceiver);
    verify(mockReceiver).onResult(expectedTokenInfo);
  }

  @Test
  public void triggerRequestTokenInfo_callsReceiverWithSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    TokenInfo expectedTokenInfo = ShadowOnDeviceIntelligenceManager.DUMMY_TOKEN_INFO;
    shadowManager.setTokenInfo(expectedTokenInfo);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    Bundle request = new Bundle();
    CancellationSignal cancellationSignal = new CancellationSignal();
    OutcomeReceiver<TokenInfo, OnDeviceIntelligenceException> mockReceiver = mockOutcomeReceiver();
    shadowManager.triggerRequestTokenInfo(
        mockFeature, request, cancellationSignal, executor, mockReceiver);
    verify(mockReceiver).onResult(expectedTokenInfo);
  }

  @Test
  public void setProcessRequestResult_processRequestCallsCallbackWithSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    Bundle expectedBundle = new Bundle();
    expectedBundle.putString("testKey", "testValue");
    shadowManager.setProcessRequestResult(expectedBundle);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    Bundle request = new Bundle();
    ProcessingSignal processingSignal = new ProcessingSignal();
    CancellationSignal cancellationSignal = new CancellationSignal();
    ProcessingCallback mockCallback = mock(ProcessingCallback.class);
    manager.processRequest(
        mockFeature,
        request,
        OnDeviceIntelligenceManager.REQUEST_TYPE_INFERENCE,
        cancellationSignal,
        processingSignal,
        executor,
        mockCallback);
    verify(mockCallback).onResult(expectedBundle);
  }

  @Test
  public void triggerProcessRequest_callsCallbackWithSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    Bundle expectedBundle = new Bundle();
    expectedBundle.putString("testKey", "testValue");
    shadowManager.setProcessRequestResult(expectedBundle);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    Bundle request = new Bundle();
    ProcessingSignal processingSignal = new ProcessingSignal();
    CancellationSignal cancellationSignal = new CancellationSignal();
    ProcessingCallback mockCallback = mock(ProcessingCallback.class);
    shadowManager.triggerProcessRequest(
        mockFeature,
        request,
        OnDeviceIntelligenceManager.REQUEST_TYPE_INFERENCE,
        cancellationSignal,
        processingSignal,
        executor,
        mockCallback);
    verify(mockCallback).onResult(expectedBundle);
  }

  @Test
  public void triggerProcessRequest_callsCallbackWithError() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    OnDeviceIntelligenceException expectedException =
        new OnDeviceIntelligenceException(1, "Test error", new PersistableBundle());
    shadowManager.setProcessRequestError(expectedException);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    Bundle request = new Bundle();
    ProcessingSignal processingSignal = new ProcessingSignal();
    CancellationSignal cancellationSignal = new CancellationSignal();
    ProcessingCallback mockCallback = mock(ProcessingCallback.class);
    shadowManager.triggerProcessRequest(
        mockFeature,
        request,
        OnDeviceIntelligenceManager.REQUEST_TYPE_INFERENCE,
        cancellationSignal,
        processingSignal,
        executor,
        mockCallback);
    verify(mockCallback).onError(expectedException);
  }

  @Test
  public void setProcessRequestStreamingResult_processRequestStreamingCallsCallbackWithSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    Bundle expectedBundle = new Bundle();
    expectedBundle.putString("testKey", "testValue");
    shadowManager.setProcessRequestStreamingResult(expectedBundle);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    Bundle request = new Bundle();
    ProcessingSignal processingSignal = new ProcessingSignal();
    CancellationSignal cancellationSignal = new CancellationSignal();
    StreamingProcessingCallback mockCallback = mock(StreamingProcessingCallback.class);
    manager.processRequestStreaming(
        mockFeature,
        request,
        OnDeviceIntelligenceManager.REQUEST_TYPE_INFERENCE,
        cancellationSignal,
        processingSignal,
        executor,
        mockCallback);
    verify(mockCallback).onResult(expectedBundle);
  }

  @Test
  public void triggerProcessRequestStreaming_callsCallbackWithSetValue() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    Bundle expectedBundle = new Bundle();
    expectedBundle.putString("testKey", "testValue");
    shadowManager.setProcessRequestStreamingResult(expectedBundle);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    Bundle request = new Bundle();
    ProcessingSignal processingSignal = new ProcessingSignal();
    CancellationSignal cancellationSignal = new CancellationSignal();
    StreamingProcessingCallback mockCallback = mock(StreamingProcessingCallback.class);
    shadowManager.triggerProcessRequestStreaming(
        mockFeature,
        request,
        OnDeviceIntelligenceManager.REQUEST_TYPE_INFERENCE,
        cancellationSignal,
        processingSignal,
        executor,
        mockCallback);
    verify(mockCallback).onResult(expectedBundle);
  }

  @Test
  public void triggerProcessRequestStreaming_callsCallbackWithError() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    OnDeviceIntelligenceException expectedException =
        new OnDeviceIntelligenceException(1, "Test error", new PersistableBundle());
    shadowManager.setProcessRequestStreamingError(expectedException);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    Bundle request = new Bundle();
    ProcessingSignal processingSignal = new ProcessingSignal();
    CancellationSignal cancellationSignal = new CancellationSignal();
    StreamingProcessingCallback mockCallback = mock(StreamingProcessingCallback.class);
    shadowManager.triggerProcessRequestStreaming(
        mockFeature,
        request,
        OnDeviceIntelligenceManager.REQUEST_TYPE_INFERENCE,
        cancellationSignal,
        processingSignal,
        executor,
        mockCallback);
    verify(mockCallback).onError(expectedException);
  }

  @Test
  public void getVersion_callsConsumerWithDummyVersion() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    LongConsumer mockConsumer = mock(LongConsumer.class);
    manager.getVersion(executor, mockConsumer);
    verify(mockConsumer).accept(ShadowOnDeviceIntelligenceManager.DUMMY_VERSION);
  }

  @Test
  public void getRemoteServicePackageName_returnsDummyPackageName() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    assertThat(manager.getRemoteServicePackageName())
        .isEqualTo(ShadowOnDeviceIntelligenceManager.DUMMY_PACKAGE_NAME);
  }

  @Test
  public void getFeature_callsReceiverWithDummyFeature() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    shadowManager.setFeature(ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE);
    OutcomeReceiver<Feature, OnDeviceIntelligenceException> mockReceiver = mockOutcomeReceiver();
    manager.getFeature(123, executor, mockReceiver);
    verify(mockReceiver).onResult(ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE);
  }

  @Test
  public void listFeatures_callsReceiverWithListContainingDummyFeature() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    shadowManager.setFeatures(ImmutableList.of(ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE));
    OutcomeReceiver<List<Feature>, OnDeviceIntelligenceException> mockReceiver =
        mockOutcomeReceiver();
    manager.listFeatures(executor, mockReceiver);
    verify(mockReceiver)
        .onResult(ImmutableList.of(ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE));
  }

  @Test
  public void getFeatureDetails_callsReceiverWithDummyFeatureDetails() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    shadowManager.setFeatureDetails(ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE_DETAILS);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    OutcomeReceiver<FeatureDetails, OnDeviceIntelligenceException> mockReceiver =
        mockOutcomeReceiver();
    manager.getFeatureDetails(mockFeature, executor, mockReceiver);
    verify(mockReceiver).onResult(ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE_DETAILS);
  }

  @Test
  public void requestTokenInfo_callsReceiverWithDummyTokenInfo() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    shadowManager.setTokenInfo(ShadowOnDeviceIntelligenceManager.DUMMY_TOKEN_INFO);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    Bundle request = new Bundle();
    CancellationSignal cancellationSignal = new CancellationSignal();
    OutcomeReceiver<TokenInfo, OnDeviceIntelligenceException> mockReceiver = mockOutcomeReceiver();
    manager.requestTokenInfo(mockFeature, request, cancellationSignal, executor, mockReceiver);
    verify(mockReceiver).onResult(ShadowOnDeviceIntelligenceManager.DUMMY_TOKEN_INFO);
  }

  @Test
  public void processRequest_callsCallbackWithDummyBundle() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    shadowManager.setProcessRequestResult(ShadowOnDeviceIntelligenceManager.DUMMY_BUNDLE);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    Bundle request = new Bundle();
    ProcessingSignal processingSignal = new ProcessingSignal();
    CancellationSignal cancellationSignal = new CancellationSignal();
    ProcessingCallback mockCallback = mock(ProcessingCallback.class);
    manager.processRequest(
        mockFeature,
        request,
        OnDeviceIntelligenceManager.REQUEST_TYPE_INFERENCE,
        cancellationSignal,
        processingSignal,
        executor,
        mockCallback);
    verify(mockCallback).onResult(ShadowOnDeviceIntelligenceManager.DUMMY_BUNDLE);
  }

  @Test
  public void getSystemService_returnsManagerAndShadow() {
    Context context = ApplicationProvider.getApplicationContext();
    OnDeviceIntelligenceManager onDeviceIntelligenceManager =
        (OnDeviceIntelligenceManager)
            context.getSystemService(Context.ON_DEVICE_INTELLIGENCE_SERVICE);
    assertThat(onDeviceIntelligenceManager).isNotNull();

    ShadowOnDeviceIntelligenceManager shadowOnDeviceIntelligenceManager =
        extract(onDeviceIntelligenceManager);
    assertThat(shadowOnDeviceIntelligenceManager).isNotNull();
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void serviceManager_getService_returnsNotNull() {
    assertThat(ServiceManager.getService(Context.ON_DEVICE_INTELLIGENCE_SERVICE)).isNotNull();
  }

  @Test
  public void getVersion_autoTriggerFalse_stashesRequest() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    shadowManager.setAutoTriggerCallbacks(false);

    long expectedVersion = 12345L;
    shadowManager.setVersion(expectedVersion);
    LongConsumer mockConsumer = mock(LongConsumer.class);

    manager.getVersion(executor, mockConsumer);

    // Verify it was NOT triggered automatically
    verify(mockConsumer, Mockito.never()).accept(any(Long.class));

    // Verify it WAS stashed
    assertThat(shadowManager.getGetVersionRequests()).hasSize(1);
    ShadowOnDeviceIntelligenceManager.GetVersionRequest stashedRequest =
        shadowManager.getLastGetVersionRequest();
    assertThat(stashedRequest.executor).isEqualTo(executor);
    assertThat(stashedRequest.consumer).isEqualTo(mockConsumer);

    // Trigger manually
    shadowManager.triggerGetVersion(stashedRequest.executor, stashedRequest.consumer);
    verify(mockConsumer).accept(expectedVersion);
  }

  @Test
  public void triggerGetFeature_canClearError() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);

    OnDeviceIntelligenceException expectedException =
        new OnDeviceIntelligenceException(1, "Test error", new PersistableBundle());
    shadowManager.setGetFeatureError(expectedException);

    OutcomeReceiver<Feature, OnDeviceIntelligenceException> errorReceiver = mockOutcomeReceiver();
    shadowManager.triggerGetFeature(123, executor, errorReceiver);
    verify(errorReceiver).onError(expectedException);

    shadowManager.setGetFeatureError(null);

    OutcomeReceiver<Feature, OnDeviceIntelligenceException> successReceiver = mockOutcomeReceiver();
    shadowManager.triggerGetFeature(123, executor, successReceiver);
    verify(successReceiver).onResult(ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE);
  }

  @Test
  public void triggerListFeatures_canClearError() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);

    OnDeviceIntelligenceException expectedException =
        new OnDeviceIntelligenceException(1, "Test error", new PersistableBundle());
    shadowManager.setListFeaturesError(expectedException);

    OutcomeReceiver<List<Feature>, OnDeviceIntelligenceException> errorReceiver =
        mockOutcomeReceiver();
    shadowManager.triggerListFeatures(executor, errorReceiver);
    verify(errorReceiver).onError(expectedException);

    shadowManager.setListFeaturesError(null);

    OutcomeReceiver<List<Feature>, OnDeviceIntelligenceException> successReceiver =
        mockOutcomeReceiver();
    shadowManager.triggerListFeatures(executor, successReceiver);
    verify(successReceiver)
        .onResult(ImmutableList.of(ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE));
  }

  @Test
  public void triggerGetFeatureDetails_canClearError() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);

    OnDeviceIntelligenceException expectedException =
        new OnDeviceIntelligenceException(1, "Test error", new PersistableBundle());
    shadowManager.setGetFeatureDetailsError(expectedException);

    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    OutcomeReceiver<FeatureDetails, OnDeviceIntelligenceException> errorReceiver =
        mockOutcomeReceiver();
    shadowManager.triggerGetFeatureDetails(mockFeature, executor, errorReceiver);
    verify(errorReceiver).onError(expectedException);

    shadowManager.setGetFeatureDetailsError(null);

    OutcomeReceiver<FeatureDetails, OnDeviceIntelligenceException> successReceiver =
        mockOutcomeReceiver();
    shadowManager.triggerGetFeatureDetails(mockFeature, executor, successReceiver);
    verify(successReceiver).onResult(ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE_DETAILS);
  }

  @Test
  public void triggerProcessRequest_canClearError() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);

    OnDeviceIntelligenceException expectedException =
        new OnDeviceIntelligenceException(1, "Test error", new PersistableBundle());
    shadowManager.setProcessRequestError(expectedException);

    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    Bundle request = new Bundle();
    ProcessingSignal processingSignal = new ProcessingSignal();
    CancellationSignal cancellationSignal = new CancellationSignal();

    ProcessingCallback errorCallback = mock(ProcessingCallback.class);
    shadowManager.triggerProcessRequest(
        mockFeature,
        request,
        OnDeviceIntelligenceManager.REQUEST_TYPE_INFERENCE,
        cancellationSignal,
        processingSignal,
        executor,
        errorCallback);
    verify(errorCallback).onError(expectedException);

    shadowManager.setProcessRequestError(null);

    ProcessingCallback successCallback = mock(ProcessingCallback.class);
    shadowManager.triggerProcessRequest(
        mockFeature,
        request,
        OnDeviceIntelligenceManager.REQUEST_TYPE_INFERENCE,
        cancellationSignal,
        processingSignal,
        executor,
        successCallback);
    verify(successCallback).onResult(ShadowOnDeviceIntelligenceManager.DUMMY_BUNDLE);
  }

  @Test
  public void triggerProcessRequestStreaming_canClearError() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);

    OnDeviceIntelligenceException expectedException =
        new OnDeviceIntelligenceException(1, "Test error", new PersistableBundle());
    shadowManager.setProcessRequestStreamingError(expectedException);

    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    Bundle request = new Bundle();
    ProcessingSignal processingSignal = new ProcessingSignal();
    CancellationSignal cancellationSignal = new CancellationSignal();

    StreamingProcessingCallback errorCallback = mock(StreamingProcessingCallback.class);
    shadowManager.triggerProcessRequestStreaming(
        mockFeature,
        request,
        OnDeviceIntelligenceManager.REQUEST_TYPE_INFERENCE,
        cancellationSignal,
        processingSignal,
        executor,
        errorCallback);
    verify(errorCallback).onError(expectedException);

    shadowManager.setProcessRequestStreamingError(null);

    StreamingProcessingCallback successCallback = mock(StreamingProcessingCallback.class);
    shadowManager.triggerProcessRequestStreaming(
        mockFeature,
        request,
        OnDeviceIntelligenceManager.REQUEST_TYPE_INFERENCE,
        cancellationSignal,
        processingSignal,
        executor,
        successCallback);
    verify(successCallback).onResult(ShadowOnDeviceIntelligenceManager.DUMMY_BUNDLE);
  }

  @Test
  public void processRequestStreaming_callsCallbackWithDummyBundle() {
    OnDeviceIntelligenceManager manager =
        ApplicationProvider.getApplicationContext()
            .getSystemService(OnDeviceIntelligenceManager.class);
    ShadowOnDeviceIntelligenceManager shadowManager = extract(manager);
    shadowManager.setProcessRequestStreamingResult(ShadowOnDeviceIntelligenceManager.DUMMY_BUNDLE);
    Feature mockFeature = ShadowOnDeviceIntelligenceManager.DUMMY_FEATURE;
    Bundle request = new Bundle();
    ProcessingSignal processingSignal = new ProcessingSignal();
    CancellationSignal cancellationSignal = new CancellationSignal();
    StreamingProcessingCallback mockCallback = mock(StreamingProcessingCallback.class);
    manager.processRequestStreaming(
        mockFeature,
        request,
        OnDeviceIntelligenceManager.REQUEST_TYPE_INFERENCE,
        cancellationSignal,
        processingSignal,
        executor,
        mockCallback);
    verify(mockCallback).onResult(ShadowOnDeviceIntelligenceManager.DUMMY_BUNDLE);
  }

  @SuppressWarnings("unchecked") // Unable to mock generic type with multiple generic types.
  private <R, E extends Throwable> OutcomeReceiver<R, E> mockOutcomeReceiver() {
    return mock(OutcomeReceiver.class);
  }
}
