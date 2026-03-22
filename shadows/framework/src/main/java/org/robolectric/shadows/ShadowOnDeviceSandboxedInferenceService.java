package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.app.ondeviceintelligence.Feature;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.service.ondeviceintelligence.OnDeviceSandboxedInferenceService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.jspecify.annotations.NullMarked;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link OnDeviceSandboxedInferenceService}. */
@NullMarked
@Implements(
    value = OnDeviceSandboxedInferenceService.class,
    minSdk = VANILLA_ICE_CREAM,
    isInAndroidSdk = false)
@SuppressWarnings("NonFinalStaticField") // Need shadow members to be non-final to allow resetting.
public class ShadowOnDeviceSandboxedInferenceService extends ShadowService {

  public static final Bundle DUMMY_BUNDLE = new Bundle();

  @Nullable
  private static Map<String, ParcelFileDescriptor> featureFileDescriptorMap = new HashMap<>();

  @Nullable private static ParcelFileDescriptor readOnlyFileDescriptor = null;
  @Nullable private static FileNotFoundException openFileInputError = null;
  @Nullable private static FileNotFoundException getReadOnlyFileDescriptorError = null;

  private static boolean autoTriggerCallbacks = true;

  private static final List<FetchFeatureFileDescriptorMapRequest>
      fetchFeatureFileDescriptorMapRequests = new ArrayList<>();
  private static final List<GetReadOnlyFileDescriptorRequest> getReadOnlyFileDescriptorRequests =
      new ArrayList<>();
  private static final List<OpenFileInputRequest> openFileInputRequests = new ArrayList<>();

  @Implementation
  protected FileInputStream openFileInput(String filename) throws FileNotFoundException {
    openFileInputRequests.add(new OpenFileInputRequest(filename));
    if (openFileInputError != null) {
      throw openFileInputError;
    }
    if (readOnlyFileDescriptor != null) {
      return new FileInputStream(readOnlyFileDescriptor.getFileDescriptor());
    }
    // Return a dummy FileInputStream
    try {
      File tempFile = File.createTempFile("dummy", "txt");
      tempFile.deleteOnExit();
      try (FileOutputStream fos = new FileOutputStream(tempFile)) {
        fos.write("dummy content".getBytes(UTF_8));
      }
      return new FileInputStream(tempFile);
    } catch (IOException e) {
      throw new FileNotFoundException(
          "Failed to create dummy file for openFileInput: " + e.getMessage());
    }
  }

  @Implementation
  protected void getReadOnlyFileDescriptor(
      String fileName, Executor executor, Consumer<ParcelFileDescriptor> resultConsumer)
      throws FileNotFoundException {
    if (autoTriggerCallbacks) {
      triggerGetReadOnlyFileDescriptor(fileName, executor, resultConsumer);
    } else {
      getReadOnlyFileDescriptorRequests.add(
          new GetReadOnlyFileDescriptorRequest(fileName, executor, resultConsumer));
    }
  }

  @Implementation
  protected void fetchFeatureFileDescriptorMap(
      Feature feature,
      Executor executor,
      Consumer<Map<String, ParcelFileDescriptor>> resultConsumer) {
    if (autoTriggerCallbacks) {
      triggerFetchFeatureFileDescriptorMap(feature, executor, resultConsumer);
    } else {
      fetchFeatureFileDescriptorMapRequests.add(
          new FetchFeatureFileDescriptorMapRequest(feature, executor, resultConsumer));
    }
  }

  public void setFeatureFileDescriptorMap(
      Map<String, ParcelFileDescriptor> featureFileDescriptorMap) {
    ShadowOnDeviceSandboxedInferenceService.featureFileDescriptorMap = featureFileDescriptorMap;
  }

  public void triggerFetchFeatureFileDescriptorMap(
      Feature feature,
      Executor executor,
      Consumer<Map<String, ParcelFileDescriptor>> resultConsumer) {
    executor.execute(() -> resultConsumer.accept(featureFileDescriptorMap));
  }

