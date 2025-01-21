package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkNotNull;

import android.app.wearable.WearableSensingManager;
import android.app.wearable.WearableSensingManager.StatusCode;
import android.content.ComponentName;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.SharedMemory;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.versioning.AndroidVersions.U;
import org.robolectric.versioning.AndroidVersions.V;

/** Shadow for VirtualDeviceManager. */
@Implements(
    value = WearableSensingManager.class,
    minSdk = U.SDK_INT,
    // TODO: remove when minimum supported compileSdk is >= 34
    isInAndroidSdk = false)
public class ShadowWearableSensingManager {

  private static @StatusCode Integer provideDataStreamResult =
      WearableSensingManager.STATUS_SUCCESS;
  private static @StatusCode Integer provideDataResult = WearableSensingManager.STATUS_SUCCESS;
  private static @StatusCode Integer startHotwordRecognitionResult =
      WearableSensingManager.STATUS_SUCCESS;
  private static @StatusCode Integer stopHotwordRecognitionResult =
      WearableSensingManager.STATUS_SUCCESS;
  private static final ArrayList<PersistableBundle> dataBundleList = new ArrayList<>();
  private static final ArrayList<SharedMemory> sharedMemoryList = new ArrayList<>();
  private static ParcelFileDescriptor lastParcelFileDescriptor;

  @Implementation
  protected void provideDataStream(
      ParcelFileDescriptor parcelFileDescriptor,
      Executor executor,
      Consumer<Integer> statusConsumer) {
    lastParcelFileDescriptor = parcelFileDescriptor;
    executor.execute(() -> statusConsumer.accept(provideDataStreamResult));
  }

  @Implementation
  protected void provideData(
      PersistableBundle data,
      SharedMemory sharedMemory,
      Executor executor,
      @StatusCode Consumer<Integer> statusConsumer) {
    dataBundleList.add(data);
    sharedMemoryList.add(sharedMemory);
    executor.execute(() -> statusConsumer.accept(provideDataResult));
  }

  @Implementation(minSdk = V.SDK_INT)
  protected void startHotwordRecognition(
      @Nullable ComponentName targetVisComponentName,
      @Nonnull Executor executor,
      @Nonnull @StatusCode Consumer<Integer> statusConsumer) {
    checkNotNull(executor);
    checkNotNull(statusConsumer);
    executor.execute(() -> statusConsumer.accept(startHotwordRecognitionResult));
  }

  @Implementation(minSdk = V.SDK_INT)
  protected void stopHotwordRecognition(
      @Nonnull Executor executor, @Nonnull @StatusCode Consumer<Integer> statusConsumer) {
    checkNotNull(executor);
    checkNotNull(statusConsumer);
    executor.execute(() -> statusConsumer.accept(stopHotwordRecognitionResult));
  }

  public void setProvideDataStreamResult(@StatusCode Integer provideDataStreamResult) {
    ShadowWearableSensingManager.provideDataStreamResult = provideDataStreamResult;
  }

  public void setProvideDataResult(@StatusCode Integer provideDataResult) {
    ShadowWearableSensingManager.provideDataResult = provideDataResult;
  }

  /**
   * Sets the status code that will be sent to the {@code @StatusCode Consumer<Integer>} when {@link
   * #startHotwordRecognition} is called.
   */
  public void setStartHotwordRecognitionResult(
      @Nonnull @StatusCode Integer startHotwordRecognitionResult) {
    checkNotNull(startHotwordRecognitionResult);
    ShadowWearableSensingManager.startHotwordRecognitionResult = startHotwordRecognitionResult;
  }

  /**
   * Sets the status code that will be sent to the {@code @StatusCode Consumer<Integer>} when {@link
   * #stopHotwordRecognition} is called.
   */
  public void setStopHotwordRecognitionResult(
      @Nonnull @StatusCode Integer stopHotwordRecognitionResult) {
    checkNotNull(stopHotwordRecognitionResult);
    ShadowWearableSensingManager.stopHotwordRecognitionResult = stopHotwordRecognitionResult;
  }

  public ParcelFileDescriptor getLastParcelFileDescriptor() {
    return lastParcelFileDescriptor;
  }

  public PersistableBundle getLastDataBundle() {
    return dataBundleList.isEmpty() ? null : Iterables.getLast(dataBundleList);
  }

  public List<PersistableBundle> getAllDataBundles() {
    return new ArrayList<>(dataBundleList);
  }

  public SharedMemory getLastSharedMemory() {
    return sharedMemoryList.isEmpty() ? null : Iterables.getLast(sharedMemoryList);
  }

  public List<SharedMemory> getAllSharedMemories() {
    return new ArrayList<>(sharedMemoryList);
  }

  @Resetter
  public static void reset() {
    provideDataStreamResult = WearableSensingManager.STATUS_SUCCESS;
    provideDataResult = WearableSensingManager.STATUS_SUCCESS;
    startHotwordRecognitionResult = WearableSensingManager.STATUS_SUCCESS;
    stopHotwordRecognitionResult = WearableSensingManager.STATUS_SUCCESS;
    dataBundleList.clear();
    sharedMemoryList.clear();
    lastParcelFileDescriptor = null;
  }
}
