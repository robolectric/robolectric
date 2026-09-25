package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.companion.AssociationInfo;
import android.net.MacAddress;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public final class AssociationInfoBuilderTest {
  private static final int ID = 7;
  private static final int USER_ID = 8;
  private static final String PACKAGE_NAME = "com.google.foo";
  private static final String TAG_NAME = "Tag name";
  private static final String DEVICE_MAC_ADDRESS = "AA:BB:CC:DD:EE:FF";
  private static final String DISPLAY_NAME = "Display Name";
  private static final String DEVICE_PROFILE = "Device Profile";
  private static final boolean SELF_MANAGED = true;
  private static final boolean NOTIFY_ON_DEVICE_NEARBY = true;
  private static final long APPROVED_MS = 1234L;
  private static final boolean REVOKED = true;
  private static final long LAST_TIME_CONNECTED_MS = 5678L;
  private static final int SYSTEM_DATA_SYNC_FLAGS = 7;

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void obtain() {
    Object associatedDeviceValue = null;
    if (ReflectionHelpers.hasField(AssociationInfo.class, "mAssociatedDevice")) {
      try {
        Class<?> associatedDeviceClazz = Class.forName("android.companion.AssociatedDevice");
        associatedDeviceValue = ReflectionHelpers.newInstance(associatedDeviceClazz);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    AssociationInfo info =
        AssociationInfoBuilder.newBuilder()
            .setId(ID)
            .setUserId(USER_ID)
            .setPackageName(PACKAGE_NAME)
            .setDeviceMacAddress(DEVICE_MAC_ADDRESS)
            .setDisplayName(DISPLAY_NAME)
            .setAssociatedDevice(associatedDeviceValue)
            .setDeviceProfile(DEVICE_PROFILE)
            .setSelfManaged(SELF_MANAGED)
            .setNotifyOnDeviceNearby(NOTIFY_ON_DEVICE_NEARBY)
            .setApprovedMs(APPROVED_MS)
            .setRevoked(REVOKED)
            .setLastTimeConnectedMs(LAST_TIME_CONNECTED_MS)
            .setSystemDataSyncFlags(SYSTEM_DATA_SYNC_FLAGS)
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
    assertThat((long) ReflectionHelpers.callInstanceMethod(info, "getLastTimeConnectedMs"))
        .isEqualTo(LAST_TIME_CONNECTED_MS);

    if (ReflectionHelpers.hasField(AssociationInfo.class, "mAssociatedDevice")) {
      Object associatedDevice = ReflectionHelpers.callInstanceMethod(info, "getAssociatedDevice");
      assertThat(associatedDevice).isEqualTo(associatedDeviceValue);
      int systemDataSyncFlags =
          ReflectionHelpers.callInstanceMethod(info, "getSystemDataSyncFlags");
      assertThat(systemDataSyncFlags).isEqualTo(SYSTEM_DATA_SYNC_FLAGS);
    }

    if (ReflectionHelpers.hasField(AssociationInfo.class, "mRevoked")) {
      boolean isRevoked = ReflectionHelpers.callInstanceMethod(info, "isRevoked");
      assertThat(isRevoked).isEqualTo(REVOKED);
    }

    if (ReflectionHelpers.hasMethod(AssociationInfo.class, "isTrusted")) {
      boolean isTrusted = ReflectionHelpers.callInstanceMethod(info, "isTrusted");
      assertThat(isTrusted).isFalse();
    }
  }

  @Test
  public void setIsTrusted_setsFieldAndReturnsBuilder() {
    AssociationInfoBuilder builder = AssociationInfoBuilder.newBuilder();
    assertThat(builder.setIsTrusted(true)).isSameInstanceAs(builder);
    boolean isTrustedField = ReflectionHelpers.getField(builder, "isTrusted");
    assertThat(isTrustedField).isTrue();

    builder.setIsTrusted(false);
    assertThat((boolean) ReflectionHelpers.getField(builder, "isTrusted")).isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void setIsTrusted_true() {
    AssociationInfo info =
        AssociationInfoBuilder.newBuilder()
            .setId(ID)
            .setDisplayName(DISPLAY_NAME)
            .setIsTrusted(true)
            .build();
    if (ReflectionHelpers.hasMethod(AssociationInfo.class, "isTrusted")) {
      assertThat((boolean) ReflectionHelpers.callInstanceMethod(info, "isTrusted")).isTrue();
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void setIsTrusted_false() {
    AssociationInfo info =
        AssociationInfoBuilder.newBuilder()
            .setId(ID)
            .setDisplayName(DISPLAY_NAME)
            .setIsTrusted(false)
            .build();
    if (ReflectionHelpers.hasMethod(AssociationInfo.class, "isTrusted")) {
      assertThat((boolean) ReflectionHelpers.callInstanceMethod(info, "isTrusted")).isFalse();
    }
  }
}

