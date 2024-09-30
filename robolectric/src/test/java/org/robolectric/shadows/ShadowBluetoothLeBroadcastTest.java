package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import android.bluetooth.BluetoothLeBroadcast;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = 33)
public class ShadowBluetoothLeBroadcastTest {
  private BluetoothLeBroadcast bluetoothLeBroadcast;
  private ShadowBluetoothLeBroadcast shadowBluetoothLeBroadcast;

  @Before
  public void setUp() throws Exception {
    bluetoothLeBroadcast = Shadow.newInstanceOf(BluetoothLeBroadcast.class);
    shadowBluetoothLeBroadcast = Shadow.extract(bluetoothLeBroadcast);
  }

  @Test
  public void testRegisterCallback() {
    Executor executor = Executors.newSingleThreadExecutor();
    BluetoothLeBroadcast.Callback callback = mock(BluetoothLeBroadcast.Callback.class);
    shadowBluetoothLeBroadcast.registerCallback(executor, callback);
    assertThat(shadowBluetoothLeBroadcast.getCallbackExecutorMap()).containsKey(callback);
  }

  @Test
  public void testGetCallbackExecutorMap_returnEmptyMap() {
    assertThat(shadowBluetoothLeBroadcast.getCallbackExecutorMap()).isEmpty();
  }
}
