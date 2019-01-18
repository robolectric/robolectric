package org.robolectric.shadows;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.os.ParcelUuid;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
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

  @Test
  public void canSetAndGetBondState() throws Exception {
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(device.getBondState()).isEqualTo(BOND_NONE);

    shadowOf(device).setBondState(BOND_BONDED);
    assertThat(device.getBondState()).isEqualTo(BOND_BONDED);
  }

  @Test
  public void canSetAndGetCreatedBond() {
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(device.createBond()).isFalse();

    shadowOf(device).setCreatedBond(true);
    assertThat(device.createBond()).isTrue();
  }

  @Test
  public void canSetAndGetFetchUuidsWithSdpResult() throws Exception {
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);
    assertThat(device.fetchUuidsWithSdp()).isFalse();

    shadowOf(device).setFetchUuidsWithSdpResult(true);
    assertThat(device.fetchUuidsWithSdp()).isTrue();
  }

  @Test
  public void getCorrectFetchUuidsWithSdpCount() throws Exception {
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);
    assertThat(shadowOf(device).getFetchUuidsWithSdpCount()).isEqualTo(0);

    device.fetchUuidsWithSdp();
    assertThat(shadowOf(device).getFetchUuidsWithSdpCount()).isEqualTo(1);

    device.fetchUuidsWithSdp();
    assertThat(shadowOf(device).getFetchUuidsWithSdpCount()).isEqualTo(2);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void connectGatt_doesntCrash() throws Exception {
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    assertThat(
            bluetoothDevice.connectGatt(
                ApplicationProvider.getApplicationContext(), false, new BluetoothGattCallback() {}))
        .isNotNull();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void canSetAndGetType() throws Exception {
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    shadowOf(device).setType(DEVICE_TYPE_CLASSIC);
    assertThat(device.getType()).isEqualTo(DEVICE_TYPE_CLASSIC);
  }
}
