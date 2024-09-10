package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link ConnectivityManager}. */
@RunWith(AndroidJUnit4.class)
public class ConnectivityManagerTest {

  @Rule
  public GrantPermissionRule mRuntimePermissionRule =
      GrantPermissionRule.grant(Manifest.permission.ACCESS_NETWORK_STATE);

  @Test
  public void connectivityManager_applicationInstance_behaviorBasedOnSdkVersion() {
    ConnectivityManager applicationConnectivityManager =
        ApplicationProvider.getApplicationContext().getSystemService(ConnectivityManager.class);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            ConnectivityManager activityConnectivityManager =
                activity.getSystemService(ConnectivityManager.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
              assertThat(applicationConnectivityManager)
                  .isNotSameInstanceAs(activityConnectivityManager);
            } else {
              assertThat(applicationConnectivityManager)
                  .isSameInstanceAs(activityConnectivityManager);
            }
          });
    }
  }

  @Test
  public void connectivityManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            ConnectivityManager activityConnectivityManager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            ConnectivityManager anotherActivityConnectivityManager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            assertThat(anotherActivityConnectivityManager)
                .isSameInstanceAs(activityConnectivityManager);
          });
    }
  }

  @Test
  public void connectivityManager_instance_retrievesSameActiveNetwork() {
    ConnectivityManager applicationConnectivityManager =
        (ConnectivityManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            ConnectivityManager activityConnectivityManager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

            Network applicationActiveNetwork = applicationConnectivityManager.getActiveNetwork();
            Network activityActiveNetwork = activityConnectivityManager.getActiveNetwork();

            assertThat(activityActiveNetwork).isEqualTo(applicationActiveNetwork);
          });
    }
  }
}
