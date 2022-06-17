package org.robolectric.shadows;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES;
import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothSocket;
import android.os.Build.VERSION_CODES;
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
  private final Application application = ApplicationProvider.getApplicationContext();

  @Test
  public void canCreateBluetoothDeviceViaNewInstance() throws Exception {
    // This test passes as long as no Exception is thrown. It tests if the constructor can be
    // executed without throwing an Exception when getService() is called inside.
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    assertThat(bluetoothDevice).isNotNull();
  }

  @Test
  public void canSetAndGetUuids() throws Exception {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
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
  public void getUuids_setUuidsNotCalled_shouldReturnNull() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);
    assertThat(device.getUuids()).isNull();
  }

  @Test
  public void canSetAndGetBondState() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(device.getBondState()).isEqualTo(BOND_NONE);

    shadowOf(device).setBondState(BOND_BONDED);
    assertThat(device.getBondState()).isEqualTo(BOND_BONDED);
  }

  @Test
  public void canSetAndGetCreatedBond() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(device.createBond()).isFalse();

    shadowOf(device).setCreatedBond(true);
    assertThat(device.createBond()).isTrue();
  }

  @Test
  public void canSetAndGetPin() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
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
  public void canSetAndGetFetchUuidsWithSdpResult() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);
    assertThat(device.fetchUuidsWithSdp()).isFalse();

    shadowOf(device).setFetchUuidsWithSdpResult(true);
    assertThat(device.fetchUuidsWithSdp()).isTrue();
  }

  @Test
  public void canSetAndGetBluetoothClass() throws Exception {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(shadowOf(device).getBluetoothClass()).isNull();

    BluetoothClass bluetoothClass =
        BluetoothClass.class.getConstructor(int.class).newInstance(AUDIO_VIDEO_HEADPHONES);
    shadowOf(device).setBluetoothClass(bluetoothClass);
    assertThat(shadowOf(device).getBluetoothClass()).isEqualTo(bluetoothClass);
  }

  @Test
  public void canCreateAndRemoveBonds() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(device.createBond()).isFalse();

    shadowOf(device).setCreatedBond(true);
    assertThat(shadowOf(device).removeBond()).isTrue();
    assertThat(device.createBond()).isFalse();
    assertThat(shadowOf(device).removeBond()).isFalse();
  }

  @Test
  public void getCorrectFetchUuidsWithSdpCount() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);
    assertThat(shadowOf(device).getFetchUuidsWithSdpCount()).isEqualTo(0);

    device.fetchUuidsWithSdp();
    assertThat(shadowOf(device).getFetchUuidsWithSdpCount()).isEqualTo(1);

    device.fetchUuidsWithSdp();
    assertThat(shadowOf(device).getFetchUuidsWithSdpCount()).isEqualTo(2);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void connectGatt_doesntCrash() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    assertThat(
            bluetoothDevice.connectGatt(
                ApplicationProvider.getApplicationContext(), false, new BluetoothGattCallback() {}))
        .isNotNull();
  }

  @Test
  @Config(minSdk = M)
  public void connectGatt_withTransport_doesntCrash() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
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
  public void connectGatt_withTransportPhy_doesntCrash() {
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
  public void connectGatt_withTransportPhyHandler_doesntCrash() {
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
  public void canSetAndGetType() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    shadowOf(device).setType(DEVICE_TYPE_CLASSIC);
    assertThat(device.getType()).isEqualTo(DEVICE_TYPE_CLASSIC);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void canGetBluetoothGatts() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
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
  public void connectGatt_setsBluetoothGattCallback() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    BluetoothGattCallback callback = new BluetoothGattCallback() {};

    BluetoothGatt bluetoothGatt =
        device.connectGatt(ApplicationProvider.getApplicationContext(), false, callback);

    assertThat(shadowOf(bluetoothGatt).getGattCallback())
        .isEqualTo(callback);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void canSimulateGattConnectionChange() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
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
  @Config(maxSdk = VERSION_CODES.R)
  public void getSetAlias() {
    String aliasName = "alias";
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    shadowOf(device).setAlias(aliasName);

    // getAlias is accessed by reflection
    try {
      Method getAliasName = android.bluetooth.BluetoothDevice.class.getMethod("getAlias");
      assertThat((String) getAliasName.invoke(device)).isEqualTo(aliasName);
    } catch (ReflectiveOperationException e) {
      throw new LinkageError("Failure accessing getAlias via reflection", e);
    }
  }

  @Test
  @Config(maxSdk = Q)
  public void getAliasName() {
    String aliasName = "alias";
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    shadowOf(device).setAlias(aliasName);

    // getAliasName is accessed by reflection
    try {
      Method getAliasName = android.bluetooth.BluetoothDevice.class.getMethod("getAliasName");
      assertThat((String) getAliasName.invoke(device)).isEqualTo(aliasName);
    } catch (ReflectiveOperationException e) {
      throw new LinkageError("Failure accessing getAliasName via reflection", e);
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void getAliasName_noBluetoothConnectPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    String aliasName = "alias";
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    ShadowBluetoothDevice shadowDevice = shadowOf(device);
    shadowDevice.setAlias(aliasName);
    shadowDevice.setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, device::getAlias);
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void getUuids_noBluetoothConnectPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    ShadowBluetoothDevice shadowDevice = shadowOf(device);
    shadowDevice.setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, device::getUuids);
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void getName_noBluetoothConnectPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    ShadowBluetoothDevice shadowDevice = shadowOf(device);
    shadowDevice.setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, device::getName);
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void getType_noBluetoothConnectPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    ShadowBluetoothDevice shadowDevice = shadowOf(device);
    shadowDevice.setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, device::getType);
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void fetchUuidsWithSdp_noBluetoothConnectPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    ShadowBluetoothDevice shadowDevice = shadowOf(device);
    shadowDevice.setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, device::fetchUuidsWithSdp);
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void getBluetoothClass_noBluetoothConnectPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    ShadowBluetoothDevice shadowDevice = shadowOf(device);
    shadowDevice.setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, device::getBluetoothClass);
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void getBondState_noBluetoothConnectPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    ShadowBluetoothDevice shadowDevice = shadowOf(device);
    shadowDevice.setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, device::getBondState);
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void getPin_noBluetoothConnectPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    ShadowBluetoothDevice shadowDevice = shadowOf(device);
    shadowDevice.setShouldThrowSecurityExceptions(true);

    byte[] pin = new byte[] {};
    assertThrows(SecurityException.class, () -> device.setPin(pin));
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void createBond_noBluetoothConnectPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    ShadowBluetoothDevice shadowDevice = shadowOf(device);
    shadowDevice.setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, device::createBond);
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void createInsecureL2capChannel_noBluetoothConnectPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    ShadowBluetoothDevice shadowDevice = shadowOf(device);
    shadowDevice.setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, () -> device.createInsecureL2capChannel(0));
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void createL2capChannel_noBluetoothConnectPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    ShadowBluetoothDevice shadowDevice = shadowOf(device);
    shadowDevice.setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, () -> device.createL2capChannel(0));
  }

  @Test
  public void getAliasName_hasPermission_noExceptionThrown() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    String aliasName = "alias";
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    ShadowBluetoothDevice shadowDevice = shadowOf(device);
    shadowDevice.setAlias(aliasName);
    shadowDevice.setShouldThrowSecurityExceptions(true);

    String retrievedAlias = device.getAlias();
    assertThat(retrievedAlias).isNotNull();
  }

  @Test
  public void
      getAliasName_noBluetoothConnectPermission_shouldThrowSecurityExceptionsFalse_noExceptionThrown() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    String aliasName = "alias";
    BluetoothDevice device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    ShadowBluetoothDevice shadowDevice = shadowOf(device);
    shadowDevice.setAlias(aliasName);
    shadowDevice.setShouldThrowSecurityExceptions(false);

    String retrievedAlias = device.getAlias();
    assertThat(retrievedAlias).isNotNull();
  }

  @Test
  @Config(maxSdk = Q)
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
      throw new LinkageError("Failure accessing getAliasName via reflection", e);
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void getMetadata_noPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);
    shadowOf(device).setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, () -> device.getMetadata(22));
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void setMetadata_noPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);
    shadowOf(device).setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, () -> device.setMetadata(22, new byte[] {123}));
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void setMetadata_shouldSaveToMap() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(device.setMetadata(22, new byte[] {123})).isTrue();
    assertThat(device.getMetadata(22)).isEqualTo(new byte[] {123});
    assertThat(device.getMetadata(11)).isNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void getBatteryLevel_noPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);
    shadowOf(device).setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, device::getBatteryLevel);
  }

  @Test
  @Config(minSdk = VERSION_CODES.O_MR1)
  public void getBatteryLevel_default() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(device.getBatteryLevel()).isEqualTo(BluetoothDevice.BATTERY_LEVEL_BLUETOOTH_OFF);
  }

  @Test
  @Config(minSdk = VERSION_CODES.O_MR1)
  public void setBatteryLevel_shouldBeSaved() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    shadowOf(device).setBatteryLevel(66);

    assertThat(device.getBatteryLevel()).isEqualTo(66);
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void isInSilenceMode_noPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);
    shadowOf(device).setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, device::isInSilenceMode);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void isInSilenceMode_defaultFalse() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(device.isInSilenceMode()).isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void setSilenceMode_noPermission_throwsException() {
    shadowOf(application).denyPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);
    shadowOf(device).setShouldThrowSecurityExceptions(true);

    assertThrows(SecurityException.class, () -> device.setSilenceMode(true));
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void setSilenceMode_shouldBeSaved() {
    shadowOf(application).grantPermissions(BLUETOOTH_CONNECT);
    BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(MOCK_MAC_ADDRESS);

    assertThat(shadowOf(device).setSilenceMode(true)).isTrue();

    assertThat(device.isInSilenceMode()).isTrue();
  }
}
