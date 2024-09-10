package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.net.vcn.VcnManager;
import android.os.Build;
import android.os.ParcelUuid;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link VcnManager}. */
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.S)
@RunWith(AndroidJUnit4.class)
public class VcnManagerTest {

  @Test
  public void vcnManager_applicationInstance_isNotSameAsActivityInstance() {
    VcnManager applicationVcnManager =
        ApplicationProvider.getApplicationContext().getSystemService(VcnManager.class);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            VcnManager activityVcnManager = activity.getSystemService(VcnManager.class);
            assertThat(applicationVcnManager).isNotSameInstanceAs(activityVcnManager);
          });
    }
  }

  @Test
  public void vcnManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            VcnManager activityVcnManager = activity.getSystemService(VcnManager.class);
            VcnManager anotherActivityVcnManager = activity.getSystemService(VcnManager.class);
            assertThat(anotherActivityVcnManager).isSameInstanceAs(activityVcnManager);
          });
    }
  }

  @Test
  public void vcnManager_instance_retrievesSameConfiguredSubscriptionGroups() {
    VcnManager applicationVcnManager =
        ApplicationProvider.getApplicationContext().getSystemService(VcnManager.class);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            VcnManager activityVcnManager = activity.getSystemService(VcnManager.class);

            List<ParcelUuid> applicationConfiguredSubscriptionGroups =
                applicationVcnManager.getConfiguredSubscriptionGroups();
            List<ParcelUuid> activityConfiguredSubscriptionGroups =
                activityVcnManager.getConfiguredSubscriptionGroups();

            assertThat(applicationConfiguredSubscriptionGroups).isNotNull();
            assertThat(activityConfiguredSubscriptionGroups).isNotNull();

            assertThat(activityConfiguredSubscriptionGroups)
                .isEqualTo(applicationConfiguredSubscriptionGroups);
          });
    }
  }
}
