package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.bluetooth.BluetoothDevice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
public class ShadowBluetoothDeviceTest {

  private static final String MOCK_MAC_ADDRESS = "00:11:22:33:AA:BB";

  @Test
  public void canCreateBluetoothDeviceViaReflection() throws Exception {
    // This test passes as long as no Exception is thrown. It tests if the constructor can be
    // executed without throwing an Exception when getService() is called inside.
    BluetoothDevice bluetoothDevice =
        ReflectionHelpers.callConstructor(
            BluetoothDevice.class,
            ReflectionHelpers.ClassParameter.from(String.class, MOCK_MAC_ADDRESS));
    assertThat(bluetoothDevice).isNotNull();
  }
}
