package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.BatteryManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = LOLLIPOP)
public class ShadowBatteryManagerTest {
  private BatteryManager batteryManager;
  private ShadowBatteryManager shadowBatteryManager;
  private static final int TEST_ID = 123;

  @Before
  public void before() {
    batteryManager = (BatteryManager) RuntimeEnvironment.application.getSystemService(
        Context.BATTERY_SERVICE);
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
}
