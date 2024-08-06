package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.testing.TestActivity;

/** Junit test for {@link ShadowCarrierConfigManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = M)
public class ShadowCarrierConfigManagerTest {

  private static final int TEST_ID = 123;
  private static final String STRING_KEY = "key1";
  private static final String STRING_VALUE = "test";
  private static final String STRING_OVERRIDE_VALUE = "override";
  private static final String INT_KEY = "key2";
  private static final int INT_VALUE = 100;
  private static final String BOOLEAN_KEY = "key3";
  private static final boolean BOOLEAN_VALUE = true;
  private CarrierConfigManager carrierConfigManager;
  private Context context;

  @Before
  public void setUp() {
    carrierConfigManager =
        (CarrierConfigManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.CARRIER_CONFIG_SERVICE);
  }

  @Test
  public void getConfigForSubId_shouldReturnNonNullValue() {
    PersistableBundle persistableBundle = carrierConfigManager.getConfigForSubId(-1);
    assertThat(persistableBundle).isNotNull();
  }

  @Test
  public void testGetConfigForSubId() {
    PersistableBundle persistableBundle = new PersistableBundle();
    persistableBundle.putString(STRING_KEY, STRING_VALUE);
    persistableBundle.putInt(INT_KEY, INT_VALUE);
    persistableBundle.putBoolean(BOOLEAN_KEY, BOOLEAN_VALUE);

    shadowOf(carrierConfigManager).setConfigForSubId(TEST_ID, persistableBundle);

    PersistableBundle verifyBundle = carrierConfigManager.getConfigForSubId(TEST_ID);
    assertThat(verifyBundle).isNotNull();

    assertThat(verifyBundle.get(STRING_KEY)).isEqualTo(STRING_VALUE);
    assertThat(verifyBundle.getInt(INT_KEY)).isEqualTo(INT_VALUE);
    assertThat(verifyBundle.getBoolean(BOOLEAN_KEY)).isEqualTo(BOOLEAN_VALUE);
  }

  @Test
  public void getConfigForSubId_defaultsToEmpty() {
    PersistableBundle persistableBundle = carrierConfigManager.getConfigForSubId(99999);
    assertThat(persistableBundle).isNotNull();
  }

  @Test
  public void getConfigForSubId_afterSetNullConfig_shouldReturnNullValue() {
    shadowOf(carrierConfigManager).setConfigForSubId(TEST_ID, null);
    PersistableBundle persistableBundle = carrierConfigManager.getConfigForSubId(TEST_ID);
    assertThat(persistableBundle).isNull();
  }

  @Test
  public void overrideConfig_setNullConfig_removesOverride() {
    // Set value
    PersistableBundle existingBundle = new PersistableBundle();
    existingBundle.putString(STRING_KEY, STRING_VALUE);
    shadowOf(carrierConfigManager).setConfigForSubId(TEST_ID, existingBundle);
    // Set override value
    PersistableBundle overrideBundle = new PersistableBundle();
    overrideBundle.putString(STRING_KEY, STRING_OVERRIDE_VALUE);
    shadowOf(carrierConfigManager).overrideConfig(TEST_ID, overrideBundle);
    // Assert override is applied
    assertThat(carrierConfigManager.getConfigForSubId(TEST_ID).get(STRING_KEY))
        .isEqualTo(STRING_OVERRIDE_VALUE);

    shadowOf(carrierConfigManager).overrideConfig(TEST_ID, null);

    assertThat(carrierConfigManager.getConfigForSubId(TEST_ID).get(STRING_KEY))
        .isEqualTo(STRING_VALUE);
  }

  @Test
  public void overrideConfig_setBundleWithValues_overridesExistingConfig() {
    PersistableBundle existingBundle = new PersistableBundle();
    existingBundle.putString(STRING_KEY, STRING_VALUE);
    shadowOf(carrierConfigManager).setConfigForSubId(TEST_ID, existingBundle);
    assertThat(carrierConfigManager.getConfigForSubId(TEST_ID)).isNotNull();
    assertThat(carrierConfigManager.getConfigForSubId(TEST_ID).get(STRING_KEY))
        .isEqualTo(STRING_VALUE);
    assertThat(carrierConfigManager.getConfigForSubId(TEST_ID).getInt(INT_KEY)).isEqualTo(0);
    assertThat(carrierConfigManager.getConfigForSubId(TEST_ID).getBoolean(BOOLEAN_KEY)).isFalse();

    PersistableBundle overrideBundle = new PersistableBundle();
    overrideBundle.putString(STRING_KEY, STRING_OVERRIDE_VALUE);
    overrideBundle.putInt(INT_KEY, INT_VALUE);
    overrideBundle.putBoolean(BOOLEAN_KEY, BOOLEAN_VALUE);
    shadowOf(carrierConfigManager).overrideConfig(TEST_ID, overrideBundle);

    PersistableBundle verifyBundle = carrierConfigManager.getConfigForSubId(TEST_ID);
    assertThat(verifyBundle).isNotNull();
    assertThat(verifyBundle.get(STRING_KEY)).isEqualTo(STRING_OVERRIDE_VALUE);
    assertThat(verifyBundle.getInt(INT_KEY)).isEqualTo(INT_VALUE);
    assertThat(verifyBundle.getBoolean(BOOLEAN_KEY)).isEqualTo(BOOLEAN_VALUE);
  }

  @Test
  @Config(minSdk = O)
  public void carrierConfigManager_activityContextEnabled_differentInstancesRetrieveConfigs() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    Activity activity = null;
    try {
      CarrierConfigManager applicationCarrierConfigManager =
          (CarrierConfigManager)
              ApplicationProvider.getApplicationContext()
                  .getSystemService(Context.CARRIER_CONFIG_SERVICE);
      activity = Robolectric.setupActivity(TestActivity.class);
      CarrierConfigManager activityCarrierConfigManager =
          (CarrierConfigManager) activity.getSystemService(Context.CARRIER_CONFIG_SERVICE);

      int subId = SubscriptionManager.getDefaultSubscriptionId();

      PersistableBundle applicationConfig =
          applicationCarrierConfigManager.getConfigForSubId(subId);
      PersistableBundle activityConfig = activityCarrierConfigManager.getConfigForSubId(subId);

      applicationConfig.putString("test_key", "application_value");
      activityConfig.putString("test_key", "activity_value");

      assertThat(applicationConfig.getString("test_key")).isEqualTo("application_value");
      assertThat(activityConfig.getString("test_key")).isEqualTo("activity_value");
    } finally {
      if (activity != null) {
        activity.finish();
      }
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
