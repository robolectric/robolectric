package android.app;

import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;

import android.net.VpnManager;
import android.net.VpnProfileState;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link VpnManager}. */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = R)
public class VpnManagerTest {

  @Test
  public void vpnManager_applicationInstance_isNotSameAsActivityInstance() {
    VpnManager applicationVpnManager =
        ApplicationProvider.getApplicationContext().getSystemService(VpnManager.class);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            VpnManager activityVpnManager = activity.getSystemService(VpnManager.class);
            assertThat(applicationVpnManager).isNotSameInstanceAs(activityVpnManager);
          });
    }
  }

  @Test
  public void vpnManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            VpnManager activityVpnManager = activity.getSystemService(VpnManager.class);
            VpnManager anotherActivityVpnManager = activity.getSystemService(VpnManager.class);
            assertThat(anotherActivityVpnManager).isSameInstanceAs(activityVpnManager);
          });
    }
  }

  @Test
  @SdkSuppress(minSdkVersion = TIRAMISU)
  public void vpnManager_instance_retrievesSameProfileState() {
    VpnManager applicationVpnManager =
        ApplicationProvider.getApplicationContext().getSystemService(VpnManager.class);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            VpnManager activityVpnManager = activity.getSystemService(VpnManager.class);

            VpnProfileState applicationProfileState =
                applicationVpnManager.getProvisionedVpnProfileState();
            VpnProfileState activityProfileState =
                activityVpnManager.getProvisionedVpnProfileState();

            assertThat(activityProfileState).isEqualTo(applicationProfileState);
          });
    }
  }
}
