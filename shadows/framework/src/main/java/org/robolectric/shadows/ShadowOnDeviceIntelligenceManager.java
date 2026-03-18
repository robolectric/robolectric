package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;

import android.app.ondeviceintelligence.DownloadCallback;
import android.app.ondeviceintelligence.Feature;
import android.app.ondeviceintelligence.FeatureDetails;
import android.app.ondeviceintelligence.OnDeviceIntelligenceException;
import android.app.ondeviceintelligence.OnDeviceIntelligenceManager;
import android.app.ondeviceintelligence.ProcessingCallback;
import android.app.ondeviceintelligence.ProcessingSignal;
import android.app.ondeviceintelligence.StreamingProcessingCallback;
import android.app.ondeviceintelligence.TokenInfo;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.OutcomeReceiver;
import android.os.PersistableBundle;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.LongConsumer;
import javax.annotation.Nullable;
import org.jspecify.annotations.NullMarked;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link OnDeviceIntelligenceManager}. */
@NullMarked
@Implements(
    value = OnDeviceIntelligenceManager.class,
    minSdk = VANILLA_ICE_CREAM,
    isInAndroidSdk = false)
@SuppressWarnings("NonFinalStaticField") // Need shadow members to be non-final to allow resetting.
public class ShadowOnDeviceIntelligenceManager {
  public static final long DUMMY_VERSION = 1L;
  public static final String DUMMY_PACKAGE_NAME = "dummy.package.name";
  public static final Feature DUMMY_FEATURE = new Feature.Builder(1).build();
  public static final FeatureDetails DUMMY_FEATURE_DETAILS =
      new FeatureDetails(0, new PersistableBundle());
  public static final TokenInfo DUMMY_TOKEN_INFO = new TokenInfo(0, new PersistableBundle());
  public static final Bundle DUMMY_BUNDLE = new Bundle();

  private static long version = DUMMY_VERSION;
  private static String remoteServicePackageName = DUMMY_PACKAGE_NAME;
  private static Feature feature = DUMMY_FEATURE;
  private static List<Feature> featureList = ImmutableList.of(DUMMY_FEATURE);
  private static FeatureDetails featureDetails = DUMMY_FEATURE_DETAILS;
  private static TokenInfo tokenInfo = DUMMY_TOKEN_INFO;

  @Nullable private static OnDeviceIntelligenceException getFeatureError;
  @Nullable private static OnDeviceIntelligenceException listFeaturesError;
  @Nullable private static OnDeviceIntelligenceException getFeatureDetailsError;
  @Nullable private static OnDeviceIntelligenceException processRequestError;
  @Nullable private static OnDeviceIntelligenceException processRequestStreamingError;
  private static Bundle bundle = DUMMY_BUNDLE;
  private static Bundle streamingBundle = DUMMY_BUNDLE;

  private static boolean autoTriggerCallbacks = true;
  private static final List<GetVersionRequest> getVersionRequests = new ArrayList<>();
  private static final List<GetFeatureRequest> getFeatureRequests = new ArrayList<>();
  private static final List<ListFeaturesRequest> listFeaturesRequests = new ArrayList<>();
  private static final List<GetFeatureDetailsRequest> getFeatureDetailsRequests = new ArrayList<>();
  private static final List<RequestFeatureDownloadRequest> requestFeatureDownloadRequests =
      new ArrayList<>();
  private static final List<RequestTokenInfoRequest> requestTokenInfoRequests = new ArrayList<>();
  private static final List<ProcessRequestRequest> processRequestRequests = new ArrayList<>();
  private static final List<ProcessRequestStreamingRequest> processRequestStreamingRequests =
      new ArrayList<>();

  @Implementation
  protected void getVersion(Executor callbackExecutor, LongConsumer versionConsumer) {
    if (autoTriggerCallbacks) {
      triggerGetVersion(callbackExecutor, versionConsumer);
    } else {
      getVersionRequests.add(new GetVersionRequest(callbackExecutor, versionConsumer));
    }
  }

  @Implementation
  @Nullable
  protected String getRemoteServicePackageName() {
    return remoteServicePackageName;
  }

