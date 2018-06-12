package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowBluetoothDeviceTest {

  private static final String MOCK_MAC_ADDRESS = "00:11:22:33:AA:BB";

  @Test
  public void canCreateBluetoothDeviceViaNewInstance() throws Exception {
    // This test passes as long as no Exception is thrown. It tests if the constructor can be
    // executed without throwing an Exception when getService() is called inside.
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    assertThat(bluetoothDevice).isNotNull();
  }

  @Test
  public void canSetAndGetUuids() throws Exception {
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);
    ParcelUuid[] uuids =
        new ParcelUuid[] {
          ParcelUuid.fromString("00000000-1111-2222-3333-000000000011"),
          ParcelUuid.fromString("00000000-1111-2222-3333-0000000000aa")
        };

    shadowOf(device).setUuids(uuids);
    assertThat(device.getUuids()).isEqualTo(uuids);
  }

  @Test
  public void getUuids_setUuidsNotCalled_shouldReturnNull() throws Exception {
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);
    assertThat(device.getUuids()).isNull();
  }
}
