package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.bluetooth.BluetoothLeAudioContentMetadata;
import android.bluetooth.BluetoothLeBroadcast;
import android.bluetooth.BluetoothLeBroadcastSettings;
import android.bluetooth.BluetoothLeBroadcastSubgroupSettings;
import android.bluetooth.BluetoothStatusCodes;
import android.os.Build.VERSION_CODES;
import java.util.concurrent.CountDownLatch;
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
  private ShadowBluetoothLeBroadcast shadowBluetoothLeBroadcast;
  private ExecutorService executors;
  private CountDownLatch latch;

  @Before
  public void setUp() {
    shadowBluetoothLeBroadcast = Shadow.extract(Shadow.newInstanceOf(BluetoothLeBroadcast.class));
    executors = Executors.newSingleThreadExecutor();
    latch = new CountDownLatch(1);
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
  public void testStartBroadcast_success() {
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

    doAnswer(
            invocation -> {
              latch.countDown();
              return null;
            })
        .when(callback)
        .onBroadcastStarted(anyInt(), anyInt());
    shadowBluetoothLeBroadcast.registerCallback(executors, callback);
    shadowBluetoothLeBroadcast.startBroadcast(broadcastSettingsBuilder.build());
    waitForLatch(1);
    verify(callback).onBroadcastStarted(BluetoothStatusCodes.REASON_LOCAL_APP_REQUEST, 0);
  }

  @Test
  public void testStartBroadcast_failure_nullSettings() {
    BluetoothLeBroadcast.Callback callback = mock(BluetoothLeBroadcast.Callback.class);

    doAnswer(
            invocation -> {
              latch.countDown();
              return null;
            })
        .when(callback)
        .onBroadcastStartFailed(anyInt());
    shadowBluetoothLeBroadcast.registerCallback(executors, callback);
    shadowBluetoothLeBroadcast.startBroadcast(null);
    waitForLatch(1);
    verify(callback).onBroadcastStartFailed(BluetoothStatusCodes.ERROR_LE_BROADCAST_INVALID_CODE);
  }

  @Test
  public void testStopBroadcast_success() {
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

    doAnswer(
            invocation -> {
              latch.countDown();
              return null;
            })
        .when(callback)
        .onBroadcastStopped(anyInt(), anyInt());
    shadowBluetoothLeBroadcast.registerCallback(executors, callback);
    shadowBluetoothLeBroadcast.startBroadcast(broadcastSettingsBuilder.build());

    shadowBluetoothLeBroadcast.stopBroadcast(0);
    waitForLatch(1);
    verify(callback).onBroadcastStopped(BluetoothStatusCodes.REASON_LOCAL_APP_REQUEST, 0);
  }

  @Test
  public void testStopBroadcast_twice_secondCallFail() {
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

    doAnswer(
            invocation -> {
              latch.countDown();
              return null;
            })
        .when(callback)
        .onBroadcastStopFailed(anyInt());
    shadowBluetoothLeBroadcast.registerCallback(executors, callback);
    shadowBluetoothLeBroadcast.startBroadcast(broadcastSettingsBuilder.build());
    shadowBluetoothLeBroadcast.stopBroadcast(0);

    shadowBluetoothLeBroadcast.stopBroadcast(0);
    waitForLatch(1);
    verify(callback).onBroadcastStopFailed(anyInt());
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

    doAnswer(
            invocation -> {
              latch.countDown();
              return null;
            })
        .when(callback)
        .onBroadcastUpdated(anyInt(), anyInt());

    shadowBluetoothLeBroadcast.registerCallback(executors, callback);
    shadowBluetoothLeBroadcast.startBroadcast(broadcastSettingsBuilder.build());
    shadowBluetoothLeBroadcast.updateBroadcast(0, broadcastSettingsBuilder.build());

    waitForLatch(1);
    verify(callback).onBroadcastUpdated(anyInt(), anyInt());
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

    doAnswer(
            invocation -> {
              latch.countDown();
              return null;
            })
        .when(callback)
        .onBroadcastUpdateFailed(anyInt(), anyInt());

    shadowBluetoothLeBroadcast.registerCallback(executors, callback);
    shadowBluetoothLeBroadcast.updateBroadcast(0, broadcastSettingsBuilder.build());

    waitForLatch(1);
    verify(callback).onBroadcastUpdateFailed(anyInt(), anyInt());
  }

  @Test
  public void testGetCallbackExecutorMap_returnEmptyMap() {
    assertThat(shadowBluetoothLeBroadcast.getCallbackExecutorMap()).isEmpty();
  }

  private void waitForLatch(int timeoutSeconds) {
    try {
      boolean completed = latch.await(timeoutSeconds, SECONDS);
      assertThat(completed).isTrue();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
