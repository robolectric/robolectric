package android.app;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;

import android.Manifest;
import android.content.Context;
import android.net.wifi.aware.AwareResources;
import android.net.wifi.aware.WifiAwareManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

@RunWith(AndroidJUnit4.class)
public class WifiAwareManagerTest {

  @Rule
  public GrantPermissionRule mRuntimePermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE);

  @Test
  public void wifiAwareManager_applicationInstance_isSameAsActivityInstance() {
    WifiAwareManager applicationWifiAwareManager =
        (WifiAwareManager) getApplicationContext().getSystemService(Context.WIFI_AWARE_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            WifiAwareManager activityWifiAwareManager =
                (WifiAwareManager) activity.getSystemService(Context.WIFI_AWARE_SERVICE);
            assertThat(applicationWifiAwareManager).isSameInstanceAs(activityWifiAwareManager);
          });
    }
  }

  @Test
  public void wifiAwareManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            WifiAwareManager activityWifiAwareManager =
                (WifiAwareManager) activity.getSystemService(Context.WIFI_AWARE_SERVICE);
            WifiAwareManager anotherActivityWifiAwareManager =
                (WifiAwareManager) activity.getSystemService(Context.WIFI_AWARE_SERVICE);
            assertThat(anotherActivityWifiAwareManager).isSameInstanceAs(activityWifiAwareManager);
          });
    }
  }

  @Test
  public void wifiAwareManager_instance_getAvailableAwareResourcesIsConsistent() {
    // Skip the test if Wi-Fi Aware is not supported
    assumeTrue(getApplicationContext().getSystemService(Context.WIFI_AWARE_SERVICE) != null);
    WifiAwareManager applicationWifiAwareManager =
        (WifiAwareManager) getApplicationContext().getSystemService(Context.WIFI_AWARE_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            WifiAwareManager activityWifiAwareManager =
                (WifiAwareManager) activity.getSystemService(Context.WIFI_AWARE_SERVICE);

            assertThat(activityWifiAwareManager).isNotNull();

            AwareResources applicationAwareResources =
                applicationWifiAwareManager.getAvailableAwareResources();
            AwareResources activityAwareResources =
                activityWifiAwareManager.getAvailableAwareResources();

            assertThat(activityAwareResources).isEqualTo(applicationAwareResources);
          });
    }
  }
}
