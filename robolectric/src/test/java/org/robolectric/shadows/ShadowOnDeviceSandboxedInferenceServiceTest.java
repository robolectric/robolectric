package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.shadow.api.Shadow.extract;

import android.app.ondeviceintelligence.Feature;
import android.os.ParcelFileDescriptor;
import android.service.ondeviceintelligence.OnDeviceSandboxedInferenceService;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowOnDeviceSandboxedInferenceService.FetchFeatureFileDescriptorMapRequest;
import org.robolectric.shadows.ShadowOnDeviceSandboxedInferenceService.GetReadOnlyFileDescriptorRequest;

/** Test for {@link ShadowOnDeviceSandboxedInferenceService}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VANILLA_ICE_CREAM)
public class ShadowOnDeviceSandboxedInferenceServiceTest {

  private Executor executor;

  @Before
  public void setUp() {
    executor = MoreExecutors.directExecutor();
  }

  @Test
  public void openFileInput_returnsDummyFileInputStream() throws IOException {
    OnDeviceSandboxedInferenceService service =
        Robolectric.buildService(TestOnDeviceSandboxedInferenceService.class).create().get();
    FileInputStream fis = service.openFileInput("dummy_file");
    assertThat(fis).isNotNull();
    fis.close();
  }

  @Test
  public void openFileInput_returnsStashedFileInputStream() throws IOException {
    OnDeviceSandboxedInferenceService service =
        Robolectric.buildService(TestOnDeviceSandboxedInferenceService.class).create().get();
    ShadowOnDeviceSandboxedInferenceService shadowService =
        (ShadowOnDeviceSandboxedInferenceService) extract(service);

    File tempFile = File.createTempFile("dummy", "txt");
    tempFile.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write("dummy content".getBytes(UTF_8));
    }
    ParcelFileDescriptor pfd =
        ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
    shadowService.setReadOnlyFileDescriptor(pfd);

    FileInputStream fis = service.openFileInput("dummy_file");
    assertThat(fis).isNotNull();
    fis.close();
  }

  @SuppressWarnings("TryFailRefactoring")
  // Keep the try-catch since using assertThrows leads to a classloader issue with JUnit4 as the
  // service instance of a SystemApi type and not found in the current classloader.
  @Test
  public void openFileInput_throwsError() throws IOException {
    OnDeviceSandboxedInferenceService service =
        Robolectric.buildService(TestOnDeviceSandboxedInferenceService.class).create().get();
    ShadowOnDeviceSandboxedInferenceService shadowService =
        (ShadowOnDeviceSandboxedInferenceService) extract(service);

    shadowService.setOpenFileInputError(new FileNotFoundException("test error"));
    try {
      service.openFileInput("dummy_file");
      Assert.fail("Expected FileNotFoundException");
    } catch (FileNotFoundException e) {
      assertThat(e).hasMessageThat().contains("test error");
    }
  }

  @Test
  public void getReadOnlyFileDescriptor_callsCallbackWithSetValue() throws IOException {
    OnDeviceSandboxedInferenceService service =
        Robolectric.buildService(TestOnDeviceSandboxedInferenceService.class).create().get();
    ShadowOnDeviceSandboxedInferenceService shadowService =
        (ShadowOnDeviceSandboxedInferenceService) extract(service);

    File tempFile = File.createTempFile("dummy", "txt");
    tempFile.deleteOnExit();
    ParcelFileDescriptor expectedPfd =
        ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
    shadowService.setReadOnlyFileDescriptor(expectedPfd);

    Consumer<ParcelFileDescriptor> mockConsumer = mockConsumer();
    service.getReadOnlyFileDescriptor("dummy_file", executor, mockConsumer);
    verify(mockConsumer).accept(expectedPfd);
  }

  @Test
  public void fetchFeatureFileDescriptorMap_callsCallbackWithSetValue() {
    OnDeviceSandboxedInferenceService service =
        Robolectric.buildService(TestOnDeviceSandboxedInferenceService.class).create().get();
    ShadowOnDeviceSandboxedInferenceService shadowService =
        (ShadowOnDeviceSandboxedInferenceService) extract(service);

    Map<String, ParcelFileDescriptor> expectedMap = new HashMap<>();
    shadowService.setFeatureFileDescriptorMap(expectedMap);

    Feature mockFeature = new Feature.Builder(1).build();
    Consumer<Map<String, ParcelFileDescriptor>> mockConsumer = mockConsumer();
    service.fetchFeatureFileDescriptorMap(mockFeature, executor, mockConsumer);
    verify(mockConsumer).accept(expectedMap);
  }

  @Test
  public void getReadOnlyFileDescriptor_autoTriggerFalse_stashesRequest() throws IOException {
    OnDeviceSandboxedInferenceService service =
        Robolectric.buildService(TestOnDeviceSandboxedInferenceService.class).create().get();
    ShadowOnDeviceSandboxedInferenceService shadowService =
        (ShadowOnDeviceSandboxedInferenceService) extract(service);
    shadowService.setAutoTriggerCallbacks(false);

    File tempFile = File.createTempFile("dummy", "txt");
    tempFile.deleteOnExit();
    ParcelFileDescriptor expectedPfd =
        ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
    shadowService.setReadOnlyFileDescriptor(expectedPfd);

    Consumer<ParcelFileDescriptor> mockConsumer = mockConsumer();
    service.getReadOnlyFileDescriptor("dummy_file", executor, mockConsumer);

    assertThat(shadowService.getGetReadOnlyFileDescriptorRequests()).hasSize(1);
    GetReadOnlyFileDescriptorRequest request =
        shadowService.getLastGetReadOnlyFileDescriptorRequest();
    assertThat(request.fileName).isEqualTo("dummy_file");
    assertThat(request.executor).isEqualTo(executor);
    assertThat(request.resultConsumer).isEqualTo(mockConsumer);
  }

  @Test
  public void fetchFeatureFileDescriptorMap_autoTriggerFalse_stashesRequest() {
    OnDeviceSandboxedInferenceService service =
        Robolectric.buildService(TestOnDeviceSandboxedInferenceService.class).create().get();
    ShadowOnDeviceSandboxedInferenceService shadowService =
        (ShadowOnDeviceSandboxedInferenceService) extract(service);
    shadowService.setAutoTriggerCallbacks(false);

    Map<String, ParcelFileDescriptor> expectedMap = new HashMap<>();
    shadowService.setFeatureFileDescriptorMap(expectedMap);

    Feature mockFeature = new Feature.Builder(1).build();
    Consumer<Map<String, ParcelFileDescriptor>> mockConsumer = mockConsumer();
    service.fetchFeatureFileDescriptorMap(mockFeature, executor, mockConsumer);

    assertThat(shadowService.getFetchFeatureFileDescriptorMapRequests()).hasSize(1);
    FetchFeatureFileDescriptorMapRequest request =
        shadowService.getLastFetchFeatureFileDescriptorMapRequest();
    assertThat(request.feature).isEqualTo(mockFeature);
    assertThat(request.executor).isEqualTo(executor);
    assertThat(request.resultConsumer).isEqualTo(mockConsumer);
  }

  @SuppressWarnings("unchecked") // Casting from mock() is safe.
  private <T> Consumer<T> mockConsumer() {
    return mock(Consumer.class);
  }
}
