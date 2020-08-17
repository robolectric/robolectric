package org.robolectric.shadows;

import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES;
import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.ParcelUuid;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
  public void canSetAndGetPin() {
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(shadowOf(device).getPin()).isNull();

    byte[] pin = new byte[] { 1, 2, 3, 4 };
    device.setPin(pin);
    assertThat(shadowOf(device).getPin()).isEqualTo(pin);
  }

  @Test
  public void canSetAndGetPairingConfirmation() {
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(shadowOf(device).getPairingConfirmation()).isNull();

    device.setPairingConfirmation(true);
    assertThat(shadowOf(device).getPairingConfirmation()).isTrue();

    device.setPairingConfirmation(false);
    assertThat(shadowOf(device).getPairingConfirmation()).isFalse();
  }

  @Test
  public void canSetAndGetFetchUuidsWithSdpResult() throws Exception {
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);
    assertThat(device.fetchUuidsWithSdp()).isFalse();

    shadowOf(device).setFetchUuidsWithSdpResult(true);
    assertThat(device.fetchUuidsWithSdp()).isTrue();
  }

  @Test
  public void canSetAndGetBluetoothClass() throws Exception {
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(shadowOf(device).getBluetoothClass()).isNull();

    BluetoothClass bluetoothClass =
        BluetoothClass.class.getConstructor(int.class).newInstance(AUDIO_VIDEO_HEADPHONES);
    shadowOf(device).setBluetoothClass(bluetoothClass);
    assertThat(shadowOf(device).getBluetoothClass()).isEqualTo(bluetoothClass);
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
  @Config(minSdk = M)
  public void connectGatt_withTransport_doesntCrash() throws Exception {
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    assertThat(
            bluetoothDevice.connectGatt(
                ApplicationProvider.getApplicationContext(),
                false,
                new BluetoothGattCallback() {},
                BluetoothDevice.TRANSPORT_LE))
        .isNotNull();
  }

  @Test
  @Config(minSdk = O)
  public void connectGatt_withTransportPhy_doesntCrash() throws Exception {
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    assertThat(
        bluetoothDevice.connectGatt(
            ApplicationProvider.getApplicationContext(),
            false,
            new BluetoothGattCallback() {},
            BluetoothDevice.TRANSPORT_LE,
            BluetoothDevice.PHY_LE_1M_MASK))
        .isNotNull();
  }

  @Test
  @Config(minSdk = O)
  public void connectGatt_withTransportPhyHandler_doesntCrash() throws Exception {
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    assertThat(
        bluetoothDevice.connectGatt(
            ApplicationProvider.getApplicationContext(),
            false,
            new BluetoothGattCallback() {},
            BluetoothDevice.TRANSPORT_LE,
            BluetoothDevice.PHY_LE_1M_MASK,
            new Handler()))
        .isNotNull();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void canSetAndGetType() throws Exception {
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    shadowOf(device).setType(DEVICE_TYPE_CLASSIC);
    assertThat(device.getType()).isEqualTo(DEVICE_TYPE_CLASSIC);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void canGetBluetoothGatts() throws Exception {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    List<BluetoothGatt> createdGatts = new ArrayList<>();

    createdGatts.add(
        device.connectGatt(
            ApplicationProvider.getApplicationContext(), false, new BluetoothGattCallback() {}));
    createdGatts.add(
        device.connectGatt(
            ApplicationProvider.getApplicationContext(), false, new BluetoothGattCallback() {}));

    assertThat(shadowOf(device).getBluetoothGatts()).isEqualTo(createdGatts);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void connectGatt_setsBluetoothGattCallback() throws Exception {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    BluetoothGattCallback callback = new BluetoothGattCallback() {};

    BluetoothGatt bluetoothGatt =
        device.connectGatt(ApplicationProvider.getApplicationContext(), false, callback);

    assertThat(shadowOf(bluetoothGatt).getGattCallback())
        .isEqualTo(callback);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void canSimulateGattConnectionChange() throws Exception {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    BluetoothGattCallback callback = mock(BluetoothGattCallback.class);
    BluetoothGatt bluetoothGatt =
        device.connectGatt(ApplicationProvider.getApplicationContext(), false, callback);
    int status = 4;
    int newState = 2;

    shadowOf(device).simulateGattConnectionChange(status, newState);

    verify(callback).onConnectionStateChange(bluetoothGatt, status, newState);
  }

  @Test
  public void createRfcommSocketToServiceRecord_returnsSocket() throws Exception {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);

    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.randomUUID());
    assertThat(socket).isNotNull();
  }

  @Test
  public void getSetAlias() {
    String aliasName = "alias";
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    shadowOf(device).setAlias(aliasName);

    // getAlias is accessed by reflection
    try {
      Method getAliasName = android.bluetooth.BluetoothDevice.class.getMethod("getAlias");
      assertThat((String) getAliasName.invoke(device)).isEqualTo(aliasName);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("Failure accessing getAlias via reflection", e);
    }
  }

  @Config(maxSdk = Q)
  @Test
  public void getAliasName() {
    String aliasName = "alias";
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    shadowOf(device).setAlias(aliasName);

    // getAliasName is accessed by reflection
    try {
      Method getAliasName = android.bluetooth.BluetoothDevice.class.getMethod("getAliasName");
      assertThat((String) getAliasName.invoke(device)).isEqualTo(aliasName);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("Failure accessing getAliasName via reflection", e);
    }
  }

  @Config(maxSdk = Q)
  @Test
  public void getAliasName_aliasNull() {
    String deviceName = "device name";
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    shadowOf(device).setName(deviceName);

    // getAliasName is accessed by reflection
    try {
      Method getAliasName = android.bluetooth.BluetoothDevice.class.getMethod("getAliasName");
      // Expect the name if alias is null.
      assertThat((String) getAliasName.invoke(device)).isEqualTo(deviceName);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("Failure accessing getAliasName via reflection", e);
    }
  }
}