  public void setReadOnlyFileDescriptor(ParcelFileDescriptor readOnlyFileDescriptor) {
    ShadowOnDeviceSandboxedInferenceService.readOnlyFileDescriptor = readOnlyFileDescriptor;
  }

  public void setGetReadOnlyFileDescriptorError(@Nullable FileNotFoundException error) {
    ShadowOnDeviceSandboxedInferenceService.getReadOnlyFileDescriptorError = error;
  }

  public void setOpenFileInputError(@Nullable FileNotFoundException error) {
    ShadowOnDeviceSandboxedInferenceService.openFileInputError = error;
  }

  public void triggerGetReadOnlyFileDescriptor(
      String fileName, Executor executor, Consumer<ParcelFileDescriptor> resultConsumer)
      throws FileNotFoundException {
    if (getReadOnlyFileDescriptorError != null) {
      throw getReadOnlyFileDescriptorError;
    }
    executor.execute(() -> resultConsumer.accept(readOnlyFileDescriptor));
  }

  public void setAutoTriggerCallbacks(boolean autoTriggerCallbacks) {
    ShadowOnDeviceSandboxedInferenceService.autoTriggerCallbacks = autoTriggerCallbacks;
  }

  public List<FetchFeatureFileDescriptorMapRequest> getFetchFeatureFileDescriptorMapRequests() {
    return ImmutableList.copyOf(fetchFeatureFileDescriptorMapRequests);
  }

  public FetchFeatureFileDescriptorMapRequest getLastFetchFeatureFileDescriptorMapRequest() {
    return Iterables.getLast(fetchFeatureFileDescriptorMapRequests);
  }

  public List<GetReadOnlyFileDescriptorRequest> getGetReadOnlyFileDescriptorRequests() {
    return ImmutableList.copyOf(getReadOnlyFileDescriptorRequests);
  }

  public GetReadOnlyFileDescriptorRequest getLastGetReadOnlyFileDescriptorRequest() {
    return Iterables.getLast(getReadOnlyFileDescriptorRequests);
  }

  public List<OpenFileInputRequest> getOpenFileInputRequests() {
    return ImmutableList.copyOf(openFileInputRequests);
  }

  public OpenFileInputRequest getLastOpenFileInputRequest() {
    return Iterables.getLast(openFileInputRequests);
  }

  @Resetter
  public static void reset() {
    fetchFeatureFileDescriptorMapRequests.clear();
    getReadOnlyFileDescriptorRequests.clear();
    openFileInputRequests.clear();
    autoTriggerCallbacks = true;
    featureFileDescriptorMap = new HashMap<>();
    readOnlyFileDescriptor = null;
    openFileInputError = null;
    getReadOnlyFileDescriptorError = null;
  }

  /** Stashed request for {@link #fetchFeatureFileDescriptorMap}. */
  public static final class FetchFeatureFileDescriptorMapRequest {
    public final Feature feature;
    public final Executor executor;
    public final Consumer<Map<String, ParcelFileDescriptor>> resultConsumer;

    public FetchFeatureFileDescriptorMapRequest(
        Feature feature,
        Executor executor,
        Consumer<Map<String, ParcelFileDescriptor>> resultConsumer) {
      this.feature = feature;
      this.executor = executor;
      this.resultConsumer = resultConsumer;
    }
  }

  /** Stashed request for {@link #getReadOnlyFileDescriptor}. */
  public static final class GetReadOnlyFileDescriptorRequest {
    public final String fileName;
    public final Executor executor;
    public final Consumer<ParcelFileDescriptor> resultConsumer;

    public GetReadOnlyFileDescriptorRequest(
        String fileName, Executor executor, Consumer<ParcelFileDescriptor> resultConsumer) {
      this.fileName = fileName;
      this.executor = executor;
      this.resultConsumer = resultConsumer;
    }
  }

  /** Stashed request for {@link #openFileInput}. */
  public static final class OpenFileInputRequest {
    public final String fileName;

    public OpenFileInputRequest(String fileName) {
      this.fileName = fileName;
    }
  }
}
