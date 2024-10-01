package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import android.bluetooth.BluetoothLeBroadcast;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = 33)
public class ShadowBluetoothLeBroadcastTest {
  private BluetoothLeBroadcast bluetoothLeBroadcast;
  private BluetoothLeBroadcast.Callback callback;
  private Executor executor;
  private ShadowBluetoothLeBroadcast shadowBluetoothLeBroadcast;

  @Before
  public void setUp() throws Exception {
    bluetoothLeBroadcast = Shadow.newInstanceOf(BluetoothLeBroadcast.class);
    callback = mock(BluetoothLeBroadcast.Callback.class);
    executor = Executors.newSingleThreadExecutor();
    shadowBluetoothLeBroadcast = Shadow.extract(bluetoothLeBroadcast);
  }

  @Test
  public void testRegisterCallback() {
    shadowBluetoothLeBroadcast.registerCallback(executor, callback);
    assertThat(shadowBluetoothLeBroadcast.getCallbackExecutorMap()).containsKey(callback);
  }

  @Test
  public void testUnregisterCallback() {
    shadowBluetoothLeBroadcast.registerCallback(executor, callback);
    assertThat(shadowBluetoothLeBroadcast.getCallbackExecutorMap()).containsKey(callback);
    shadowBluetoothLeBroadcast.unregisterCallback(callback);
    assertThat(shadowBluetoothLeBroadcast.getCallbackExecutorMap()).isEmpty();
  }

  @Test
  public void testGetCallbackExecutorMap_returnEmptyMap() {
    assertThat(shadowBluetoothLeBroadcast.getCallbackExecutorMap()).isEmpty();
  }
}
