
package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.bluetooth.BluetoothA2dp;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowBluetoothA2dpTest {
  private static final String MOCK_MAC_ADDRESS = "00:11:22:33:AA:BB";

  @Test
  public void getConnectionState_isA2dp() throws Exception {
    BluetoothA2dp bluetoothA2dp = Shadow.newInstanceOf(BluetoothA2dp.class);

    assertThat(
            bluetoothA2dp.getConnectionState(ShadowBluetoothDevice.newInstance(MOCK_MAC_ADDRESS)))
        .isNotNull();
  }
}
