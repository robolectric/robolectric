package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.Manifest;
import android.content.Context;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link CarrierConfigManager}. */
@RunWith(AndroidJUnit4.class)
public class CarrierConfigManagerTest {

  @Rule
  public GrantPermissionRule mRuntimePermissionRule =
      GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE);

  @Test
  public void carrierConfigManager_applicationInstance_isNotSameAsActivityInstance() {
    CarrierConfigManager applicationCarrierConfigManager =
        (CarrierConfigManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.CARRIER_CONFIG_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CarrierConfigManager activityCarrierConfigManager =
                (CarrierConfigManager) activity.getSystemService(Context.CARRIER_CONFIG_SERVICE);
            assertThat(applicationCarrierConfigManager)
                .isNotSameInstanceAs(activityCarrierConfigManager);
          });
    }
  }

  @Test
  public void carrierConfigManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CarrierConfigManager activityCarrierConfigManager =
                (CarrierConfigManager) activity.getSystemService(Context.CARRIER_CONFIG_SERVICE);
            CarrierConfigManager anotherActivityCarrierConfigManager =
                (CarrierConfigManager) activity.getSystemService(Context.CARRIER_CONFIG_SERVICE);
            assertThat(anotherActivityCarrierConfigManager)
                .isSameInstanceAs(activityCarrierConfigManager);
          });
    }
  }

  @Test
  public void carrierConfigManager_instance_retrievesSameConfigs() {
    CarrierConfigManager applicationCarrierConfigManager =
        (CarrierConfigManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.CARRIER_CONFIG_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CarrierConfigManager activityCarrierConfigManager =
                (CarrierConfigManager) activity.getSystemService(Context.CARRIER_CONFIG_SERVICE);

            int subId = SubscriptionManager.getDefaultSubscriptionId();

            PersistableBundle applicationConfigs =
                applicationCarrierConfigManager.getConfigForSubId(subId);
            PersistableBundle activityConfigs =
                activityCarrierConfigManager.getConfigForSubId(subId);

            Parcel applicationParcel = Parcel.obtain();
            Parcel activityParcel = Parcel.obtain();

            applicationConfigs.writeToParcel(applicationParcel, 0);
            activityConfigs.writeToParcel(activityParcel, 0);

            byte[] applicationBytes = applicationParcel.marshall();
            byte[] activityBytes = activityParcel.marshall();

            assertThat(activityBytes).isEqualTo(applicationBytes);

            applicationParcel.recycle();
            activityParcel.recycle();
          });
    }
  }
}
