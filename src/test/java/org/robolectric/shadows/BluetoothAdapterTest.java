package com.xtremelabs.robolectric.shadows;


import android.bluetooth.BluetoothAdapter;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
