package android.app;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;

import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.testapp.TestActivity;

/** Tests for {@link CompanionDeviceManager}. */
public class CompanionDeviceManagerTest {
  private boolean hasCompanionDeviceSetup = false;

  @Before
  public void setUp() {
    // Use more flexible software approach to check whether we can run tests in this file
    // instead of required it with use-feature in AndroidManifest.xml.
    PackageManager packageManager = ApplicationProvider.getApplicationContext().getPackageManager();
    hasCompanionDeviceSetup =
        packageManager.hasSystemFeature(PackageManager.FEATURE_COMPANION_DEVICE_SETUP);
  }

  @Test
  public void companionDeviceManager_applicationInstance_isNotSameAsActivityInstance() {
    assumeTrue(hasCompanionDeviceSetup);
    CompanionDeviceManager applicationCompanionDeviceManager =
        (CompanionDeviceManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.COMPANION_DEVICE_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CompanionDeviceManager activityCompanionDeviceManager =
                (CompanionDeviceManager)
                    activity.getSystemService(Context.COMPANION_DEVICE_SERVICE);
            assertThat(applicationCompanionDeviceManager)
                .isNotSameInstanceAs(activityCompanionDeviceManager);
          });
    }
  }

  @Test
  public void companionDeviceManager_activityInstance_isSameAsActivityInstance() {
    assumeTrue(hasCompanionDeviceSetup);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CompanionDeviceManager activityCompanionDeviceManager =
                (CompanionDeviceManager)
                    activity.getSystemService(Context.COMPANION_DEVICE_SERVICE);
            CompanionDeviceManager anotherActivityCompanionDeviceManager =
                (CompanionDeviceManager)
                    activity.getSystemService(Context.COMPANION_DEVICE_SERVICE);
            assertThat(anotherActivityCompanionDeviceManager)
                .isSameInstanceAs(activityCompanionDeviceManager);
          });
    }
  }

  @Test
  public void companionDeviceManager_activityContextEnabled_differentInstances() {
    assumeTrue(hasCompanionDeviceSetup);
    CompanionDeviceManager applicationCompanionDeviceManager =
        ApplicationProvider.getApplicationContext().getSystemService(CompanionDeviceManager.class);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CompanionDeviceManager activityCompanionDeviceManager =
                activity.getSystemService(CompanionDeviceManager.class);

            assertThat(applicationCompanionDeviceManager)
                .isNotSameInstanceAs(activityCompanionDeviceManager);

            List<String> applicationAssociations =
                applicationCompanionDeviceManager.getAssociations();
            List<String> activityAssociations = activityCompanionDeviceManager.getAssociations();

            assertThat(applicationAssociations).isNotNull();
            assertThat(activityAssociations).isNotNull();

            assertThat(activityAssociations).isEqualTo(applicationAssociations);
          });
    }
  }
}
