package org.robolectric.shadows;

import android.app.wearable.WearableSensingManager;
import android.app.wearable.WearableSensingManager.StatusCode;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.SharedMemory;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for VirtualDeviceManager. */
@Implements(
    value = WearableSensingManager.class,
    minSdk = U.SDK_INT,
    // TODO: remove when minimum supported compileSdk is >= 34
    isInAndroidSdk = false)
public class ShadowWearableSensingManager {

  private @StatusCode Integer provideDataStreamResult = WearableSensingManager.STATUS_SUCCESS;
  private @StatusCode Integer provideDataResult = WearableSensingManager.STATUS_SUCCESS;
  private PersistableBundle lastDataBundle;
  private SharedMemory lastSharedMemory;
  private ParcelFileDescriptor lastParcelFileDescriptor;

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
    lastDataBundle = data;
    lastSharedMemory = sharedMemory;
    executor.execute(() -> statusConsumer.accept(provideDataResult));
  }

  public void setProvideDataStreamResult(@StatusCode Integer provideDataStreamResult) {
    this.provideDataStreamResult = provideDataStreamResult;
  }

  public void setProvideDataResult(@StatusCode Integer provideDataResult) {
    this.provideDataResult = provideDataResult;
  }

  public ParcelFileDescriptor getLastParcelFileDescriptor() {
    return lastParcelFileDescriptor;
  }

  public PersistableBundle getLastDataBundle() {
    return lastDataBundle;
  }

  public SharedMemory getLastSharedMemory() {
    return lastSharedMemory;
  }
}
