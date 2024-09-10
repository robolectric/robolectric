package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.companion.virtual.VirtualDevice;
import android.companion.virtual.VirtualDeviceManager;
import android.content.Context;
import android.os.Build;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link VirtualDeviceManager}. */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class VirtualDeviceManagerTest {

  @Test
  public void virtualDeviceManager_applicationInstance_isNotSameAsActivityInstance() {
    VirtualDeviceManager applicationVirtualDeviceManager =
        (VirtualDeviceManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.VIRTUAL_DEVICE_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            VirtualDeviceManager activityVirtualDeviceManager =
                (VirtualDeviceManager) activity.getSystemService(Context.VIRTUAL_DEVICE_SERVICE);
            assertThat(applicationVirtualDeviceManager)
                .isNotSameInstanceAs(activityVirtualDeviceManager);
          });
    }
  }

  @Test
  public void virtualDeviceManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            VirtualDeviceManager activityVirtualDeviceManager =
                (VirtualDeviceManager) activity.getSystemService(Context.VIRTUAL_DEVICE_SERVICE);
            VirtualDeviceManager anotherActivityVirtualDeviceManager =
                (VirtualDeviceManager) activity.getSystemService(Context.VIRTUAL_DEVICE_SERVICE);
            assertThat(anotherActivityVirtualDeviceManager)
                .isSameInstanceAs(activityVirtualDeviceManager);
          });
    }
  }

  @Test
  public void virtualDeviceManager_instance_retrievesSameVirtualDevices() {
    VirtualDeviceManager applicationVirtualDeviceManager =
        (VirtualDeviceManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.VIRTUAL_DEVICE_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            VirtualDeviceManager activityVirtualDeviceManager =
                (VirtualDeviceManager) activity.getSystemService(Context.VIRTUAL_DEVICE_SERVICE);

            List<VirtualDevice> applicationVirtualDevices =
                applicationVirtualDeviceManager.getVirtualDevices();
            List<VirtualDevice> activityVirtualDevices =
                activityVirtualDeviceManager.getVirtualDevices();

            assertThat(applicationVirtualDevices).isNotNull();
            assertThat(activityVirtualDevices).isNotNull();

            assertThat(activityVirtualDevices).isEqualTo(applicationVirtualDevices);
          });
    }
  }
}
