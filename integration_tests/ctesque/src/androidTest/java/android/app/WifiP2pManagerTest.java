package android.app;

import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;

import android.Manifest;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link WifiP2pManager}. */
@RunWith(AndroidJUnit4.class)
public class WifiP2pManagerTest {

  @Rule
  public GrantPermissionRule mRuntimePermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE);

  @Test
  public void wifiP2pManager_applicationInstance_isSameOrDifferentAsActivityInstance() {
    WifiP2pManager applicationWifiP2pManager =
        (WifiP2pManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            WifiP2pManager activityWifiP2pManager =
                (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              assertThat(applicationWifiP2pManager).isNotSameInstanceAs(activityWifiP2pManager);
            } else {
              assertThat(applicationWifiP2pManager).isSameInstanceAs(activityWifiP2pManager);
            }
          });
    }
  }

  @Test
  public void wifiP2pManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            WifiP2pManager activityWifiP2pManager =
                (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
            WifiP2pManager anotherActivityWifiP2pManager =
                (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
            assertThat(anotherActivityWifiP2pManager).isSameInstanceAs(activityWifiP2pManager);
          });
    }
  }

  @Test
  public void wifiP2pManager_instance_retrievesSameGroupInfo() throws InterruptedException {
    WifiP2pManager applicationWifiP2pManager =
        (WifiP2pManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
    WifiP2pManager.Channel applicationChannel =
        applicationWifiP2pManager.initialize(
            ApplicationProvider.getApplicationContext(), getMainLooper(), null);

    CountDownLatch latch = new CountDownLatch(2);
    final String[] applicationGroupNameHolder = new String[1];
    final String[] activityGroupNameHolder = new String[1];

    applicationWifiP2pManager.requestGroupInfo(
        applicationChannel,
        group -> {
          if (group != null) {
            applicationGroupNameHolder[0] = group.getNetworkName();
          }
          latch.countDown();
        });

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            WifiP2pManager activityWifiP2pManager =
                (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
            WifiP2pManager.Channel activityChannel =
                activityWifiP2pManager.initialize(activity, activity.getMainLooper(), null);

            activityWifiP2pManager.requestGroupInfo(
                activityChannel,
                group -> {
                  if (group != null) {
                    activityGroupNameHolder[0] = group.getNetworkName();
                  }
                  latch.countDown();
                });
          });
    }

    latch.await(5, TimeUnit.SECONDS);

    assertThat(applicationGroupNameHolder[0]).isEqualTo(activityGroupNameHolder[0]);
  }
}
