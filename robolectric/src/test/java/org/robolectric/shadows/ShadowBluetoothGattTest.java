package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowBluetoothGatt}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = JELLY_BEAN_MR2)
public class ShadowBluetoothGattTest {

  private static final byte[] CHARACTERISTIC_VALUE = new byte[] {'a', 'b', 'c'};
  private static final int INITIAL_VALUE = -99;
  private static final String MOCK_MAC_ADDRESS = "00:11:22:33:AA:BB";
  private static final String ACTION_CONNECTION = "CONNECT/DISCONNECT";
  private static final String ACTION_DISCOVER = "DISCOVER";
  private static final String ACTION_READ = "READ";
  private static final String ACTION_WRITE = "WRITE";
  private static final String REMOTE_ADDRESS = "R-A";

  private int resultStatus = INITIAL_VALUE;
  private int resultState = INITIAL_VALUE;
  private String resultAction;
  private BluetoothGattCharacteristic resultCharacteristic;
  private BluetoothGattDescriptor resultDescriptor;
  private BluetoothGatt bluetoothGatt;

  private static final BluetoothGattService service1 =
      new BluetoothGattService(
          UUID.fromString("00000000-0000-0000-0000-0000000000A1"),
          BluetoothGattService.SERVICE_TYPE_PRIMARY);
  private static final BluetoothGattService service2 =
      new BluetoothGattService(
          UUID.fromString("00000000-0000-0000-0000-0000000000A2"),
          BluetoothGattService.SERVICE_TYPE_SECONDARY);