  @Implementation
  protected void getFeature(
      int featureId,
      Executor callbackExecutor,
      OutcomeReceiver<Feature, OnDeviceIntelligenceException> featureReceiver) {
    if (autoTriggerCallbacks) {
      triggerGetFeature(featureId, callbackExecutor, featureReceiver);
    } else {
      getFeatureRequests.add(new GetFeatureRequest(featureId, callbackExecutor, featureReceiver));
    }
  }

  @Implementation
  protected void listFeatures(
      Executor callbackExecutor,
      OutcomeReceiver<List<Feature>, OnDeviceIntelligenceException> featureListReceiver) {
    if (autoTriggerCallbacks) {
      triggerListFeatures(callbackExecutor, featureListReceiver);
    } else {
      listFeaturesRequests.add(new ListFeaturesRequest(callbackExecutor, featureListReceiver));
    }
  }

  @Implementation
  protected void getFeatureDetails(
      Feature feature,
      Executor callbackExecutor,
      OutcomeReceiver<FeatureDetails, OnDeviceIntelligenceException> featureDetailsReceiver) {
    if (autoTriggerCallbacks) {
      triggerGetFeatureDetails(feature, callbackExecutor, featureDetailsReceiver);
    } else {
      getFeatureDetailsRequests.add(
          new GetFeatureDetailsRequest(feature, callbackExecutor, featureDetailsReceiver));
    }
  }

  @Implementation
  protected void requestFeatureDownload(
      Feature feature,
      @Nullable CancellationSignal cancellationSignal,
      Executor callbackExecutor,
      DownloadCallback callback) {
    if (autoTriggerCallbacks) {
      triggerRequestFeatureDownload(feature, cancellationSignal, callbackExecutor, callback);
    } else {
      requestFeatureDownloadRequests.add(
          new RequestFeatureDownloadRequest(
              feature, cancellationSignal, callbackExecutor, callback));
    }
  }

  @Implementation
  protected void requestTokenInfo(
      Feature feature,
      Bundle request,
      @Nullable CancellationSignal cancellationSignal,
      Executor callbackExecutor,
      OutcomeReceiver<TokenInfo, OnDeviceIntelligenceException> outcomeReceiver) {
    if (autoTriggerCallbacks) {
      triggerRequestTokenInfo(
          feature, request, cancellationSignal, callbackExecutor, outcomeReceiver);
    } else {
      requestTokenInfoRequests.add(
          new RequestTokenInfoRequest(
              feature, request, cancellationSignal, callbackExecutor, outcomeReceiver));
    }
  }

  @Implementation
  protected void processRequest(
      Feature feature,
      Bundle request,
      int requestType,
      @Nullable CancellationSignal cancellationSignal,
      @Nullable ProcessingSignal processingSignal,
      Executor callbackExecutor,
      ProcessingCallback processingCallback) {
    if (autoTriggerCallbacks) {
      triggerProcessRequest(
          feature,
          request,
          requestType,
          cancellationSignal,
          processingSignal,
          callbackExecutor,
          processingCallback);
    } else {
      processRequestRequests.add(
          new ProcessRequestRequest(
              feature,
              request,
              requestType,
              cancellationSignal,
              processingSignal,
              callbackExecutor,
              processingCallback));
    }
  }

  @Implementation
  protected void processRequestStreaming(
      Feature feature,
      Bundle request,
      int requestType,
      @Nullable CancellationSignal cancellationSignal,
      @Nullable ProcessingSignal processingSignal,
      Executor callbackExecutor,
      StreamingProcessingCallback streamingProcessingCallback) {
    if (autoTriggerCallbacks) {
      triggerProcessRequestStreaming(
          feature,
          request,
          requestType,
          cancellationSignal,
          processingSignal,
          callbackExecutor,
          streamingProcessingCallback);
    } else {
      processRequestStreamingRequests.add(
          new ProcessRequestStreamingRequest(
              feature,
              request,
              requestType,
              cancellationSignal,
              processingSignal,
              callbackExecutor,
              streamingProcessingCallback));
    }
  }

  /** Sets the value to be returned by {@link #getRemoteServicePackageName()}. */
  public void setRemoteServicePackageName(String remoteServicePackageName) {
    ShadowOnDeviceIntelligenceManager.remoteServicePackageName = remoteServicePackageName;
  }

  public void setVersion(long version) {
    ShadowOnDeviceIntelligenceManager.version = version;
  }

