package com.xtremelabs.robolectric.shadows;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.util.HashSet;
import java.util.Set;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BluetoothAdapter.class)
public class ShadowBluetoothAdapter {

    private Set<BluetoothDevice> bondedDevices = new HashSet<BluetoothDevice>();

    @Implementation
    public static BluetoothAdapter getDefaultAdapter() {
        return shadowOf(Robolectric.application).getBluetoothAdapter();
    }

    @Implementation
    public Set<BluetoothDevice> getBondedDevices() {
        return bondedDevices;
    }

    public void setBondedDevices(Set<BluetoothDevice> bluetoothDevices) {
        bondedDevices = bluetoothDevices;
    }
}