  private final BluetoothGattCallback callback =
      new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
          resultStatus = status;
          resultState = newState;
          resultAction = ACTION_CONNECTION;
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
          resultStatus = status;
          resultAction = ACTION_DISCOVER;
        }

        @Override
        public void onCharacteristicRead(
            BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
          resultStatus = status;
          resultCharacteristic = characteristic;
          resultAction = ACTION_READ;
        }

        @Override
        public void onCharacteristicWrite(
            BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
          resultStatus = status;
          resultCharacteristic = characteristic;
          resultAction = ACTION_WRITE;
        }

        @Override
        public void onDescriptorWrite(
            BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
          resultStatus = status;
          resultDescriptor = descriptor;
          resultAction = ACTION_WRITE;
        }
      };

  private final BluetoothGattCharacteristic characteristicWithReadProperty =
      new BluetoothGattCharacteristic(
          UUID.fromString("00000000-0000-0000-0000-0000000000A3"),
          BluetoothGattCharacteristic.PROPERTY_READ,
          BluetoothGattCharacteristic.PERMISSION_READ);

  private final BluetoothGattCharacteristic characteristicWithWriteProperties =
      new BluetoothGattCharacteristic(
          UUID.fromString("00000000-0000-0000-0000-0000000000A4"),
          BluetoothGattCharacteristic.PROPERTY_WRITE
              | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
          BluetoothGattCharacteristic.PERMISSION_WRITE);

  private final BluetoothGattDescriptor descriptor =
      new BluetoothGattDescriptor(
          UUID.fromString("00000000-0000-0000-0000-0000000000A5"),
          BluetoothGattDescriptor.PERMISSION_WRITE);

  @Before
  public void setUp() throws Exception {
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    bluetoothGatt = ShadowBluetoothGatt.newInstance(bluetoothDevice);
  }

  @After
  public void tearDown() {
    shadowOf(bluetoothGatt).getBluetoothConnectionManager().resetConnections();
  }

  @Test
  public void canCreateBluetoothGattViaNewInstance() {
    assertThat(bluetoothGatt).isNotNull();
  }

  @Test
  public void canSetAndGetGattCallback() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    assertThat(shadowOf(bluetoothGatt).getGattCallback()).isEqualTo(callback);
  }

  @Test
  public void isNotConnected_beforeConnect() {
    assertThat(shadowOf(bluetoothGatt).isConnected()).isFalse();
    assertThat(resultStatus).isEqualTo(INITIAL_VALUE);
    assertThat(resultState).isEqualTo(INITIAL_VALUE);
    assertThat(resultAction).isNull();
  }

  @Test
  public void isConnected_returnsFalseWithoutCallback() {
    assertThat(bluetoothGatt.connect()).isFalse();
    assertThat(shadowOf(bluetoothGatt).isConnected()).isFalse();
  }

  @Test
  public void isConnected_afterConnect() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    assertThat(bluetoothGatt.connect()).isTrue();
    assertThat(shadowOf(bluetoothGatt).isConnected()).isTrue();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultState).isEqualTo(BluetoothProfile.STATE_CONNECTED);
    assertThat(resultAction).isEqualTo(ACTION_CONNECTION);
  }

  @Test
  public void isConnected_afterConnectAndDisconnect() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    bluetoothGatt.connect();
    bluetoothGatt.disconnect();
    assertThat(shadowOf(bluetoothGatt).isConnected()).isFalse();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultState).isEqualTo(BluetoothProfile.STATE_DISCONNECTED);
    assertThat(resultAction).isEqualTo(ACTION_CONNECTION);
  }

  @Test
  public void isNotConnected_afterOnlyDisconnect() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    bluetoothGatt.disconnect();
    assertThat(shadowOf(bluetoothGatt).isConnected()).isFalse();
    assertThat(resultStatus).isEqualTo(INITIAL_VALUE);
    assertThat(resultState).isEqualTo(INITIAL_VALUE);
    assertThat(resultAction).isNull();
  }

  @Test
  public void isNotConnected_afterConnectAndDisconnectWithoutCallback() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    bluetoothGatt.connect();
    shadowOf(bluetoothGatt).setGattCallback(null);
    bluetoothGatt.disconnect();
    assertThat(shadowOf(bluetoothGatt).isConnected()).isFalse();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultState).isEqualTo(BluetoothProfile.STATE_CONNECTED);
  }

  @Test
  public void isNotClosedbeforeClose() {
    assertThat(shadowOf(bluetoothGatt).isClosed()).isFalse();
  }

  @Test
  public void isClosedafterClose() {
    bluetoothGatt.close();
    assertThat(shadowOf(bluetoothGatt).isClosed()).isTrue();
  }

  @Test
  public void isDisconnected_afterClose() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    bluetoothGatt.connect();
    bluetoothGatt.close();
    assertThat(shadowOf(bluetoothGatt).isConnected()).isFalse();
  }

  @Test
  @Config(minSdk = O)
  public void getConnectionPriority_atInitiation() {
    assertThat(shadowOf(bluetoothGatt).getConnectionPriority())
        .isEqualTo(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
  }

  @Test
  @Config(minSdk = O)
  public void requestConnectionPriority_inRange() {
    boolean res =
        bluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER);
    assertThat(shadowOf(bluetoothGatt).getConnectionPriority())
        .isEqualTo(BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER);
    assertThat(res).isTrue();
    res = bluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
    assertThat(shadowOf(bluetoothGatt).getConnectionPriority())
        .isEqualTo(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);
    assertThat(res).isTrue();
  }

  @Test
  @Config(minSdk = O)
  public void requestConnectionPriority_notInRange_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> bluetoothGatt.requestConnectionPriority(-9));
    assertThrows(IllegalArgumentException.class, () -> bluetoothGatt.requestConnectionPriority(9));
  }

  @Test
  @Config(minSdk = O)
  public void discoverServices_noDiscoverableServices_returnsFalse() {
    assertThat(bluetoothGatt.discoverServices()).isFalse();
    assertThat(bluetoothGatt.getServices()).isEmpty();
  }

  @Test
  @Config(minSdk = O)
  public void getServices_afterAddService() {
    shadowOf(bluetoothGatt).addDiscoverableService(service1);
    assertThat(bluetoothGatt.discoverServices()).isFalse();
    assertThat(bluetoothGatt.getServices()).hasSize(1);
  }

  @Test
  @Config(minSdk = O)
  public void getServices_afterAddMultipleService() {
    shadowOf(bluetoothGatt).addDiscoverableService(service1);
    shadowOf(bluetoothGatt).addDiscoverableService(service2);
    assertThat(bluetoothGatt.discoverServices()).isFalse();
    assertThat(bluetoothGatt.getServices()).hasSize(2);
  }

  @Test
  @Config(minSdk = O)
  public void getServices_noDiscoverableServices_withCallback() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    assertThat(bluetoothGatt.discoverServices()).isFalse();
    assertThat(bluetoothGatt.getServices()).isEmpty();
    assertThat(resultStatus).isEqualTo(INITIAL_VALUE);
    assertThat(resultAction).isNull();
  }

  @Test
  @Config(minSdk = O)
  public void getServices_afterAddService_withCallback() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    shadowOf(bluetoothGatt).addDiscoverableService(service1);
    assertThat(bluetoothGatt.discoverServices()).isTrue();
    assertThat(bluetoothGatt.getServices()).hasSize(1);
    assertThat(bluetoothGatt.getServices()).contains(service1);
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultAction).isEqualTo(ACTION_DISCOVER);
  }

  @Test
  @Config(minSdk = O)
  public void getServices_afterAddMultipleService_withCallback() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    shadowOf(bluetoothGatt).addDiscoverableService(service1);
    shadowOf(bluetoothGatt).addDiscoverableService(service2);
    assertThat(bluetoothGatt.discoverServices()).isTrue();
    assertThat(bluetoothGatt.getServices()).hasSize(2);
    assertThat(bluetoothGatt.getServices()).contains(service1);
    assertThat(bluetoothGatt.getServices()).contains(service2);
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultAction).isEqualTo(ACTION_DISCOVER);
  }

  @Test
  @Config(minSdk = O)
  public void getService_afterAddService() {
    shadowOf(bluetoothGatt).addDiscoverableService(service1);
    assertThat(bluetoothGatt.discoverServices()).isFalse();
    assertThat(bluetoothGatt.getService(service1.getUuid())).isEqualTo(service1);
    assertThat(bluetoothGatt.getService(service2.getUuid())).isNull();
  }

  @Test
  @Config(minSdk = O)
  public void discoverServices_clearsService() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    shadowOf(bluetoothGatt).addDiscoverableService(service1);
    shadowOf(bluetoothGatt).removeDiscoverableService(service1);
    shadowOf(bluetoothGatt).removeDiscoverableService(service2);
    assertThat(bluetoothGatt.discoverServices()).isFalse();
    assertThat(bluetoothGatt.getServices()).isEmpty();
  }

  @Test
  @Config(minSdk = O)
  public void writeDescriptor() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    service1.addCharacteristic(characteristicWithReadProperty);
    characteristicWithReadProperty.addDescriptor(descriptor);
    assertThat(descriptor.getCharacteristic().getService()).isNotNull();

    descriptor.setValue(CHARACTERISTIC_VALUE);
    assertThat(shadowOf(bluetoothGatt).writeDescriptor(descriptor)).isTrue();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultAction).isEqualTo(ACTION_WRITE);
    assertThat(resultDescriptor).isEqualTo(descriptor);
    assertThat(shadowOf(bluetoothGatt).getLatestWrittenBytes()).isEqualTo(CHARACTERISTIC_VALUE);
  }

  @Test
  @Config(minSdk = O)
  public void writeDetachedDescriptor() {
    shadowOf(bluetoothGatt).setGattCallback(callback);

    descriptor.setValue(CHARACTERISTIC_VALUE);
    assertThat(shadowOf(bluetoothGatt).writeDescriptor(descriptor)).isFalse();
    assertThat(resultStatus).isEqualTo(INITIAL_VALUE);
    assertThat(resultAction).isNull();
    assertThat(resultDescriptor).isNull();
    assertThat(shadowOf(bluetoothGatt).getLatestWrittenBytes()).isNull();
  }

  @Test
  @Config
  public void readIncomingCharacteristic_withoutCallback() {
    assertThrows(
        IllegalStateException.class,
        () -> shadowOf(bluetoothGatt).readIncomingCharacteristic(characteristicWithReadProperty));
  }

  @Test
  @Config
  public void readIncomingCharacteristic_withCallback() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    assertThat(shadowOf(bluetoothGatt).readIncomingCharacteristic(characteristicWithReadProperty))
        .isFalse();
    assertThat(resultStatus).isEqualTo(INITIAL_VALUE);
    assertThat(resultAction).isNull();
    assertThat(resultCharacteristic).isNull();
    assertThat(shadowOf(bluetoothGatt).getLatestReadBytes()).isNull();
  }

  @Test
  @Config
  public void readIncomingCharacteristic_withCallbackAndServiceSet() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    service1.addCharacteristic(characteristicWithReadProperty);
    assertThat(characteristicWithReadProperty.getService()).isNotNull();
    assertThat(shadowOf(bluetoothGatt).readIncomingCharacteristic(characteristicWithReadProperty))
        .isTrue();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultAction).isEqualTo(ACTION_READ);
    assertThat(resultCharacteristic).isEqualTo(characteristicWithReadProperty);
    assertThat(shadowOf(bluetoothGatt).getLatestReadBytes()).isNull();
  }

  @Test
  @Config
  public void readIncomingCharacteristic_withCallbackAndServiceSet_withValue() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    service1.addCharacteristic(characteristicWithReadProperty);
    assertThat(characteristicWithReadProperty.getService()).isNotNull();
    characteristicWithReadProperty.setValue(CHARACTERISTIC_VALUE);
    assertThat(shadowOf(bluetoothGatt).readIncomingCharacteristic(characteristicWithReadProperty))
        .isTrue();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultAction).isEqualTo(ACTION_READ);
    assertThat(resultCharacteristic).isEqualTo(characteristicWithReadProperty);
    assertThat(shadowOf(bluetoothGatt).getLatestReadBytes()).isEqualTo(CHARACTERISTIC_VALUE);
  }

  @Test
  @Config
  public void readIncomingCharacteristic_withCallbackAndServiceSet_wrongProperty() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    service1.addCharacteristic(characteristicWithWriteProperties);
    assertThat(characteristicWithWriteProperties.getService()).isNotNull();
    assertThat(
            shadowOf(bluetoothGatt).readIncomingCharacteristic(characteristicWithWriteProperties))
        .isFalse();
    assertThat(resultStatus).isEqualTo(INITIAL_VALUE);
    assertThat(resultAction).isNull();
    assertThat(resultCharacteristic).isNull();
    assertThat(shadowOf(bluetoothGatt).getLatestReadBytes()).isNull();
  }

  @Test
  @Config
  public void writeIncomingCharacteristic_withoutCallback() {
    service1.addCharacteristic(characteristicWithWriteProperties);
    assertThrows(
        IllegalStateException.class,
        () ->
            shadowOf(bluetoothGatt).writeIncomingCharacteristic(characteristicWithWriteProperties));
  }

  @Test
  @Config
  public void writeIncomingCharacteristic_withCallbackOnly() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    assertThat(
            shadowOf(bluetoothGatt).writeIncomingCharacteristic(characteristicWithWriteProperties))
        .isFalse();
    assertThat(resultStatus).isEqualTo(INITIAL_VALUE);
    assertThat(resultAction).isNull();
    assertThat(resultCharacteristic).isNull();
    assertThat(shadowOf(bluetoothGatt).getLatestWrittenBytes()).isNull();
  }

  @Test
  @Config
  public void writeIncomingCharacteristic_withCallbackAndServiceSet() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    service2.addCharacteristic(characteristicWithWriteProperties);
    assertThat(characteristicWithWriteProperties.getService()).isNotNull();
    assertThat(
            shadowOf(bluetoothGatt).writeIncomingCharacteristic(characteristicWithWriteProperties))
        .isTrue();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultAction).isEqualTo(ACTION_WRITE);
    assertThat(resultCharacteristic).isEqualTo(characteristicWithWriteProperties);
    assertThat(shadowOf(bluetoothGatt).getLatestWrittenBytes()).isNull();
  }

  @Test
  @Config
  public void writeIncomingCharacteristic_withCallbackAndServiceSet_wrongProperty() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    service1.addCharacteristic(characteristicWithReadProperty);
    assertThat(characteristicWithReadProperty.getService()).isNotNull();
    assertThat(shadowOf(bluetoothGatt).writeIncomingCharacteristic(characteristicWithReadProperty))
        .isFalse();
    assertThat(resultStatus).isEqualTo(INITIAL_VALUE);
    assertThat(resultAction).isNull();
    assertThat(resultCharacteristic).isNull();
    assertThat(shadowOf(bluetoothGatt).getLatestWrittenBytes()).isNull();
  }

  @Test
  @Config
  public void writeIncomingCharacteristic_correctlySetup_noValue() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    service1.addCharacteristic(characteristicWithWriteProperties);
    assertThat(characteristicWithWriteProperties.getService()).isNotNull();
    assertThat(
            shadowOf(bluetoothGatt).writeIncomingCharacteristic(characteristicWithWriteProperties))
        .isTrue();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultAction).isEqualTo(ACTION_WRITE);
    assertThat(resultCharacteristic).isEqualTo(characteristicWithWriteProperties);
    assertThat(shadowOf(bluetoothGatt).getLatestWrittenBytes()).isNull();
  }

  @Test
  @Config
  public void writeIncomingCharacteristic_correctlySetup_withValue() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    service1.addCharacteristic(characteristicWithWriteProperties);
    characteristicWithWriteProperties.setValue(CHARACTERISTIC_VALUE);
    assertThat(characteristicWithWriteProperties.getService()).isNotNull();
    assertThat(
            shadowOf(bluetoothGatt).writeIncomingCharacteristic(characteristicWithWriteProperties))
        .isTrue();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultAction).isEqualTo(ACTION_WRITE);
    assertThat(resultCharacteristic).isEqualTo(characteristicWithWriteProperties);

    assertThat(shadowOf(bluetoothGatt).getLatestWrittenBytes()).isEqualTo(CHARACTERISTIC_VALUE);
  }

  @Test
  @Config
  public void writeIncomingCharacteristic_correctlySetup_onlyWriteProperty() {

    BluetoothGattCharacteristic characteristic =
        new BluetoothGattCharacteristic(
            UUID.fromString("00000000-0000-0000-0000-0000000000A6"),
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE);

    shadowOf(bluetoothGatt).setGattCallback(callback);
    service1.addCharacteristic(characteristic);
    characteristic.setValue(CHARACTERISTIC_VALUE);
    assertThat(shadowOf(bluetoothGatt).writeIncomingCharacteristic(characteristic)).isTrue();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultAction).isEqualTo(ACTION_WRITE);
    assertThat(resultCharacteristic).isEqualTo(characteristic);
    assertThat(shadowOf(bluetoothGatt).getLatestWrittenBytes()).isEqualTo(CHARACTERISTIC_VALUE);
  }

  @Test
  @Config
  public void writeIncomingCharacteristic_correctlySetup_onlyWriteNoResponseProperty() {

    BluetoothGattCharacteristic characteristic =
        new BluetoothGattCharacteristic(
            UUID.fromString("00000000-0000-0000-0000-0000000000A7"),
            BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE);

    shadowOf(bluetoothGatt).setGattCallback(callback);
    service1.addCharacteristic(characteristic);
    characteristic.setValue(CHARACTERISTIC_VALUE);
    assertThat(shadowOf(bluetoothGatt).writeIncomingCharacteristic(characteristic)).isTrue();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultAction).isEqualTo(ACTION_WRITE);
    assertThat(resultCharacteristic).isEqualTo(characteristic);
    assertThat(shadowOf(bluetoothGatt).getLatestWrittenBytes()).isEqualTo(CHARACTERISTIC_VALUE);
  }

  @Test
  public void test_getBluetoothConnectionManager() {
    assertThat(shadowOf(bluetoothGatt).getBluetoothConnectionManager()).isNotNull();
  }

  @Test
  public void test_notifyConnection_connects() {
    shadowOf(bluetoothGatt).notifyConnection(REMOTE_ADDRESS);
    assertThat(shadowOf(bluetoothGatt).isConnected()).isTrue();
    assertThat(
            shadowOf(bluetoothGatt)
                .getBluetoothConnectionManager()
                .hasGattClientConnection(REMOTE_ADDRESS))
        .isTrue();
    assertThat(resultStatus).isEqualTo(INITIAL_VALUE);
    assertThat(resultState).isEqualTo(INITIAL_VALUE);
    assertThat(resultAction).isNull();
  }

  @Test
  public void test_notifyConnection_connectsWithCallbackSet() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    shadowOf(bluetoothGatt).notifyConnection(REMOTE_ADDRESS);
    assertThat(shadowOf(bluetoothGatt).isConnected()).isTrue();
    assertThat(
            shadowOf(bluetoothGatt)
                .getBluetoothConnectionManager()
                .hasGattClientConnection(REMOTE_ADDRESS))
        .isTrue();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultState).isEqualTo(BluetoothProfile.STATE_CONNECTED);
    assertThat(resultAction).isEqualTo(ACTION_CONNECTION);
  }

  @Test
  public void test_notifyDisconnection_disconnects() {
    shadowOf(bluetoothGatt).notifyDisconnection(REMOTE_ADDRESS);
    assertThat(shadowOf(bluetoothGatt).isConnected()).isFalse();
    assertThat(
            shadowOf(bluetoothGatt)
                .getBluetoothConnectionManager()
                .hasGattClientConnection(REMOTE_ADDRESS))
        .isFalse();
    assertThat(resultStatus).isEqualTo(INITIAL_VALUE);
    assertThat(resultState).isEqualTo(INITIAL_VALUE);
    assertThat(resultAction).isNull();
  }

  @Test
  public void test_notifyDisconnection_disconnectsWithCallbackSet() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    shadowOf(bluetoothGatt).notifyDisconnection(REMOTE_ADDRESS);
    assertThat(shadowOf(bluetoothGatt).isConnected()).isFalse();
    assertThat(
            shadowOf(bluetoothGatt)
                .getBluetoothConnectionManager()
                .hasGattClientConnection(REMOTE_ADDRESS))
        .isFalse();
    assertThat(resultStatus).isEqualTo(INITIAL_VALUE);
    assertThat(resultState).isEqualTo(INITIAL_VALUE);
    assertThat(resultAction).isNull();
  }

  @Test
  public void test_notifyDisconnection_disconnectsWithCallbackSet_connectedInitially() {
    shadowOf(bluetoothGatt).setGattCallback(callback);
    shadowOf(bluetoothGatt).notifyConnection(REMOTE_ADDRESS);
    shadowOf(bluetoothGatt).notifyDisconnection(REMOTE_ADDRESS);
    assertThat(
            shadowOf(bluetoothGatt)
                .getBluetoothConnectionManager()
                .hasGattClientConnection(REMOTE_ADDRESS))
        .isFalse();
    assertThat(shadowOf(bluetoothGatt).isConnected()).isFalse();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultState).isEqualTo(BluetoothProfile.STATE_DISCONNECTED);
    assertThat(resultAction).isEqualTo(ACTION_CONNECTION);
  }

  @Test
  @Config(minSdk = O)
  public void allowCharacteristicNotification_canSetNotification() {
    service1.addCharacteristic(characteristicWithReadProperty);
    shadowOf(bluetoothGatt).addDiscoverableService(service1);
    shadowOf(bluetoothGatt).allowCharacteristicNotification(characteristicWithReadProperty);
    assertThat(bluetoothGatt.setCharacteristicNotification(characteristicWithReadProperty, true))
        .isTrue();
  }

  @Test
  @Config(minSdk = O)
  public void disallowCharacteristicNotification_cannotSetNotification() {
    service1.addCharacteristic(characteristicWithReadProperty);
    shadowOf(bluetoothGatt).addDiscoverableService(service1);
    shadowOf(bluetoothGatt).disallowCharacteristicNotification(characteristicWithReadProperty);
    assertThat(bluetoothGatt.setCharacteristicNotification(characteristicWithReadProperty, true))
        .isFalse();
  }
}
