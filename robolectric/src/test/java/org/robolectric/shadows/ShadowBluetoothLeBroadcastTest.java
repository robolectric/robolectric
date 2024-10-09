package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothLeAudioContentMetadata;
import android.bluetooth.BluetoothLeBroadcast;
import android.bluetooth.BluetoothLeBroadcastSettings;
import android.bluetooth.BluetoothLeBroadcastSubgroupSettings;
import android.bluetooth.BluetoothStatusCodes;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
public class ShadowBluetoothLeBroadcastTest {
  private ShadowLooper shadowMainLooper;
  private ShadowBluetoothLeBroadcast shadowBluetoothLeBroadcast;
  private ExecutorService executors;

  @Before
  public void setUp() {
    shadowMainLooper = shadowOf(Looper.getMainLooper());
    shadowBluetoothLeBroadcast = Shadow.extract(Shadow.newInstanceOf(BluetoothLeBroadcast.class));
    executors = Executors.newSingleThreadExecutor();
  }

  @Test
  public void testRegisterCallback_success() {
    BluetoothLeBroadcast.Callback callback = mock(BluetoothLeBroadcast.Callback.class);

    shadowBluetoothLeBroadcast.registerCallback(executors, callback);
    assertThat(shadowBluetoothLeBroadcast.getCallbackExecutorMap()).containsKey(callback);
  }

  @Test
  public void testRegisterCallback_nullPara() {
    assertThrows(
        NullPointerException.class, () -> shadowBluetoothLeBroadcast.registerCallback(null, null));
    assertThrows(
        NullPointerException.class,
        () -> shadowBluetoothLeBroadcast.registerCallback(executors, null));
    assertThrows(
        NullPointerException.class,
        () ->
            shadowBluetoothLeBroadcast.registerCallback(
                null, mock(BluetoothLeBroadcast.Callback.class)));
  }

  @Test
  public void testUnregisterCallback_success() {
    BluetoothLeBroadcast.Callback callback = mock(BluetoothLeBroadcast.Callback.class);

    shadowBluetoothLeBroadcast.registerCallback(executors, callback);
    assertThat(shadowBluetoothLeBroadcast.getCallbackExecutorMap()).containsKey(callback);
    shadowBluetoothLeBroadcast.unregisterCallback(callback);
    assertThat(shadowBluetoothLeBroadcast.getCallbackExecutorMap()).isEmpty();
  }

  @Test
  public void testUnregisterCallback_nullPara() {
    assertThrows(
        NullPointerException.class, () -> shadowBluetoothLeBroadcast.unregisterCallback(null));
  }

  @Test
  public void testStartBroadcast_stopBroadcast() {
    BluetoothLeBroadcast.Callback callback = mock(BluetoothLeBroadcast.Callback.class);

    BluetoothLeBroadcastSettings.Builder broadcastSettingsBuilder =
        new BluetoothLeBroadcastSettings.Builder();
    BluetoothLeAudioContentMetadata contentMetadata =
        new BluetoothLeAudioContentMetadata.Builder()
            .setProgramInfo("Test Program Info")
            .setLanguage("deu")
            .build();
    BluetoothLeBroadcastSubgroupSettings.Builder builderSubgroup =
        new BluetoothLeBroadcastSubgroupSettings.Builder()
            .setPreferredQuality(0)
            .setContentMetadata(contentMetadata);
    BluetoothLeBroadcastSubgroupSettings[] subgroupSettings =
        new BluetoothLeBroadcastSubgroupSettings[] {builderSubgroup.build()};
    for (BluetoothLeBroadcastSubgroupSettings setting : subgroupSettings) {
      broadcastSettingsBuilder.addSubgroupSettings(setting);
    }

    shadowBluetoothLeBroadcast.registerCallback(executors, callback);
    assertThat(shadowBluetoothLeBroadcast.getCallbackExecutorMap()).containsKey(callback);

    shadowBluetoothLeBroadcast.startBroadcast(broadcastSettingsBuilder.build());
    verify(callback).onBroadcastStarted(BluetoothStatusCodes.REASON_LOCAL_APP_REQUEST, 0);

    shadowBluetoothLeBroadcast.stopBroadcast(0);
    shadowMainLooper.idle();
    verify(callback).onBroadcastStopped(BluetoothStatusCodes.REASON_LOCAL_APP_REQUEST, 0);

    shadowBluetoothLeBroadcast.stopBroadcast(0);
    verify(callback).onBroadcastStopFailed(anyInt());
  }

