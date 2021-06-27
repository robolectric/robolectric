package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = JELLY_BEAN_MR2)
public class ShadowBluetoothManagerTest {
  private static final String DEVICE_ADDRESS_1 = "00:11:22:AA:BB:CC";
  private static final String DEVICE_ADDRESS_2 = "11:22:33:BB:CC:DD";
  private static final int INVALID_PROFILE = -1;
  private static final int INVALID_STATE = -1;
  private static final int PROFILE_GATT = BluetoothProfile.GATT;
  private static final int PROFILE_GATT_SERVER = BluetoothProfile.GATT_SERVER;
  private static final int PROFILE_STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;
  private static final int PROFILE_STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING;
  private static final int[] CONNECTED_STATES = new int[] {PROFILE_STATE_CONNECTED};

  private BluetoothManager manager;
  private BluetoothAdapter adapter;
  private ShadowBluetoothManager shadowManager;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    adapter = manager.getAdapter();
    shadowManager = shadowOf(manager);
  }

  @Test
  public void getAdapter_shouldReturnBluetoothAdapter() {
    assertThat(adapter).isSameInstanceAs(BluetoothAdapter.getDefaultAdapter());
  }

  @Test
  public void getDevicesMatchingConnectionStates_invalidProfile_throwsIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> manager.getDevicesMatchingConnectionStates(INVALID_PROFILE, CONNECTED_STATES));
  }

  @Test
  public void getDevicesMatchingConnectionStates_nullStates_returnsEmptyList() {
    assertThat(manager.getDevicesMatchingConnectionStates(PROFILE_GATT, null)).isEmpty();
  }

  @Test
  public void getDevicesMatchingConnectionStates_noDevicesRegistered_returnsEmptyList() {
    assertThat(manager.getDevicesMatchingConnectionStates(PROFILE_GATT, CONNECTED_STATES))
        .isEmpty();
  }

  @Test
  public void getDevicesMatchingConnectionStates_invalidStateRegistered_returnsEmptyList() {
    shadowManager.addDevice(PROFILE_GATT, INVALID_STATE, createBluetoothDevice(DEVICE_ADDRESS_1));

    List<BluetoothDevice> result =
        manager.getDevicesMatchingConnectionStates(PROFILE_GATT, new int[] {INVALID_STATE});

    assertThat(result).isEmpty();
  }

  @Test
  public void getDevicesMatchingConnectionStates_invalidProfileRegistered_throwsException() {
    shadowManager.addDevice(
        INVALID_PROFILE, PROFILE_STATE_CONNECTED, createBluetoothDevice(DEVICE_ADDRESS_1));

    assertThrows(
        IllegalArgumentException.class,
        () -> manager.getDevicesMatchingConnectionStates(INVALID_PROFILE, CONNECTED_STATES));
  }

  @Test
  public void getDevicesMatchingConnectionStates_subsetMatched_returnsBluetoothDeviceList() {
    BluetoothDevice match = createBluetoothDevice(DEVICE_ADDRESS_1);
    shadowManager.addDevice(PROFILE_GATT, PROFILE_STATE_CONNECTED, match);
    BluetoothDevice noMatchState = createBluetoothDevice(DEVICE_ADDRESS_2);
    shadowManager.addDevice(PROFILE_GATT, PROFILE_STATE_CONNECTING, noMatchState);
    BluetoothDevice noMatchProfile = createBluetoothDevice(DEVICE_ADDRESS_2);
    shadowManager.addDevice(PROFILE_GATT_SERVER, PROFILE_STATE_CONNECTED, noMatchProfile);
    ImmutableList<BluetoothDevice> expected = ImmutableList.of(match);

    List<BluetoothDevice> result =
        manager.getDevicesMatchingConnectionStates(PROFILE_GATT, CONNECTED_STATES);

    assertThat(result).containsExactlyElementsIn(expected);
  }

  @Test
  public void getDevicesMatchingConnectionStates_multiStatesMatched_returnsBluetoothDeviceList() {
    BluetoothDevice match1 = createBluetoothDevice(DEVICE_ADDRESS_1);
    shadowManager.addDevice(PROFILE_GATT, PROFILE_STATE_CONNECTED, match1);
    BluetoothDevice match2 = createBluetoothDevice(DEVICE_ADDRESS_2);
    shadowManager.addDevice(PROFILE_GATT, PROFILE_STATE_CONNECTING, match2);
    ImmutableList<BluetoothDevice> expected = ImmutableList.of(match1, match2);

    List<BluetoothDevice> result =
        manager.getDevicesMatchingConnectionStates(
            PROFILE_GATT, new int[] {PROFILE_STATE_CONNECTED, PROFILE_STATE_CONNECTING});

    assertThat(result).containsExactlyElementsIn(expected);
  }

  @Test
  public void getDevicesMatchingConnectionStates_multiProfilesMatched_returnsBluetoothDeviceList() {
    BluetoothDevice match1 = createBluetoothDevice(DEVICE_ADDRESS_1);
    shadowManager.addDevice(PROFILE_GATT_SERVER, PROFILE_STATE_CONNECTED, match1);
    BluetoothDevice match2 = createBluetoothDevice(DEVICE_ADDRESS_2);
    shadowManager.addDevice(PROFILE_GATT_SERVER, PROFILE_STATE_CONNECTED, match2);
    ImmutableList<BluetoothDevice> expected = ImmutableList.of(match1, match2);

    List<BluetoothDevice> result =
        manager.getDevicesMatchingConnectionStates(PROFILE_GATT_SERVER, CONNECTED_STATES);

    assertThat(result).containsExactlyElementsIn(expected);
  }

  private BluetoothDevice createBluetoothDevice(String address) {
    return adapter.getRemoteDevice(address);
  }
}
