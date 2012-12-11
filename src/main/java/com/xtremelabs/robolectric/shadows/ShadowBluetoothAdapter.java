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
    private static final int ADDRESS_LENGTH = 17;

    private Set<BluetoothDevice> bondedDevices = new HashSet<BluetoothDevice>();
    private boolean isDiscovering;
    private String address;
    private int state;

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

    @Implementation
    public int getState() {
        return state;
    }


    /**
     * Validate a Bluetooth address, such as "00:43:A8:23:10:F0"
     * <p>Alphabetic characters must be uppercase to be valid.
     *
     * @param address
     *         Bluetooth address as string
     * @return true if the address is valid, false otherwise
     */
    @Implementation
    public static boolean checkBluetoothAddress(String address) {
        if (address == null || address.length() != ADDRESS_LENGTH) {
            return false;
        }
        for (int i = 0; i < ADDRESS_LENGTH; i++) {
            char c = address.charAt(i);
            switch (i % 3) {
            case 0:
            case 1:
                if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                    // hex character, OK
                    break;
                }
                return false;
            case 2:
                if (c == ':') {
                    break;  // OK
                }
                return false;
            }
        }
        return true;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setState(int state) {
        this.state = state;
    }
}
