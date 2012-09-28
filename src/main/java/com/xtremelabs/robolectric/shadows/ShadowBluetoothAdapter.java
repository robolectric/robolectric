package com.xtremelabs.robolectric.shadows;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BluetoothAdapter.class)
public class ShadowBluetoothAdapter {
    private Set<BluetoothDevice> bondedDevices = new HashSet<BluetoothDevice>();
    private boolean isDiscovering;
    private String address;

    @Implementation
    public static BluetoothAdapter getDefaultAdapter() {
        return (BluetoothAdapter) shadowOf(Robolectric.application).getBluetoothAdapter();
    }

    @Implementation
    public Set<BluetoothDevice> getBondedDevices() {
        return Collections.unmodifiableSet(bondedDevices);
    }

    public void setBondedDevices(Set<BluetoothDevice> bluetoothDevices) {
        bondedDevices = bluetoothDevices;
    }

    @Implementation
    public boolean startDiscovery() {
        isDiscovering = true;
        return true;
    }

    @Implementation
    public boolean cancelDiscovery() {
        isDiscovering = false;
        return true;
    }

    @Implementation
    public boolean isDiscovering() {
        return isDiscovering;
    }

    @Implementation
    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
