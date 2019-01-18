package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Test for {@link ShadowBluetoothHeadset} */
@RunWith(AndroidJUnit4.class)
public class ShadowBluetoothHeadsetTest {
  private BluetoothDevice device1;
  private BluetoothDevice device2;
  private BluetoothHeadset bluetoothHeadset;

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    device1 = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:11:22:33:AA:BB");
    device2 = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("11:22:33:AA:BB:00");
    bluetoothHeadset = Shadow.newInstanceOf(BluetoothHeadset.class);
  }

  @Test
  public void getConnectedDevices_defaultsToEmptyList() {
    assertThat(bluetoothHeadset.getConnectedDevices()).isEmpty();
  }

  @Test
  public void getConnectedDevices_canBeSetUpWithAddConnectedDevice() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);
    shadowOf(bluetoothHeadset).addConnectedDevice(device2);

    assertThat(bluetoothHeadset.getConnectedDevices()).containsExactly(device1, device2);
  }

  @Test
  public void getConnectionState_defaultsToDisconnected() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);
    shadowOf(bluetoothHeadset).addConnectedDevice(device2);

    assertThat(bluetoothHeadset.getConnectionState(device1))
        .isEqualTo(BluetoothProfile.STATE_CONNECTED);
    assertThat(bluetoothHeadset.getConnectionState(device2))
        .isEqualTo(BluetoothProfile.STATE_CONNECTED);
  }

  @Test
  public void getConnectionState_canBeSetUpWithAddConnectedDevice() {
    assertThat(bluetoothHeadset.getConnectionState(device1))
        .isEqualTo(BluetoothProfile.STATE_DISCONNECTED);
  }

  @Test
  @Config(minSdk = KITKAT)
  public void sendVendorSpecificResultCode_defaultsToTrueForConnectedDevice() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);

    assertThat(bluetoothHeadset.sendVendorSpecificResultCode(device1, "command", "arg")).isTrue();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void sendVendorSpecificResultCode_alwaysFalseForDisconnectedDevice() {
    assertThat(bluetoothHeadset.sendVendorSpecificResultCode(device1, "command", "arg")).isFalse();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void sendVendorSpecificResultCode_canBeForcedToFalseForConnectedDevice() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);
    shadowOf(bluetoothHeadset).setAllowsSendVendorSpecificResultCode(false);

    assertThat(bluetoothHeadset.sendVendorSpecificResultCode(device1, "command", "arg")).isFalse();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void sendVendorSpecificResultCode_throwsOnNullCommand() {
    try {
      bluetoothHeadset.sendVendorSpecificResultCode(device1, null, "arg");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }
}
