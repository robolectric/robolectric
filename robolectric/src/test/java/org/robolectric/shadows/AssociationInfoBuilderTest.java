package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.companion.AssociationInfo;
import android.net.MacAddress;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public final class AssociationInfoBuilderTest {
  private static final int ID = 7;
  private static final int USER_ID = 8;
  private static final String PACKAGE_NAME = "com.google.foo";
  private static final String DEVICE_MAC_ADDRESS = "AA:BB:CC:DD:EE:FF";
  private static final String DISPLAY_NAME = "Display Name";
  private static final String DEVICE_PROFILE = "Device Profile";
  private static final boolean SELF_MANAGED = true;
  private static final boolean NOTIFY_ON_DEVICE_NEARBY = true;
  private static final long APPROVED_MS = 1234L;
  private static final long LAST_TIME_CONNECTED_MS = 5678L;

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void obtain() {
    AssociationInfo info =
        AssociationInfoBuilder.newBuilder()
            .setId(ID)
            .setUserId(USER_ID)
            .setPackageName(PACKAGE_NAME)
            .setDeviceMacAddress(DEVICE_MAC_ADDRESS)
            .setDisplayName(DISPLAY_NAME)
            .setDeviceProfile(DEVICE_PROFILE)
            .setSelfManaged(SELF_MANAGED)
            .setNotifyOnDeviceNearby(NOTIFY_ON_DEVICE_NEARBY)
            .setApprovedMs(APPROVED_MS)
            .setLastTimeConnectedMs(LAST_TIME_CONNECTED_MS)
            .build();

    assertThat(info.getId()).isEqualTo(ID);
    assertThat(info.getUserId()).isEqualTo(USER_ID);
    assertThat(info.getPackageName()).isEqualTo(PACKAGE_NAME);
    assertThat(info.getDeviceMacAddress()).isEqualTo(MacAddress.fromString(DEVICE_MAC_ADDRESS));
    assertThat(info.getDisplayName().toString()).isEqualTo(DISPLAY_NAME);
    assertThat(info.getDeviceProfile()).isEqualTo(DEVICE_PROFILE);
    assertThat(info.isSelfManaged()).isEqualTo(SELF_MANAGED);
    assertThat(info.isNotifyOnDeviceNearby()).isEqualTo(NOTIFY_ON_DEVICE_NEARBY);
    assertThat(info.getTimeApprovedMs()).isEqualTo(APPROVED_MS);
    assertThat(info.getLastTimeConnectedMs()).isEqualTo(LAST_TIME_CONNECTED_MS);
  }
}
