package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.ranging.RangingCapabilities.DISABLED_USER_RESTRICTIONS;
import static android.ranging.RangingCapabilities.ENABLED;
import static android.ranging.RangingManager.BLE_CS;
import static android.ranging.RangingManager.UWB;
import static com.google.common.truth.Truth.assertThat;

import android.ranging.RangingCapabilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = BAKLAVA)
public final class RangingCapabilitiesBuilderTest {

  @Test
  public void constructRangingCapabilities_returnsRangingCapabilities() {
    RangingCapabilities rangingCapabilities =
        new RangingCapabilitiesBuilder()
            .addAvailability(UWB, ENABLED)
            .addAvailability(BLE_CS, DISABLED_USER_RESTRICTIONS)
            .build();

    assertThat(rangingCapabilities).isNotNull();
    assertThat(rangingCapabilities.getTechnologyAvailability())
        .containsExactly(UWB, ENABLED, BLE_CS, DISABLED_USER_RESTRICTIONS);
  }

}
