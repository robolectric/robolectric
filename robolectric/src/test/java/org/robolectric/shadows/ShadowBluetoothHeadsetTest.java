package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Test for {@link ShadowBluetoothHeadset} */
@RunWith(AndroidJUnit4.class)
public class ShadowBluetoothHeadsetTest {
  private BluetoothDevice device1;
  private BluetoothDevice device2;
  private BluetoothHeadset bluetoothHeadset;
  private Application context;

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    device1 = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:11:22:33:AA:BB");
    device2 = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("11:22:33:AA:BB:00");
    bluetoothHeadset = Shadow.newInstanceOf(BluetoothHeadset.class);
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void getConnectedDevices_defaultsToEmptyList() {
    assertThat(bluetoothHeadset.getConnectedDevices()).isEmpty();
  }

  @Test
  public void getConnectedDevices_canBeSetUpWithAddConnectedDevice() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);
    shadowOf(bluetoothHeadset).addConnectedDevice(device2);

    assertThat(bluetoothHeadset.getConnectedDevices()).containsExactly(device1, device2);
  }

  @Test
  public void getConnectionState_defaultsToDisconnected() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);
    shadowOf(bluetoothHeadset).addConnectedDevice(device2);

    assertThat(bluetoothHeadset.getConnectionState(device1))
        .isEqualTo(BluetoothProfile.STATE_CONNECTED);
    assertThat(bluetoothHeadset.getConnectionState(device2))
        .isEqualTo(BluetoothProfile.STATE_CONNECTED);
  }

  @Test
  public void getConnectionState_canBeSetUpWithAddConnectedDevice() {
    assertThat(bluetoothHeadset.getConnectionState(device1))
        .isEqualTo(BluetoothProfile.STATE_DISCONNECTED);
  }

  @Test
  public void isAudioConnected_defaultsToFalse() {
    assertThat(bluetoothHeadset.isAudioConnected(device1)).isFalse();
  }

  @Test
  public void isAudioConnected_canBeSetUpWithStartVoiceRecognition() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);

    bluetoothHeadset.startVoiceRecognition(device1);

    assertThat(bluetoothHeadset.isAudioConnected(device1)).isTrue();
  }

  @Test
  public void isAudioConnected_isFalseAfterStopVoiceRecognition() {
    bluetoothHeadset.startVoiceRecognition(device1);
    bluetoothHeadset.stopVoiceRecognition(device1);

    assertThat(bluetoothHeadset.isAudioConnected(device1)).isFalse();
  }

  @Test
  public void startVoiceRecogntion_shouldEmitBroadcast() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);
    IntentFilter intentFilter = new IntentFilter(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
    List<Integer> extraStateList = new ArrayList<>();
    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            extraStateList.add(intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1));
          }
        };
    context.registerReceiver(receiver, intentFilter);

    bluetoothHeadset.startVoiceRecognition(device1);
    shadowOf(getMainLooper()).idle();

    assertThat(extraStateList)
        .containsExactly(
            BluetoothHeadset.STATE_AUDIO_CONNECTING, BluetoothHeadset.STATE_AUDIO_CONNECTED);
  }

  @Test
  public void startVoiceRecogniton_returnsFalseIfAlreadyStarted() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);
    shadowOf(bluetoothHeadset).addConnectedDevice(device2);

    bluetoothHeadset.startVoiceRecognition(device1);

    assertThat(bluetoothHeadset.startVoiceRecognition(device2)).isFalse();
  }

  @Test
  public void startVoiceRecogntion_stopsAlreadyStartedRecognition() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);
    shadowOf(bluetoothHeadset).addConnectedDevice(device2);

    bluetoothHeadset.startVoiceRecognition(device1);
    bluetoothHeadset.startVoiceRecognition(device2);

    assertThat(bluetoothHeadset.isAudioConnected(device1)).isFalse();
  }

  @Test
  public void stopVoiceRecognition_returnsFalseIfNoVoiceRecognitionStarted() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);

    assertThat(bluetoothHeadset.stopVoiceRecognition(device1)).isFalse();
  }

  @Test
  public void stopVoiceRecognition_shouldEmitBroadcast() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);
    IntentFilter intentFilter = new IntentFilter(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
    List<Integer> extraStateList = new ArrayList<>();
    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            extraStateList.add(intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1));
          }
        };
    context.registerReceiver(receiver, intentFilter);

    bluetoothHeadset.startVoiceRecognition(device1);
    bluetoothHeadset.stopVoiceRecognition(device1);
    shadowOf(getMainLooper()).idle();

    assertThat(extraStateList)
        .containsExactly(
            BluetoothHeadset.STATE_AUDIO_CONNECTING,
            BluetoothHeadset.STATE_AUDIO_CONNECTED,
            BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
  }

  @Test
  @Config(minSdk = S)
  public void isVoiceRecognitionSupported_supportedByDefault() {
    assertThat(bluetoothHeadset.isVoiceRecognitionSupported(device1)).isTrue();
  }

  @Test
  @Config(minSdk = S)
  public void setVoiceRecognitionSupported_false_notSupported() {
    shadowOf(bluetoothHeadset).setVoiceRecognitionSupported(false);

    assertThat(bluetoothHeadset.isVoiceRecognitionSupported(device1)).isFalse();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void sendVendorSpecificResultCode_defaultsToTrueForConnectedDevice() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);

    assertThat(bluetoothHeadset.sendVendorSpecificResultCode(device1, "command", "arg")).isTrue();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void sendVendorSpecificResultCode_alwaysFalseForDisconnectedDevice() {
    assertThat(bluetoothHeadset.sendVendorSpecificResultCode(device1, "command", "arg")).isFalse();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void sendVendorSpecificResultCode_canBeForcedToFalseForConnectedDevice() {
    shadowOf(bluetoothHeadset).addConnectedDevice(device1);
    shadowOf(bluetoothHeadset).setAllowsSendVendorSpecificResultCode(false);

    assertThat(bluetoothHeadset.sendVendorSpecificResultCode(device1, "command", "arg")).isFalse();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void sendVendorSpecificResultCode_throwsOnNullCommand() {
    try {
      bluetoothHeadset.sendVendorSpecificResultCode(device1, null, "arg");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  @Config(minSdk = P)
  public void setActiveDevice_setNull_shouldSaveNull() {
    assertThat(bluetoothHeadset.setActiveDevice(null)).isTrue();

    assertThat(bluetoothHeadset.getActiveDevice()).isNull();
    Intent intent = shadowOf(context).getBroadcastIntents().get(0);
    assertThat(intent.getAction()).isEqualTo(BluetoothHeadset.ACTION_ACTIVE_DEVICE_CHANGED);
    assertThat((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void getActiveDevice_returnValueFromSetter() {
    assertThat(bluetoothHeadset.setActiveDevice(device1)).isTrue();

    assertThat(bluetoothHeadset.getActiveDevice()).isEqualTo(device1);
    Intent intent = shadowOf(context).getBroadcastIntents().get(0);
    assertThat(intent.getAction()).isEqualTo(BluetoothHeadset.ACTION_ACTIVE_DEVICE_CHANGED);
    assertThat((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))
        .isEqualTo(device1);
  }
}