  @Test
  public void testStartBroadcast_failure_nullSettings() {
    BluetoothLeBroadcast.Callback callback = mock(BluetoothLeBroadcast.Callback.class);

    shadowBluetoothLeBroadcast.registerCallback(executors, callback);
    shadowBluetoothLeBroadcast.startBroadcast(null);
    shadowMainLooper.idle();

    verify(callback).onBroadcastStartFailed(BluetoothStatusCodes.ERROR_LE_BROADCAST_INVALID_CODE);
  }

  @Test
  public void testStopBroadcast_failure_noCallback() {
    assertThrows(IllegalStateException.class, () -> shadowBluetoothLeBroadcast.stopBroadcast(0));
  }

  @Test
  public void testUpdateBroadcast_nullSettings() {
    assertThrows(
        NullPointerException.class, () -> shadowBluetoothLeBroadcast.updateBroadcast(0, null));
  }

  @Test
  public void testUpdateBroadcast_success() {
    BluetoothLeBroadcast.Callback callback = mock(BluetoothLeBroadcast.Callback.class);

    BluetoothLeBroadcastSettings.Builder broadcastSettingsBuilder =
        new BluetoothLeBroadcastSettings.Builder();
    BluetoothLeAudioContentMetadata contentMetadata =
        new BluetoothLeAudioContentMetadata.Builder()
            .setProgramInfo("Test Program Info")
            .setLanguage("deu")
            .build();
    BluetoothLeBroadcastSubgroupSettings.Builder builderSubgroup =
        new BluetoothLeBroadcastSubgroupSettings.Builder()
            .setPreferredQuality(0)
            .setContentMetadata(contentMetadata);
    BluetoothLeBroadcastSubgroupSettings[] subgroupSettings =
        new BluetoothLeBroadcastSubgroupSettings[] {builderSubgroup.build()};
    for (BluetoothLeBroadcastSubgroupSettings setting : subgroupSettings) {
      broadcastSettingsBuilder.addSubgroupSettings(setting);
    }

    shadowBluetoothLeBroadcast.registerCallback(executors, callback);
    shadowMainLooper.idle();

    shadowBluetoothLeBroadcast.startBroadcast(broadcastSettingsBuilder.build());
    shadowMainLooper.idle();

    shadowBluetoothLeBroadcast.updateBroadcast(0, broadcastSettingsBuilder.build());
    shadowMainLooper.idle();
    verify(callback).onBroadcastUpdated(anyInt(), anyInt());

    shadowBluetoothLeBroadcast.stopBroadcast(0);
    shadowMainLooper.idle();
  }

  @Test
  public void testUpdateBroadcast_fail() {
    BluetoothLeBroadcast.Callback callback = mock(BluetoothLeBroadcast.Callback.class);

    BluetoothLeBroadcastSettings.Builder broadcastSettingsBuilder =
        new BluetoothLeBroadcastSettings.Builder();
    BluetoothLeAudioContentMetadata contentMetadata =
        new BluetoothLeAudioContentMetadata.Builder()
            .setProgramInfo("Test Program Info")
            .setLanguage("deu")
            .build();
    BluetoothLeBroadcastSubgroupSettings.Builder builderSubgroup =
        new BluetoothLeBroadcastSubgroupSettings.Builder()
            .setPreferredQuality(0)
            .setContentMetadata(contentMetadata);
    BluetoothLeBroadcastSubgroupSettings[] subgroupSettings =
        new BluetoothLeBroadcastSubgroupSettings[] {builderSubgroup.build()};
    for (BluetoothLeBroadcastSubgroupSettings setting : subgroupSettings) {
      broadcastSettingsBuilder.addSubgroupSettings(setting);
    }

    shadowBluetoothLeBroadcast.registerCallback(executors, callback);
    shadowMainLooper.idle();

    shadowBluetoothLeBroadcast.updateBroadcast(0, broadcastSettingsBuilder.build());
    shadowMainLooper.idle();
    verify(callback).onBroadcastUpdateFailed(anyInt(), anyInt());
  }

  @Test
  public void testGetCallbackExecutorMap_returnEmptyMap() {
    assertThat(shadowBluetoothLeBroadcast.getCallbackExecutorMap()).isEmpty();
  }
}
