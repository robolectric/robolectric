package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.os.BatteryManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowBatteryManagerTest {
  private static final int TEST_ID = 123;
  private BatteryManager batteryManager;
  private ShadowBatteryManager shadowBatteryManager;

  @Before
  public void before() {
    batteryManager =
        (BatteryManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.BATTERY_SERVICE);
    shadowBatteryManager = shadowOf(batteryManager);
  }

  @Test
  @Config(minSdk = M)
  public void testIsCharging() {
    assertThat(batteryManager.isCharging()).isFalse();

    shadowBatteryManager.setIsCharging(true);

    assertThat(batteryManager.isCharging()).isTrue();

    shadowBatteryManager.setIsCharging(false);

    assertThat(batteryManager.isCharging()).isFalse();
  }

  @Test
  public void testGetIntProperty() {
    assertThat(batteryManager.getIntProperty(TEST_ID)).isEqualTo(Integer.MIN_VALUE);

    shadowBatteryManager.setIntProperty(TEST_ID, 5);
    assertThat(batteryManager.getIntProperty(TEST_ID)).isEqualTo(5);

    shadowBatteryManager.setIntProperty(TEST_ID, 0);
    assertThat(batteryManager.getIntProperty(TEST_ID)).isEqualTo(0);

    shadowBatteryManager.setIntProperty(TEST_ID, Integer.MAX_VALUE);
    assertThat(batteryManager.getIntProperty(TEST_ID)).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  public void testGetLongProperty() {
    assertThat(batteryManager.getLongProperty(TEST_ID)).isEqualTo(Long.MIN_VALUE);

    shadowBatteryManager.setLongProperty(TEST_ID, 5L);
    assertThat(batteryManager.getLongProperty(TEST_ID)).isEqualTo(5L);

    shadowBatteryManager.setLongProperty(TEST_ID, 0);
    assertThat(batteryManager.getLongProperty(TEST_ID)).isEqualTo(0);

    shadowBatteryManager.setLongProperty(TEST_ID, Long.MAX_VALUE);
    assertThat(batteryManager.getLongProperty(TEST_ID)).isEqualTo(Long.MAX_VALUE);
  }

  @Test
  @Config(minSdk = P)
  public void testChargeTimeRemaining() {
    shadowBatteryManager.setChargeTimeRemaining(0L);
    assertThat(batteryManager.computeChargeTimeRemaining()).isEqualTo(0L);

    shadowBatteryManager.setChargeTimeRemaining(20L);
    assertThat(batteryManager.computeChargeTimeRemaining()).isEqualTo(20L);

    shadowBatteryManager.setChargeTimeRemaining(-1L);
    assertThat(batteryManager.computeChargeTimeRemaining()).isEqualTo(-1L);
  }

  @Test
  @Config(minSdk = P)
  public void testChargeTimeRemainingRejectsInvalidValues() {
    assertThrows(
        IllegalArgumentException.class, () -> shadowBatteryManager.setChargeTimeRemaining(-50L));
    assertThrows(
        IllegalArgumentException.class, () -> shadowBatteryManager.setChargeTimeRemaining(-100L));
  }

  @Test
  @Config(minSdk = P)
  public void batteryManager_activityContextEnabled_sharedState() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    Activity activity = null;
    try {
      Context context = ApplicationProvider.getApplicationContext();
      BatteryManager applicationBatteryManager = context.getSystemService(BatteryManager.class);
      activity = Robolectric.setupActivity(Activity.class);
      BatteryManager activityBatteryManager = activity.getSystemService(BatteryManager.class);

      assertThat(applicationBatteryManager).isNotSameInstanceAs(activityBatteryManager);

      ShadowBatteryManager shadowApplicationBatteryManager = shadowOf(applicationBatteryManager);
      shadowApplicationBatteryManager.setIsCharging(true);

      assertThat(activityBatteryManager.isCharging()).isTrue();
    } finally {
      if (activity != null) {
        activity.finish();
      }
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
