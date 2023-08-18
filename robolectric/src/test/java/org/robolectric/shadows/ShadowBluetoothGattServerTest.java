package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowBluetoothGattServer}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public class ShadowBluetoothGattServerTest {

  private static final int INITIAL_VALUE = -99;
  private static final String ACTION_CONNECTION = "CONNECT/DISCONNECT";
  private static final String MOCK_MAC_ADDRESS = "00:11:22:33:AA:BB";
  private static final byte[] RESPONSE_VALUE1 = new byte[] {'a', 'b', 'c'};
  private static final byte[] RESPONSE_VALUE2 = new byte[] {'1', '2', '3'};

  private BluetoothManager manager;
  private Context context;
  private BluetoothGattServer server;
  private BluetoothDevice device;

  private int resultStatus = INITIAL_VALUE;
  private int resultState = INITIAL_VALUE;
  private String resultAction;

  private final BluetoothGattServerCallback callback =
      new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
          resultStatus = status;
          resultState = newState;
          resultAction = ACTION_CONNECTION;
        }
      };

  @Before
  @Config()
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    server = manager.openGattServer(context, new BluetoothGattServerCallback() {}, 0);
    device = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
  }

  @After
  public void tearDown() {
    shadowOf(server).getBluetoothConnectionManager().resetConnections();
  }

  @Test
  public void test_setAndGetGattServerCallback() {
    shadowOf(server).setGattServerCallback(callback);
    assertThat(shadowOf(server).getGattServerCallback()).isSameInstanceAs(callback);
  }

  @Test
  public void test_getConnectionManager() {
    assertThat(shadowOf(server).getBluetoothConnectionManager()).isNotNull();
  }

  @Test
  public void test_isNotClosed_initially() {
    assertThat(shadowOf(server).isClosed()).isFalse();
  }

  @Test
  public void test_isClosed_afterClose() {
    server.close();
    assertThat(shadowOf(server).isClosed()).isTrue();
  }

  @Test
  public void test_getResponses_initially() {
    assertThat(shadowOf(server).getResponses()).isEmpty();
  }

  @Test
  public void test_getResponses_afterSendResponses() {
    shadowOf(server).sendResponse(device, 0, 0, 0, RESPONSE_VALUE1);
    assertThat(shadowOf(server).getResponses()).hasSize(1);
    shadowOf(server).sendResponse(device, 0, 0, 0, RESPONSE_VALUE2);
    assertThat(shadowOf(server).getResponses()).hasSize(2);
    assertThat(shadowOf(server).getResponses().get(0)).isEqualTo(RESPONSE_VALUE1);
    assertThat(shadowOf(server).getResponses().get(1)).isEqualTo(RESPONSE_VALUE2);
  }

  @Test
  public void test_getResponses_afterClearResponses() {
    shadowOf(server).sendResponse(device, 0, 0, 0, RESPONSE_VALUE1);
    shadowOf(server).sendResponse(device, 0, 0, 0, RESPONSE_VALUE2);
    shadowOf(server).clearResponses();
    assertThat(shadowOf(server).getResponses()).isEmpty();
  }

  @Test
  public void test_getResponses_acceptsNull() {
    shadowOf(server).sendResponse(device, 0, 0, 0, RESPONSE_VALUE1);
    assertThat(shadowOf(server).getResponses()).hasSize(1);
    shadowOf(server).sendResponse(device, 0, 0, 0, null);
    assertThat(shadowOf(server).getResponses()).hasSize(2);
    assertThat(shadowOf(server).getResponses().get(0)).isEqualTo(RESPONSE_VALUE1);
    assertThat(shadowOf(server).getResponses().get(1)).isEqualTo(null);
  }

  @Test
  public void test_isConnectedToDevice_initially() {
    assertThat(shadowOf(server).isConnectedToDevice(device)).isFalse();
  }

  @Test
  public void test_notifyConnection_withoutCallback() {
    shadowOf(server).setGattServerCallback(null);
    shadowOf(server).notifyConnection(device);
    assertThat(shadowOf(server).isConnectedToDevice(device)).isTrue();
    assertThat(resultStatus).isEqualTo(INITIAL_VALUE);
    assertThat(resultState).isEqualTo(INITIAL_VALUE);
    assertThat(resultAction).isNull();
  }

  @Test
  public void test_notifyConnection_withCallback() {
    shadowOf(server).setGattServerCallback(callback);
    shadowOf(server).notifyConnection(device);
    assertThat(shadowOf(server).isConnectedToDevice(device)).isTrue();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultState).isEqualTo(BluetoothAdapter.STATE_CONNECTED);
    assertThat(resultAction).isEqualTo(ACTION_CONNECTION);
  }

  @Test
  public void test_notifyDisconnection_withoutCallback() {
    shadowOf(server).setGattServerCallback(null);
    shadowOf(server).notifyConnection(device);
    shadowOf(server).notifyDisconnection(device);
    assertThat(shadowOf(server).isConnectionCancelled(device)).isTrue();
    assertThat(shadowOf(server).isConnectedToDevice(device)).isFalse();
    assertThat(resultStatus).isEqualTo(INITIAL_VALUE);
    assertThat(resultState).isEqualTo(INITIAL_VALUE);
    assertThat(resultAction).isNull();
  }

  @Test
  public void test_notifyDisconnection_withCallback() {
    shadowOf(server).setGattServerCallback(callback);
    shadowOf(server).notifyConnection(device);
    shadowOf(server).notifyDisconnection(device);
    assertThat(shadowOf(server).isConnectionCancelled(device)).isTrue();
    assertThat(shadowOf(server).isConnectedToDevice(device)).isFalse();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultState).isEqualTo(BluetoothAdapter.STATE_DISCONNECTED);
    assertThat(resultAction).isEqualTo(ACTION_CONNECTION);
  }

  @Test
  public void test_notifyDisconnection_withCallback_beforeConnection() {
    shadowOf(server).setGattServerCallback(callback);
    shadowOf(server).notifyDisconnection(device);
    assertThat(shadowOf(server).isConnectionCancelled(device)).isTrue();
    assertThat(shadowOf(server).isConnectedToDevice(device)).isFalse();
    assertThat(resultStatus).isEqualTo(BluetoothGatt.GATT_SUCCESS);
    assertThat(resultState).isEqualTo(BluetoothAdapter.STATE_DISCONNECTED);
    assertThat(resultAction).isEqualTo(ACTION_CONNECTION);
  }

  @Test
  public void test_isConnectionCancelled_afterCancelConnection() {
    server.cancelConnection(device);
    assertThat(shadowOf(server).isConnectionCancelled(device)).isTrue();
  }

  @Test
  public void test_isConnectionCancelled_afterCancelConnection_whileConnected() {
    server.connect(device, true);
    server.cancelConnection(device);
    assertThat(shadowOf(server).isConnectionCancelled(device)).isTrue();
  }

  @Test
  public void test_isConnectionCancelled_afterCancelConnection_aftereNotifyConnection() {
    shadowOf(server).setGattServerCallback(callback);
    shadowOf(server).notifyConnection(device);
    server.cancelConnection(device);
    assertThat(shadowOf(server).isConnectionCancelled(device)).isTrue();
  }

  @Test
  public void test_removeCancelledDevice_afterNotifyConnection() {
    shadowOf(server).setGattServerCallback(callback);
    shadowOf(server).notifyDisconnection(device);
    shadowOf(server).notifyConnection(device);
    assertThat(shadowOf(server).isConnectionCancelled(device)).isFalse();
  }
}
