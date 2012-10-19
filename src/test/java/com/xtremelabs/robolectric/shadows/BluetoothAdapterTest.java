package com.xtremelabs.robolectric.shadows;


import android.bluetooth.BluetoothAdapter;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class BluetoothAdapterTest {
    private BluetoothAdapter bluetoothAdapter;
    private ShadowBluetoothAdapter shadowBluetoothAdapter;

    @Before
    public void setUp() throws Exception {
        bluetoothAdapter = Robolectric.newInstanceOf(BluetoothAdapter.class);
        shadowBluetoothAdapter = Robolectric.shadowOf(bluetoothAdapter);
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
}
