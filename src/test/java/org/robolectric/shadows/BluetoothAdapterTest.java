package org.robolectric.shadows;


import android.bluetooth.BluetoothAdapter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.junit.Assert.*;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class BluetoothAdapterTest {
  private BluetoothAdapter bluetoothAdapter;
  private ShadowBluetoothAdapter shadowBluetoothAdapter;

  @Before
  public void setUp() throws Exception {
    bluetoothAdapter = Robolectric.newInstanceOf(BluetoothAdapter.class);
    shadowBluetoothAdapter = shadowOf(bluetoothAdapter);
  }

  @Test
  public void testAdapterDefaultsDisabled() {
    assertFalse(bluetoothAdapter.isEnabled());
  }

  @Test
  public void testAdapterCanBeEnabled() {
    shadowBluetoothAdapter.setEnabled(true);
    assertTrue(bluetoothAdapter.isEnabled());
  }

  @Test
  public void canGetAndSetAddress() throws Exception {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    shadowOf(adapter).setAddress("expected");
    assertEquals("expected", adapter.getAddress());
  }
}