  public void triggerGetVersion(Executor executor, LongConsumer consumer) {
    executor.execute(() -> consumer.accept(version));
  }

  public void setFeature(Feature feature) {
    ShadowOnDeviceIntelligenceManager.feature = feature;
  }

  public void setGetFeatureError(@Nullable OnDeviceIntelligenceException exception) {
    ShadowOnDeviceIntelligenceManager.getFeatureError = exception;
  }

  public void triggerGetFeature(
      int featureId,
      Executor executor,
      OutcomeReceiver<Feature, OnDeviceIntelligenceException> receiver) {
    if (getFeatureError != null) {
      executor.execute(() -> receiver.onError(getFeatureError));
    } else {
      executor.execute(() -> receiver.onResult(feature));
    }
  }

  public void setFeatures(List<Feature> features) {
    ShadowOnDeviceIntelligenceManager.featureList = features;
  }

  public void setListFeaturesError(@Nullable OnDeviceIntelligenceException exception) {
    ShadowOnDeviceIntelligenceManager.listFeaturesError = exception;
  }

  public void triggerListFeatures(
      Executor executor, OutcomeReceiver<List<Feature>, OnDeviceIntelligenceException> receiver) {
    if (listFeaturesError != null) {
      executor.execute(() -> receiver.onError(listFeaturesError));
    } else {
      executor.execute(() -> receiver.onResult(featureList));
    }
  }

  public void setFeatureDetails(FeatureDetails featureDetails) {
    ShadowOnDeviceIntelligenceManager.featureDetails = featureDetails;
  }

  public void setGetFeatureDetailsError(@Nullable OnDeviceIntelligenceException exception) {
    ShadowOnDeviceIntelligenceManager.getFeatureDetailsError = exception;
  }

  public void triggerGetFeatureDetails(
      Feature feature,
      Executor executor,
      OutcomeReceiver<FeatureDetails, OnDeviceIntelligenceException> receiver) {
    if (getFeatureDetailsError != null) {
      executor.execute(() -> receiver.onError(getFeatureDetailsError));
    } else {
      executor.execute(() -> receiver.onResult(featureDetails));
    }
  }

  public void triggerRequestFeatureDownload(
      Feature feature,
      @Nullable CancellationSignal signal,
      Executor executor,
      DownloadCallback callback) {
    executor.execute(() -> callback.onDownloadCompleted(new PersistableBundle()));
  }

  public void setTokenInfo(TokenInfo tokenInfo) {
    ShadowOnDeviceIntelligenceManager.tokenInfo = tokenInfo;
  }

  public void triggerRequestTokenInfo(
      Feature feature,
      Bundle request,
      @Nullable CancellationSignal signal,
      Executor executor,
      OutcomeReceiver<TokenInfo, OnDeviceIntelligenceException> receiver) {
    executor.execute(() -> receiver.onResult(tokenInfo));
  }

  public void setProcessRequestResult(Bundle result) {
    ShadowOnDeviceIntelligenceManager.bundle = result;
  }

  public void setProcessRequestError(@Nullable OnDeviceIntelligenceException exception) {
    ShadowOnDeviceIntelligenceManager.processRequestError = exception;
  }

  public void triggerProcessRequest(
      Feature feature,
      Bundle request,
      int requestType,
      @Nullable CancellationSignal signal,
      @Nullable ProcessingSignal processingSignal,
      Executor executor,
      ProcessingCallback callback) {
    if (processRequestError != null) {
      executor.execute(() -> callback.onError(processRequestError));
    } else {
      executor.execute(() -> callback.onResult(bundle));
    }
  }

  public void setProcessRequestStreamingResult(Bundle result) {
    ShadowOnDeviceIntelligenceManager.streamingBundle = result;
  }

  public void setProcessRequestStreamingError(@Nullable OnDeviceIntelligenceException exception) {
    ShadowOnDeviceIntelligenceManager.processRequestStreamingError = exception;
  }

  public void triggerProcessRequestStreaming(
      Feature feature,
      Bundle request,
      int requestType,
      @Nullable CancellationSignal signal,
      @Nullable ProcessingSignal processingSignal,
      Executor executor,
      StreamingProcessingCallback callback) {
    if (processRequestStreamingError != null) {
      executor.execute(() -> callback.onError(processRequestStreamingError));
    } else {
      executor.execute(() -> callback.onResult(streamingBundle));
    }
  }

