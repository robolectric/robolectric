package com.xtremelabs.robolectric.shadows;

import android.bluetooth.BluetoothAdapter;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class BluetoothAdapterTest {
    @Test
    public void canGetAndSetAddress() throws Exception {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        shadowOf(adapter).setAddress("expected");
        assertEquals("expected", adapter.getAddress());
    }
}
