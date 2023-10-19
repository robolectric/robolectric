package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;

import android.app.wearable.WearableSensingManager;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.SharedMemory;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.function.Consumer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit test for ShadowWearableSensingManager. */
@Config(minSdk = UPSIDE_DOWN_CAKE)
@RunWith(RobolectricTestRunner.class)
public class ShadowWearableSensingManagerTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock private Consumer<Integer> callback;

  @Test
  public void provideDataStream() throws Exception {
    WearableSensingManager wearableSensingManager =
        (WearableSensingManager)
            getApplicationContext().getSystemService(Context.WEARABLE_SENSING_SERVICE);
    ShadowWearableSensingManager shadowWearableSensingManager =
        Shadow.extract(wearableSensingManager);

    ParcelFileDescriptor[] descriptors = ParcelFileDescriptor.createPipe();
    shadowWearableSensingManager.setProvideDataStreamResult(
        WearableSensingManager.STATUS_ACCESS_DENIED);

    wearableSensingManager.provideDataStream(
        descriptors[0], MoreExecutors.directExecutor(), callback);

    verify(callback).accept(WearableSensingManager.STATUS_ACCESS_DENIED);
    assertThat(shadowWearableSensingManager.getLastParcelFileDescriptor())
        .isSameInstanceAs(descriptors[0]);
  }

  @Test
  public void provideData() throws Exception {
    WearableSensingManager wearableSensingManager =
        (WearableSensingManager)
            getApplicationContext().getSystemService(Context.WEARABLE_SENSING_SERVICE);
    ShadowWearableSensingManager shadowWearableSensingManager =
        Shadow.extract(wearableSensingManager);

    PersistableBundle persistableBundle = new PersistableBundle();
    SharedMemory sharedMemory = SharedMemory.create("name", 100);
    shadowWearableSensingManager.setProvideDataResult(WearableSensingManager.STATUS_ACCESS_DENIED);

    wearableSensingManager.provideData(
        persistableBundle, sharedMemory, MoreExecutors.directExecutor(), callback);

    verify(callback).accept(WearableSensingManager.STATUS_ACCESS_DENIED);
    assertThat(shadowWearableSensingManager.getLastDataBundle())
        .isSameInstanceAs(persistableBundle);
    assertThat(shadowWearableSensingManager.getLastSharedMemory()).isSameInstanceAs(sharedMemory);
  }
}
