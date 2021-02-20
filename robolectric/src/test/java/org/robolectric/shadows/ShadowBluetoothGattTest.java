package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowBluetoothGatt}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = JELLY_BEAN_MR2)
public class ShadowBluetoothGattTest {

  private static final String MOCK_MAC_ADDRESS = "00:11:22:33:AA:BB";

  @Test
  public void canCreateBluetoothGattViaNewInstance() {
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    BluetoothGatt bluetoothGatt = ShadowBluetoothGatt.newInstance(bluetoothDevice);
    assertThat(bluetoothGatt).isNotNull();
  }

  @Test
  public void canSetAndGetGattCallback() {
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    BluetoothGatt bluetoothGatt = ShadowBluetoothGatt.newInstance(bluetoothDevice);
    BluetoothGattCallback callback = new BluetoothGattCallback() {};

    shadowOf(bluetoothGatt).setGattCallback(callback);

    assertThat(shadowOf(bluetoothGatt).getGattCallback()).isEqualTo(callback);
  }

  @Config(minSdk = JELLY_BEAN_MR2)
  public void connect_returnsTrue() {
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS);
    BluetoothGatt bluetoothGatt = ShadowBluetoothGatt.newInstance(bluetoothDevice);
    assertThat(bluetoothGatt.connect()).isTrue();
  }
}