  public void setAutoTriggerCallbacks(boolean autoTriggerCallbacks) {
    ShadowOnDeviceIntelligenceManager.autoTriggerCallbacks = autoTriggerCallbacks;
  }

  public List<GetVersionRequest> getGetVersionRequests() {
    return ImmutableList.copyOf(getVersionRequests);
  }

  public GetVersionRequest getLastGetVersionRequest() {
    return Iterables.getLast(getVersionRequests);
  }

  public List<GetFeatureRequest> getGetFeatureRequests() {
    return ImmutableList.copyOf(getFeatureRequests);
  }

  public GetFeatureRequest getLastGetFeatureRequest() {
    return Iterables.getLast(getFeatureRequests);
  }

  public List<ListFeaturesRequest> getListFeaturesRequests() {
    return ImmutableList.copyOf(listFeaturesRequests);
  }

  public ListFeaturesRequest getLastListFeaturesRequest() {
    return Iterables.getLast(listFeaturesRequests);
  }

  public List<GetFeatureDetailsRequest> getGetFeatureDetailsRequests() {
    return ImmutableList.copyOf(getFeatureDetailsRequests);
  }

  public GetFeatureDetailsRequest getLastGetFeatureDetailsRequest() {
    return Iterables.getLast(getFeatureDetailsRequests);
  }

  public List<RequestFeatureDownloadRequest> getRequestFeatureDownloadRequests() {
    return ImmutableList.copyOf(requestFeatureDownloadRequests);
  }

  public RequestFeatureDownloadRequest getLastRequestFeatureDownloadRequest() {
    return Iterables.getLast(requestFeatureDownloadRequests);
  }

  public List<RequestTokenInfoRequest> getRequestTokenInfoRequests() {
    return ImmutableList.copyOf(requestTokenInfoRequests);
  }

  public RequestTokenInfoRequest getLastRequestTokenInfoRequest() {
    return Iterables.getLast(requestTokenInfoRequests);
  }

  public List<ProcessRequestRequest> getProcessRequestRequests() {
    return ImmutableList.copyOf(processRequestRequests);
  }

  public ProcessRequestRequest getLastProcessRequestRequest() {
    return Iterables.getLast(processRequestRequests);
  }

  public List<ProcessRequestStreamingRequest> getProcessRequestStreamingRequests() {
    return ImmutableList.copyOf(processRequestStreamingRequests);
  }

  public ProcessRequestStreamingRequest getLastProcessRequestStreamingRequest() {
    return Iterables.getLast(processRequestStreamingRequests);
  }

  @Resetter
  public static void reset() {
    getVersionRequests.clear();
    getFeatureRequests.clear();
    listFeaturesRequests.clear();
    getFeatureDetailsRequests.clear();
    requestFeatureDownloadRequests.clear();
    requestTokenInfoRequests.clear();
    processRequestRequests.clear();
    processRequestStreamingRequests.clear();
    autoTriggerCallbacks = true;

    version = DUMMY_VERSION;
    remoteServicePackageName = DUMMY_PACKAGE_NAME;
    feature = DUMMY_FEATURE;
    featureList = ImmutableList.of(DUMMY_FEATURE);
    featureDetails = DUMMY_FEATURE_DETAILS;
    tokenInfo = DUMMY_TOKEN_INFO;
    getFeatureError = null;
    listFeaturesError = null;
    getFeatureDetailsError = null;
    processRequestError = null;
    processRequestStreamingError = null;
    bundle = DUMMY_BUNDLE;
    streamingBundle = DUMMY_BUNDLE;
  }

  /** Stashed request for {@link #getVersion}. */
  public static final class GetVersionRequest {
    public final Executor executor;
    public final LongConsumer consumer;

    public GetVersionRequest(Executor executor, LongConsumer consumer) {
      this.executor = executor;
      this.consumer = consumer;
    }
  }

  /** Stashed request for {@link #getFeature}. */
  public static final class GetFeatureRequest {
    public final int featureId;
    public final Executor executor;
    public final OutcomeReceiver<Feature, OnDeviceIntelligenceException> receiver;

