package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.app.wearable.WearableSensingManager;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.SharedMemory;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
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

    PersistableBundle persistableBundle1 = new PersistableBundle();
    PersistableBundle persistableBundle2 = new PersistableBundle();
    SharedMemory sharedMemory1 = SharedMemory.create("name1", 100);
    SharedMemory sharedMemory2 = SharedMemory.create("name2", 200);
    shadowWearableSensingManager.setProvideDataResult(WearableSensingManager.STATUS_ACCESS_DENIED);

    wearableSensingManager.provideData(
        persistableBundle1, sharedMemory1, MoreExecutors.directExecutor(), callback);
    wearableSensingManager.provideData(
        persistableBundle2, sharedMemory2, MoreExecutors.directExecutor(), callback);

    verify(callback, times(2)).accept(WearableSensingManager.STATUS_ACCESS_DENIED);
    assertThat(shadowWearableSensingManager.getLastDataBundle())
        .isSameInstanceAs(persistableBundle2);
    assertThat(shadowWearableSensingManager.getLastSharedMemory()).isSameInstanceAs(sharedMemory2);
    assertThat(shadowWearableSensingManager.getAllDataBundles())
        .containsExactly(persistableBundle1, persistableBundle2);
    assertThat(shadowWearableSensingManager.getAllSharedMemories())
        .containsExactly(sharedMemory1, sharedMemory2);
  }

  @Test
  public void getLastDataBundle_noDataProvided_doesNotThrow() throws Exception {
    WearableSensingManager wearableSensingManager =
        (WearableSensingManager)
            getApplicationContext().getSystemService(Context.WEARABLE_SENSING_SERVICE);
    ShadowWearableSensingManager shadowWearableSensingManager =
        Shadow.extract(wearableSensingManager);

    shadowWearableSensingManager.getLastDataBundle();
  }

  @Test
  public void getLastSharedMemory_noDataProvided_doesNotThrow() throws Exception {
    WearableSensingManager wearableSensingManager =
        (WearableSensingManager)
            getApplicationContext().getSystemService(Context.WEARABLE_SENSING_SERVICE);
    ShadowWearableSensingManager shadowWearableSensingManager =
        Shadow.extract(wearableSensingManager);

    shadowWearableSensingManager.getLastSharedMemory();
  }

  @Test
  public void wearableSensingManager_activityContextEnabled_differentInstancesProvideDataStream() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      WearableSensingManager applicationWearableSensingManager =
          RuntimeEnvironment.getApplication().getSystemService(WearableSensingManager.class);
      Activity activity = controller.get();
      WearableSensingManager activityWearableSensingManager =
          activity.getSystemService(WearableSensingManager.class);

      assertThat(applicationWearableSensingManager)
          .isNotSameInstanceAs(activityWearableSensingManager);

      ParcelFileDescriptor applicationPfd =
          ParcelFileDescriptor.adoptFd(-1); // Placeholder descriptor
      ParcelFileDescriptor activityPfd = ParcelFileDescriptor.adoptFd(-1); // Placeholder descriptor
      Executor executor = Runnable::run; // Simple executor for test purposes

      final int[] applicationStatus = new int[1];
      final int[] activityStatus = new int[1];

      Consumer<Integer> applicationStatusConsumer = status -> applicationStatus[0] = status;
      Consumer<Integer> activityStatusConsumer = status -> activityStatus[0] = status;

      applicationWearableSensingManager.provideDataStream(
          applicationPfd, executor, applicationStatusConsumer);
      activityWearableSensingManager.provideDataStream(
          activityPfd, executor, activityStatusConsumer);

      assertThat(activityStatus[0]).isEqualTo(applicationStatus[0]);
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
