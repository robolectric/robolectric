package org.robolectric.shadows;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.internal.Shadow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowBluetoothAdapterTest {
  private BluetoothAdapter bluetoothAdapter;
  private ShadowBluetoothAdapter shadowBluetoothAdapter;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    bluetoothAdapter = Shadow.newInstanceOf(BluetoothAdapter.class);
    shadowBluetoothAdapter = shadowOf(bluetoothAdapter);
  }

  @Test
  public void testAdapterDefaultsDisabled() {
    assertThat(bluetoothAdapter.isEnabled()).isFalse();
  }

  @Test
  public void testAdapterCanBeEnabled_forTesting() {
    shadowBluetoothAdapter.setEnabled(true);
    assertThat(bluetoothAdapter.isEnabled()).isTrue();
  }

  @Test
  public void canGetAndSetAddress() throws Exception {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    shadowOf(adapter).setAddress("expected");
    assertThat(adapter.getAddress()).isEqualTo("expected");
  }

  @Test
  public void canEnable_withAndroidApi() throws Exception {
    bluetoothAdapter.enable();
    assertThat(bluetoothAdapter.isEnabled()).isTrue();
  }

  @Test
  public void canDisable_withAndroidApi() throws Exception {
    shadowBluetoothAdapter.setEnabled(true);
    bluetoothAdapter.disable();
    assertThat(bluetoothAdapter.isEnabled()).isFalse();
  }

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.JELLY_BEAN_MR2,
      Build.VERSION_CODES.KITKAT,
      Build.VERSION_CODES.LOLLIPOP })
  public void testLeScan() {
    BluetoothAdapter.LeScanCallback callback1 = newLeScanCallback();
    BluetoothAdapter.LeScanCallback callback2 = newLeScanCallback();

    bluetoothAdapter.startLeScan(callback1);
    assertThat(shadowBluetoothAdapter.getLeScanCallbacks()).containsOnly(callback1);
    bluetoothAdapter.startLeScan(callback2);
    assertThat(shadowBluetoothAdapter.getLeScanCallbacks()).containsOnly(callback1, callback2);

    bluetoothAdapter.stopLeScan(callback1);
    assertThat(shadowBluetoothAdapter.getLeScanCallbacks()).containsOnly(callback2);
    bluetoothAdapter.stopLeScan(callback2);
    assertThat(shadowBluetoothAdapter.getLeScanCallbacks()).isEmpty();
  }

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.JELLY_BEAN_MR2,
      Build.VERSION_CODES.KITKAT,
      Build.VERSION_CODES.LOLLIPOP })
  public void testGetSingleLeScanCallback() {
    BluetoothAdapter.LeScanCallback callback1 = newLeScanCallback();
    BluetoothAdapter.LeScanCallback callback2 = newLeScanCallback();

    bluetoothAdapter.startLeScan(callback1);
    assertThat(shadowBluetoothAdapter.getSingleLeScanCallback()).isEqualTo(callback1);

    bluetoothAdapter.startLeScan(callback2);
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("There are 2 callbacks");
    shadowBluetoothAdapter.getSingleLeScanCallback();
  }

  private BluetoothAdapter.LeScanCallback newLeScanCallback() {
    return new BluetoothAdapter.LeScanCallback() {
      @Override
      public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {}
    };
  }
}