    public GetFeatureRequest(
        int featureId,
        Executor executor,
        OutcomeReceiver<Feature, OnDeviceIntelligenceException> receiver) {
      this.featureId = featureId;
      this.executor = executor;
      this.receiver = receiver;
    }
  }

  /** Stashed request for {@link #listFeatures}. */
  public static final class ListFeaturesRequest {
    public final Executor executor;
    public final OutcomeReceiver<List<Feature>, OnDeviceIntelligenceException> receiver;

    public ListFeaturesRequest(
        Executor executor, OutcomeReceiver<List<Feature>, OnDeviceIntelligenceException> receiver) {
      this.executor = executor;
      this.receiver = receiver;
    }
  }

  /** Stashed request for {@link #getFeatureDetails}. */
  public static final class GetFeatureDetailsRequest {
    public final Feature feature;
    public final Executor executor;
    public final OutcomeReceiver<FeatureDetails, OnDeviceIntelligenceException> receiver;

    public GetFeatureDetailsRequest(
        Feature feature,
        Executor executor,
        OutcomeReceiver<FeatureDetails, OnDeviceIntelligenceException> receiver) {
      this.feature = feature;
      this.executor = executor;
      this.receiver = receiver;
    }
  }

  /** Stashed request for {@link #requestFeatureDownload}. */
  public static final class RequestFeatureDownloadRequest {
    public final Feature feature;
    @Nullable public final CancellationSignal signal;
    public final Executor executor;
    public final DownloadCallback callback;

    public RequestFeatureDownloadRequest(
        Feature feature,
        @Nullable CancellationSignal signal,
        Executor executor,
        DownloadCallback callback) {
      this.feature = feature;
      this.signal = signal;
      this.executor = executor;
      this.callback = callback;
    }
  }

  /** Stashed request for {@link #requestTokenInfo}. */
  public static final class RequestTokenInfoRequest {
    public final Feature feature;
    public final Bundle request;
    @Nullable public final CancellationSignal signal;
    public final Executor executor;
    public final OutcomeReceiver<TokenInfo, OnDeviceIntelligenceException> receiver;

    public RequestTokenInfoRequest(
        Feature feature,
        Bundle request,
        @Nullable CancellationSignal signal,
        Executor executor,
        OutcomeReceiver<TokenInfo, OnDeviceIntelligenceException> receiver) {
      this.feature = feature;
      this.request = request;
      this.signal = signal;
      this.executor = executor;
      this.receiver = receiver;
    }
  }

  /** Stashed request for {@link #processRequest}. */
  public static final class ProcessRequestRequest {
    public final Feature feature;
    public final Bundle request;
    public final int requestType;
    @Nullable public final CancellationSignal cancellationSignal;
    @Nullable public final ProcessingSignal processingSignal;
    public final Executor executor;
    public final ProcessingCallback callback;

    public ProcessRequestRequest(
        Feature feature,
        Bundle request,
        int requestType,
        @Nullable CancellationSignal cancellationSignal,
        @Nullable ProcessingSignal processingSignal,
        Executor executor,
        ProcessingCallback callback) {
      this.feature = feature;
      this.request = request;
      this.requestType = requestType;
      this.cancellationSignal = cancellationSignal;
      this.processingSignal = processingSignal;
      this.executor = executor;
      this.callback = callback;
    }
  }

  /** Stashed request for {@link #processRequestStreaming}. */
  public static final class ProcessRequestStreamingRequest {
    public final Feature feature;
    public final Bundle request;
    public final int requestType;
    @Nullable public final CancellationSignal cancellationSignal;
    @Nullable public final ProcessingSignal processingSignal;
    public final Executor executor;
    public final StreamingProcessingCallback callback;

    public ProcessRequestStreamingRequest(
        Feature feature,
        Bundle request,
        int requestType,
        @Nullable CancellationSignal cancellationSignal,
        @Nullable ProcessingSignal processingSignal,
        Executor executor,
        StreamingProcessingCallback callback) {
      this.feature = feature;
      this.request = request;
      this.requestType = requestType;
      this.cancellationSignal = cancellationSignal;
      this.processingSignal = processingSignal;
      this.executor = executor;
      this.callback = callback;
    }
  }
}
